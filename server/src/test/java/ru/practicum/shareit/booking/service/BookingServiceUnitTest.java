package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingAllFieldsDto;
import ru.practicum.shareit.booking.dto.BookingSavingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.error.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import static java.time.LocalDateTime.now;
import static java.util.List.of;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Page.empty;
import static ru.practicum.shareit.booking.enums.BookingState.*;
import static ru.practicum.shareit.booking.enums.BookingTimeState.*;
import static ru.practicum.shareit.item.mapper.ItemMapper.mapToItemAllFieldsDto;
import static ru.practicum.shareit.user.mapper.UserMapper.mapToUser;


@ExtendWith(MockitoExtension.class)
class BookingServiceUnitTest {
    @Mock
    private BookingRepository bookingRepository;
    private BookingSavingDto bookingCreatedDto;
    private BookingService bookingService;
    @Mock
    private UserService userService;
    private UserDto userDto;
    private Booking booking;

    @BeforeEach
    void initialize() {
        bookingService = new BookingServiceImpl(bookingRepository, userService);
        bookingCreatedDto = BookingSavingDto.builder()
                .id(1L)
                .start(now())
                .end(now().plusHours(2))
                .itemId(1L)
                .booker(2L)
                .status(WAITING.name())
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("Lara")
                .email("lara@mail.ru")
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(now())
                .end(now().plusHours(2))
                .item(new Item(1L, "test", "test2colour", true, mapToUser(userDto), null))
                .booker(new User(2L, "Maggie", "maggie@mail.ru"))
                .status(WAITING)
                .build();
    }

    private BookingAllFieldsDto saveBookingDto() {
        when(userService.get(any()))
                .thenReturn(userDto);
        when(bookingRepository.findBookingsByItem_IdIsAndStatusIsAndEndIsAfter(anyLong(), any(), any()))
                .thenReturn(of());
        when(bookingRepository.save(any()))
                .thenReturn(booking);
        return bookingService.save(
                bookingCreatedDto,
                mapToItemAllFieldsDto(
                        booking.getItem(),
                        null,
                        null,
                        of()
                ),
                2L);
    }


    @Test
    void saveBookingTest() {
        var bookingAllFieldsDto = saveBookingDto();
        assertEquals(bookingAllFieldsDto.getId(), booking.getId());
        assertEquals(bookingAllFieldsDto.getItem().getId(), booking.getItem().getId());
    }


    @Test
    void approveBookingTest() {
        var approved = new Booking(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem(),
                booking.getBooker(),
                APPROVED);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(ofNullable(booking));
        when(bookingRepository.save(any()))
                .thenReturn(approved);
        var approvedFrom = bookingService.approve(
                booking.getId(),
                true,
                userDto.getId()
        );
        assertEquals(approvedFrom.getStatus(), approved.getStatus().name());
        assertEquals(approvedFrom.getId(), approved.getId());
    }


    @Test
    void getNotFoundBookingTest() {
        when(bookingRepository.findById(anyLong()))
                .thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(
                        7L,
                        7L)
        );
    }


    @Test
    void getBookingTest() {
        var bookingAllFieldsDto = saveBookingDto();
        when(bookingRepository.findById(anyLong()))
                .thenReturn(ofNullable(booking));
        var bookingFrom = bookingService.getBookingById(
                bookingAllFieldsDto.getId(),
                userDto.getId()
        );
        assertEquals(bookingFrom.getItem().getId(), booking.getItem().getId());
        assertEquals(bookingFrom.getId(), booking.getId());
    }

    @Test
    void getBookingByAnotherUserTest() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(ofNullable(booking));
        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(
                        booking.getId(),
                        7L)
        );
    }

    @Test
    void getAllBookingsIncorrectEndPaginationTest() {
        var exception = assertThrows(ValidationException.class,
                () -> bookingService.getAllBookings(
                        userDto.getId(),
                        "Unknown",
                        0,
                        0)
        );
        assertEquals("size <= 0 || from < 0", exception.getMessage());
    }

    @Test
    void getAllBookingsTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsOrderByStartDesc(any()))
                .thenReturn(of(booking));
        var bookings = bookingService.getAllBookings(
                userDto.getId(),
                null,
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getAllBookingsIncorrectStartPaginationTest() {
        var exception = assertThrows(ValidationException.class,
                () -> bookingService.getAllBookings(
                        userDto.getId(),
                        "Unknown",
                        -1,
                        14)
        );
        assertEquals("size <= 0 || from < 0", exception.getMessage());
    }


    @Test
    void getAllBookingsFutureStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(any(), any()))
                .thenReturn(of(booking));
        var bookings = bookingService.getAllBookings(
                userDto.getId(),
                FUTURE.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getAllBookingsPastStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndEndBeforeOrderByStartDesc(any(), any()))
                .thenReturn(of(booking));
        var bookings = bookingService.getAllBookings(
                userDto.getId(),
                PAST.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getAllBookingsCurrentStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(of(booking));
        var bookings = bookingService.getAllBookings(
                userDto.getId(),
                CURRENT.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getAllBookingsEmptyTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(any(), any()))
                .thenReturn(of());
        var bookings = bookingService.getAllBookings(
                userDto.getId(),
                FUTURE.name(),
                null,
                null
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsRejectStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStatusIsOrderByStartDesc(any(), any()))
                .thenReturn(of(booking));
        var bookings = bookingService.getAllBookings(
                userDto.getId(),
                REJECTED.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getAllBookingsCancelStateEmptyTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStatusIsOrderByStartDesc(any(), any()))
                .thenReturn(of());
        var bookings = bookingService.getAllBookings(
                userDto.getId(),
                CANCELED.name(),
                null,
                null
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdPastStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(any(), any()))
                .thenReturn(of(booking));
        var bookings = bookingService.getBookingsByOwnerId(
                userDto.getId(),
                PAST.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getBookingsByOwnerIdTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsOrderByStartDesc(any()))
                .thenReturn(of(booking));
        var bookings = bookingService.getBookingsByOwnerId(
                userDto.getId(),
                null,
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getBookingsByOwnerIdAllStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsOrderByStartDesc(any()))
                .thenReturn(of(booking));
        var bookings = bookingService.getBookingsByOwnerId(
                userDto.getId(),
                ALL.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getBookingsByOwnerIdFutureStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerAndStartAfterOrderByStartDesc(any(), any()))
                .thenReturn(of(booking));
        var bookings = bookingService.getBookingsByOwnerId(
                userDto.getId(),
                FUTURE.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getBookingsByOwnerIdRejectStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsAndStatusIsOrderByStartDesc(any(), any()))
                .thenReturn(of(booking));
        var bookings = bookingService.getBookingsByOwnerId(
                userDto.getId(),
                REJECTED.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getBookingsByOwnerIdCurrentStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(of(booking));
        var bookings = bookingService.getBookingsByOwnerId(
                userDto.getId(),
                CURRENT.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getBookingsByItemEmptyTest() {
        when(bookingRepository.findBookingsByItem_IdAndItem_Owner_IdIsOrderByStart(anyLong(), anyLong()))
                .thenReturn(of());
        var bookings = bookingService.getBookingsByItem(
                1L,
                2L
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByItemTest() {
        when(bookingRepository.findBookingsByItem_IdAndItem_Owner_IdIsOrderByStart(anyLong(), anyLong()))
                .thenReturn(of(booking));
        var bookings = bookingService.getBookingsByItem(
                1L,
                2L
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getAllBookingsPaginationFutureTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(empty());
        var bookings = bookingService.getAllBookings(
                userDto.getId(),
                FUTURE.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsPaginationAllTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsOrderByStartDesc(any(), any()))
                .thenReturn(empty());
        var bookings = bookingService.getAllBookings(
                userDto.getId(),
                ALL.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsPaginationPastTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndEndBeforeOrderByStartDesc(any(), any(), any()))
                .thenReturn(empty());
        var bookings = bookingService.getAllBookings(
                userDto.getId(),
                PAST.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsPaginationCurrentTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any()))
                .thenReturn(empty());
        var bookings = bookingService.getAllBookings(
                userDto.getId(),
                CURRENT.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsPaginationAnyTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStatusIsOrderByStartDesc(any(), any(), any()))
                .thenReturn(empty());
        var bookings = bookingService.getAllBookings(
                userDto.getId(),
                CANCELED.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdPastTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(any(), any()))
                .thenReturn(of());
        var bookings = bookingService.getBookingsByOwnerId(
                userDto.getId(),
                PAST.name()
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdCurrentTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(of());
        var bookings = bookingService.getBookingsByOwnerId(
                userDto.getId(),
                CURRENT.name()
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdFutureTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerAndStartAfterOrderByStartDesc(any(), any()))
                .thenReturn(of());
        var bookings = bookingService.getBookingsByOwnerId(
                userDto.getId(),
                FUTURE.name()
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdAnyTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsAndStatusIsOrderByStartDesc(any(), any()))
                .thenReturn(of());
        var bookings = bookingService.getBookingsByOwnerId(
                userDto.getId(),
                CANCELED.name()
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdPaginationNotNullTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsOrderByStartDesc(any(), any()))
                .thenReturn(empty());
        var bookings = bookingService.getBookingsByOwnerId(
                userDto.getId(),
                ALL.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdPaginationPastTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(any(), any(), any()))
                .thenReturn(empty());
        var bookings = bookingService.getBookingsByOwnerId(
                userDto.getId(),
                PAST.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdPaginationCurrentTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any()))
                .thenReturn(empty());
        var bookings = bookingService.getBookingsByOwnerId(
                userDto.getId(),
                CURRENT.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdPaginationFutureTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerAndStartAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(empty());
        var bookings = bookingService.getBookingsByOwnerId(
                userDto.getId(),
                FUTURE.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdPaginationAnyTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsAndStatusIsOrderByStartDesc(any(), any(), any()))
                .thenReturn(empty());
        var bookings = bookingService.getBookingsByOwnerId(
                userDto.getId(),
                CANCELED.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsAllTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsOrderByStartDesc(any()))
                .thenReturn(of());
        var bookings = bookingService.getAllBookings(
                userDto.getId(),
                ALL.name()
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsCurrentTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(of());
        var bookings = bookingService.getAllBookings(
                userDto.getId(),
                CURRENT.name()
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsFutureTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(any(), any()))
                .thenReturn(of());
        var bookings = bookingService.getAllBookings(
                userDto.getId(),
                FUTURE.name()
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsAnyTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStatusIsOrderByStartDesc(any(), any()))
                .thenReturn(of());
        var bookings = bookingService.getAllBookings(
                userDto.getId(),
                CANCELED.name()
        );
        assertEquals(bookings.size(), 0);
    }
}

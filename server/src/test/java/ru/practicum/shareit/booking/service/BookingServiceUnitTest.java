package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingAllFieldsDto;
import ru.practicum.shareit.booking.dto.BookingSavingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.booking.enums.BookingState.APPROVED;
import static ru.practicum.shareit.booking.enums.BookingState.WAITING;

@ExtendWith(MockitoExtension.class)
class BookingServiceUnitTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    private BookingService bookingService;
    private BookingSavingDto bookingCreatedDto;
    private UserDto userDto;
    private Booking booking;

    @Captor
    private ArgumentCaptor<Booking> bookingCaptor;

    @BeforeEach
    void initialize() {
        bookingService = new BookingServiceImpl(bookingRepository, userService);


        bookingCreatedDto = BookingSavingDto.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(2))
                .itemId(1L)
                .build();


        userDto = UserDto.builder()
                .id(1L)
                .name("Lara")
                .email("lara@mail.ru")
                .build();


        booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(2))
                .item(new Item(1L, "test", "test2colour", true, UserMapper.mapToUser(userDto), null))
                .booker(new User(2L, "Maggie", "maggie@mail.ru"))
                .status(WAITING)
                .build();
    }

    /**
     * Вспомогательный метод для сохранения бронирования и настройки моков.
     */
    private BookingAllFieldsDto saveBookingDto() {

        when(userService.get(anyLong()))
                .thenReturn(userDto);


        when(bookingRepository.findBookingsByItem_IdIsAndStatusIsAndEndIsAfter(anyLong(), any(), any()))
                .thenReturn(List.of());


        when(bookingRepository.save(any()))
                .thenReturn(booking);


        return bookingService.save(
                bookingCreatedDto,
                ItemMapper.mapToItemAllFieldsDto(
                        booking.getItem(),
                        null,
                        null,
                        List.of()
                ),
                2L);
    }

    @Test
    @DisplayName("Сохранение бронирования")
    void saveBookingTest() {
        var bookingAllFieldsDto = saveBookingDto();
        assertEquals(bookingAllFieldsDto.getId(), booking.getId());
        assertEquals(bookingAllFieldsDto.getItem().getId(), booking.getItem().getId());
    }


    @Test
    @DisplayName("Одобрение бронирования")
    void approveBookingTest() {
        var approved = new Booking(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem(),
                booking.getBooker(),
                APPROVED);


        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));


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
    @DisplayName("Получение несуществующего бронирования")
    void getNotFoundBookingTest() {

        when(bookingRepository.findById(anyLong()))
                .thenThrow(NotFoundException.class);


        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(
                        7L,
                        7L) //
        );
    }


    @Test
    @DisplayName("Получение бронирования по ID")
    void getBookingTest() {
        var bookingAllFieldsDto = saveBookingDto();


        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));


        var bookingFrom = bookingService.getBookingById(
                bookingAllFieldsDto.getId(),
                userDto.getId()
        );


        assertEquals(bookingFrom.getItem().getId(), booking.getItem().getId());
        assertEquals(bookingFrom.getId(), booking.getId());
    }


    @Test
    @DisplayName("Получение бронирования другим пользователем")
    void getBookingByAnotherUserTest() {

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // Ожидаем выброс исключения, если другой пользователь пытается получить бронирование
        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(
                        booking.getId(),
                        7L) // ID другого пользователя
        );
    }
}

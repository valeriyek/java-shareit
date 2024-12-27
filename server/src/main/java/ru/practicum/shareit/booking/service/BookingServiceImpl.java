package ru.practicum.shareit.booking.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingAllFieldsDto;
import ru.practicum.shareit.booking.dto.BookingSavingDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingTimeState;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.error.ValidationException;
import ru.practicum.shareit.item.dto.ItemAllFieldsDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static ru.practicum.shareit.booking.enums.BookingState.*;
import static ru.practicum.shareit.booking.enums.BookingTimeState.*;
import static ru.practicum.shareit.booking.mapper.BookingMapper.mapToBooking;
import static ru.practicum.shareit.booking.mapper.BookingMapper.mapToBookingAllFieldsDto;
import static ru.practicum.shareit.item.mapper.ItemMapper.mapToItem;
import static ru.practicum.shareit.user.mapper.UserMapper.mapToUser;
import static ru.practicum.shareit.utils.Pagination.makePageRequest;


@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;

    private void validate(BookingSavingDto bookingSavingDto) {

        if (bookingSavingDto.getStart() == null)
            throw new ValidationException("Пожалуйста, укажите дату начала бронирования");


        if (bookingSavingDto.getEnd() == null)
            throw new ValidationException("Пожалуйста, укажите дату окончания бронирования");


        if (bookingSavingDto.getStart().toLocalDate().isBefore(LocalDate.now()))
            throw new ValidationException("Некорректная дата начала бронирования");


        if (bookingSavingDto.getEnd().isBefore(bookingSavingDto.getStart())
                || bookingSavingDto.getEnd().toLocalDate().isBefore(LocalDate.now()))
            throw new ValidationException("Некорректная дата окончания бронирования");


    }

    @Override
    @Transactional
    public BookingAllFieldsDto save(BookingSavingDto bookingSavingDto, ItemAllFieldsDto itemDto, Long bookerId) {
        if (itemDto.getOwnerId().equals(bookerId))
            throw new NotFoundException("Вещь#" + itemDto.getId() + " не может быть забронирована владельцем");
        if (!itemDto.getAvailable()) {
            log.warn("Attempt to book unavailable item with id: {}", itemDto.getId());
            throw new IllegalStateException("Unexpected error during booking");
        }
        validate(bookingSavingDto);

        var booker = mapToUser(userService.get(bookerId));
        var item = mapToItem(itemDto);
        var bookings = bookingRepository.findBookingsByItem_IdIsAndStatusIsAndEndIsAfter(
                item.getId(),
                APPROVED,
                bookingSavingDto.getStart());
        if (!bookings.isEmpty())
            throw new NotFoundException("Эта вещь не может быть забронирована: " + item.getName());
        var booking = mapToBooking(bookingSavingDto);
        booking.setStatus(WAITING);
        booking.setBooker(booker);
        booking.setItem(item);
        var savedBooking = bookingRepository.save(booking);
        return mapToBookingAllFieldsDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingAllFieldsDto approve(Long bookingId, boolean approved, Long userId) {
        var booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new NotFoundException("Бронь с id#" + bookingId + " не существует"));
        if (booking.getBooker().getId().equals(userId))
            throw new NotFoundException("Нет доступного одобрения для пользователя с id#" + userId);
        if (!booking.getItem().getOwner().getId().equals(userId)
                || !booking.getStatus().equals(WAITING))
            throw new ValidationException("Статус бронирования не может быть обновлен");
        booking.setStatus(approved ? APPROVED : REJECTED);
        var savedBooking = bookingRepository.save(booking);
        return mapToBookingAllFieldsDto(savedBooking);
    }

    @Override
    public BookingAllFieldsDto getBookingById(Long bookingId, Long userId) {
        var booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new NotFoundException("Бронь с id#" + bookingId + "не существует"));
        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Нет доступного одобрения для пользователя с id#" + userId);
        }
        return mapToBookingAllFieldsDto(booking);
    }

    @Override
    public List<BookingAllFieldsDto> getBookingsByItem(Long itemId, Long userId) {
        return bookingRepository
                .findBookingsByItem_IdAndItem_Owner_IdIsOrderByStart(itemId, userId)
                .stream()
                .map(BookingMapper::mapToBookingAllFieldsDto)
                .collect(toList());
    }

    @Override
    public List<BookingAllFieldsDto> getAllBookings(Long bookerId, String state) {
        Stream<Booking> stream = null;
        var userDto = userService.get(bookerId);
        var user = mapToUser(userDto);
        if (state == null || ALL.name().equals(state))
            stream = bookingRepository
                    .findBookingsByBookerIsOrderByStartDesc(user)
                    .stream();
        if (PAST.name().equals(state))
            stream = bookingRepository
                    .findBookingsByBookerIsAndEndBeforeOrderByStartDesc(user, now())
                    .stream();
        if (CURRENT.name().equals(state))
            stream = bookingRepository
                    .findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, now(), now())
                    .stream();
        if (FUTURE.name().equals(state))
            stream = bookingRepository
                    .findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(user, now())
                    .stream();
        if (Arrays.stream(BookingState.values()).anyMatch(bookingState -> bookingState.name().equals(state)))
            stream = bookingRepository
                    .findBookingsByBookerIsAndStatusIsOrderByStartDesc(user, BookingState.valueOf(state))
                    .stream();
        if (stream != null)
            return stream
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        else
            throw new ValidationException("Неизвестный статус: " + state);
    }

    @Override
    public List<BookingAllFieldsDto> getAllBookings(Long bookerId, String state, Integer from, Integer size) {
        Stream<Booking> stream = null;
        var pageRequest = makePageRequest(from, size, Sort.by("start").descending());
        var userDto = userService.get(bookerId);
        var user = mapToUser(userDto);
        if (state == null || ALL.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingRepository
                        .findBookingsByBookerIsOrderByStartDesc(user)
                        .stream();
            else
                stream = bookingRepository
                        .findBookingsByBookerIsOrderByStartDesc(user, pageRequest)
                        .stream();
        }
        if (PAST.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingRepository
                        .findBookingsByBookerIsAndEndBeforeOrderByStartDesc(user, now())
                        .stream();
            else
                stream = bookingRepository
                        .findBookingsByBookerIsAndEndBeforeOrderByStartDesc(user, now(), pageRequest)
                        .stream();
        }
        if (CURRENT.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingRepository
                        .findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, now(), now())
                        .stream();
            else
                stream = bookingRepository
                        .findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, now(), now(), pageRequest)
                        .stream();
        }
        if (FUTURE.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingRepository
                        .findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(user, now())
                        .stream();
            else
                stream = bookingRepository
                        .findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(user, now(), pageRequest)
                        .stream();
        }
        if (Arrays.stream(BookingState.values()).anyMatch(bookingState -> bookingState.name().equals(state))) {
            if (pageRequest == null)
                stream = bookingRepository
                        .findBookingsByBookerIsAndStatusIsOrderByStartDesc(user, BookingState.valueOf(state))
                        .stream();
            else
                stream = bookingRepository
                        .findBookingsByBookerIsAndStatusIsOrderByStartDesc(user, BookingState.valueOf(state), pageRequest)
                        .stream();
        }
        if (stream != null)
            return stream
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        else
            throw new ValidationException("Неизвестный статус: " + state);
    }

    @Override
    public List<BookingAllFieldsDto> getBookingsByOwnerId(Long userId, BookingTimeState state, Integer from, Integer size) {
        var pageRequest = (from != null && size != null)
                ? makePageRequest(from, size, Sort.by("start").descending())
                : null;

        var user = mapToUser(userService.get(userId));
        Stream<Booking> stream;

        if (state == null || state == BookingTimeState.ALL) {
            stream = pageRequest == null
                    ? bookingRepository.findBookingsByItemOwnerIsOrderByStartDesc(user).stream()
                    : bookingRepository.findBookingsByItemOwnerIsOrderByStartDesc(user, pageRequest).stream();
        } else if (state == BookingTimeState.PAST) {
            stream = pageRequest == null
                    ? bookingRepository.findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(user, now()).stream()
                    : bookingRepository.findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(user, now(), pageRequest).stream();
        } else if (state == BookingTimeState.CURRENT) {
            stream = pageRequest == null
                    ? bookingRepository.findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, now(), now()).stream()
                    : bookingRepository.findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, now(), now(), pageRequest).stream();
        } else if (state == BookingTimeState.FUTURE) {
            stream = pageRequest == null
                    ? bookingRepository.findBookingsByItemOwnerAndStartAfterOrderByStartDesc(user, now()).stream()
                    : bookingRepository.findBookingsByItemOwnerAndStartAfterOrderByStartDesc(user, now(), pageRequest).stream();
        } else {
            throw new ValidationException("Неизвестный статус: " + state);
        }

        return stream
                .map(BookingMapper::mapToBookingAllFieldsDto)
                .collect(toList());
    }


}
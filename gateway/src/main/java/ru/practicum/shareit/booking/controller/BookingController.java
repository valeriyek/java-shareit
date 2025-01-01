
package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private static final String HEADER_SHARER_USER_ID = "X-Sharer-User-Id";
    private final BookingClient bookingClient;

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                   @Positive @RequestParam(name = "size", defaultValue = "10") Integer size,
                                                   @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                                   @RequestHeader(HEADER_SHARER_USER_ID) Long userId) {
        var state = BookingState.from(stateParam).orElseThrow(
                () -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getOwnerBookings(userId, state, from, size);
    }

    @GetMapping
    public ResponseEntity<Object> getBookings(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size,
                                              @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                              @RequestHeader(HEADER_SHARER_USER_ID) Long userId) {
        var state = BookingState.from(stateParam).orElseThrow(
                () -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getBookings(userId, state, from, size);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader(value = HEADER_SHARER_USER_ID) Long userId,
                                                 @RequestParam(required = false) Boolean approved,
                                                 @PathVariable Integer bookingId) {
        return bookingClient.approveBooking(bookingId, approved, userId);
    }

    @Validated
    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader(HEADER_SHARER_USER_ID) Long userId,
                                                @RequestBody @Valid BookingRequestDto requestDto) {
        if (requestDto.getStart().isAfter(requestDto.getEnd()))
            throw new IllegalArgumentException("Incorrect date of booking");
        return bookingClient.createBooking(userId, requestDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(HEADER_SHARER_USER_ID) Long userId,
                                             @PathVariable Long bookingId) {
        return bookingClient.getBooking(userId, bookingId);
    }
}
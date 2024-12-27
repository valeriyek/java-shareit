package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingRequestDto {

    private Long itemId;
    @FutureOrPresent(message = "Incorrect start date of booking")
    private LocalDateTime start;
    @Future(message = "Incorrect end date of booking")
    private LocalDateTime end;

}

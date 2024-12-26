package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingRequestDto {
    private Long itemId;

    @FutureOrPresent(message = "Дата начала должна быть в будущем")
    private LocalDateTime start;

    @FutureOrPresent(message = "Дата окончания должна быть в будущем")
    private LocalDateTime end;

}

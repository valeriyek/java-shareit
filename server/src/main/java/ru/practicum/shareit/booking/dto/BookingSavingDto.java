package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
public class BookingSavingDto {
    private Long id;
    @NotNull
    @PastOrPresent
    private LocalDateTime start;
    @NotNull
    @Past
    private LocalDateTime end;
    private Long itemId;
    private Long booker;
    private String status;
}

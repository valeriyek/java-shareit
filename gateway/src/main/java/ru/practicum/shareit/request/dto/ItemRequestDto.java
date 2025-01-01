package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;


@Data
//@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {

    private Integer id;
    @NotBlank(message = "Description cannot be null")
    private String description;
    private Integer requestorId;
    private LocalDateTime created;
}

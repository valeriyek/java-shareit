package ru.practicum.shareit.user.dto;


import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserDto {
    @NotNull
    private final Long id;
    @Nullable
    private final String name;


    @NotNull
    @NotBlank
    @Email(message = "Некорректный формат email")
    private final String email;
}

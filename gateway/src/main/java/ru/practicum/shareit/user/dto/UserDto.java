package ru.practicum.shareit.user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import ru.practicum.shareit.validation.Create;
import ru.practicum.shareit.validation.Update;

@Data

public class UserDto {

    private Long id;
    @NotBlank(groups = Create.class, message = "Name cannot be blank")
    private String name;
    @Email(groups = {Update.class, Create.class}, message = "Email is incorrect")
    @NotEmpty(groups = Create.class, message = "Email cannot be empty")
    private String email;
}

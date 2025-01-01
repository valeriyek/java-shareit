package ru.practicum.shareit.item.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.validation.Create;

@Data
@AllArgsConstructor
public class ItemDto {

    private Long id;
    @NotBlank(groups = Create.class, message = "Item name cannot be null")
    private String name;
    @NotBlank(groups = Create.class, message = "Item description cannot be null")
    private String description;
    @NotNull(groups = Create.class, message = "Available cannot be null")
    private Boolean available;
    private Long ownerId;
    private Long requestId;
}

package ru.practicum.shareit.item.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
   @NotNull
    private Long id;
 @NotNull
 @NotBlank

    private String name;
 @NotNull
 @NotBlank
    private String description;
 @NotNull

    private Boolean available;
    @NotNull
    private Long ownerId;
    @NotNull
    private Long requestId;


}

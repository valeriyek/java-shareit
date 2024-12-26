package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    private Long itemId;
    @NotNull
    @NotEmpty(message = "Текст комментария не должен быть пустым")
    private String text;
    private String authorName;
    private LocalDateTime created;
}

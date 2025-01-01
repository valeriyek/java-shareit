package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;


@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private static final String HEADER_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @RequestHeader(required = false, value = HEADER_SHARER_USER_ID) Long userId,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size,
                                              @NotNull @RequestParam(required = false) String text) {
        return itemClient.searchItems(text, userId, from, size);
    }

    @GetMapping()
    public ResponseEntity<Object> getAllItems(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @RequestHeader(required = false, value = HEADER_SHARER_USER_ID) Long userId,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemClient.getItems(userId, from, size);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(required = false, value = HEADER_SHARER_USER_ID) Long userId,
                                             @RequestBody ItemDto itemDto,
                                             @PathVariable Long itemId) {
        return itemClient.updateItem(itemDto, itemId, userId);
    }

    @PostMapping()
    @Validated()
    public ResponseEntity<Object> createItem(@RequestHeader(required = false, value = HEADER_SHARER_USER_ID) Long userId,
                                             @RequestBody @Valid ItemDto itemDto) {
        return itemClient.createItem(itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader(required = false, value = HEADER_SHARER_USER_ID) Long userId,
                                          @PathVariable Long itemId) {
        return itemClient.getItem(itemId, userId);
    }

    @Validated
    @PostMapping("{itemId}/comment")
    public ResponseEntity<Object> createItemComment(@RequestHeader(value = HEADER_SHARER_USER_ID) Long userId,
                                                    @RequestBody @Valid CommentDto commentDto,
                                                    @PathVariable Long itemId) {
        if (userId == null) throw new IllegalArgumentException("Field userId is null");
        return itemClient.createComment(commentDto, itemId, userId);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable long itemId) {
        itemClient.deleteItem(itemId);
    }

}

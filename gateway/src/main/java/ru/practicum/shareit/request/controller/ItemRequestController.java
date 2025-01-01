//package ru.practicum.shareit.request;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//import ru.practicum.shareit.request.dto.ItemRequestDto;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/requests")
//@RequiredArgsConstructor
//public class ItemRequestController {
//    private final ItemRequestService requestService;
//
//    @PostMapping
//    public ItemRequestDto createRequest(@RequestBody ItemRequestDto itemRequestDto,
//                                        @RequestHeader("X-Sharer-User-Id") Long userId) {
//        return requestService.createRequest(itemRequestDto, userId);
//    }
//
//    @GetMapping
//    public List<ItemRequestDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
//        return requestService.getUserRequests(userId);
//    }
//
//    @GetMapping("/all")
//    public List<ItemRequestDto> getOtherUsersRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
//        return requestService.getOtherUsersRequests(userId);
//
//    }
//
//    @GetMapping("/{requestId}")
//    public ItemRequestDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
//                                         @PathVariable Long requestId) {
//        return requestService.getRequestById(requestId, userId);
//    }
//}
package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;


@Validated
@RestController
@AllArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private static final String HEADER_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemRequestClient itemRequestClient;

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                     @RequestHeader(required = false, value = HEADER_SHARER_USER_ID) Long userId,
                                                     @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemRequestClient.getAllItemRequests(from, size, userId);
    }

    @Validated
    @PostMapping()
    public ResponseEntity<Object> createItemRequest(@RequestHeader(value = HEADER_SHARER_USER_ID) Long userId,
                                                    @RequestBody @Valid ItemRequestDto itemRequestDto) {
        return itemRequestClient.createItemRequest(itemRequestDto, userId);
    }

    @GetMapping()
    public ResponseEntity<Object> getItemRequests(@RequestHeader(value = HEADER_SHARER_USER_ID) Long userId) {
        return itemRequestClient.getItemRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequest(@RequestHeader(value = HEADER_SHARER_USER_ID) Long userId,
                                                 @PathVariable Long requestId) {
        return itemRequestClient.getItemRequest(requestId, userId);
    }
}

package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.error.ValidationException;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.error.NotFoundException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Optional;

import static ru.practicum.shareit.item.mapper.ItemMapper.mapToItemDto;
import static ru.practicum.shareit.item.mapper.ItemMapper.mapToItem;
import static ru.practicum.shareit.user.mapper.UserMapper.mapToUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static java.time.LocalDateTime.now;
import static org.mockito.Mockito.when;
import static java.util.List.*;


@ExtendWith(MockitoExtension.class)
class ItemServiceUnitTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingService bookingService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserService userService;
    private ItemService itemService;
    private ItemDto itemDto;
    private UserDto userDto;
    private Item item;

    @BeforeEach
    void initialize() {
        itemService = new ItemServiceImpl(
                commentRepository,
                itemRepository,
                bookingService,
                userService
        );
        userDto = new UserDto(
                1L,
                "Eddie",
                "eddie@mail.кг");
        item = new Item(
                1L,
                "PocketTest",
                "Testocket",
                true,
                mapToUser(userDto),
                null);
        itemDto = mapToItemDto(item);
    }

    private ItemDto saveItemDto() {
        when(userService.get(any()))
                .thenReturn(userDto);
        when(itemRepository.save(any()))
                .thenReturn(mapToItem(itemDto));
        return itemService.save(itemDto, null, userDto.getId());
    }

    @Test
    void saveTest() {
        var saved = saveItemDto();
        assertEquals(saved.getName(), item.getName());
        assertEquals(saved.getId(), item.getId());
    }

    @Test
    void updateTest() {
        var dto = saveItemDto();
        var updated = new Item(
                dto.getId(),
                "Anthonylia",
                itemDto.getDescription(),
                itemDto.getAvailable(),
                mapToUser(userDto),
                null
        );
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        when(itemRepository.save(any()))
                .thenReturn(updated);
        var update = itemService.update(mapToItemDto(updated), userDto.getId());
        assertNotEquals(dto.getName(), update.getName());
        assertEquals(dto.getId(), update.getId());
    }

    @Test
    void searchTest() {
        saveItemDto();
        when(itemRepository.search(anyString()))
                .thenReturn(of(item));
        var search = itemService.search(
                "oops",
                userDto.getId(),
                null,
                null
        );
        assertEquals(search.get(0).getId(), item.getId());
        assertEquals(search.size(), 1);
    }

//    @Test
//    void updateNullOwnerTest() {
//        var exception = assertThrows(ValidationException.class,
//                () -> itemService.update(itemDto, null));
//        assertEquals("id пользователя не null", exception.getMessage());
//    }


    @Test
    void getItemNotFoundTest() {
        saveItemDto();
        when(itemRepository.findById(anyLong()))
                .thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> itemService.get(42L, userDto.getId()));
    }

    @Test
    void saveCommentNotFoundItemTest() {
        var commentDto = new CommentDto(
                1L,

                itemDto.getId(),
                "pinkRose",
                userDto.getName(),
                now()
        );
        when(itemRepository.findById(anyLong()))
                .thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> itemService.saveComment(commentDto, 42L, 2L));
    }

    @Test
    void searchEmptyTextTest() {
        var search = itemService.search(
                "",
                userDto.getId(),
                null,
                null
        );
        assertEquals(search.size(), 0);
    }

    @Test
    void searchEmptyResultTest() {
        saveItemDto();
        when(itemRepository.search(anyString()))
                .thenReturn(of());
        var search = itemService.search(
                "Fang",
                userDto.getId(),
                null,
                null
        );
        assertEquals(search.size(), 0);
    }

    @Test
    void searchNullTextTest() {
        var commentDto = new CommentDto(
                1L,
                null,
                userDto.getName(),
                itemDto.getId().toString(),
                            now()
        );
        var exception = assertThrows(ValidationException.class,
                () -> itemService.saveComment(commentDto, itemDto.getId(), 2L));
        assertEquals("Текст комментария не пустой", exception.getMessage());
    }

    @Test
    void saveCommentEmptyTextTest() {
        var commentDto = new CommentDto(
                1L,

                itemDto.getId(),
                "",
                userDto.getName(),
                now()
        );
        var exception = assertThrows(ValidationException.class,
                () -> itemService.saveComment(commentDto, itemDto.getId(), 2L));
        assertEquals("Текст комментария не пустой", exception.getMessage());
    }

    @Test
    void getCommentsTest() {
        var commentDto = new CommentDto(
                1L,

                itemDto.getId(),
                "комментарий",
                userDto.getName(),
                now()
        );
        var comment = new Comment(
                1L,
                commentDto.getText(),
                item,
                mapToUser(userDto),
                now()
        );
        when(commentRepository.findCommentByItem_IdIsOrderByCreated(anyLong()))
                .thenReturn(of(comment));
        var allComments = itemService.getAllComments(item.getId());
        assertEquals(allComments.get(0).getId(), comment.getId());
        assertEquals(allComments.size(), 1);
    }

    @Test
    void saveItemEmptyNameTest() {
        var exception = assertThrows(ValidationException.class,
                () -> itemService.save(
                        new ItemDto(
                                null,
                                "",
                                "Blender",
                                true,
                                1L,
                                1L),
                        null,
                        1L)
        );
        assertEquals("Имя не пустое", exception.getMessage());
    }


    @Test
    void getAllCommentsTest() {
        var commentDto = new CommentDto(
                1L,

                itemDto.getId(),
                "space",
                userDto.getName(),
                now()
        );
        var comment = new Comment(
                1L,
                commentDto.getText(),
                item,
                mapToUser(userDto),
                now()
        );
        when(commentRepository.findAll())
                .thenReturn(of(comment));
        var allComments = itemService.getAllComments();
        assertEquals(allComments.get(0).getId(), comment.getId());
        assertEquals(allComments.size(), 1);
    }

    @Test
    void saveItemEmptyDescriptionTest() {
        var exception = assertThrows(ValidationException.class,
                () -> itemService.save(
                        new ItemDto(
                                null,
                                "space",
                                "",
                                true,
                                1L,
                                1L),
                        null,
                        1L)
        );
        assertEquals("Описание не пустое", exception.getMessage());
    }

    @Test
    void saveItemAvailableTest() {
        var exception = assertThrows(ValidationException.class,
                () -> itemService.save(
                        new ItemDto(
                                null,
                                "Joe",
                                "Joe's thing",
                                null,
                                1L,
                                1L),
                        null,
                        1L)
        );
        assertEquals("Available не null", exception.getMessage());
    }
}

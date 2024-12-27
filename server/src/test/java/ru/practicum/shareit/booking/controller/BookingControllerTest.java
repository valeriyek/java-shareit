package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingAllFieldsDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.of;
import static java.time.Month.DECEMBER;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    private final ItemDto itemDto = new ItemDto(1L, "Тестовый предмет", "Описание цвета", true, 1L, 1L);
    private final LocalDateTime startTime = of(2000, DECEMBER, 3, 0, 5, 10);
    private final LocalDateTime endTime = of(2000, DECEMBER, 5, 0, 5, 10);
    private final UserDto userDto = new UserDto(1L, "Лара", "lara@gmail.com");
    private final String headerSharerUserId = "X-Sharer-User-Id";

    @MockBean
    BookingService bookingService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private final BookingAllFieldsDto bookingAllFieldsDto = BookingAllFieldsDto.builder()
            .id(1L)
            .start(startTime)
            .end(endTime)
            .item(itemDto)
            .booker(userDto)
            .status("WAITING")
            .build();


}

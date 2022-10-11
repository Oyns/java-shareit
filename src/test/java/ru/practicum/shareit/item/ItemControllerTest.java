package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingHistory;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.booking.mapper.BookingMapper.toBooking;
import static ru.practicum.shareit.booking.mapper.BookingMapper.toSimpleBookingDto;

@WebMvcTest(controllers = ItemController.class)
@AutoConfigureMockMvc
public class ItemControllerTest {

    @MockBean
    private ItemServiceImpl itemService;


    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("Пила")
            .description("Очень острая пила")
            .available(true)
            .owner(1L)
            .build();

    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("Евгений")
            .email("eugen@mailbox.com")
            .build();

    private final CommentDto commentDto = CommentDto.builder()
            .id(1L)
            .text("Очень ценный комментарий")
            .item(itemDto)
            .authorName(userDto.getName())
            .author(userDto)
            .created(LocalDate.of(2022, 10, 10))
            .build();

    private final BookingDto lastBooking = BookingDto.builder()
            .id(1L)
            .start(LocalDateTime.of(2022, 10, 11, 17, 5))
            .end(LocalDateTime.of(2022, 10, 11, 17, 7))
            .item(new BookingDto.ItemDto(itemDto.getId(), itemDto.getName()))
            .booker(new BookingDto.BookerDto(userDto.getId()))
            .status(BookingState.APPROVED)
            .build();

    private final BookingDto nextBooking = BookingDto.builder()
            .id(2L)
            .start(LocalDateTime.of(2022, 11, 11, 17, 5))
            .end(LocalDateTime.of(2022, 12, 11, 17, 5))
            .item(new BookingDto.ItemDto(2L, "Стул"))
            .booker(new BookingDto.BookerDto(userDto.getId()))
            .status(BookingState.APPROVED)
            .build();

    private final ItemWithBookingHistory.CommentDto commentDto1 = new ItemWithBookingHistory
            .CommentDto(commentDto.getId(),
            commentDto.getText(), commentDto.getAuthorName(),
            commentDto.getCreated());

    private final List<ItemWithBookingHistory.CommentDto> commentDtos = List.of(commentDto1);

    private final ItemWithBookingHistory itemWithBookingHistory = ItemWithBookingHistory.builder()
            .id(itemDto.getId())
            .name(itemDto.getName())
            .description(itemDto.getDescription())
            .available(itemDto.getAvailable())
            .comments(commentDtos)
            .lastBooking(toSimpleBookingDto(toBooking(lastBooking)))
            .nextBooking(toSimpleBookingDto(toBooking(nextBooking)))
            .build();


    @Test
    void postItem() throws Exception {
        when(itemService.postItem(eq(userDto.getId()), any()))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", userDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.owner", is(itemDto.getOwner()), Long.class))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId())));
    }

    @Test
    void postComment() throws Exception {
        when(itemService.postComment(eq(userDto.getId()), any(), any()))
                .thenReturn(commentDto);

        mvc.perform(post("/items/{itemId}/comment", userDto.getId())
                        .content(mapper.writeValueAsString(commentDto))
                        .header("X-Sharer-User-Id", userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.item", is(commentDto.getItem()), ItemDto.class))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.author", is(commentDto.getAuthor()), UserDto.class))
                .andExpect(jsonPath("$.created", is(commentDto.getCreated()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))));
    }

    @Test
    void updateItemInfo() throws Exception {
        when(itemService.updateItemInfo(eq(userDto.getId()), any(), any()))
                .thenReturn(itemDto);

        mvc.perform(patch("/items/{itemId}", itemDto.getId())
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.owner", is(itemDto.getOwner()), Long.class))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId())));
    }

    @Test
    void getItemByIdWithBookingHistory() throws Exception {
        when(itemService.getItemByIdWithBookingHistory(eq(userDto.getId()), anyLong()))
                .thenReturn(itemWithBookingHistory);

        mvc.perform(get("/items/{itemId}", itemWithBookingHistory.getId())
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemWithBookingHistory.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemWithBookingHistory.getName())))
                .andExpect(jsonPath("$.description", is(itemWithBookingHistory.getDescription())))
                .andExpect(jsonPath("$.available", is(itemWithBookingHistory.getAvailable())))
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.lastBooking[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$.lastBooking[?(@.start == '2022-10-11T17:05:00')]").exists())
                .andExpect(jsonPath("$.lastBooking[?(@.end == '2022-10-11T17:07:00')]").exists())
                .andExpect(jsonPath("$.lastBooking[?(@.itemId == 1)]").exists())
                .andExpect(jsonPath("$.lastBooking[?(@.bookerId == 1)]").exists())
                .andExpect(jsonPath("$.nextBooking[?(@.id == 2)]").exists())
                .andExpect(jsonPath("$.nextBooking[?(@.start == '2022-11-11T17:05:00')]").exists())
                .andExpect(jsonPath("$.nextBooking[?(@.end == '2022-12-11T17:05:00')]").exists())
                .andExpect(jsonPath("$.nextBooking[?(@.itemId == 2)]").exists())
                .andExpect(jsonPath("$.nextBooking[?(@.bookerId == 1)]").exists());
    }

    @Test
    void getAllItems() throws Exception {
        when(itemService.getAllItems(eq(userDto.getId())))
                .thenReturn(List.of(itemWithBookingHistory));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemWithBookingHistory.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemWithBookingHistory.getName())))
                .andExpect(jsonPath("$[0].description", is(itemWithBookingHistory.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemWithBookingHistory.getAvailable())))
                .andExpect(jsonPath("$[0].comments[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$[0].comments[?(@.text == 'Очень ценный комментарий')]").exists())
                .andExpect(jsonPath("$[0].comments[?(@.authorName == 'Евгений')]").exists())
                .andExpect(jsonPath("$[0].comments[?(@.created == '2022-10-10')]").exists())
                .andExpect(jsonPath("$[0].lastBooking[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$[0].lastBooking[?(@.start == '2022-10-11T17:05:00')]").exists())
                .andExpect(jsonPath("$[0].lastBooking[?(@.end == '2022-10-11T17:07:00')]").exists())
                .andExpect(jsonPath("$[0].lastBooking[?(@.itemId == 1)]").exists())
                .andExpect(jsonPath("$[0].lastBooking[?(@.bookerId == 1)]").exists())
                .andExpect(jsonPath("$[0].nextBooking[?(@.id == 2)]").exists())
                .andExpect(jsonPath("$[0].nextBooking[?(@.start == '2022-11-11T17:05:00')]").exists())
                .andExpect(jsonPath("$[0].nextBooking[?(@.end == '2022-12-11T17:05:00')]").exists())
                .andExpect(jsonPath("$[0].nextBooking[?(@.itemId == 2)]").exists())
                .andExpect(jsonPath("$[0].nextBooking[?(@.bookerId == 1)]").exists());
    }

    @Test
    void searchItemsByUserId() throws Exception {
        when(itemService.searchForItemsResult(eq(userDto.getId()), anyString()))
                .thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search").param("text", "пила")
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$[0].owner", is(itemDto.getOwner()), Long.class));
    }
}

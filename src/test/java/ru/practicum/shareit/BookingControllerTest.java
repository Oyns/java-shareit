package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @MockBean
    private BookingServiceImpl bookingService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private final DateTimeFormatter formatters = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final BookingDto bookingDto = BookingDto.builder()
            .id(1L)
            .start(LocalDateTime.parse(LocalDateTime.of(2022, 10, 11, 17, 5)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))))
            .end(LocalDateTime.parse(LocalDateTime.of(2022, 10, 11, 17, 7)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))))
            .itemId(1L)
            .bookerId(1L)
            .status(BookingState.APPROVED)
            .build();

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

    private final BookerDto bookerDto = BookerDto.builder()
            .id(bookingDto.getBookerId())
            .build();

    private final ItemWithBookingDto itemWithBookingDto = ItemWithBookingDto.builder()
            .id(itemDto.getId())
            .start(bookingDto.getStart())
            .end(bookingDto.getEnd())
            .booker(bookerDto)
            .item(itemDto)
            .build();

    @Test
    void shouldCreateBooking() throws Exception {
        when(bookingService.createBooking(eq(userDto.getId()), any()))
                .thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", userDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().format(formatters))))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().format(formatters))))
                .andExpect(jsonPath("$.itemId", is(bookingDto.getItemId()), Long.class))
                .andExpect(jsonPath("$.bookerId", is(bookingDto.getBookerId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));
    }

    @Test
    void shouldUpdateBooking() throws Exception {
        when(bookingService.updateBooking(eq(userDto.getId()), anyLong(), anyString()))
                .thenReturn(itemWithBookingDto);

        mvc.perform(patch("/bookings/{bookingId}", bookingDto.getId())
                        .param("approved", "approved")
                        .content(mapper.writeValueAsString(itemWithBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", userDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

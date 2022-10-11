package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@AutoConfigureMockMvc
public class BookingControllerTest {

    @MockBean
    private BookingServiceImpl bookingService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private final DateTimeFormatter formatters = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");


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
    private final BookingDto bookingDto = BookingDto.builder()
            .id(1L)
            .start(LocalDateTime.parse(LocalDateTime.of(2022, 10, 11, 17, 5)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))))
            .end(LocalDateTime.parse(LocalDateTime.of(2022, 10, 11, 17, 7)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))))
            .item(new BookingDto.ItemDto(itemDto.getId(), itemDto.getName()))
            .booker(new BookingDto.BookerDto(userDto.getId()))
            .status(BookingState.APPROVED)
            .build();

    private final BookerDto bookerDto = BookerDto.builder()
            .id(bookingDto.getBooker().getId())
            .build();

    private final ItemWithBookingDto itemWithBookingDto = ItemWithBookingDto.builder()
            .id(itemDto.getId())
            .start(bookingDto.getStart())
            .end(bookingDto.getEnd())
            .booker(bookerDto)
            .item(itemDto)
            .build();

    @Test
    void createBooking() throws Exception {
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
                .andExpect(jsonPath("$.item[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$.item[?(@.name == 'Пила')]").exists())
                .andExpect(jsonPath("$.booker[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));
    }

    @Test
    void updateBooking() throws Exception {
        when(bookingService.updateBooking(eq(userDto.getId()), anyLong(), anyString()))
                .thenReturn(itemWithBookingDto);

        mvc.perform(patch("/bookings/{bookingId}", bookingDto.getId())
                        .param("approved", "approved")
                        .content(mapper.writeValueAsString(itemWithBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", userDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemWithBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(itemWithBookingDto.getStart().format(formatters))))
                .andExpect(jsonPath("$.end", is(itemWithBookingDto.getEnd().format(formatters))))
                .andExpect(jsonPath("$.booker[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$.item[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$.item[?(@.name == 'Пила')]").exists())
                .andExpect(jsonPath("$.item[?(@.description == 'Очень острая пила')]").exists())
                .andExpect(jsonPath("$.item[?(@.available == true)]").exists())
                .andExpect(jsonPath("$.item[?(@.owner == 1)]").exists());
    }

    @Test
    void getBookingById() throws Exception {
        when(bookingService.getBookingById(eq(userDto.getId()), anyLong()))
                .thenReturn(itemWithBookingDto);

        mvc.perform(get("/bookings/{bookingId}", bookingDto.getId())
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemWithBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(itemWithBookingDto.getStart().format(formatters))))
                .andExpect(jsonPath("$.end", is(itemWithBookingDto.getEnd().format(formatters))))
                .andExpect(jsonPath("$.booker[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$.item[?(@.name == 'Пила')]").exists())
                .andExpect(jsonPath("$.item[?(@.description == 'Очень острая пила')]").exists())
                .andExpect(jsonPath("$.item[?(@.available == true)]").exists())
                .andExpect(jsonPath("$.item[?(@.owner == 1)]").exists());
    }

    @Test
    void getAllBookingsByUserId() throws Exception {
        when(bookingService.getAllBookingsByUserId(eq(userDto.getId()), any(), any(), any()))
                .thenReturn(List.of(itemWithBookingDto));

        mvc.perform(get("/bookings/")
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemWithBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(itemWithBookingDto.getStart().format(formatters))))
                .andExpect(jsonPath("$[0].end", is(itemWithBookingDto.getEnd().format(formatters))))
                .andExpect(jsonPath("$[0].booker[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$[0].item[?(@.name == 'Пила')]").exists())
                .andExpect(jsonPath("$[0].item[?(@.description == 'Очень острая пила')]").exists())
                .andExpect(jsonPath("$[0].item[?(@.available == true)]").exists())
                .andExpect(jsonPath("$[0].item[?(@.owner == 1)]").exists());
    }

    @Test
    void getAllBookingsForOwner() throws Exception {
        when(bookingService.getAllBookingsForOwner(eq(userDto.getId()), any(), any(), any()))
                .thenReturn(List.of(itemWithBookingDto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemWithBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(itemWithBookingDto.getStart().format(formatters))))
                .andExpect(jsonPath("$[0].end", is(itemWithBookingDto.getEnd().format(formatters))))
                .andExpect(jsonPath("$[0].booker[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$[0].item[?(@.name == 'Пила')]").exists())
                .andExpect(jsonPath("$[0].item[?(@.description == 'Очень острая пила')]").exists())
                .andExpect(jsonPath("$[0].item[?(@.available == true)]").exists())
                .andExpect(jsonPath("$[0].item[?(@.owner == 1)]").exists());
    }
}

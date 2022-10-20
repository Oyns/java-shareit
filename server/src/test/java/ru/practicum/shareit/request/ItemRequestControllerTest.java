package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@AutoConfigureMockMvc
class ItemRequestControllerTest {

    @MockBean
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private final DateTimeFormatter formatters = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .id(1L)
            .description("Одолжите грабли")
            .requestor(1L)
            .created(LocalDateTime.of(2022, 10, 11, 17, 5))
            .build();

    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("Евгений")
            .email("eugen@mailbox.com")
            .build();

    private final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("Пила")
            .description("Очень острая пила")
            .available(true)
            .owner(1L)
            .build();

    private final RequestWithItemsDto requestWithItemsDto = RequestWithItemsDto.builder()
            .id(1L)
            .description("Одолжите краба")
            .requestorId(1L)
            .created(LocalDateTime.of(2022, 10, 11, 17, 5))
            .items(List.of(itemDto))
            .build();


    @Test
    void postItemRequest() throws Exception {
        when(itemRequestService.postItemRequest(eq(userDto.getId()), any()))
                .thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", userDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.requestor", is(itemRequestDto.getRequestor()), Long.class))
                .andExpect(jsonPath("$.created", is(itemRequestDto.getCreated().format(formatters))));
    }

    @Test
    void getSelfRequests() throws Exception {
        when(itemRequestService.getSelfRequests(eq(userDto.getId())))
                .thenReturn(List.of(requestWithItemsDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestWithItemsDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestWithItemsDto.getDescription())))
                .andExpect(jsonPath("$[0].requestorId", is(requestWithItemsDto.getRequestorId()), Long.class))
                .andExpect(jsonPath("$[0].created", is(requestWithItemsDto.getCreated().format(formatters))))
                .andExpect(jsonPath("$[0].items[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$[0].items[?(@.name == 'Пила')]").exists())
                .andExpect(jsonPath("$[0].items[?(@.description == 'Очень острая пила')]").exists())
                .andExpect(jsonPath("$[0].items[?(@.available == true)]").exists())
                .andExpect(jsonPath("$[0].items[?(@.owner == 1)]").exists());
    }

    @Test
    void getRequests() throws Exception {
        when(itemRequestService.getRequests(eq(userDto.getId()), any(), any()))
                .thenReturn(List.of(requestWithItemsDto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestWithItemsDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestWithItemsDto.getDescription())))
                .andExpect(jsonPath("$[0].requestorId", is(requestWithItemsDto.getRequestorId()), Long.class))
                .andExpect(jsonPath("$[0].created", is(requestWithItemsDto.getCreated().format(formatters))))
                .andExpect(jsonPath("$[0].items[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$[0].items[?(@.name == 'Пила')]").exists())
                .andExpect(jsonPath("$[0].items[?(@.description == 'Очень острая пила')]").exists())
                .andExpect(jsonPath("$[0].items[?(@.available == true)]").exists())
                .andExpect(jsonPath("$[0].items[?(@.owner == 1)]").exists());
    }

    @Test
    void getRequestById() throws Exception {
        when(itemRequestService.getRequestById(eq(userDto.getId()), any()))
                .thenReturn(requestWithItemsDto);

        mvc.perform(get("/requests/{requestId}", requestWithItemsDto.getId())
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestWithItemsDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestWithItemsDto.getDescription())))
                .andExpect(jsonPath("$.requestorId", is(requestWithItemsDto.getRequestorId()), Long.class))
                .andExpect(jsonPath("$.created", is(requestWithItemsDto.getCreated().format(formatters))))
                .andExpect(jsonPath("$.items[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$.items[?(@.name == 'Пила')]").exists())
                .andExpect(jsonPath("$.items[?(@.description == 'Очень острая пила')]").exists())
                .andExpect(jsonPath("$.items[?(@.available == true)]").exists())
                .andExpect(jsonPath("$.items[?(@.owner == 1)]").exists());
    }
}
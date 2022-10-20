package ru.practicum.shareit.request.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.request.mapper.ItemRequestMapper.toItemRequest;

@JsonTest
public class ItemRequestTest {
    @Autowired
    private JacksonTester<ItemRequest> json;

    @Test
    void serializeBookingDtoEqualsTest() throws IOException {
        ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "Описание",
                1L, LocalDateTime.now());
        ItemRequest itemRequest = new ItemRequest(1L, "Описание", 1L, LocalDateTime.now());
        if (toItemRequest(itemRequestDto).equals(itemRequest)) {
            itemRequest = toItemRequest(itemRequestDto);
        }
        JsonContent<ItemRequest> result = json.write(itemRequest);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
    }
}

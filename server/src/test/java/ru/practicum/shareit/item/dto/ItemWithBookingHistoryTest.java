package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;


@JsonTest
public class ItemWithBookingHistoryTest {
    @Autowired
    private JacksonTester<ItemWithBookingHistory.CommentDto> json;

    @Test
    void serializeTest() throws Exception {
        ItemWithBookingHistory.CommentDto withBookingHistory = new ItemWithBookingHistory
                .CommentDto(1L, "текст", new ItemDto(), "Egor",
                new UserDto(), LocalDate.of(2022, 10, 10));

        JsonContent<ItemWithBookingHistory.CommentDto> result = json.write(withBookingHistory);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo(withBookingHistory.getText());
        assertThat(result).extractingJsonPathStringValue("$.authorName")
                .isEqualTo(withBookingHistory.getAuthorName());
    }
}

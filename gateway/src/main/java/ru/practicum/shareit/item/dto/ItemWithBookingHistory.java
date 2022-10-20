package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.SimpleBookingDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemWithBookingHistory {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private SimpleBookingDto lastBooking;
    private SimpleBookingDto nextBooking;
    private List<CommentDto> comments;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentDto {
    private Long id;
    private String text;
    private ItemDto item;
    private String authorName;
    private UserDto author;
    private LocalDate created;
    }
}

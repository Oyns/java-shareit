package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.SimpleBookingDto;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(of = "id")
@Builder
public class ItemWithBookingHistory {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private SimpleBookingDto lastBooking;
    private SimpleBookingDto nextBooking;
    private List<CommentDto> comments;

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    @EqualsAndHashCode(of = "id")
    @Builder
    public static class CommentDto {
        private Long id;
        private String text;
        private String authorName;
        private LocalDate created;
    }
}

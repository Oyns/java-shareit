package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(of = "id")
@Builder
public class CommentDto {
    private Long id;
    private String text;
    private ItemDto item;
    private String authorName;
    private UserDto author;
    private LocalDate created;
}

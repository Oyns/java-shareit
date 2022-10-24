package ru.practicum.shareit.request.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(of = "id")
@Builder
public class ItemRequestDto {
    private Long id;
    private String description;
    private Long requestor;
    private LocalDateTime created;
}

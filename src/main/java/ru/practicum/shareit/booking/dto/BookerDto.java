package ru.practicum.shareit.booking.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(of = "id")
@Builder
public class BookerDto {
    private Long id;
}

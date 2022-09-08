package ru.practicum.shareit.user.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public class UserDto {
    private Long id;
    private String name;
    private String email;
}

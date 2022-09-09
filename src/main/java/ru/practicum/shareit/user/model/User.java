package ru.practicum.shareit.user.model;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public class User {
    private Long id;
    private String name;
    private String email;
}

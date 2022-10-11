package ru.practicum.shareit.request.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@NotNull
@Builder
@Entity
@Table(name = "requests")
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description")
    @NotNull
    private String description;

    @JoinTable(name = "users", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "requestor_id")
    private Long requestor;

    @Column(name = "created")
    private LocalDateTime created;
}

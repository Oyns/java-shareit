package ru.practicum.shareit.item.model;

import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
@Entity
@DynamicUpdate
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "texts")
    private String text;

    @Column(name = "item_id")
    @JoinTable(name = "items", joinColumns = @JoinColumn(name = "id"))
    private Long itemId;

    @Column(name = "author_id")
    @JoinTable(name = "users", joinColumns = @JoinColumn(name = "id"))
    private Long authorId;

    @Column(name = "creation_date")
    private LocalDate created;
}

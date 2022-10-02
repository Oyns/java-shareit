package ru.practicum.shareit.booking.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "start_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime start;

    @Column(name = "end_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime end;

    @Column(name = "item_id")
    @JoinTable(name = "items", joinColumns = @JoinColumn(name = "id"))
    private Long itemId;

    @JoinTable(name = "users", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "booker_id")
    private Long booker;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookingState status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id)
                && Objects.equals(start, booking.start)
                && Objects.equals(end, booking.end)
                && Objects.equals(itemId, booking.itemId)
                && Objects.equals(booker, booking.booker)
                && status == booking.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, start, end, itemId, booker, status);
    }
}

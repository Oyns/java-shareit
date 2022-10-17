package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.itemId = :item_id AND b.end < :end")
    Booking findBookingByItemIdAndEndIsAfter(@Param("item_id") Long itemId, LocalDateTime end);

    Booking findBookingById(@Param("booking_id") Long bookingId);

    List<Booking> findBookingsByBooker(Long userId, Pageable pageable);

    List<Booking> findBookingsByBooker(Long userId);

    @Query("SELECT b FROM Booking b " +
            "LEFT JOIN Item i ON b.itemId = i.id " +
            "WHERE i.owner = :owner_id")
    List<Booking> findBookingsByOwnerId(@Param("owner_id") Long userId);

    @Query("SELECT b FROM Booking b " +
            "LEFT JOIN Item i ON b.itemId = i.id " +
            "WHERE i.owner = :owner_id")
    List<Booking> findBookingsByOwnerId(@Param("owner_id") Long userId, Pageable pageable);

    @Query(value = "SELECT * FROM booking b " +
            "LEFT JOIN items i on i.id = b.item_id " +
            "WHERE b.item_id = :id AND (b.booker_id = :booker_id OR i.owner_id = :booker_id) " +
            "ORDER BY b.start_date DESC LIMIT 1", nativeQuery = true)
    Booking findNextBooking(@Param("id") Long itemId, @Param("booker_id") Long userId);

    @Query(value = "SELECT * FROM booking b " +
            "LEFT JOIN items i on i.id = b.item_id " +
            "WHERE b.item_id = :id AND (b.booker_id = :booker_id OR i.owner_id = :booker_id) " +
            "ORDER BY b.start_date LIMIT 1", nativeQuery = true)
    Booking findLastBooking(@Param("id") Long itemId, @Param("booker_id") Long userId);
}

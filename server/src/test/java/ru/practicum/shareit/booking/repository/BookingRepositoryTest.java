package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class BookingRepositoryTest {
    @Autowired
    private BookingRepository repository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;
    private Booking booking;

    @BeforeEach
    void setUp() {
        userRepository.save(new User(1L, "Egor", "egorka@mail.ru"));
        itemRepository.save(new Item(1L, "Item", "Coolest", true, 1L, null));
        booking = repository.save(new Booking(1L,
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(1),
                1L, 1L, BookingState.APPROVED));
    }

    @Test
    void getBooking() {
        Booking booking1 = repository.findBookingByItemIdAndEndIsAfter(1L, LocalDateTime.now().plusMinutes(3));
        assertEquals(booking.getId(), booking1.getId());
    }
}

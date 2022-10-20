package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.booking.mapper.BookingMapper.toSimpleBookingDto;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class BookingServiceImplTest {

    private final EntityManager em;

    private final BookingServiceImpl bookingService;

    private User user;

    private Item item;

    private Booking booking;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Jack");
        user.setEmail("jjjjack@ya.ru");
        em.persist(user);

        item = new Item();
        item.setName("Вещь");
        item.setDescription("Важная вещь");
        item.setAvailable(true);
        em.persist(item);

        booking = new Booking();
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setStart(LocalDateTime.now().plusMinutes(1));
        booking.setItemId(item.getId());
        booking.setStatus(BookingState.APPROVED);
        booking.setBooker(user.getId());
        em.persist(booking);
    }

    @AfterEach
    void afterEach() {
        em.createNativeQuery("drop table items cascade ");
        em.createNativeQuery("drop table users cascade ");
        em.createNativeQuery("drop table requests cascade ");
        em.createNativeQuery("drop table comments cascade ");
        em.createNativeQuery("drop table booking cascade ");
    }

    @Test
    void createBooking() {
        BookingDto bookingDto = bookingService.createBooking(user.getId(), toSimpleBookingDto(booking));

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking finalBooking = query.setParameter("id", bookingDto.getId()).getSingleResult();

        assertThat(finalBooking.getId(), equalTo(bookingDto.getId()));
        assertThat(finalBooking.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(finalBooking.getStart(), equalTo(bookingDto.getStart()));
        assertThat(finalBooking.getItemId(), equalTo(bookingDto.getItem().getId()));
        assertThat(finalBooking.getBooker(), equalTo(bookingDto.getBooker().getId()));
        assertThat(finalBooking.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    void createBookingFailedOwnerBooking() {
        item.setOwner(user.getId());
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () ->
                bookingService.createBooking(user.getId(), toSimpleBookingDto(booking)));

        assertEquals("Владелец не может бронировать предмет.", thrown.getMessage());
    }

    @Test
    void createBookingFailedAvailable() {
        item.setAvailable(false);
        ValidationException thrown = assertThrows(ValidationException.class, () ->
                bookingService.createBooking(user.getId(), toSimpleBookingDto(booking)));

        assertEquals("Предмет занят другим пользователем.", thrown.getMessage());
    }

    @Test
    void createBookingFailedTiming() {
        booking.setEnd(LocalDateTime.now().minusYears(5));
        ValidationException thrown = assertThrows(ValidationException.class, () ->
                bookingService.createBooking(user.getId(), toSimpleBookingDto(booking)));

        assertEquals("Некорректная дата бронирования", thrown.getMessage());
    }

    @Test
    void updateBooking() {
        booking.setStatus(BookingState.REJECTED);
        item.setOwner(user.getId());

        bookingService.updateBooking(user.getId(),
                booking.getId(),
                "true");

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking booking1 = query.setParameter("id", booking.getId()).getSingleResult();

        assertThat(booking1.getStatus(), equalTo(BookingState.APPROVED));
    }

    @Test
    void updateBookingFailedState() {
        item.setOwner(user.getId());
        ValidationException thrown = assertThrows(ValidationException.class, () ->
                bookingService.updateBooking(user.getId(),
                        booking.getId(),
                        "true"));

        assertEquals("Нельзя изменить статус на идентичный", thrown.getMessage());
    }

    @Test
    void getBookingById() {
        item.setOwner(user.getId());

        ItemWithBookingDto withBookingDto = bookingService.getBookingById(user.getId(), booking.getId());

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking finalBooking = query.setParameter("id", booking.getId()).getSingleResult();

        assertThat(withBookingDto.getId(), notNullValue());
        assertThat(withBookingDto.getBooker().getId(), equalTo(finalBooking.getBooker()));
        assertThat(withBookingDto.getStart(), equalTo(finalBooking.getStart()));
        assertThat(withBookingDto.getEnd(), equalTo(finalBooking.getEnd()));
        assertThat(withBookingDto.getStatus(), equalTo(finalBooking.getStatus()));
    }

    @Test
    void getAllBookingsByUserIdStateAll() {
        item.setOwner(user.getId());

        List<ItemWithBookingDto> withBookingDtos = bookingService
                .getAllBookingsByUserId(user.getId(), "ALL", 0, 1);

        ItemWithBookingDto withBookingDto = withBookingDtos.stream()
                .findFirst()
                .orElseThrow();

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking finalBooking = query.setParameter("id", withBookingDto.getId()).getSingleResult();

        assertThat(withBookingDto.getId(), notNullValue());
        assertThat(withBookingDto.getBooker().getId(), equalTo(finalBooking.getBooker()));
        assertThat(withBookingDto.getStart(), equalTo(finalBooking.getStart()));
        assertThat(withBookingDto.getEnd(), equalTo(finalBooking.getEnd()));
        assertThat(withBookingDto.getStatus(), equalTo(finalBooking.getStatus()));
    }

    @Test
    void getAllBookingsByUserIdWithoutPagesAndState() {
        item.setOwner(user.getId());

        List<ItemWithBookingDto> withBookingDtos = bookingService
                .getAllBookingsByUserId(user.getId(), null, null, null);

        ItemWithBookingDto withBookingDto = withBookingDtos.stream()
                .findFirst()
                .orElseThrow();

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking finalBooking = query.setParameter("id", withBookingDto.getId()).getSingleResult();

        assertThat(withBookingDto.getId(), notNullValue());
        assertThat(withBookingDto.getBooker().getId(), equalTo(finalBooking.getBooker()));
        assertThat(withBookingDto.getStart(), equalTo(finalBooking.getStart()));
        assertThat(withBookingDto.getEnd(), equalTo(finalBooking.getEnd()));
        assertThat(withBookingDto.getStatus(), equalTo(finalBooking.getStatus()));
    }

    @Test
    void getAllBookingsByUserIdStateCurrent() {
        booking.setStart(LocalDateTime.now().minusMinutes(1));
        booking.setEnd(LocalDateTime.now().plusMinutes(1));
        item.setOwner(user.getId());

        List<ItemWithBookingDto> withBookingDtos = bookingService
                .getAllBookingsByUserId(user.getId(), "CURRENT", 0, 1);

        ItemWithBookingDto withBookingDto = withBookingDtos.stream()
                .findFirst()
                .orElseThrow();

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking finalBooking = query.setParameter("id", withBookingDto.getId()).getSingleResult();

        assertThat(withBookingDto.getId(), notNullValue());
        assertThat(withBookingDto.getBooker().getId(), equalTo(finalBooking.getBooker()));
        assertThat(withBookingDto.getStart(), equalTo(finalBooking.getStart()));
        assertThat(withBookingDto.getEnd(), equalTo(finalBooking.getEnd()));
        assertThat(withBookingDto.getStatus(), equalTo(finalBooking.getStatus()));
    }

    @Test
    void getAllBookingsByUserIdStatePast() {
        booking.setEnd(LocalDateTime.now().minusMinutes(2));
        booking.setStart(LocalDateTime.now().minusDays(1));
        item.setOwner(user.getId());

        List<ItemWithBookingDto> withBookingDtos = bookingService
                .getAllBookingsByUserId(user.getId(), "PAST", 0, 1);

        ItemWithBookingDto withBookingDto = withBookingDtos.stream()
                .findFirst()
                .orElseThrow();

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking finalBooking = query.setParameter("id", withBookingDto.getId()).getSingleResult();

        assertThat(withBookingDto.getId(), notNullValue());
        assertThat(withBookingDto.getBooker().getId(), equalTo(finalBooking.getBooker()));
        assertThat(withBookingDto.getStart(), equalTo(finalBooking.getStart()));
        assertThat(withBookingDto.getEnd(), equalTo(finalBooking.getEnd()));
        assertThat(withBookingDto.getStatus(), equalTo(finalBooking.getStatus()));
    }

    @Test
    void getAllBookingsByUserIdStateFuture() {
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setStart(LocalDateTime.now().plusDays(1));
        item.setOwner(user.getId());

        List<ItemWithBookingDto> withBookingDtos = bookingService
                .getAllBookingsByUserId(user.getId(), "FUTURE", 0, 1);

        ItemWithBookingDto withBookingDto = withBookingDtos.stream()
                .findFirst()
                .orElseThrow();

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking finalBooking = query.setParameter("id", withBookingDto.getId()).getSingleResult();

        assertThat(withBookingDto.getId(), notNullValue());
        assertThat(withBookingDto.getBooker().getId(), equalTo(finalBooking.getBooker()));
        assertThat(withBookingDto.getStart(), equalTo(finalBooking.getStart()));
        assertThat(withBookingDto.getEnd(), equalTo(finalBooking.getEnd()));
        assertThat(withBookingDto.getStatus(), equalTo(finalBooking.getStatus()));
    }

    @Test
    void getAllBookingsByUserIdStateRejected() {
        booking.setStatus(BookingState.REJECTED);
        item.setOwner(user.getId());

        List<ItemWithBookingDto> withBookingDtos = bookingService
                .getAllBookingsByUserId(user.getId(), "REJECTED", 0, 1);

        ItemWithBookingDto withBookingDto = withBookingDtos.stream()
                .findFirst()
                .orElseThrow();

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking finalBooking = query.setParameter("id", withBookingDto.getId()).getSingleResult();

        assertThat(withBookingDto.getId(), notNullValue());
        assertThat(withBookingDto.getBooker().getId(), equalTo(finalBooking.getBooker()));
        assertThat(withBookingDto.getStart(), equalTo(finalBooking.getStart()));
        assertThat(withBookingDto.getEnd(), equalTo(finalBooking.getEnd()));
        assertThat(withBookingDto.getStatus(), equalTo(finalBooking.getStatus()));
    }

    @Test
    void getAllBookingsByUserIdStateWaiting() {
        booking.setStatus(BookingState.WAITING);
        item.setOwner(user.getId());

        List<ItemWithBookingDto> withBookingDtos = bookingService
                .getAllBookingsByUserId(user.getId(), "WAITING", 0, 1);

        ItemWithBookingDto withBookingDto = withBookingDtos.stream()
                .findFirst()
                .orElseThrow();

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking finalBooking = query.setParameter("id", withBookingDto.getId()).getSingleResult();

        assertThat(withBookingDto.getId(), notNullValue());
        assertThat(withBookingDto.getBooker().getId(), equalTo(finalBooking.getBooker()));
        assertThat(withBookingDto.getStart(), equalTo(finalBooking.getStart()));
        assertThat(withBookingDto.getEnd(), equalTo(finalBooking.getEnd()));
        assertThat(withBookingDto.getStatus(), equalTo(finalBooking.getStatus()));
    }

    @Test
    void getAllBookingsByUserIdFailedState() {
        String state = "Fail";
        ValidationException thrown = assertThrows(ValidationException.class, () ->
                bookingService
                        .getAllBookingsByUserId(user.getId(), state, 0, 1));

        assertEquals(String.format("Unknown state: %s", state), thrown.getMessage());
    }

    @Test
    void getAllBookingsForOwnerStateAll() {
        item.setOwner(user.getId());

        List<ItemWithBookingDto> withBookingDtos = bookingService
                .getAllBookingsForOwner(user.getId(), "ALL", 0, 1);

        ItemWithBookingDto withBookingDto = withBookingDtos.stream()
                .findFirst()
                .orElseThrow();

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking finalBooking = query.setParameter("id", withBookingDto.getId()).getSingleResult();

        assertThat(withBookingDto.getId(), notNullValue());
        assertThat(withBookingDto.getBooker().getId(), equalTo(finalBooking.getBooker()));
        assertThat(withBookingDto.getStart(), equalTo(finalBooking.getStart()));
        assertThat(withBookingDto.getEnd(), equalTo(finalBooking.getEnd()));
        assertThat(withBookingDto.getStatus(), equalTo(finalBooking.getStatus()));
    }

    @Test
    void getAllBookingsForOwnerWithoutStateAndPages() {
        item.setOwner(user.getId());

        List<ItemWithBookingDto> withBookingDtos = bookingService
                .getAllBookingsForOwner(user.getId(), null, null, null);

        ItemWithBookingDto withBookingDto = withBookingDtos.stream()
                .findFirst()
                .orElseThrow();

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking finalBooking = query.setParameter("id", withBookingDto.getId()).getSingleResult();

        assertThat(withBookingDto.getId(), notNullValue());
        assertThat(withBookingDto.getBooker().getId(), equalTo(finalBooking.getBooker()));
        assertThat(withBookingDto.getStart(), equalTo(finalBooking.getStart()));
        assertThat(withBookingDto.getEnd(), equalTo(finalBooking.getEnd()));
        assertThat(withBookingDto.getStatus(), equalTo(finalBooking.getStatus()));
    }

    @Test
    void getAllBookingsForOwnerStateCurrent() {
        booking.setStart(LocalDateTime.now().minusMinutes(1));
        booking.setEnd(LocalDateTime.now().plusMinutes(1));
        item.setOwner(user.getId());

        List<ItemWithBookingDto> withBookingDtos = bookingService
                .getAllBookingsForOwner(user.getId(), "CURRENT", 0, 1);

        ItemWithBookingDto withBookingDto = withBookingDtos.stream()
                .findFirst()
                .orElseThrow();

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking finalBooking = query.setParameter("id", withBookingDto.getId()).getSingleResult();

        assertThat(withBookingDto.getId(), notNullValue());
        assertThat(withBookingDto.getBooker().getId(), equalTo(finalBooking.getBooker()));
        assertThat(withBookingDto.getStart(), equalTo(finalBooking.getStart()));
        assertThat(withBookingDto.getEnd(), equalTo(finalBooking.getEnd()));
        assertThat(withBookingDto.getStatus(), equalTo(finalBooking.getStatus()));
    }

    @Test
    void getAllBookingsForOwnerStatePast() {
        booking.setEnd(LocalDateTime.now().minusMinutes(2));
        booking.setStart(LocalDateTime.now().minusDays(1));
        item.setOwner(user.getId());

        List<ItemWithBookingDto> withBookingDtos = bookingService
                .getAllBookingsForOwner(user.getId(), "PAST", 0, 1);

        ItemWithBookingDto withBookingDto = withBookingDtos.stream()
                .findFirst()
                .orElseThrow();

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking finalBooking = query.setParameter("id", withBookingDto.getId()).getSingleResult();

        assertThat(withBookingDto.getId(), notNullValue());
        assertThat(withBookingDto.getBooker().getId(), equalTo(finalBooking.getBooker()));
        assertThat(withBookingDto.getStart(), equalTo(finalBooking.getStart()));
        assertThat(withBookingDto.getEnd(), equalTo(finalBooking.getEnd()));
        assertThat(withBookingDto.getStatus(), equalTo(finalBooking.getStatus()));
    }

    @Test
    void getAllBookingsForOwnerStateFuture() {
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setStart(LocalDateTime.now().plusDays(1));
        item.setOwner(user.getId());

        List<ItemWithBookingDto> withBookingDtos = bookingService
                .getAllBookingsForOwner(user.getId(), "FUTURE", 0, 1);

        ItemWithBookingDto withBookingDto = withBookingDtos.stream()
                .findFirst()
                .orElseThrow();

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking finalBooking = query.setParameter("id", withBookingDto.getId()).getSingleResult();

        assertThat(withBookingDto.getId(), notNullValue());
        assertThat(withBookingDto.getBooker().getId(), equalTo(finalBooking.getBooker()));
        assertThat(withBookingDto.getStart(), equalTo(finalBooking.getStart()));
        assertThat(withBookingDto.getEnd(), equalTo(finalBooking.getEnd()));
        assertThat(withBookingDto.getStatus(), equalTo(finalBooking.getStatus()));
    }

    @Test
    void getAllBookingsForOwnerStateRejected() {
        booking.setStatus(BookingState.REJECTED);
        item.setOwner(user.getId());

        List<ItemWithBookingDto> withBookingDtos = bookingService
                .getAllBookingsForOwner(user.getId(), "REJECTED", 0, 1);

        ItemWithBookingDto withBookingDto = withBookingDtos.stream()
                .findFirst()
                .orElseThrow();

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking finalBooking = query.setParameter("id", withBookingDto.getId()).getSingleResult();

        assertThat(withBookingDto.getId(), notNullValue());
        assertThat(withBookingDto.getBooker().getId(), equalTo(finalBooking.getBooker()));
        assertThat(withBookingDto.getStart(), equalTo(finalBooking.getStart()));
        assertThat(withBookingDto.getEnd(), equalTo(finalBooking.getEnd()));
        assertThat(withBookingDto.getStatus(), equalTo(finalBooking.getStatus()));
    }

    @Test
    void getAllBookingsForOwnerStateWaiting() {
        booking.setStatus(BookingState.WAITING);
        item.setOwner(user.getId());

        List<ItemWithBookingDto> withBookingDtos = bookingService
                .getAllBookingsForOwner(user.getId(), "WAITING", 0, 1);

        ItemWithBookingDto withBookingDto = withBookingDtos.stream()
                .findFirst()
                .orElseThrow();

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking finalBooking = query.setParameter("id", withBookingDto.getId()).getSingleResult();

        assertThat(withBookingDto.getId(), notNullValue());
        assertThat(withBookingDto.getBooker().getId(), equalTo(finalBooking.getBooker()));
        assertThat(withBookingDto.getStart(), equalTo(finalBooking.getStart()));
        assertThat(withBookingDto.getEnd(), equalTo(finalBooking.getEnd()));
        assertThat(withBookingDto.getStatus(), equalTo(finalBooking.getStatus()));
    }

    @Test
    void getAllBookingsForOwnerFailedState() {
        item.setOwner(user.getId());
        String state = "Fail";
        ValidationException thrown = assertThrows(ValidationException.class, () ->
                bookingService
                        .getAllBookingsForOwner(user.getId(), state, 0, 1));

        assertEquals(String.format("Unknown state: %s", state), thrown.getMessage());
    }
}

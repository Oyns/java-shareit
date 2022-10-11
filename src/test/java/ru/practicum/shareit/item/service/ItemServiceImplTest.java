package ru.practicum.shareit.item.service;

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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingHistory;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.booking.mapper.BookingMapper.toBooking;
import static ru.practicum.shareit.item.mapper.ItemMapper.*;
import static ru.practicum.shareit.user.mapper.UserMapper.toUserDto;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class ItemServiceImplTest {

    private final EntityManager em;

    private final ItemServiceImpl itemService;

    private User user;

    private Item item;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Jack");
        user.setEmail("jjjjack@ya.ru");
        em.persist(user);

        item = new Item();
        item.setName("Вещь");
        item.setDescription("Ценная вещь");
        item.setOwner(user.getId());
        item.setAvailable(true);
        em.persist(item);
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
    void postItem() {
        ItemDto itemDto = itemService.postItem(user.getId(), toItemDto(item));

        TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item item = query.setParameter("id", itemDto.getId()).getSingleResult();

        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.getRequest(), equalTo(itemDto.getRequestId()));
        assertThat(item.getOwner(), equalTo(itemDto.getOwner()));
        assertThat(item.getAvailable(), equalTo(itemDto.getAvailable()));
    }

    @Test
    void postItemWithoutStatus() {
        item.setAvailable(null);

        ValidationException thrown = assertThrows(ValidationException.class, () ->
                itemService.postItem(user.getId(), toItemDto(item)));
        assertEquals("Статус состояния отсутствует.", thrown.getMessage());
    }

    @Test
    void postItemWithoutName() {
        item.setName("");

        ValidationException thrown = assertThrows(ValidationException.class, () ->
                itemService.postItem(user.getId(), toItemDto(item)));
        assertEquals("Отсутствует название предмета.", thrown.getMessage());
    }

    @Test
    void postItemWithoutDescription() {
        item.setDescription(null);

        ValidationException thrown = assertThrows(ValidationException.class, () ->
                itemService.postItem(user.getId(), toItemDto(item)));
        assertEquals("Отсутствует описание предмета", thrown.getMessage());
    }

    @Test
    void postItemFailedAvailable() {
        item.setAvailable(false);

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () ->
                itemService.postItem(user.getId(), toItemDto(item)));
        assertEquals("Предмет занят.", thrown.getMessage());
    }

    @Test
    void postComment() {
        ItemDto itemDto1 = itemService.getItemById(item.getId());

        BookingDto bookingDto = new BookingDto();
        bookingDto.setEnd(LocalDateTime.now().minusDays(1));
        bookingDto.setStart(LocalDateTime.now().minusDays(2));
        bookingDto.setItemId(itemDto1.getId());
        bookingDto.setBookerId(user.getId());
        bookingDto.setStatus(BookingState.APPROVED);
        em.persist(toBooking(bookingDto));

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Ну текст");
        commentDto.setItem(itemDto1);
        commentDto.setAuthor(toUserDto(user));
        commentDto.setAuthorName(user.getName());
        commentDto.setCreated(LocalDate.now());
        itemService.postComment(user.getId(), itemDto1.getId(), toComment(commentDto));
        TypedQuery<Comment> query = em.createQuery("SELECT c FROM Comment c WHERE c.text = :text", Comment.class);
        Comment comment = query.setParameter("text", commentDto.getText()).getSingleResult();

        assertThat(comment.getId(), notNullValue());
        assertThat(comment.getText(), equalTo(commentDto.getText()));
    }

    @Test
    void updateItemInfo() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Saw");
        itemDto.setDescription("Chain saw");
        itemDto.setAvailable(true);

        itemService.updateItemInfo(user.getId(), item.getId(), itemDto);

        TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item item1 = query.setParameter("id", item.getId()).getSingleResult();

        assertThat(item1.getId(), notNullValue());
    }

    @Test
    void getItemByIdWithBookingHistory() {
        ItemDto itemDto = itemService.getItemById(item.getId());

        BookingDto lastBooking = new BookingDto();
        lastBooking.setEnd(LocalDateTime.now().minusDays(1));
        lastBooking.setStart(LocalDateTime.now().minusDays(2));
        lastBooking.setItemId(itemDto.getId());
        lastBooking.setBookerId(user.getId());
        lastBooking.setStatus(BookingState.APPROVED);
        Booking booking1 = toBooking(lastBooking);
        em.persist(booking1);

        BookingDto nextBooking = new BookingDto();
        nextBooking.setEnd(LocalDateTime.now());
        nextBooking.setStart(LocalDateTime.now().plusDays(2));
        nextBooking.setItemId(itemDto.getId());
        nextBooking.setBookerId(user.getId());
        nextBooking.setStatus(BookingState.APPROVED);
        em.persist(toBooking(nextBooking));

        Comment comment = new Comment();
        comment.setText("Комментарий");
        comment.setAuthorId(user.getId());
        comment.setItemId(item.getId());
        comment.setCreated(LocalDate.now());
        em.persist(comment);

        ItemWithBookingHistory itemWithBookings = itemService.getItemByIdWithBookingHistory(user.getId(), item.getId());

        TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item item1 = query.setParameter("id", item.getId()).getSingleResult();

        TypedQuery<Booking> query1 = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking booking = query1.setParameter("id", booking1.getId()).getSingleResult();

        TypedQuery<Comment> query2 = em.createQuery("SELECT c FROM Comment c WHERE c.id = :id", Comment.class);
        Comment comment1 = query2.setParameter("id", comment.getId()).getSingleResult();

        assertThat(itemWithBookings.getName(), equalTo(item1.getName()));
        assertThat(itemWithBookings.getDescription(), equalTo(item1.getDescription()));
        assertThat(toBooking(itemWithBookings.getLastBooking()), equalTo(booking));
        ItemWithBookingHistory.CommentDto commentDto = itemWithBookings.getComments().stream()
                .filter(commentDto1 -> commentDto1.getId().equals(comment.getId()))
                .findFirst()
                .orElse(null);
        assert commentDto != null;
        assertThat(commentDto.getText(), equalTo(comment1.getText()));
        assertThat(commentDto.getCreated(), equalTo(comment1.getCreated()));
    }

    @Test
    void getAllItems() {
        ItemDto itemDto = itemService.getItemById(item.getId());

        BookingDto lastBooking = new BookingDto();
        lastBooking.setEnd(LocalDateTime.now().minusDays(1));
        lastBooking.setStart(LocalDateTime.now().minusDays(2));
        lastBooking.setItemId(itemDto.getId());
        lastBooking.setBookerId(user.getId());
        lastBooking.setStatus(BookingState.APPROVED);
        Booking booking1 = toBooking(lastBooking);
        em.persist(booking1);

        BookingDto nextBooking = new BookingDto();
        nextBooking.setEnd(LocalDateTime.now());
        nextBooking.setStart(LocalDateTime.now().plusDays(2));
        nextBooking.setItemId(itemDto.getId());
        nextBooking.setBookerId(user.getId());
        nextBooking.setStatus(BookingState.APPROVED);
        em.persist(toBooking(nextBooking));

        Comment comment = new Comment();
        comment.setText("Комментарий");
        comment.setAuthorId(user.getId());
        comment.setItemId(item.getId());
        comment.setCreated(LocalDate.now());
        em.persist(comment);

        List<ItemWithBookingHistory> itemsWithBookings = itemService.getAllItems(user.getId());

        TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item item1 = query.setParameter("id", item.getId()).getSingleResult();

        TypedQuery<Booking> query1 = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking booking = query1.setParameter("id", booking1.getId()).getSingleResult();

        TypedQuery<Comment> query2 = em.createQuery("SELECT c FROM Comment c WHERE c.id = :id", Comment.class);
        Comment comment1 = query2.setParameter("id", comment.getId()).getSingleResult();

        ItemWithBookingHistory itemWithBookings = itemsWithBookings.stream()
                .filter(itemWithBookingHistory -> itemWithBookingHistory.getId().equals(item.getId()))
                .findAny()
                .orElseThrow();

        assertThat(itemsWithBookings, hasSize(1));
        assertThat(itemWithBookings.getName(), equalTo(item1.getName()));
        assertThat(itemWithBookings.getDescription(), equalTo(item1.getDescription()));
        assertThat(toBooking(itemWithBookings.getLastBooking()), equalTo(booking));
        ItemWithBookingHistory.CommentDto commentDto = itemWithBookings.getComments().stream()
                .filter(commentDto1 -> commentDto1.getId().equals(comment.getId()))
                .findFirst()
                .orElse(null);
        assert commentDto != null;
        assertThat(commentDto.getText(), equalTo(comment1.getText()));
        assertThat(commentDto.getCreated(), equalTo(comment1.getCreated()));
    }

    @Test
    void searchForItemsResult() {
        item.setDescription("important item");
        List<ItemDto> items = itemService.searchForItemsResult(user.getId(), "pOrt");

        assertThat(items, hasSize(1));

        TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item item1 = query.setParameter("id", item.getId()).getSingleResult();

        Item item2 = toItem(items.stream().findFirst().orElseThrow());

        assertThat(item1, equalTo(item2));
    }
}

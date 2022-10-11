package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.item.mapper.ItemMapper.toItem;
import static ru.practicum.shareit.request.mapper.ItemRequestMapper.toItemRequestDto;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class ItemRequestServiceImplTest {

    private final EntityManager em;

    private final ItemRequestServiceImpl requestService;

    private User user;

    private Item item;

    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Воробушек");
        user.setEmail("chik@ya.ru");
        em.persist(user);

        item = new Item();
        item.setName("Вещь");
        item.setDescription("Важная вещь");
        item.setAvailable(true);
        em.persist(item);

        itemRequest = new ItemRequest();
        itemRequest.setRequestor(user.getId());
        itemRequest.setDescription("Птичку жалко");
        itemRequest.setCreated(LocalDateTime.now());

        em.persist(itemRequest);
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
    void postItemRequest() {
        ItemRequestDto itemRequestDto = requestService.postItemRequest(user.getId(), toItemRequestDto(itemRequest));

        TypedQuery<ItemRequest> query = em
                .createQuery("SELECT r FROM ItemRequest r WHERE r.id = :id", ItemRequest.class);
        ItemRequest request = query.setParameter("id", itemRequestDto.getId()).getSingleResult();

        assertThat(itemRequestDto.getId(), equalTo(request.getId()));
        assertThat(itemRequestDto.getDescription(), equalTo(request.getDescription()));
        assertThat(itemRequestDto.getRequestor(), equalTo(request.getRequestor()));
        assertThat(itemRequestDto.getCreated(), equalTo(request.getCreated()));
    }

    @Test
    void getSelfRequests() {
        Item item1 = new Item();
        item1.setName("Вещь");
        item1.setDescription("Важная вещь");
        item1.setAvailable(true);
        item1.setRequest(1L);
        em.persist(item1);
        List<RequestWithItemsDto> list = requestService.getSelfRequests(user.getId());

        RequestWithItemsDto requestWithItem = list.stream().findFirst().orElseThrow();

        TypedQuery<ItemRequest> query = em
                .createQuery("SELECT r FROM ItemRequest r WHERE r.id = :id", ItemRequest.class);
        ItemRequest request = query.setParameter("id", requestWithItem.getId()).getSingleResult();

        TypedQuery<Item> query1 = em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item item2 = query1.setParameter("id", item1.getId()).getSingleResult();

        assertThat(requestWithItem.getId(), equalTo(request.getId()));
        assertThat(toItem(requestWithItem.getItems().get(0)), equalTo(item2));
        assertThat(requestWithItem.getDescription(), equalTo(request.getDescription()));
        assertThat(requestWithItem.getCreated(), equalTo(request.getCreated()));
        assertThat(requestWithItem.getRequestorId(), equalTo(request.getRequestor()));
    }

    @Test
    void getRequests() {
        item.setRequest(itemRequest.getId());
        item.setOwner(user.getId());
        List<RequestWithItemsDto> list = requestService.getRequests(user.getId(), 0, 1);

        RequestWithItemsDto requestWithItem = list.stream().findFirst().orElseThrow();

        TypedQuery<ItemRequest> query = em
                .createQuery("SELECT r FROM ItemRequest r WHERE r.id = :id", ItemRequest.class);
        ItemRequest request = query.setParameter("id", requestWithItem.getId()).getSingleResult();

        TypedQuery<Item> query1 = em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item item1 = query1.setParameter("id", item.getId()).getSingleResult();

        assertThat(requestWithItem.getId(), equalTo(request.getId()));
        assertThat(toItem(requestWithItem.getItems().get(0)), equalTo(item1));
        assertThat(requestWithItem.getDescription(), equalTo(request.getDescription()));
        assertThat(requestWithItem.getCreated(), equalTo(request.getCreated()));
        assertThat(requestWithItem.getRequestorId(), equalTo(request.getRequestor()));
    }

    @Test
    void getRequestsFailedPagination() {
        item.setRequest(itemRequest.getId());
        item.setOwner(user.getId());
        ValidationException thrown = assertThrows(ValidationException.class, () ->
                requestService.getRequests(user.getId(), -1, -1));
        assertEquals("Страница и диапазон поиска не могут быть отрицательными.", thrown.getMessage());
    }

    @Test
    void getRequestsWithoutPagination() {
        item.setRequest(itemRequest.getId());
        item.setOwner(user.getId());
        List<RequestWithItemsDto> list = requestService.getRequests(user.getId(), null, null);

        RequestWithItemsDto requestWithItem = list.stream().findFirst().orElseThrow();

        TypedQuery<ItemRequest> query = em
                .createQuery("SELECT r FROM ItemRequest r WHERE r.id = :id", ItemRequest.class);
        ItemRequest request = query.setParameter("id", requestWithItem.getId()).getSingleResult();

        TypedQuery<Item> query1 = em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item item1 = query1.setParameter("id", item.getId()).getSingleResult();

        assertThat(requestWithItem.getId(), equalTo(request.getId()));
        assertThat(toItem(requestWithItem.getItems().get(0)), equalTo(item1));
        assertThat(requestWithItem.getDescription(), equalTo(request.getDescription()));
        assertThat(requestWithItem.getCreated(), equalTo(request.getCreated()));
        assertThat(requestWithItem.getRequestorId(), equalTo(request.getRequestor()));
    }

    @Test
    void getRequestById() {
        item.setRequest(itemRequest.getId());

        RequestWithItemsDto requestWithItem = requestService.getRequestById(user.getId(), itemRequest.getId());

        TypedQuery<ItemRequest> query = em
                .createQuery("SELECT r FROM ItemRequest r WHERE r.id = :id", ItemRequest.class);
        ItemRequest request = query.setParameter("id", requestWithItem.getId()).getSingleResult();

        TypedQuery<Item> query1 = em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item item1 = query1.setParameter("id", item.getId()).getSingleResult();

        assertThat(requestWithItem.getId(), equalTo(request.getId()));
        assertThat(toItem(requestWithItem.getItems().get(0)), equalTo(item1));
        assertThat(requestWithItem.getDescription(), equalTo(request.getDescription()));
        assertThat(requestWithItem.getCreated(), equalTo(request.getCreated()));
        assertThat(requestWithItem.getRequestorId(), equalTo(request.getRequestor()));
    }
}

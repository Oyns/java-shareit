package ru.practicum.shareit.item.service;


import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingHistory;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.mapper.BookingMapper.toBookingDto;
import static ru.practicum.shareit.item.mapper.ItemMapper.*;
import static ru.practicum.shareit.utilities.Validator.validateItemDto;

@Service
public class ItemServiceImpl implements ItemService {
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final UserServiceImpl userServiceImpl;
    private final ItemRepository itemRepository;

    public ItemServiceImpl(CommentRepository commentRepository, BookingRepository bookingRepository,
                           UserServiceImpl userServiceImpl, ItemRepository itemRepository) {
        this.commentRepository = commentRepository;
        this.bookingRepository = bookingRepository;
        this.userServiceImpl = userServiceImpl;
        this.itemRepository = itemRepository;
    }

    @Override
    public ItemDto postItem(Long userId, ItemDto itemDto) {
        validateItemDto(itemDto);
        checkItemOwner(userId);
        itemDto.setOwner(userId);
        return toItemDto(itemRepository.save(toItem(itemDto)));
    }

    @Override
    public CommentDto postComment(Long userId, Long itemId, Comment comment) {
        if (comment == null || comment.getText().isEmpty()) {
            throw new ValidationException("Поле комментария не может быть пустым.");
        }
        checkItemOwner(userId);
        ItemDto itemDto = toItemDto(itemRepository.findById(itemId).orElseThrow());
        UserDto userDto = userServiceImpl.getUserById(userId);
        validateItemDto(itemDto);
        if (bookingRepository.findBookingByItemIdAndEndIsAfter(itemId, LocalDateTime.now()) == null) {
            throw new ValidationException("Вы не можете разместить комментарий.");
        }
        CommentDto commentDto = new CommentDto();
        commentDto.setText(comment.getText());
        commentDto.setItem(itemDto);
        commentDto.setAuthorName(userDto.getName());
        commentDto.setCreated(LocalDate.now());
        commentDto.setAuthor(userDto);
        return toCommentDto(commentRepository.save(toComment(commentDto)), itemDto, userDto);
    }

    @Override
    public ItemDto updateItemInfo(Long userId, Long itemId, ItemDto itemDto) {
        checkItemOwner(userId);
        Item item = itemRepository.findById(itemId).orElse(null);
        Item itemFromDto = toItem(itemDto);
        assert item != null;
        if (!item.getOwner().equals(userId) && item.getAvailable().equals(false)) {
            throw new EntityNotFoundException("Нельзя изменить данные чужой вещи");
        }
        if (itemFromDto.getName() != null) {
            item.setName(itemFromDto.getName());
        }
        if (itemFromDto.getDescription() != null) {
            item.setDescription(itemFromDto.getDescription());
        }
        if (itemFromDto.getAvailable() != null && !itemFromDto.getAvailable().equals(item.getAvailable())) {
            item.setAvailable(itemFromDto.getAvailable());
        }
        itemRepository.save(item);
        return toItemDto(item);
    }


    @Override
    public ItemDto getItemById(Long itemId) {
        try {
            return toItemDto(itemRepository.findById(itemId).orElseThrow());
        } catch (Exception e) {
            throw new EntityNotFoundException("Предмета с таким id не существует");
        }
    }

    @Override
    public ItemWithBookingHistory getItemByIdWithBookingHistory(Long userId, Long itemId) {
        if (itemRepository.findById(itemId).orElse(null) == null) {
            throw new EntityNotFoundException(String.format("Предмета с id %s не существует.", itemId));
        }
        ItemWithBookingHistory itemWithBookingHistory = new ItemWithBookingHistory();
        ItemDto itemDto = toItemDto(itemRepository.findById(itemId).orElseThrow());
        List<ItemWithBookingHistory.CommentDto> commentDtos = new ArrayList<>();
        BookingDto lastBooking = new BookingDto();
        BookingDto nextBooking = new BookingDto();
        if (commentRepository.findCommentByItemId(itemId) != null) {
            Comment comment = commentRepository.findCommentByItemId(itemId);
            CommentDto commentDto = toCommentDto(comment, itemDto, userServiceImpl.getUserById(comment.getAuthorId()));
            ItemWithBookingHistory.CommentDto commentDto1 = new ItemWithBookingHistory.CommentDto(commentDto.getId(),
                    commentDto.getText(), commentDto.getAuthorName(),
                    commentDto.getCreated());
            commentDtos.add(commentDto1);
        } else {
            itemWithBookingHistory.setComments(new ArrayList<>());
        }
        if (bookingRepository.findNextBooking(itemId, userId) != null
                && bookingRepository.findLastBooking(itemId, userId) != null) {
            lastBooking = toBookingDto(bookingRepository.findLastBooking(itemId, userId));
            nextBooking = toBookingDto(bookingRepository.findNextBooking(itemId, userId));
        }
        if (!itemDto.getOwner().equals(userId)) {
            lastBooking = null;
            nextBooking = null;
        }
        return toItemWithBookingHistory(itemDto, lastBooking, nextBooking, commentDtos);
    }

    @Override
    public List<ItemWithBookingHistory> getAllItems(Long userId) {
        ItemWithBookingHistory itemWithBookingHistory = new ItemWithBookingHistory();
        List<ItemWithBookingHistory.CommentDto> commentDtos = new ArrayList<>();
        List<ItemWithBookingHistory> itemDtos = new ArrayList<>();
        BookingDto lastBooking = new BookingDto();
        BookingDto nextBooking = new BookingDto();
        ItemDto itemDto;
        for (Item item : itemRepository.findAll()) {
            if (Objects.equals(item.getOwner(), userId)) {
                itemDto = toItemDto(itemRepository.findById(item.getId()).orElseThrow());
                if (bookingRepository.findNextBooking(item.getId(), userId) != null
                        && bookingRepository.findLastBooking(item.getId(), userId) != null) {
                    lastBooking = toBookingDto(bookingRepository.findLastBooking(item.getId(), userId));
                    nextBooking = toBookingDto(bookingRepository.findNextBooking(item.getId(), userId));
                }
                if (commentRepository.findCommentByItemId(item.getId()) != null) {
                    Comment comment = commentRepository.findCommentByItemId(item.getId());
                    CommentDto commentDto = toCommentDto(comment, itemDto,
                            userServiceImpl.getUserById(comment.getAuthorId()));
                    ItemWithBookingHistory.CommentDto commentDto1
                            = new ItemWithBookingHistory.CommentDto(commentDto.getId(),
                            commentDto.getText(), commentDto.getAuthorName(),
                            commentDto.getCreated());
                    commentDtos.add(commentDto1);
                } else {
                    itemWithBookingHistory.setComments(new ArrayList<>());
                }
                if (bookingRepository.findNextBooking(item.getId(), userId) != null
                        && bookingRepository.findLastBooking(item.getId(), userId) != null
                        && Objects.requireNonNull(lastBooking).getStatus().equals(BookingState.APPROVED)) {
                    itemDtos.add(toItemWithBookingHistory(itemDto, lastBooking, nextBooking, commentDtos));
                } else {
                    itemDtos.add(toItemWithBookingHistory(itemDto, null, null, commentDtos));
                }
            }
        }
        return itemDtos.stream()
                .sorted(Comparator.comparing(ItemWithBookingHistory::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchForItemsResult(Long userId, String text) {
        List<ItemDto> itemDtos = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        List<Item> items = itemRepository.findAll().stream()
                .filter(item -> ((item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase())))
                        && item.getAvailable().equals(true))
                .collect(Collectors.toList());
        for (Item item : items) {
            itemDtos.add(toItemDto(item));
        }
        return itemDtos;
    }

    private void checkItemOwner(Long userId) {
        userServiceImpl.getAllUsers().stream()
                .filter(user -> user.getId().equals(userId))
                .findAny()
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }
}

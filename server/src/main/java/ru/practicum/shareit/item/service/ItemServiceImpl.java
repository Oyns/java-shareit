package ru.practicum.shareit.item.service;


import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.SimpleBookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
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

import static ru.practicum.shareit.booking.mapper.BookingMapper.toSimpleBookingDto;
import static ru.practicum.shareit.item.mapper.ItemMapper.*;

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
        validateItemOwner(userId);
        itemDto.setOwner(userId);
        return toItemDto(itemRepository.save(toItem(itemDto)));
    }

    @Override
    public ItemWithBookingHistory.CommentDto postComment(Long userId,
                                                         Long itemId,
                                                         ItemWithBookingHistory.CommentDto comment) {
        validateForPostComment(itemId);
        validateItemOwner(userId);
        ItemDto itemDto = toItemDto(itemRepository.findById(itemId).orElseThrow());
        UserDto userDto = userServiceImpl.getUserById(userId);
        ItemWithBookingHistory.CommentDto commentDto = new ItemWithBookingHistory.CommentDto();
        commentDto.setText(comment.getText());
        commentDto.setItem(itemDto);
        commentDto.setAuthorName(userDto.getName());
        commentDto.setCreated(LocalDate.now());
        commentDto.setAuthor(userDto);
        return toCommentDto(commentRepository.save(toComment(commentDto)), itemDto, userDto);
    }

    @Override
    public ItemDto updateItemInfo(Long userId, Long itemId, ItemDto itemDto) {
        validateItemOwner(userId);
        return toItemDto(itemRepository.save(validateItemForUpdate(itemDto, itemId, userId)));
    }


    @Override
    public ItemDto getItemById(Long itemId) {
        validateItemExists(itemId);
        return toItemDto(itemRepository.findById(itemId).orElseThrow());
    }

    @Override
    public ItemWithBookingHistory getItemByIdWithBookingHistory(Long userId, Long itemId) {
        validateItemExists(itemId);
        ItemWithBookingHistory itemWithBookingHistory = new ItemWithBookingHistory();
        ItemDto itemDto = toItemDto(itemRepository.findById(itemId).orElseThrow());
        List<ItemWithBookingHistory.CommentDto> commentDtos = new ArrayList<>();
        SimpleBookingDto lastBooking = new SimpleBookingDto();
        SimpleBookingDto nextBooking = new SimpleBookingDto();
        if (commentRepository.findCommentByItemId(itemId) != null) {
            Comment comment = commentRepository.findCommentByItemId(itemId);
            ItemWithBookingHistory.CommentDto commentDto = toCommentDto(comment, itemDto, userServiceImpl.getUserById(comment.getAuthorId()));
            commentDtos.add(commentDto);
        } else {
            itemWithBookingHistory.setComments(new ArrayList<>());
        }
        if (bookingRepository.findNextBooking(itemId, userId) != null
                && bookingRepository.findLastBooking(itemId, userId) != null) {
            lastBooking = toSimpleBookingDto(bookingRepository.findLastBooking(itemId, userId));
            nextBooking = toSimpleBookingDto(bookingRepository.findNextBooking(itemId, userId));
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
        SimpleBookingDto lastBooking = new SimpleBookingDto();
        SimpleBookingDto nextBooking = new SimpleBookingDto();
        ItemDto itemDto;
        for (Item item : itemRepository.findAll()) {
            if (Objects.equals(item.getOwner(), userId)) {
                itemDto = toItemDto(itemRepository.findById(item.getId()).orElseThrow());
                if (bookingRepository.findNextBooking(item.getId(), userId) != null
                        && bookingRepository.findLastBooking(item.getId(), userId) != null) {
                    lastBooking = toSimpleBookingDto(bookingRepository.findLastBooking(item.getId(), userId));
                    nextBooking = toSimpleBookingDto(bookingRepository.findNextBooking(item.getId(), userId));
                }
                if (commentRepository.findCommentByItemId(item.getId()) != null) {
                    Comment comment = commentRepository.findCommentByItemId(item.getId());
                    ItemWithBookingHistory.CommentDto commentDto = toCommentDto(comment, itemDto,
                            userServiceImpl.getUserById(comment.getAuthorId()));
                    commentDtos.add(commentDto);
                } else {
                    itemWithBookingHistory.setComments(new ArrayList<>());
                }
                if (bookingRepository.findNextBooking(item.getId(), userId) != null
                        && bookingRepository.findLastBooking(item.getId(), userId) != null
                        && Objects.requireNonNull(bookingRepository.findBookingById(lastBooking.getId()))
                        .getStatus().equals(BookingState.APPROVED)) {
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

    private void validateItemOwner(Long userId) {
        userServiceImpl.getAllUsers().stream()
                .filter(user -> user.getId().equals(userId))
                .findAny()
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    private void validateItemExists(Long itemId) {
        if (itemRepository.findById(itemId).orElse(null) == null) {
            throw new EntityNotFoundException(String.format("Предмета с id %s не существует.", itemId));
        }
    }

    private void validateForPostComment(Long itemId) {
        if (bookingRepository.findBookingByItemIdAndEndIsAfter(itemId, LocalDateTime.now()) == null) {
            throw new ValidationException("Вы не можете разместить комментарий.");
        }
    }

    private Item validateItemForUpdate(ItemDto itemDto, Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new ValidationException("Предмета не существует"));
        if (!item.getOwner().equals(userId) && item.getAvailable().equals(false)) {
            throw new EntityNotFoundException("Нельзя изменить данные чужой вещи");
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getAvailable() != null && !itemDto.getAvailable().equals(item.getAvailable())) {
            item.setAvailable(itemDto.getAvailable());
        }
        return item;
    }
}

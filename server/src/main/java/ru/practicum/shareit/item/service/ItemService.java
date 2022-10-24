package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingHistory;

import java.util.List;

public interface ItemService {
    ItemDto postItem(Long userId, ItemDto itemDto);

    ItemWithBookingHistory.CommentDto postComment(Long userId, Long itemId, ItemWithBookingHistory.CommentDto comment);

    ItemDto updateItemInfo(Long userId, Long itemId, ItemDto itemDto);

    ItemDto getItemById(Long itemId);

    ItemWithBookingHistory getItemByIdWithBookingHistory(Long userId, Long itemId);

    List<ItemWithBookingHistory> getAllItems(Long userId);

    List<ItemDto> searchForItemsResult(Long userId, String text);
}

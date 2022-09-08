package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item postItem(String userId, ItemDto itemDto);

    Item updateItemInfo(String userId, Long itemId, ItemDto itemDto);

    Item getItemById(Long itemId);

    List<Item> getAllItems(String userId);

    List<Item> searchForItemsResult(String userId, String text);
}

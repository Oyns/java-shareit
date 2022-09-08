package ru.practicum.shareit.item.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.utilities.Validator.itemValidator;

@Repository
public class ItemServiceImpl implements ItemService {

    private final List<Item> items = new ArrayList<>();
    private Long id = 1L;
    private final ItemMapper itemMapper;

    private final UserServiceImpl userServiceImpl;

    @Autowired
    public ItemServiceImpl(ItemMapper itemMapper, UserServiceImpl userServiceImpl) {
        this.itemMapper = itemMapper;
        this.userServiceImpl = userServiceImpl;
    }

    @Override
    public Item postItem(String userId, ItemDto itemDto) {
        itemValidator(itemDto);
        itemOwnerCheck(userId);
        itemDto.setId(id++);
        itemDto.setOwner(Long.parseLong(userId));
        items.add(itemMapper.toItem(itemDto));
        return itemMapper.toItem(itemDto);
    }

    @Override
    public Item updateItemInfo(String userId, Long itemId, ItemDto itemDto) {
        itemOwnerCheck(userId);
        Item item = items.stream()
                .filter(item1 -> item1.getId().equals(itemId))
                .findAny()
                .orElse(null);
        assert item != null;
        if (!item.getOwner().equals(Long.parseLong(userId)) && item.getAvailable().equals(false)) {
            throw new EntityNotFoundException("Нельзя изменить данные чужой вещи");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null && !itemDto.getAvailable().equals(item.getAvailable())) {
            item.setAvailable(itemDto.getAvailable());
        }
        return item;
    }


    @Override
    public Item getItemById(Long itemId) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findAny()
                .orElseThrow();
    }

    @Override
    public List<Item> getAllItems(String userId) {
        return items.stream()
                .filter(item -> item.getOwner().equals(Long.parseLong(userId)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> itemsFromSearchResult(String userId, String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        return items.stream()
                .filter(item -> ((item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase())))
                        && item.getAvailable().equals(true))
                .collect(Collectors.toList());
    }

    private void itemOwnerCheck(String userId) {
        userServiceImpl.getAllUsers().stream()
                .filter(user -> user.getId().equals(Long.parseLong(userId)))
                .findAny()
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }
}

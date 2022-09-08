package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemServiceImpl itemServiceImpl;

    @Autowired
    public ItemController(ItemServiceImpl itemServiceImpl) {
        this.itemServiceImpl = itemServiceImpl;
    }

    @PostMapping
    @ResponseBody
    public Item postItem(@RequestHeader("X-Sharer-User-Id") String userId,
                         @RequestBody ItemDto itemDto) {
        return itemServiceImpl.postItem(userId, itemDto);
    }

    @PatchMapping("{itemId}")
    @ResponseBody
    public Item updateItemInfo(@RequestHeader("X-Sharer-User-Id") String userId,
                               @PathVariable Long itemId,
                               @RequestBody ItemDto itemDto) {
        return itemServiceImpl.updateItemInfo(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public Item getItemById(@PathVariable Long itemId) {
        return itemServiceImpl.getItemById(itemId);
    }

    @GetMapping
    public List<Item> getAllItems(@RequestHeader("X-Sharer-User-Id") String userId) {
        return itemServiceImpl.getAllItems(userId);
    }

    @GetMapping("/search")
    public List<Item> itemsFromSearchResult(@RequestHeader("X-Sharer-User-Id") String userId,
                                            @RequestParam String text) {
        return itemServiceImpl.itemsFromSearchResult(userId, text);
    }
}

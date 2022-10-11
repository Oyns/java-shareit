package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingHistory;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import java.util.List;

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
    public ItemDto postItem(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                            @RequestBody ItemDto itemDto) {
        return itemServiceImpl.postItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseBody
    public ItemWithBookingHistory.CommentDto postComment(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                  @PathVariable Long itemId,
                                  @RequestBody Comment comment) {
        return itemServiceImpl.postComment(userId, itemId, comment);
    }

    @PatchMapping("/{itemId}")
    @ResponseBody
    public ItemDto updateItemInfo(@RequestHeader("X-Sharer-User-Id") Long userId,
                                  @PathVariable Long itemId,
                                  @RequestBody ItemDto itemDto) {
        return itemServiceImpl.updateItemInfo(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingHistory getItemByIdWithBookingHistory(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                @PathVariable(required = false) Long itemId) {
        return itemServiceImpl.getItemByIdWithBookingHistory(userId, itemId);
    }

    @GetMapping
    public List<ItemWithBookingHistory> getAllItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemServiceImpl.getAllItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam String text) {
        return itemServiceImpl.searchForItemsResult(userId, text);
    }
}

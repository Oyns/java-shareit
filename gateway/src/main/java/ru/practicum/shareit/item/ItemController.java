package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingHistory;

import javax.validation.constraints.Positive;

import static ru.practicum.shareit.utilities.Validator.validateCommentText;
import static ru.practicum.shareit.utilities.Validator.validateItemDto;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> postItem(@RequestHeader(value = "X-Sharer-User-Id") @Positive Long userId,
                                           @RequestBody ItemDto itemDto) {
        log.info("Creating item {}, userId={}", itemDto, userId);
        validateItemDto(itemDto);
        return itemClient.postItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> postComment(@RequestHeader(value = "X-Sharer-User-Id") @Positive Long userId,
                                              @PathVariable @Positive Long itemId,
                                              @RequestBody ItemWithBookingHistory.CommentDto commentDto) {
        log.info("Creating userId={}, itemId={}, comment {}", userId, itemId, commentDto);
        validateCommentText(commentDto);
        return itemClient.postComment(userId, itemId, commentDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItemInfo(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                 @PathVariable @Positive Long itemId,
                                                 @RequestBody ItemDto itemDto) {
        log.info("Creating item={}, userId={}, itemId={}", itemDto, userId, itemId);
        return itemClient.updateItemInfo(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemByIdWithBookingHistory(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                                @PathVariable(required = false) @Positive Long itemId) {
        log.info("Creating userId={}, itemId={}", userId, itemId);
        return itemClient.getItemByIdWithBookingHistory(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItems(@RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("Creating userId={}", userId);
        return itemClient.getAllItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItemsByUserId(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                      @RequestParam(name = "text") String text) {
        log.info("Creating userId={}, text={}", userId, text);
        return itemClient.searchItemsByUserId(userId, text);
    }
}

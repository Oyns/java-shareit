package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.utilities.Validator.validateRequestDescription;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    @ResponseBody
    public ResponseEntity<Object> postItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Creating userId={}, request={}", userId, itemRequestDto);
        validateRequestDescription(itemRequestDto);
        return itemRequestClient.postItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getSelfRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Creating userId={}", userId);
        return itemRequestClient.getSelfRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam(name = "from",
                                                      defaultValue = "0") @PositiveOrZero Integer from,
                                              @RequestParam(name = "size",
                                                      defaultValue = "10") @Positive Integer size) {
        log.info("Creating userId={}, from={}, size={}", userId, from, size);
        return itemRequestClient.getRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable Long requestId) {
        log.info("Creating userId={}, requestId={}", userId, requestId);
        return itemRequestClient.getRequestById(userId, requestId);
    }
}

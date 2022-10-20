package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    @ResponseBody
    public ResponseEntity<Object> postItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestClient.postItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getSelfRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.getSelfRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam(name = "from",
                                                      defaultValue = "0") Integer from,
                                              @RequestParam(name = "size",
                                                      defaultValue = "10") Integer size) {
        return itemRequestClient.getRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable Long requestId) {
        return itemRequestClient.getRequestById(userId, requestId);
    }
}

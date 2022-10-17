package ru.practicum.shareit.request;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;

import java.util.List;


@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestServiceImpl itemRequestServiceImpl;

    public ItemRequestController(ItemRequestServiceImpl itemRequestServiceImpl) {
        this.itemRequestServiceImpl = itemRequestServiceImpl;
    }

    @PostMapping
    @ResponseBody
    public ItemRequestDto postItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestServiceImpl.postItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public List<RequestWithItemsDto> getSelfRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestServiceImpl.getSelfRequests(userId);
    }

    @GetMapping("/all")
    public List<RequestWithItemsDto> getRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestParam(required = false) Integer from,
                                                 @RequestParam(required = false) Integer size) {
        return itemRequestServiceImpl.getRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public RequestWithItemsDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long requestId) {
        return itemRequestServiceImpl.getRequestById(userId, requestId);
    }
}

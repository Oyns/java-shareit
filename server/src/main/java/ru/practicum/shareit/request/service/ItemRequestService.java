package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto postItemRequest(Long userId, ItemRequestDto itemRequestDto);

    List<RequestWithItemsDto> getSelfRequests(Long userId);

    List<RequestWithItemsDto> getRequests(Long userId, Integer from, Integer size);

    RequestWithItemsDto getRequestById(Long userId, Long requestId);
}

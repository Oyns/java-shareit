package ru.practicum.shareit.request.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Component
public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto) {
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .description(itemRequestDto.getDescription())
                .requestor(itemRequestDto.getRequestor())
                .created(itemRequestDto.getCreated())
                .build();
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestor(itemRequest.getRequestor())
                .created(itemRequest.getCreated())
                .build();
    }

    public static RequestWithItemsDto toRequestWithItemsDto(ItemRequest itemRequest,
                                                            List<ItemDto> items) {
        return RequestWithItemsDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestorId(itemRequest.getRequestor())
                .created(itemRequest.getCreated())
                .items(items)
                .build();
    }
}

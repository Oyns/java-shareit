package ru.practicum.shareit.request.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.request.mapper.ItemRequestMapper.*;
import static ru.practicum.shareit.utilities.Validator.validatePageAndSize;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;

    private final UserServiceImpl userServiceImpl;

    private final ItemRepository itemRepository;

    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository,
                                  UserServiceImpl userServiceImpl,
                                  ItemRepository itemRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userServiceImpl = userServiceImpl;
        this.itemRepository = itemRepository;
    }

    @Override
    public ItemRequestDto postItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        validateItemOwner(userId);
        if (itemRequestDto.getDescription() == null) {
            throw new ValidationException("Описание не может быть пустым.");
        }
        itemRequestDto.setRequestor(userId);
        itemRequestDto.setCreated(LocalDateTime.now());
        return toItemRequestDto(itemRequestRepository.save(toItemRequest(itemRequestDto)));
    }

    @Override
    public List<RequestWithItemsDto> getSelfRequests(Long userId) {
        validateItemOwner(userId);
        RequestWithItemsDto requestWithItemsDto;
        List<RequestWithItemsDto> requestWithItems = new ArrayList<>();
        List<ItemDto> itemDtos;
        List<ItemRequestDto> requests = itemRequestRepository.findAllByRequestor(userId).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        for (ItemRequestDto itemRequestDto : requests) {
            itemDtos = itemRepository.findAllByRequest(itemRequestDto.getId()).stream()
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
            requestWithItemsDto = toRequestWithItemsDto(toItemRequest(itemRequestDto), itemDtos);
            requestWithItems.add(requestWithItemsDto);
        }
        return requestWithItems;
    }

    @Override
    public List<RequestWithItemsDto> getRequests(Long userId, Integer from, Integer size) {
        validateItemOwner(userId);
        validatePageAndSize(from, size);
        RequestWithItemsDto requestWithItemsDto;
        List<RequestWithItemsDto> requestWithItems = new ArrayList<>();
        if (from == null || size == null) {
            List<ItemRequestDto> requests = itemRequestRepository.findAll().stream()
                    .filter(itemRequest -> !itemRequest.getRequestor().equals(userId))
                    .map(ItemRequestMapper::toItemRequestDto)
                    .collect(Collectors.toList());
            for (ItemRequestDto itemRequestDto : requests) {
                List<ItemDto> itemDtos = itemRepository.findAll().stream()
                        .map(ItemMapper::toItemDto)
                        .collect(Collectors.toList());
                requestWithItemsDto = toRequestWithItemsDto(toItemRequest(itemRequestDto), itemDtos);
                requestWithItems.add(requestWithItemsDto);
            }
        } else {
            int page = from / size;
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by("created"));
            List<ItemRequestDto> requests = itemRequestRepository.findAll(pageRequest).stream()
                    .filter(itemRequest -> !itemRequest.getRequestor().equals(userId))
                    .map(ItemRequestMapper::toItemRequestDto)
                    .collect(Collectors.toList());
            for (ItemRequestDto itemRequestDto : requests) {
                List<ItemDto> itemDtos = itemRepository.findAllByRequest(itemRequestDto.getId()).stream()
                        .map(ItemMapper::toItemDto)
                        .collect(Collectors.toList());
                requestWithItemsDto = toRequestWithItemsDto(toItemRequest(itemRequestDto), itemDtos);
                requestWithItems.add(requestWithItemsDto);
            }
        }
        return requestWithItems;
    }

    @Override
    public RequestWithItemsDto getRequestById(Long userId, Long requestId) {
        validateItemOwner(userId);
        ItemRequestDto itemRequestDto = toItemRequestDto(itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Запрос не найден.")));
        List<ItemDto> itemDtos = itemRepository.findAllByRequest(itemRequestDto.getId()).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        return toRequestWithItemsDto(toItemRequest(itemRequestDto), itemDtos);
    }

    private void validateItemOwner(Long userId) {
        userServiceImpl.getAllUsers().stream()
                .filter(user -> user.getId().equals(userId))
                .findAny()
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }
}

package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.SimpleBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.mapper.BookingMapper.*;
import static ru.practicum.shareit.item.mapper.ItemMapper.toItemDto;
import static ru.practicum.shareit.item.mapper.ItemMapper.toItemWithBookingDto;
import static ru.practicum.shareit.utilities.Validator.validatePageAndSize;

@Service
public class BookingServiceImpl implements BookingService {

    private final ItemRepository itemRepository;
    private final UserServiceImpl userServiceImpl;
    private final BookingRepository bookingRepository;
    private final ItemServiceImpl itemServiceImpl;

    public BookingServiceImpl(ItemRepository itemRepository,
                              UserServiceImpl userServiceImpl,
                              BookingRepository bookingRepository,
                              ItemServiceImpl itemServiceImpl) {
        this.itemRepository = itemRepository;
        this.userServiceImpl = userServiceImpl;
        this.bookingRepository = bookingRepository;
        this.itemServiceImpl = itemServiceImpl;
    }

    @Override
    public BookingDto createBooking(Long userId, SimpleBookingDto simpleBookingDto) {
        validateUser(userId);
        ItemDto itemDto = itemServiceImpl.getItemById(simpleBookingDto.getItemId());
        validateForBookingCreation(userId, itemDto, simpleBookingDto);
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItem(toBookingItemDto(itemDto));
        bookingDto.setStart(simpleBookingDto.getStart());
        bookingDto.setEnd(simpleBookingDto.getEnd());
        bookingDto.setStatus(BookingState.WAITING);
        bookingDto.setBooker(new BookingDto.BookerDto(userId));
        Booking booking = bookingRepository.save(toBooking(bookingDto));
        bookingDto.setId(booking.getId());
        return bookingDto;
    }

    @Override
    public ItemWithBookingDto updateBooking(Long userId, Long bookingId, String approved) {
        Booking booking = bookingRepository.findBookingById(bookingId);
        ItemDto itemDto = toItemDto(itemRepository.findAllByOwner(userId).stream()
                .filter(itemDto1 -> Objects.equals(itemDto1.getId(), booking.getItemId()))
                .findAny()
                .orElseThrow(() -> new EntityNotFoundException("Статус бронирования может изменить только владелец")));
        if (approved.equals("true")) {
            approved = "APPROVED";
        } else {
            approved = "REJECTED";
        }
        validateForBookingUpdate(approved, toBookingDto(booking, itemDto));
        booking.setStatus(BookingState.valueOf(approved));
        bookingRepository.save(booking);
        return ItemWithBookingDto.builder()
                .id(bookingId)
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(BookingState.valueOf(approved))
                .booker(new BookingDto.BookerDto(booking.getBooker()))
                .item(itemDto)
                .build();
    }

    @Override
    public ItemWithBookingDto getBookingById(Long userId, Long bookingId) {
        if (bookingRepository.findBookingById(bookingId) == null) {
            throw new EntityNotFoundException(String.format("Брони с id %s не существует", bookingId));
        }
        Booking booking = bookingRepository.findBookingById(bookingId);
        ItemDto itemDto = itemServiceImpl.getItemById(booking.getItemId());
        if (!userId.equals(itemDto.getOwner()) && !userId.equals(booking.getBooker())) {
            throw new EntityNotFoundException("Вы не можете получить данные чужой брони.");
        }
        return ItemWithBookingDto.builder()
                .id(bookingId)
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(new BookingDto.BookerDto(booking.getBooker()))
                .item(itemDto)
                .build();
    }

    @Override
    public List<ItemWithBookingDto> getAllBookingsByUserId(Long userId,
                                                           String state,
                                                           Integer from,
                                                           Integer size) {
        validateUser(userId);
        List<ItemWithBookingDto> itemWithBookingDtos = new ArrayList<>();
        validateBookingsForBooker(userId, state, itemWithBookingDtos, from, size);
        return itemWithBookingDtos;
    }

    @Override
    public List<ItemWithBookingDto> getAllBookingsForOwner(Long userId,
                                                           String state,
                                                           Integer from,
                                                           Integer size) {
        validateUser(userId);
        List<ItemWithBookingDto> itemWithBookingDtos = new ArrayList<>();
        validateBookingsForOwner(userId, state, itemWithBookingDtos, from, size);
        return itemWithBookingDtos;
    }

    private void validateUser(Long userId) {
        userServiceImpl.getAllUsers().stream()
                .filter(user -> user.getId().equals(userId))
                .findAny()
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    private void validateForBookingCreation(Long userId, ItemDto itemDto, SimpleBookingDto simpleBookingDto) {
        if (userId.equals(itemDto.getOwner())) {
            throw new EntityNotFoundException("Владелец не может бронировать предмет.");
        }
        if (!itemDto.getAvailable()) {
            throw new ValidationException("Предмет занят другим пользователем.");
        }
        if (simpleBookingDto.getStart().isAfter(simpleBookingDto.getEnd())
                || simpleBookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Некорректная дата бронирования");
        }
    }

    private void validateForBookingUpdate(String approved, BookingDto bookingDto) {
        if (bookingDto.getStatus().toString().equals(approved)) {
            throw new ValidationException("Нельзя изменить статус на идентичный");
        }
    }

    private void validateBookingsForBooker(Long userId,
                                           String state,
                                           List<ItemWithBookingDto> itemWithBookingDtos,
                                           Integer from,
                                           Integer size) {
        validatePageAndSize(from, size);
        List<Booking> bookings;
        if (from != null && size != null) {
            int page = from / size;
            Pageable pageRequest = PageRequest.of(page, size, Sort.by("start").descending());
            bookings = bookingRepository.findBookingsByBooker(userId, pageRequest);
        } else {
            bookings = bookingRepository.findBookingsByBooker(userId).stream()
                    .sorted(Comparator.comparing(Booking::getStart).reversed())
                    .collect(Collectors.toList());
        }
        for (Booking booking : bookings) {
            ItemDto itemDto = itemServiceImpl.getItemById(booking.getItemId());
            if (state == null) {
                itemWithBookingDtos.add(toItemWithBookingDto(booking,
                        itemDto,
                        toBookingDtoFromBooker(toBookingDto(booking, itemDto))));
            } else {
                switch (state) {
                    case "ALL":
                        itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                itemDto,
                                toBookingDtoFromBooker(toBookingDto(booking, itemDto))));
                        break;
                    case "PAST":
                        if (booking.getEnd().isBefore(LocalDateTime.now())) {
                            itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                    itemDto,
                                    toBookingDtoFromBooker(toBookingDto(booking, itemDto))));
                        }
                        break;
                    case "FUTURE":
                        if (booking.getStart().isAfter(LocalDateTime.now())) {
                            itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                    itemDto,
                                    toBookingDtoFromBooker(toBookingDto(booking, itemDto))));
                        }
                        break;
                    case "CURRENT":
                        if (booking.getStart().isBefore(LocalDateTime.now())
                                && booking.getEnd().isAfter(LocalDateTime.now())) {
                            itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                    itemDto,
                                    toBookingDtoFromBooker(toBookingDto(booking, itemDto))));
                        }
                        break;
                    case "REJECTED":
                        if (booking.getStatus().equals(BookingState.REJECTED)) {
                            itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                    itemDto,
                                    toBookingDtoFromBooker(toBookingDto(booking, itemDto))));
                        }
                        break;
                    case "WAITING":
                        if (booking.getStatus().equals(BookingState.WAITING)) {
                            itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                    itemDto,
                                    toBookingDtoFromBooker(toBookingDto(booking, itemDto))));
                        }
                        break;
                    default:
                        throw new ValidationException(String.format("Unknown state: %s", state));
                }
            }
        }
    }


    private void validateBookingsForOwner(Long userId,
                                          String state,
                                          List<ItemWithBookingDto> itemWithBookingDtos,
                                          Integer from,
                                          Integer size) {
        validatePageAndSize(from, size);
        List<Booking> bookings;
        if (from != null && size != null) {
            int page = from / size;
            Pageable pageRequest = PageRequest.of(page, size, Sort.by("start").descending());
            bookings = bookingRepository.findBookingsByOwnerId(userId, pageRequest);
        } else {
            bookings = bookingRepository.findBookingsByOwnerId(userId).stream()
                    .sorted(Comparator.comparing(Booking::getStart).reversed())
                    .collect(Collectors.toList());
        }
        for (Booking booking : bookings) {
            ItemDto itemDto = itemServiceImpl.getItemById(booking.getItemId());
            if (state == null) {
                itemWithBookingDtos.add(toItemWithBookingDto(booking,
                        itemDto,
                        toBookingDtoFromBooker(toBookingDto(booking, itemDto))));
            } else {
                switch (state) {
                    case "ALL":
                        itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                itemDto,
                                toBookingDtoFromBooker(toBookingDto(booking, itemDto))));
                        break;
                    case "PAST":
                        if (booking.getEnd().isBefore(LocalDateTime.now())) {
                            itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                    itemDto,
                                    toBookingDtoFromBooker(toBookingDto(booking, itemDto))));
                        }
                        break;
                    case "FUTURE":
                        if (booking.getStart().isAfter(LocalDateTime.now())) {
                            itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                    itemDto,
                                    toBookingDtoFromBooker(toBookingDto(booking, itemDto))));
                        }
                        break;
                    case "CURRENT":
                        if (booking.getStart().isBefore(LocalDateTime.now())
                                && booking.getEnd().isAfter(LocalDateTime.now())
                                && booking.getStatus().equals(BookingState.APPROVED)) {
                            itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                    itemDto,
                                    toBookingDtoFromBooker(toBookingDto(booking, itemDto))));
                        }
                    case "REJECTED":
                        if (booking.getStatus().equals(BookingState.REJECTED)) {
                            itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                    itemDto,
                                    toBookingDtoFromBooker(toBookingDto(booking, itemDto))));
                        }
                        break;
                    case "WAITING":
                        if (booking.getStatus().equals(BookingState.WAITING)) {
                            itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                    itemDto,
                                    toBookingDtoFromBooker(toBookingDto(booking, itemDto))));
                        }
                        break;
                    default:
                        throw new ValidationException(String.format("Unknown state: %s", state));
                }
            }
        }
    }
}

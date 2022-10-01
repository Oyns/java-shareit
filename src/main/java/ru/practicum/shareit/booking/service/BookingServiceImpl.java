package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
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

import static ru.practicum.shareit.booking.mapper.BookingMapper.*;
import static ru.practicum.shareit.item.mapper.ItemMapper.*;

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
    public BookingDto createBooking(Long userId, BookingDto bookingDto) {
        Booking booking = toBooking(bookingDto);
        checkItemOwner(userId);
        ItemDto itemDto = itemServiceImpl.getItemById(bookingDto.getItemId());
        if (userId.equals(itemDto.getOwner())) {
            throw new EntityNotFoundException("Владелец не может бронировать предмет.");
        }
        if (!itemDto.getAvailable()) {
            throw new ValidationException("Предмет занят другим пользователем.");
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd())
                || bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Некорректная даты бронирования");
        }
        booking.setItemId(toItem(itemServiceImpl.getItemById(bookingDto.getItemId())).getId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setStatus(BookingState.WAITING);
        booking.setBooker(userId);
        bookingRepository.save(booking);
        return toBookingDto(booking);
    }

    @Override
    public ItemWithBookingDto updateBooking(Long userId, Long bookingId, String approved) {
        BookingDto bookingDto = toBookingDto(bookingRepository.findBookingById(bookingId));
        ItemDto itemDto = toItemDto(itemRepository.findAllByOwner(userId).stream()
                .filter(itemDto1 -> Objects.equals(itemDto1.getId(), bookingDto.getItemId()))
                .findAny()
                .orElseThrow(() -> new EntityNotFoundException("Статус бронирования может изменить только владелец")));
        if (approved.equals("true")) {
            approved = "APPROVED";
        } else {
            approved = "REJECTED";
        }
        if (bookingDto.getStatus().toString().equals(approved)) {
            throw new ValidationException("Нельзя изменить статус на идентичный");
        }
        bookingDto.setStatus(BookingState.valueOf(approved));
        bookingRepository.save(toBooking(bookingDto));
        return ItemWithBookingDto.builder()
                .id(bookingId)
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .status(BookingState.valueOf(approved))
                .booker(toBookingDtoFromBooker(bookingDto))
                .item(itemDto)
                .build();
    }

    @Override
    public ItemWithBookingDto getBookingById(Long userId, Long bookingId) {
        if (bookingRepository.findBookingById(bookingId) == null) {
            throw new EntityNotFoundException(String.format("Брони с id %s не существует", bookingId));
        }
        BookingDto bookingDto = toBookingDto(bookingRepository.findBookingById(bookingId));
        ItemDto itemDto = itemServiceImpl.getItemById(bookingDto.getItemId());
        if (!userId.equals(itemDto.getOwner()) && !userId.equals(bookingDto.getBookerId())) {
            throw new EntityNotFoundException("Вы не можете получить данные чужой брони.");
        }
        return ItemWithBookingDto.builder()
                .id(bookingId)
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .status(bookingDto.getStatus())
                .booker(toBookingDtoFromBooker(bookingDto))
                .item(itemDto)
                .build();
    }

    @Override
    public List<ItemWithBookingDto> getAllBookingsByUserId(Long userId, String state) {
        checkItemOwner(userId);
        List<ItemWithBookingDto> itemWithBookingDtos = new ArrayList<>();
        for (Booking booking : bookingRepository.findBookingsByBooker(userId)) {
            ItemDto itemDto = itemServiceImpl.getItemById(toBookingDto(booking).getItemId());
            switch (state) {
                case "ALL":
                    itemWithBookingDtos.add(toItemWithBookingDto(booking,
                            itemDto,
                            toBookingDtoFromBooker(toBookingDto(booking))));
                    break;
                case "PAST":
                    if (booking.getEnd().isBefore(LocalDateTime.now())) {
                        itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                itemDto,
                                toBookingDtoFromBooker(toBookingDto(booking))));
                    }
                    break;
                case "FUTURE":
                    if (booking.getStart().isAfter(LocalDateTime.now())) {
                        itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                itemDto,
                                toBookingDtoFromBooker(toBookingDto(booking))));
                    }
                    break;
                case "CURRENT":
                    if (booking.getStart().isBefore(LocalDateTime.now())
                            && booking.getEnd().isAfter(LocalDateTime.now())) {
                        itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                itemDto,
                                toBookingDtoFromBooker(toBookingDto(booking))));
                    }
                    break;
                case "REJECTED":
                    if (booking.getStatus().equals(BookingState.REJECTED)) {
                        itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                itemDto,
                                toBookingDtoFromBooker(toBookingDto(booking))));
                    }
                    break;
                case "WAITING":
                    if (booking.getStatus().equals(BookingState.WAITING)) {
                        itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                itemDto,
                                toBookingDtoFromBooker(toBookingDto(booking))));
                    }
                    break;
                default:
                    throw new ValidationException(String.format("Unknown state: %s", state));
            }
        }
        itemWithBookingDtos.sort(Comparator.comparing(ItemWithBookingDto::getStart).reversed());
        return itemWithBookingDtos;
    }

    @Override
    public List<ItemWithBookingDto> getAllBookingsForOwner(Long userId, String state) {
        checkItemOwner(userId);
        List<ItemWithBookingDto> itemWithBookingDtos = new ArrayList<>();
        for (Booking booking : bookingRepository.findBookingsByOwnerId(userId)) {
            ItemDto itemDto = itemServiceImpl.getItemById(toBookingDto(booking).getItemId());
            switch (state) {
                case "ALL":
                    itemWithBookingDtos.add(toItemWithBookingDto(booking,
                            itemDto,
                            toBookingDtoFromBooker(toBookingDto(booking))));
                    break;
                case "PAST":
                    if (booking.getEnd().isBefore(LocalDateTime.now())) {
                        itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                itemDto,
                                toBookingDtoFromBooker(toBookingDto(booking))));
                    }
                    break;
                case "FUTURE":
                    if (booking.getStart().isAfter(LocalDateTime.now())) {
                        itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                itemDto,
                                toBookingDtoFromBooker(toBookingDto(booking))));
                    }
                    break;
                case "CURRENT":
                    if (booking.getStart().isBefore(LocalDateTime.now())
                            && booking.getEnd().isAfter(LocalDateTime.now())
                            && booking.getStatus().equals(BookingState.APPROVED)) {
                        itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                itemDto,
                                toBookingDtoFromBooker(toBookingDto(booking))));
                    }
                case "REJECTED":
                    if (booking.getStatus().equals(BookingState.REJECTED)) {
                        itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                itemDto,
                                toBookingDtoFromBooker(toBookingDto(booking))));
                    }
                    break;
                case "WAITING":
                    if (booking.getStatus().equals(BookingState.WAITING)) {
                        itemWithBookingDtos.add(toItemWithBookingDto(booking,
                                itemDto,
                                toBookingDtoFromBooker(toBookingDto(booking))));
                    }
                    break;
                default:
                    throw new ValidationException(String.format("Unknown state: %s", state));
            }
        }
        itemWithBookingDtos.sort(Comparator.comparing(ItemWithBookingDto::getStart).reversed());
        return itemWithBookingDtos;
    }

    private void checkItemOwner(Long userId) {
        userServiceImpl.getAllUsers().stream()
                .filter(user -> user.getId().equals(userId))
                .findAny()
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }
}

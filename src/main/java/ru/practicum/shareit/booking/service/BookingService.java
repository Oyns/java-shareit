package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.SimpleBookingDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(Long userId, SimpleBookingDto simpleBookingDto);

    ItemWithBookingDto updateBooking(Long userId, Long bookingId, String approved);

    ItemWithBookingDto getBookingById(Long userId, Long bookingId);

    List<ItemWithBookingDto> getAllBookingsByUserId(Long userId,
                                                    String state,
                                                    Integer from,
                                                    Integer size);

    List<ItemWithBookingDto> getAllBookingsForOwner(Long userId,
                                                    String state,
                                                    Integer from,
                                                    Integer size);
}

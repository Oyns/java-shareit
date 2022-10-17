package ru.practicum.shareit.booking.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.SimpleBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;

@Component
public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking, ItemDto itemDto) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(new BookingDto.ItemDto(itemDto.getId(), itemDto.getName()))
                .booker(new BookingDto.BookerDto(booking.getBooker()))
                .build();
    }

    public static SimpleBookingDto toSimpleBookingDto(Booking booking) {
        return SimpleBookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(booking.getItemId())
                .bookerId(booking.getBooker())
                .build();
    }

    public static Booking toBooking(BookingDto bookingDto) {
        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .itemId(bookingDto.getItem().getId())
                .booker(bookingDto.getBooker().getId())
                .status(bookingDto.getStatus())
                .build();
    }

    public static BookingDto.ItemDto toBookingItemDto(ItemDto itemDto) {
        return BookingDto.ItemDto.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .build();
    }

    public static BookingDto.BookerDto toBookingDtoFromBooker(BookingDto bookingDto) {
        return BookingDto.BookerDto.builder()
                .id(bookingDto.getBooker().getId())
                .build();
    }
}

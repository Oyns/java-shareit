package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.SimpleBookingDto;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingServiceImpl bookingServiceImpl;

    @Autowired
    public BookingController(BookingServiceImpl bookingServiceImpl) {
        this.bookingServiceImpl = bookingServiceImpl;
    }

    @PostMapping
    @ResponseBody
    public BookingDto createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @RequestBody SimpleBookingDto simpleBookingDto) {
        return bookingServiceImpl.createBooking(userId, simpleBookingDto);
    }

    @PatchMapping("{bookingId}")
    @ResponseBody
    public ItemWithBookingDto updateBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @PathVariable Long bookingId,
                                            @RequestParam String approved) {
        return bookingServiceImpl.updateBooking(userId, bookingId, approved);
    }

    @GetMapping("{bookingId}")
    public ItemWithBookingDto getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long bookingId) {
        return bookingServiceImpl.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<ItemWithBookingDto> getAllBookingsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                           @RequestParam(required = false) String state,
                                                           @RequestParam(required = false) Integer from,
                                                           @RequestParam(required = false) Integer size) {
        return bookingServiceImpl.getAllBookingsByUserId(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<ItemWithBookingDto> getAllBookingsForOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                           @RequestParam(required = false) String state,
                                                           @RequestParam(required = false) Integer from,
                                                           @RequestParam(required = false) Integer size) {
        return bookingServiceImpl.getAllBookingsForOwner(userId, state, from, size);
    }
}

package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.SimpleBookingDto;
import ru.practicum.shareit.exception.ValidationException;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestBody SimpleBookingDto simpleBookingDto) {
        log.info("Creating booking {}, userId={}", simpleBookingDto, userId);
        return bookingClient.createBooking(userId, simpleBookingDto);
    }

    @PatchMapping("{bookingId}")
    public ResponseEntity<Object> updateBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @PathVariable Long bookingId,
                                                @RequestParam(name = "approved") String approved) {
        log.info("Creating userId={}, bookingId={}, approved={}", userId, bookingId, approved);
        return bookingClient.updateBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllBookingsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                         @RequestParam(name = "state",
                                                                 defaultValue = "all") String stateParam,
                                                         @RequestParam(name = "from",
                                                                 defaultValue = "0") Integer from,
                                                         @RequestParam(name = "size",
                                                                 defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new ValidationException(String.format("Unknown state: %s", stateParam)));
        log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getAllBookingsByUserId(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllBookingsForOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                         @RequestParam(name = "state",
                                                                 defaultValue = "all") String stateParam,
                                                         @RequestParam(name = "from",
                                                                 defaultValue = "0") Integer from,
                                                         @RequestParam(name = "size",
                                                                 defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new ValidationException("Unknown state: " + stateParam));
        log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getAllBookingsForOwner(userId, state, from, size);
    }
}

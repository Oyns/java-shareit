package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.booking.mapper.BookingMapper.toBooking;

@JsonTest
public class BookingDtoTest {
    @Autowired
    private JacksonTester<BookingDto> json;

    @Autowired
    private JacksonTester<BookingDto.ItemDto> json1;

    @Autowired
    private JacksonTester<BookingDto.BookerDto> json2;

    @Autowired
    private JacksonTester<Booking> json3;

    @Test
    void serializeBookingDtoTest() throws IOException {
        BookingDto bookingDto = new BookingDto(1L, LocalDateTime.now(), LocalDateTime.now().plusMinutes(2),
                BookingState.APPROVED, new BookingDto.BookerDto(1L), new BookingDto.ItemDto(1L, "axe"));

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
    }


    @Test
    void serializeItemDtoTest() throws IOException {
        BookingDto.ItemDto itemDto = new BookingDto.ItemDto(1L, "axe");

        JsonContent<BookingDto.ItemDto> result = json1.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(itemDto.getName());
    }

    @Test
    void serializeBookerDtoTest() throws IOException {
        BookingDto.BookerDto bookerDto = new BookingDto.BookerDto(1L);

        JsonContent<BookingDto.BookerDto> result = json2.write(bookerDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
    }

    @Test
    void serializeBookingDtoEqualsTest() throws IOException {
        BookingDto bookingDto = new BookingDto(1L, LocalDateTime.now(), LocalDateTime.now().plusMinutes(2),
                BookingState.APPROVED, new BookingDto.BookerDto(1L), new BookingDto.ItemDto(1L, "axe"));
        Booking booking = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusMinutes(2),
                1L, 1L,
                BookingState.APPROVED);
        if (toBooking(bookingDto).equals(booking)) {
            booking = toBooking(bookingDto);
        }
        JsonContent<Booking> result = json3.write(booking);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
    }
}

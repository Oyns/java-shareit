package ru.practicum.shareit.utilities;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.SimpleBookingDto;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingHistory;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Component
public class Validator {

    public static void validateUserDto(UserDto userDto) throws ValidationException {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank() || !userDto.getEmail().contains("@")) {
            throw new ValidationException("Некорректный email.");
        }
    }

    public static void validateItemDto(ItemDto itemDto) {
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус состояния отсутствует.");
        }
        if (itemDto.getName().isBlank() || itemDto.getName() == null) {
            throw new ValidationException("Отсутствует название предмета.");
        }
        if (itemDto.getDescription() == null) {
            throw new ValidationException("Отсутствует описание предмета");
        }
        if (!itemDto.getAvailable()) {
            throw new EntityNotFoundException("Предмет занят.");
        }
    }

    public static void validateBookingDate(SimpleBookingDto simpleBookingDto) {
        if (simpleBookingDto.getStart().isAfter(simpleBookingDto.getEnd())
                || simpleBookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Некорректная дата бронирования");
        }
    }

    public static void validateCommentText(ItemWithBookingHistory.CommentDto commentDto) {
        if (commentDto.getText() == null || commentDto.getText().isEmpty()) {
            throw new ValidationException("Поле комментария не может быть пустым.");
        }
    }

    public static void validateRequestDescription(ItemRequestDto requestDto) {
        if (requestDto.getDescription() == null) {
            throw new ValidationException("Описание не может быть пустым.");
        }
    }
}

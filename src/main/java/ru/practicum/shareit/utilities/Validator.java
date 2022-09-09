package ru.practicum.shareit.utilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

@Component
@Slf4j
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
}

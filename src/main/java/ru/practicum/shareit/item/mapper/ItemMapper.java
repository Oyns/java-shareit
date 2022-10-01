package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.dto.ItemWithBookingHistory;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Component
public class ItemMapper {

    public static Item toItem(ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(itemDto.getOwner())
                .request(itemDto.getRequest())
                .build();
    }

    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .owner(item.getOwner())
                .request(item.getRequest())
                .build();
    }

    public static ItemWithBookingDto toItemWithBookingDto(Booking booking, ItemDto itemDto, BookerDto bookerDto) {
        return ItemWithBookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(bookerDto)
                .item(itemDto)
                .build();
    }

    public static CommentDto toCommentDto(Comment comment, ItemDto itemDto, UserDto userDto) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .item(itemDto)
                .authorName(userDto.getName())
                .author(userDto)
                .created(comment.getCreated())
                .build();
    }

    public static Comment toComment(CommentDto commentDto) {
        return Comment.builder()
                .id(commentDto.getId())
                .text(commentDto.getText())
                .itemId(commentDto.getItem().getId())
                .authorId(commentDto.getAuthor().getId())
                .created(commentDto.getCreated())
                .build();
    }

    public static ItemWithBookingHistory toItemWithBookingHistory(ItemDto itemDto,
                                                                  BookingDto lastBooking,
                                                                  BookingDto nextBooking,
                                                                  List<ItemWithBookingHistory.CommentDto> commentDtos) {
        return ItemWithBookingHistory.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .comments(commentDtos)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .build();
    }
}

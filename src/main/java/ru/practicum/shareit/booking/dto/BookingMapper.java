package ru.practicum.shareit.booking.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.dto.UserMapper;

@UtilityClass
public class BookingMapper {

    public BookingDto toBookingDto(Booking booking) {
        return new BookingDto(booking.getId(), booking.getStart(),
                booking.getEnd(), ItemMapper.toItemDto(booking.getItem()),
                UserMapper.toUserDto(booking.getBooker()), booking.getStatus(),
                booking.getItem().getId());
    }

    public Booking toBooking(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(ItemMapper.toItem(bookingDto.getItem()));
        booking.setBooker(UserMapper.toUser(bookingDto.getBooker()));
        booking.setStatus(bookingDto.getStatus());

        return booking;
    }

    public BookingShortDto toBookingShortDto (Booking booking) {
        return new BookingShortDto(booking.getId(), booking.getStart(), booking.getEnd(), booking.getItem(),
                UserMapper.toUserDtoShort(booking.getBooker()), booking.getBooker().getId(), booking.getStatus());
    }
}

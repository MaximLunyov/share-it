package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.List;

public interface BookingService {

    Booking createBooking(Long bookerId, BookingDto bookingDto);

    Booking updateBooking(Long ownerId, Long bookingId, Boolean approved);

    BookingShortDto getById(Long userId, Long bookingId);

    List<BookingShortDto> getByUserId(Long userId, String state);

    List<BookingShortDto> getByOwnerId(Long ownerId, String state);
}

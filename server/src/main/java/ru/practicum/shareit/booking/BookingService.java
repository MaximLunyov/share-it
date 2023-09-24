package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(Long bookerId, BookingDto bookingDto);

    BookingDto updateBooking(Long ownerId, Long bookingId, Boolean approved);

    BookingShortDto getById(Long userId, Long bookingId);

    List<BookingShortDto> getByUserId(Long userId, String state, Integer from, Integer size);

    List<BookingShortDto> getByOwnerId(Long ownerId, String state, Integer from, Integer size);
}

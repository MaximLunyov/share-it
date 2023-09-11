package ru.practicum.shareit.booking.dto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.NoSuchElementException;

@Slf4j
@Service
public class BookingMapper {

    @Autowired
    public BookingMapper(ItemRepository itemRepository, UserService userService) {
        this.itemRepository = itemRepository;
        this.userService = userService;
    }

    private ItemRepository itemRepository;
    private UserService userService;


    public BookingDto toBookingDto(Booking booking) {
        return new BookingDto(booking.getId(), booking.getStart(),
                booking.getEnd(), ItemMapper.toItemDto(booking.getItem()),
                UserMapper.toUserDto(booking.getBooker()), booking.getStatus(),
                booking.getItem().getId(), booking.getBooker().getId());
    }

    public Booking toBooking(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());

        if (itemRepository.findById(bookingDto.getItemId()).isEmpty()) {
            throw new NoSuchElementException();
        }

        booking.setItem(itemRepository.findById(bookingDto.getItemId()).get());
        booking.setBooker(userService.findUserById(bookingDto.getUserId()));
        booking.setStatus(BookingStatus.WAITING);

        return booking;
    }

    public BookingShortDto toBookingShortDto(Booking booking) {
        return new BookingShortDto(booking.getId(), booking.getStart(), booking.getEnd(), booking.getItem(),
                UserMapper.toUserDtoShort(booking.getBooker()), booking.getBooker().getId(), booking.getStatus());

    }
}

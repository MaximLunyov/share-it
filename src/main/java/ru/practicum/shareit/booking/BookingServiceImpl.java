package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Transactional
    @Override
    public Booking createBooking(Long bookerId, BookingDto bookingDto) {
        checkItemIsAvailable(bookingDto.getItemId(), bookerId);
        checkBookingTime(bookingDto);

        if (itemService.findItemById(bookingDto.getItemId(), bookerId).getUserId() == bookerId) {
            throw new NoSuchElementException("Владелец вещи не может бронировать свою вещь");
        }
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(itemService.findItemById(bookingDto.getItemId(), bookerId));
        booking.setBooker(userService.findUserById(bookerId));
        booking.setStatus(BookingStatus.WAITING);
        return bookingRepository.save(booking);
    }

    @Transactional
    @Override
    public Booking updateBooking(Long ownerId, Long bookingId, Boolean approved) {
        if (approved == null) {
            throw new ValidationException();
        }
        Booking booking = getBookingIfExists(bookingId);

        if (booking.getStatus().equals(BookingStatus.APPROVED) && approved.equals(true)) {
            throw new ValidationException(true + " - статус уже была присвоен");
        }
        if (booking.getStatus().equals(BookingStatus.REJECTED) && approved.equals(false)) {
            throw new ValidationException(false + " - статус уже был присвоен");
        }

        if (booking.getItem().getUserId() != ownerId) {
            throw new NoSuchElementException();
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        return bookingRepository.save(booking);
    }

    @Transactional
    @Override
    public BookingShortDto getById(Long userId, Long bookingId) {
        Booking booking = getBookingIfExists(bookingId);
        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getUserId();
        if (bookerId.equals(userId) || ownerId.equals(userId)) {
            return BookingMapper.toBookingShortDto(booking);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Transactional
    @Override
    public List<BookingShortDto> getByUserId(Long userId, String state) {
        userService.findUserById(userId);
        BookingState bookingState = BookingState.UNKNOWN;
        final LocalDateTime now = LocalDateTime.now();
        List<BookingShortDto> bookingShortDto = new ArrayList<>();

        if (!state.isBlank()) {
            for (BookingState bs : BookingState.values()) {
                if (bs.toString().equals(state)) {
                    bookingState = bs;
                }
            }
        }

        switch (bookingState) {
            case ALL: {
                bookingShortDto = bookingRepository.findAllBookingsByBooker(userId)
                        .stream()
                        .map(BookingMapper::toBookingShortDto)
                        .collect(Collectors.toList());
                break;
            }

            case CURRENT: {
                bookingShortDto = bookingRepository.findAllBookingsForBookerWithStartAndEndTime
                        (userId, now).stream()
                        .map(BookingMapper::toBookingShortDto)
                        .collect(Collectors.toList());
                break;
            }

            case PAST: {
                bookingShortDto = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now)
                        .stream()
                        .map(BookingMapper::toBookingShortDto)
                        .collect(Collectors.toList());
                break;
            }

            case FUTURE: {
                bookingShortDto = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now)
                        .stream()
                        .map(BookingMapper::toBookingShortDto)
                        .collect(Collectors.toList());
                break;
            }

            case REJECTED: {
                bookingShortDto = bookingRepository.findAllByBookerIdAndStatus(userId, BookingStatus.REJECTED)
                        .stream()
                        .map(BookingMapper::toBookingShortDto)
                        .collect(Collectors.toList());
                break;
            }

            case WAITING: {
                bookingShortDto = bookingRepository.findAllByBookerIdAndStatus(userId, BookingStatus.WAITING)
                        .stream()
                        .map(BookingMapper::toBookingShortDto)
                        .collect(Collectors.toList());
                break;
            }

            case UNKNOWN: {
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
            }
        }
        return bookingShortDto;
    }

    @Transactional
    @Override
    public List<BookingShortDto> getByOwnerId(Long userId, String state) {
        userService.findUserById(userId);
        BookingState bookingState = BookingState.UNKNOWN;
        final LocalDateTime now = LocalDateTime.now();
        List<BookingShortDto> bookingShortDto = new ArrayList<>();

        if (!state.isBlank()) {
            for (BookingState bs : BookingState.values()) {
                if (bs.toString().equals(state)) {
                    bookingState = bs;
                }
            }
        }

        switch (bookingState) {
            case ALL: {
                bookingShortDto = bookingRepository.findAllByOwnerId
                                (userId).stream()
                        .map(BookingMapper::toBookingShortDto)
                        .collect(Collectors.toList());
                break;
            }
            case CURRENT: {
                bookingShortDto = bookingRepository.findAllByOwnerIdAndStartBeforeAndEndAfter
                                (userId, now).stream()
                        .map(BookingMapper::toBookingShortDto)
                        .collect(Collectors.toList());
                break;
            }

            case FUTURE: {
                bookingShortDto = bookingRepository.findAllByOwnerIdAndStartAfter(userId, now)
                        .stream()
                        .map(BookingMapper::toBookingShortDto)
                        .collect(Collectors.toList());
                break;
            }

            case WAITING: {
                bookingShortDto = bookingRepository.findAllByOwnerIdAndStatus(userId, BookingStatus.WAITING)
                        .stream()
                        .map(BookingMapper::toBookingShortDto)
                        .collect(Collectors.toList());
                break;
            }

            case REJECTED: {
                bookingShortDto = bookingRepository.findAllByOwnerIdAndStatus(userId, BookingStatus.REJECTED)
                        .stream()
                        .map(BookingMapper::toBookingShortDto)
                        .collect(Collectors.toList());
                break;
            }

            case PAST: {
                bookingShortDto = bookingRepository.findAllByOwnerIdAndEndBefore(userId, now)
                        .stream()
                        .map(BookingMapper::toBookingShortDto)
                        .collect(Collectors.toList());
                break;
            }

            case UNKNOWN: {
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
            }
        }
        return bookingShortDto;
    }


    private Booking getBookingIfExists(Long id) {
        Optional<Booking> bookingOptional = bookingRepository.findById(id);
        if (bookingOptional.isEmpty()) {
            throw new NoSuchElementException();
        }
        return bookingOptional.get();
    }

    private void checkBookingTime(BookingDto bookingDto) {
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new ValidationException();
        }
        if (bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new ValidationException();
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            throw new ValidationException();
        }
    }

    private void checkItemIsAvailable(long itemId, long userId) {
        Item item;
        try {
            item = itemService.findItemById(itemId, userId);
            if (!item.getAvailable()) {
                throw new ValidationException();
            }
        } catch (NullPointerException e) {
            throw new ValidationException();
        }
    }
}

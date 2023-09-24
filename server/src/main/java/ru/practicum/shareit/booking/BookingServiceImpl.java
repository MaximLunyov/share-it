package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.Pagination;
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
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingMapper bookingMapper;

    @Transactional
    @Override
    public BookingDto createBooking(Long bookerId, BookingDto bookingDto) {
        checkItemIsAvailable(bookingDto.getItemId(), bookerId);
        checkBookingTime(bookingDto);
        if (itemService.findItemById(bookingDto.getItemId(), bookerId).getUserId() == bookerId) {
            throw new NoSuchElementException("Владелец вещи не может бронировать свою вещь");
        }

        bookingDto.setUserId(bookerId);
        Booking booking = bookingMapper.toBooking(bookingDto);

        bookingRepository.save(booking);
        return bookingMapper.toBookingDto(booking);
    }

    @Transactional
    @Override
    public BookingDto updateBooking(Long ownerId, Long bookingId, Boolean approved) {
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
        bookingRepository.save(booking);
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingShortDto getById(Long userId, Long bookingId) {
        Booking booking = getBookingIfExists(bookingId);
        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getUserId();
        if (bookerId.equals(userId) || ownerId.equals(userId)) {
            return bookingMapper.toBookingShortDto(booking);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public List<BookingShortDto> getByUserId(Long userId, String state, Integer from, Integer size) {
        userService.findUserById(userId);
        BookingState bookingState = BookingState.UNKNOWN;
        List<BookingShortDto> bookingShortDto = new ArrayList<>();

        if (!state.isBlank()) {
            for (BookingState bs : BookingState.values()) {
                if (bs.toString().equals(state)) {
                    bookingState = bs;
                }
            }
        }

        Pageable pageable;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        Page<Booking> page;
        Pagination pager = new Pagination(from, size);

        if (size == null) {
            pageable =
                    PageRequest.of(pager.getIndex(), pager.getPageSize(), sort);
            do {
                page = getPageBooking(state, userId, pageable);
                try {
                    bookingShortDto.addAll(page.stream()
                            .map(bookingMapper::toBookingShortDto)
                            .collect(Collectors.toList()));
                    pageable = pageable.next();
                } catch (NullPointerException e) {
                    throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
                }

            } while (page.hasNext());

        } else {

            for (int i = pager.getIndex(); i < pager.getTotalPages(); i++) {
                pageable =
                        PageRequest.of(i, pager.getPageSize(), sort);
                page = getPageBooking(state, userId, pageable);
                bookingShortDto.addAll(page.stream()
                        .map(bookingMapper::toBookingShortDto)
                        .collect(Collectors.toList()));
                if (!page.hasNext()) {
                    break;
                }
            }
            bookingShortDto = bookingShortDto.stream()
                    .limit(size)
                    .collect(Collectors.toList());
        }
        return bookingShortDto;
    }

    private Page<Booking> getPageBooking(String bookingState, Long userId, Pageable pageable) {
        final LocalDateTime now = LocalDateTime.now();
        Page<Booking> page = null;

        switch (bookingState) {
            case "ALL": {
                page = bookingRepository.findAllBookingsByBooker(userId, pageable);
                break;
            }

            case "CURRENT": { //findAllBookingsForBookerWithStartAndEndTime
                page = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, now, now, pageable);
                break;
            }

            case "PAST": { //findAllByBookerIdAndEndBeforeOrderByStartDesc
                page = bookingRepository.findByBookerIdAndEndIsBefore(userId, now, pageable);
                break;
            }

            case "FUTURE": { //findAllByBookerIdAndStartAfterOrderByStartDesc
                page = bookingRepository.findByBookerIdAndStartIsAfter(userId, now, pageable);
                break;
            }

            case "REJECTED": { //findAllByBookerIdAndStatus
                page = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, pageable);
                break;
            }

            case "WAITING": { //findAllByBookerIdAndStatus
                page = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, pageable);
                break;
            }

            case "UNKNOWN": {
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
            }
        }

        return page;
    }

    @Override
    public List<BookingShortDto> getByOwnerId(Long userId, String state, Integer from, Integer size) {
        userService.findUserById(userId);
        BookingState bookingState = BookingState.UNKNOWN;
        List<BookingShortDto> bookingShortDto = new ArrayList<>();

        if (!state.isBlank()) {
            for (BookingState bs : BookingState.values()) {
                if (bs.toString().equals(state)) {
                    bookingState = bs;
                }
            }
        }

        Pageable pageable;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        Page<Booking> page;
        Pagination pager = new Pagination(from, size);

        if (size == null) {
            pageable =
                    PageRequest.of(pager.getIndex(), pager.getPageSize(), sort);
            do {
                page = getPageBookingByOwner(state, userId, pageable);
                try {
                    bookingShortDto.addAll(page.stream()
                            .map(bookingMapper::toBookingShortDto)
                            .collect(Collectors.toList()));
                    pageable = pageable.next();
                } catch (NullPointerException e) {
                    throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
                }
            } while (page.hasNext());

        } else {
            for (int i = pager.getIndex(); i < pager.getTotalPages(); i++) {
                pageable =
                        PageRequest.of(i, pager.getPageSize(), sort);
                page = getPageBookingByOwner(state, userId, pageable);
                bookingShortDto.addAll(page.stream()
                        .map(bookingMapper::toBookingShortDto)
                        .collect(Collectors.toList()));
                if (!page.hasNext()) {
                    break;
                }
            }
            bookingShortDto = bookingShortDto.stream().limit(size).collect(Collectors.toList());
        }
        return bookingShortDto;
    }

    private Page<Booking> getPageBookingByOwner(String bookingState, Long ownerId, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<Booking> page = null;
        switch (bookingState) {
            case "ALL": {
                page = bookingRepository.findByItemOwnerId(ownerId, pageable);
                break;
            }
            case "CURRENT": {
                page = bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(ownerId, now, pageable);
                break;
            }

            case "FUTURE": {
                page = bookingRepository.findByItemOwnerIdAndStartIsAfter(ownerId, now, pageable);
                break;
            }

            case "WAITING": {
                page = bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, pageable);
                break;
            }

            case "REJECTED": {
                page = bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, pageable);
                break;
            }

            case "PAST": {
                page = bookingRepository.findByItemOwnerIdAndEndIsBefore(ownerId, now, pageable);
                break;
            }

            case "UNKNOWN": {
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
            }
        }
        return page;
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

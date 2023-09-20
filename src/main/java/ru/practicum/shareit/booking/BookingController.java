package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto add(@RequestHeader("X-Sharer-User-Id") Long bookerId,
                          @Valid @RequestBody BookingDto bookingDto) {
        log.info(String.format("Получен запрос на бронирование: '%s' пользователем %s", bookingDto, bookerId));
        return bookingService.createBooking(bookerId, bookingDto);
    }


    @PatchMapping("/{bookingId}")
    BookingDto update(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                     @PathVariable Long bookingId,
                          @RequestParam(value = "approved",
                          required = false) Boolean approved) {
        log.info(String.format("Получен запрос на обновление брони: '%s' пользователем %s", bookingId, ownerId));
        return bookingService.updateBooking(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    BookingShortDto getWithStatusById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @PathVariable Long bookingId) {
        log.info(String.format("Получен запрос на получение информации о брони:" +
                " '%s' пользователем %s", bookingId, userId));
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    List<BookingShortDto> getByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @RequestParam(value = "state", defaultValue = "ALL") String state,
                                      @RequestParam(defaultValue = "0") Integer from,
                                      @RequestParam(required = false) Integer size) {
        log.info(String.format("Получен запрос на получение броней со статусом: '%s' пользователем %s", state, userId));
        return bookingService.getByUserId(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingShortDto> getByOwnerId(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                              @RequestParam(value = "state", defaultValue = "ALL") String state,
                                              @RequestParam(defaultValue = "0") Integer from,
                                              @RequestParam(required = false) Integer size) {
        log.info(String.format("Получен запрос на получение броней со статусом: '%s' владельцем %s", state, ownerId));
        return bookingService.getByOwnerId(ownerId, state, from, size);
    }
}

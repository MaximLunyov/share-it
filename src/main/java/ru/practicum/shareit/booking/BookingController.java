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
    public Booking add(@RequestHeader("X-Sharer-User-Id") Long bookerId,
                          @Valid @RequestBody BookingDto bookingDto) {
        return bookingService.createBooking(bookerId, bookingDto);
    }


    @PatchMapping("/{bookingId}")
    Booking update(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                     @PathVariable Long bookingId,
                          @RequestParam(value = "approved",
                          required = false) Boolean approved) {


        return bookingService.updateBooking(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    BookingShortDto getWithStatusById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @PathVariable Long bookingId) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    List<BookingShortDto> getByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @RequestParam(value = "state",
                                                 defaultValue = "ALL", required = false) String state) {
        return bookingService.getByUserId(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingShortDto> getByOwnerId(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                 @RequestParam(value = "state", defaultValue = "ALL",
                                                         required = false) String state) {
        return bookingService.getByOwnerId(ownerId, state);
    }
}

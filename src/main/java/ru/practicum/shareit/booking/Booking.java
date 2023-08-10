package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
public class Booking {
    private long id;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Item item;
    private User booker;
    private BookingStatus status;
}

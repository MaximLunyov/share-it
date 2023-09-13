package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
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
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final BookingMapper bookingMapper;

    @Transactional
    @Override
    public Item createItem(ItemDto itemDto, long sharerUserId) {
        if (itemDto.getName() == null || itemDto.getDescription() == null
                || itemDto.getAvailable() == null) {
            throw new ValidationException();
        }
        if (itemDto.getName().isBlank() || itemDto.getDescription().isBlank()
                || itemDto.getAvailable() == null) {
            throw new ValidationException();
        }
        log.info(String.valueOf(itemDto));
        if (userService.findUserById(sharerUserId) == null) {
            throw new NoSuchElementException();
        }
        Item item = ItemMapper.toItem(itemDto);
        item.setUserId(userService.findUserById(sharerUserId).getId());

        return itemRepository.save(item);

    }

    @Transactional
    @Override
    public Item updateItem(long id, ItemDto itemDto, long sharerUserId) {
        checkUserExists(sharerUserId);
        if (findItemById(id, sharerUserId).getUserId() != sharerUserId) {
            throw new NoSuchElementException();
        }

        Optional<Item> itemOptional = itemRepository.findById(id);

        Item item = itemOptional.get();

        if (item.getUserId() != sharerUserId) {
            throw new NoSuchElementException();
        }

        if (!item.getName().equals(itemDto.getName()) && itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }

        if (!item.getDescription().equals(itemDto.getDescription()) && itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }

        if (!item.getAvailable().equals(itemDto.getAvailable()) && itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        log.info(item.toString());
        return itemRepository.save(item);
    }

    @Override
    public List<ItemDto> findAllItems(long sharerUserId) {
        checkUserExists(sharerUserId);
        List<Long> idList = itemRepository.findAllByUserIdOrderById(sharerUserId)
                .stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        List<ItemDto> itemDto = new ArrayList<>();
        for (Long id : idList) {
            itemDto.add(addNextAndLastBookingsAndComments(id, sharerUserId));
        }
        return itemDto;

    }

    @Override
    public ItemDto findItemDtoById(long id, long sharerUserId) {
        checkUserExists(sharerUserId);
        if (itemRepository.findById(id).isEmpty()) {
            throw new NoSuchElementException();
        }

        return addNextAndLastBookingsAndComments(id, sharerUserId);
    }

    @Override
    public Item findItemById(long id, long sharerUserId) {
        checkUserExists(sharerUserId);
        if (itemRepository.findById(id).isEmpty()) {
            throw new NoSuchElementException();
        }
        return itemRepository.findById(id).get();
    }

    @Transactional
    @Override
    public CommentDto createComment(Comment comment, long sharerUserId, long itemId) {
        findItemById(itemId, sharerUserId);
        checkUserMadeBooking(sharerUserId, itemId);

        comment.setItem(findItemById(itemId, sharerUserId));
        comment.setAuthor(userService.findUserById(sharerUserId));
        comment.setCreated(LocalDateTime.now());

        commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment);
    }

    @Transactional
    @Override
    public void deleteItem(long id, long sharerUserId) {

    }

    @Override
    public List<Item> searchByText(String text, long sharerUserId) {
        checkUserExists(sharerUserId);
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.search(text);
    }

    @Override
    public void checkUserExists(long sharerUserId) {
        if (userService.findUserById(sharerUserId) == null) {
            throw new NoSuchElementException();
        }
    }

    private ItemDto addNextAndLastBookingsAndComments(Long id, Long sharerUserId) {
        Item item = itemRepository.findById(id).get();
        ItemDto itemDto = ItemMapper.toItemDto(item);


        if (item.getUserId() == sharerUserId) {
            Optional<Booking> bk = Optional.ofNullable(bookingRepository.findFirstByItemIdAndEndBeforeOrderByEndDesc(id,
                    LocalDateTime.now()));

            if (bk.isEmpty()) {
                List<Booking> bookingList = bookingRepository.test2(id, LocalDateTime.now());
                if (bookingList.size() == 1) {
                    Optional<Booking> booking = bookingRepository.test2(id, LocalDateTime.now()).stream().findFirst();
                    if (booking.isPresent() && booking.get().getStatus().equals(BookingStatus.APPROVED)) {
                        itemDto.setLastBooking(bookingMapper.toBookingShortDto(booking.get()));
                    }
                } else {
                    itemDto.setLastBooking(null);
                }
            } else {
                BookingShortDto bookingShortDto = bookingMapper.toBookingShortDto(bk.get());
                if (bookingShortDto.getStatus().equals(BookingStatus.APPROVED)) {
                    itemDto.setLastBooking(bookingShortDto);
                } else {
                    itemDto.setLastBooking(null);
                }
            }
        } else {
            itemDto.setLastBooking(null);
        }

        if (item.getUserId() == sharerUserId) {
            Optional<Booking> bk = Optional.ofNullable(bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(id,
                    LocalDateTime.now()));

            if (bk.isEmpty()) {
                itemDto.setNextBooking(null);
            } else {
                BookingShortDto bookingShortDto = bookingMapper.toBookingShortDto(bk.get());
                if (bookingShortDto.getStatus().equals(BookingStatus.APPROVED)) {
                    itemDto.setNextBooking(bookingShortDto);
                } else {
                    itemDto.setNextBooking(null);
                }
            }

        } else {
            itemDto.setNextBooking(null);
        }

        try {
            itemDto.setComments(commentRepository.findAllByItemId(id).stream()
                    .map(CommentMapper::toCommentDto)
                    .collect(Collectors.toList()));
        } catch (Error e) {
            log.info("error 2");
        }

        return itemDto;
    }

    private void checkUserMadeBooking(long userId, long itemId) {

        List<Booking> bookingList = bookingRepository.findAllBookingsByBooker(userId)
                .stream()
                .filter(booking -> booking.getItem().getId() == itemId)
                .filter(booking -> booking.getBooker().getId() == userId)
                .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());

        if (bookingList.isEmpty()) {
            throw new ValidationException();
        }
    }
}

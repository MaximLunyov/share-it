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
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

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

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> findAllItems(long sharerUserId) {
        checkUserExists(sharerUserId);
        List<Long> idList = itemRepository.findAllByUserIdOrderById(sharerUserId)
                .stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        List<ItemDto> itemDto = new ArrayList<>();
        for (Long id : idList) {
            itemDto.add(findItemDtoById(id, sharerUserId));
        }
        return itemDto;

    }

    @Override
    public ItemDto findItemDtoById(long id, long sharerUserId) {
        checkUserExists(sharerUserId);
        if (itemRepository.findById(id).isEmpty()) {
            throw new NoSuchElementException();
        }
        Item item = itemRepository.findById(id).get();
        ItemDto itemDto = ItemMapper.toItemDto(item);
        try {
            List<BookingShortDto> bookingList = bookingRepository.findAllByOwnerId(sharerUserId)
                    .stream()
                    .map(BookingMapper::toBookingShortDto)
                    .collect(Collectors.toList());
            if (!bookingList.isEmpty()) {

                if (bookingList.size() > 1) { //1, 2
                    itemDto.setNextBooking(bookingList.get(bookingList.size() - 2));
                    itemDto.setLastBooking(bookingList.get(bookingList.size() - 1));
                }
            }
        } catch (Error e) {
            log.info("error 1");
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

    @Transactional(readOnly = true)
    @Override
    public Item findItemById(long id, long sharerUserId) {
        checkUserExists(sharerUserId);
        if (itemRepository.findById(id).isEmpty()) {
            throw new NoSuchElementException();
        }
        return itemRepository.findById(id).get();
    }

    /*Привет, Патимат!
    * Выполнил задание согласно ТЗ, но есть проблема с GET методом для получения Item с комментариями
    * и next/last booking, комментарии добавляются, бронирования тоже, если я правильно понял логику задания.
    * Но тесты Postman сыпятся на этих моментах, не понимаю что еще можно сделать, чтобы их пройти.
    * Подскажи, пожалуйста, в чем мои ошибки, как их можно исправить?
    * */
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

    @Transactional(readOnly = true)
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

package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserService userService;
    private final ItemMapper itemMapper;

    @Override
    public Item createItem(ItemDto itemDto, long sharerUserId) {
        log.info(itemDto.toString());
        if (userService.findUserById(sharerUserId) != null) {
            Item item = itemMapper.toItem(itemDto);
            item.setOwner(userService.findUserById(sharerUserId));
            return itemStorage.createItem(item);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public Item updateItem(long id, ItemDto itemDto, long sharerUserId) {
        checkUserExists(sharerUserId);
        if (findItemById(id, sharerUserId).getOwner().getId() != sharerUserId) {
            throw new NoSuchElementException();
        } else {
            return itemStorage.updateItem(id, itemMapper.toItem(itemDto));
        }
    }

    @Override
    public List<Item> findAllItems(long sharerUserId) {
        checkUserExists(sharerUserId);
        return itemStorage.findAllItems(sharerUserId);
    }

    @Override
    public Item findItemById(long id, long sharerUserId) {
        checkUserExists(sharerUserId);
        return itemStorage.findItemById(id, sharerUserId);
    }

    @Override
    public void deleteItem(long id, long sharerUserId) {
        itemStorage.deleteItem(id, sharerUserId);
    }

    @Override
    public List<Item> searchByText(String text, long sharerUserId) {
        checkUserExists(sharerUserId);
        return itemStorage.searchByText(text, sharerUserId);
    }

    private void checkUserExists(long sharerUserId) {
        if (userService.findUserById(sharerUserId) == null) {
            throw new NoSuchElementException();
        }
    }
}

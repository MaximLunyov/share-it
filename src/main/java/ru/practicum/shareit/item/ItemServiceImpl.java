package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserStorage;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public Item createItem(ItemDto itemDto, long sharerUserId) {
        if (userStorage.findUserById(sharerUserId) == null) {
            throw new NoSuchElementException();
        }
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userStorage.findUserById(sharerUserId));

        return itemStorage.createItem(item);

    }

    @Override
    public Item updateItem(long id, ItemDto itemDto, long sharerUserId) {
        checkUserExists(sharerUserId);
        if (findItemById(id, sharerUserId).getOwner().getId() != sharerUserId) {
            throw new NoSuchElementException();
        }
        return itemStorage.updateItem(id, ItemMapper.toItem(itemDto));
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
        if (userStorage.findUserById(sharerUserId) == null) {
            throw new NoSuchElementException();
        }
    }
}

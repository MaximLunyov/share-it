package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {

    Item createItem(Item item);

    Item updateItem(long id, Item item);

    List<Item> findAllItems(long sharerUserId);

    Item findItemById(long id, long sharerUserId);

    void deleteItem(long id, long sharerUserId);

    List<Item> searchByText(String text, long sharerUserId);

}

package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    Item createItem(ItemDto itemDto, long sharerUserId);

    Item updateItem(long id, ItemDto itemDto, long sharerUserId);

    List<Item> findAllItems(long sharerUserId);

    Item findItemById(long id, long sharerUserId);

    void deleteItem(long id, long sharerUserId);

    List<Item> searchByText(String text, long sharerUserId);
}

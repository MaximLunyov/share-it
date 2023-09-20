package ru.practicum.shareit.item;

import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    Item createItem(ItemDto itemDto, long sharerUserId);

    Item updateItem(long id, ItemDto itemDto, long sharerUserId);

    List<ItemDto> getItemsByOwner(long sharerUserId, Integer from, Integer size);

    ItemDto findItemDtoById(long id, long sharerUserId);

    void deleteItem(long id, long sharerUserId);

    List<Item> searchByText(String text, Integer from, Integer size);

    void checkUserExists(long id);

    Item findItemById(long id, long sharerUserId);

    CommentDto createComment(Comment comment, long sharerUserId, long itemId);

    List<ItemDto> getItemsByRequestId(Long requestId);

}

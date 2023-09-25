package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<ItemDto> findAllItems(@RequestHeader("x-sharer-user-id") long sharerUserId,
                                      @RequestParam(defaultValue = "0") Integer from,
                                      @RequestParam(required = false) Integer size) {
        log.info("Получен запрос на получение списка предметов владельца " + sharerUserId);
        return itemService.getItemsByOwner(sharerUserId, from, size);
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@PathVariable long id, @RequestHeader("x-sharer-user-id") long sharerUserId) {
        log.info(String.format("Получен запрос на получения предмета с id: '%s' пользователем %s", id, sharerUserId));
        return itemService.findItemDtoById(id, sharerUserId);
    }

    @GetMapping("/search")
    public List<Item> searchByText(@RequestParam String text,
                                   @RequestParam(defaultValue = "0") Integer from,
                                   @RequestParam(required = false) Integer size) {
        return itemService.searchByText(text, from, size);
    }

    @PostMapping
    public Item create(@RequestBody ItemDto itemDto, @RequestHeader("x-sharer-user-id") long sharerUserId) {
        log.info(String.format("Получен запрос на добавление предмета: '%s' пользователем %s", itemDto, sharerUserId));
        return itemService.createItem(itemDto, sharerUserId);
    }

    @PatchMapping("/{id}")
    public Item update(@PathVariable long id,@RequestBody ItemDto itemDto,
                       @RequestHeader("x-sharer-user-id") long sharerUserId) {
        log.info("Получен запрос на обновление предмета: " + id);
        return itemService.updateItem(id, itemDto, sharerUserId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(name = "X-Sharer-User-Id") Long sharerUserId,
                                 @PathVariable("itemId") Long itemId,
                                 @RequestBody Comment comment) {
        return itemService.createComment(comment, sharerUserId, itemId);
    }
}

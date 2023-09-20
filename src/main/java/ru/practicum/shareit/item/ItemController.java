package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;
import javax.validation.ValidationException;
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
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return itemService.findItemDtoById(id, sharerUserId);
    }

    @GetMapping("/search")
    public List<Item> searchByText(@RequestParam String text,
                                   @RequestParam(defaultValue = "0") Integer from,
                                   @RequestParam(required = false) Integer size) {
        return itemService.searchByText(text, from, size);
    }

    @PostMapping
    public Item create(@Valid @RequestBody ItemDto itemDto, @RequestHeader("x-sharer-user-id") long sharerUserId) {
        log.info(String.format("Получен запрос на добавление предмета: '%s' пользователем %s", itemDto, sharerUserId));
        if (itemDto.getAvailable() == null) {
            throw new ValidationException();
        }
        return itemService.createItem(itemDto, sharerUserId);
    }

    @PatchMapping("/{id}")
    public Item update(@PathVariable long id,@RequestBody ItemDto itemDto,
                       @RequestHeader("x-sharer-user-id") long sharerUserId) throws ValidationException {
        log.info("Получен запрос на обновление предмета: " + id);
        return itemService.updateItem(id, itemDto, sharerUserId);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable long id, @RequestHeader("x-sharer-user-id") long sharerUserId) {
        log.info("Получен запрос на удаление пользователя c id: " + id);
        itemService.deleteItem(id, sharerUserId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(name = "X-Sharer-User-Id") Long sharerUserId,
                                 @PathVariable("itemId") Long itemId,
                                 @Valid @RequestBody Comment comment) {
        return itemService.createComment(comment, sharerUserId, itemId);
    }
}

package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryItemStorage implements ItemStorage {

    private Map<Long, Item> itemMap = new HashMap<>();
    protected long id = 0;

    @Override
    public Item createItem(Item item) {
        item.setId(++id);
        itemMap.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(long id, Item item) {
        if (!itemMap.containsKey(id)) {
            throw new NoSuchElementException();
        }
        Item newItem = itemMap.get(id);
        if (item.getName() != null) {
            newItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            newItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            newItem.setAvailable(item.getAvailable());
        }
        itemMap.replace(id, newItem);
        return newItem;
    }

    @Override
    public List<Item> findAllItems(long sharerUserId) {
        List<Item> items = new ArrayList<>();
        for (Item item: itemMap.values()) {
            if (item.getOwner().getId() == sharerUserId) {
                items.add(item);
            }
        }
        return items;
    }


//    Привет, Патимат!
//    Возникла проблема с Postman тестом в данном методе.
//    Тест требует объект, который не принадлежит пользователю X-Sharer-User-Id = 3,
//    поэтому не проходит валидация в методе и я возвращаю код ошибки.
//    Подскажи пожалуйста, это я не так понял смысл данного метода или это ошибка в тестах Postman?
    @Override
    public Item findItemById(long id, long sharerUserId) {

        if (itemMap.containsKey(id) && itemMap.get(id).getOwner() != null
                && itemMap.get(id).getOwner().getId() == sharerUserId) {
            return itemMap.get(id);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void deleteItem(long id, long sharerUserId) {
        if (itemMap.containsKey(id)) {
            itemMap.remove(id);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public List<Item> searchByText(String text, long sharerUserId) {
        if (text == null) {
            throw new NoSuchElementException();
        }
        if (text.isBlank()) {
            return new ArrayList<>();
        } else {
            List<Item> result = new ArrayList<>(itemMap.values()).stream()
                    .filter(Item::getAvailable)
                    .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) ||
                            item.getDescription().toLowerCase().contains(text.toLowerCase()))
                    .collect(Collectors.toList());
            log.info(result.toString());
            return result;
        }
    }
}

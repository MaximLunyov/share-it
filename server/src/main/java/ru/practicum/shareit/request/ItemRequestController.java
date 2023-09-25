package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService service;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.service = itemRequestService;
    }

    @ResponseBody
    @PostMapping
    public ItemRequestDto create(@RequestBody ItemRequestDto itemRequestDto,
                                 @RequestHeader("X-Sharer-User-Id") Long requestorId) {
        log.info("Получен POST-запрос к эндпоинту: '/requests' " +
                "на создание запроса вещи от пользователя с ID={}", requestorId);
        return service.create(itemRequestDto, requestorId, LocalDateTime.now());
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequestById(@PathVariable("requestId") Long itemRequestId,
                                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET-запрос к эндпоинту: '/requests' на получение запроса с ID={}", itemRequestId);
        return service.getItemRequestById(itemRequestId, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getOwnItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET-запрос к эндпоинту: '/requests' на получение запросов пользователя ID={}",
                userId);
        return service.getOwnItemRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(defaultValue = "0") Integer from,
                                                   @RequestParam(required = false) Integer size) {
        log.info("Получен GET-запрос к эндпоинту: '/requests/all' от пользователя с ID={} на получение всех запросов",
                userId);
        return service.getAllItemRequests(userId, from, size);
    }
}

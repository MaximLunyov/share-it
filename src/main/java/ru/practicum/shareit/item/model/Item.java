package ru.practicum.shareit.item.model;

import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class Item {

    private long id;
    @NotBlank
    private String name;
    @NotBlank
    @NotNull
    private String description;
    private Boolean available;
    private User owner;
    private ItemRequest request;
}

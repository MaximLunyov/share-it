package ru.practicum.shareit.item.model;

import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class Item {

    private long id;
    @NotNull
    @NotBlank
    private String name;
    @NotBlank
    @NotNull
    private String description;
    private Boolean available;
    private User owner;
    private ItemRequest request;
}

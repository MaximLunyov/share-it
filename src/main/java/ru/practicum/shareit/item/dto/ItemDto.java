package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Data
public class ItemDto {

    @NotBlank
    @NotNull
    private String name;
    @NotBlank
    @NotNull
    private String description;
    private Boolean available;

}

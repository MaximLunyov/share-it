package ru.practicum.shareit.item.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private long id;

    @NotBlank
    @Column(name = "name")
    private String name;

    @NotBlank
    @NotNull
    @Column(name = "description")
    private String description;

    @Column(name = "available")
    @NotNull
    private Boolean available;

    @Column(name = "user_id")
    private long userId;

}

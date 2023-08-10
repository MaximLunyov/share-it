package ru.practicum.shareit.user.model;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class User {

    private long id;
    private String name;
    @Email
    private String email;
}

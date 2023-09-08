package ru.practicum.shareit.user.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long id;

    @Column(name = "name")
    @NotBlank(message = "Передано пустое имя")
    private String name;

    @Column(name = "email", nullable = false, length = 256, unique = true)
    @Email(message = "Некорректно указана электронная почта")
    private String email;
}

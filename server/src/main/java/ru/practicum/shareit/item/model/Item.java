package ru.practicum.shareit.item.model;

import javax.persistence.*;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @NotBlank
    @Column(name = "name")
    private String name;


    @Column(name = "available")
    @NotNull
    private Boolean available;

    @Column(name = "user_id")
    private long userId;

    @Column(name = "request_id")
    private Long requestId;

    public Item(long id, String name, String description, Boolean available, long userId, Long requestId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.userId = userId;
        this.requestId = requestId;
    }

    public Item() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "available")
    private Boolean available;

    @Column(name = "user_id")
    private long userId;

    @Column(name = "request_id")
    private Long requestId;

    public Item(long id, String name, String description, Boolean available, long userId, Long requestId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.userId = userId;
        this.requestId = requestId;
    }

    public Item() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", available=" + available +
                ", userId=" + userId +
                '}';
    }




}

package ru.practicum.shareit.item.comment;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByItemId(long itemId);

    List<Comment> findByItemIn(Collection<Item> items, Sort sort);

    List<Comment> findByItem(Item item, Sort sort);

}

package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {
    Page<Item> findByUserId(Long userId, Pageable pageable);

    @Query("select i from Item i " +
            "where lower(i.name) like lower(concat('%', :search, '%')) " +
            " or lower(i.description) like lower(concat('%', :search, '%')) " +
            " and i.available = true")
    Page<Item> getItemsBySearchQuery(@Param("search") String text, Pageable pageable);

    List<Item> findAllByRequestId(Long requestId, Sort sort);

}

package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends PagingAndSortingRepository<Booking, Long> {
    @Query("select b from Booking b where b.booker.id = ?1")
    Page<Booking> findAllBookingsByBooker(Long bookerId, Pageable pageable);


    @Query("select b from Booking b where b.booker.id = ?1 order by b.start DESC")
    List<Booking> findAllBookingsByBooker(Long id);

    Page<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start,
                                                              LocalDateTime end, Pageable pageable);

    @Query("select b from Booking b where b.item.userId = ?1 ")
    Page<Booking> findByItemOwnerId(Long ownerId, Pageable pageable);


    @Query("select b from Booking b" +
            " where b.item.userId = ?1 and b.start < ?2 and b.end > ?2")
    Page<Booking> findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(Long ownerId, LocalDateTime start, Pageable pageable);

    Page<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Pageable pageable);

    Page<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Pageable pageable);

    Page<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Pageable pageable);

    @Query("select b from Booking b where b.item.userId = ?1 and b.status = ?2")
    Page<Booking> findByItemOwnerIdAndStatus(Long bookerId, BookingStatus status, Pageable pageable);

    @Query("select b from Booking b" +
            " where b.item.userId = ?1 and b.start > ?2")
    Page<Booking> findByItemOwnerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Pageable pageable);

    @Query("select b from Booking b" +
            " where b.item.userId = ?1 and b.end < ?2")
    Page<Booking> findByItemOwnerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Pageable pageable);

    Booking findFirstByItemIdAndEndBeforeOrderByEndDesc(Long itemId, LocalDateTime end);

    Booking findFirstByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime end);

    @Query("select b from Booking b" +
            " where b.item.id = ?1" +
            " order by b.start ASC") //b.start DESC
    List<Booking> test2(Long itemId, LocalDateTime end);

    @Query(" select b " +
            "from Booking b " +
            "where b.item in ?1 " +
            "  and b.status = 'APPROVED'")
    List<Booking> findApprovedForItems(Collection<Item> items, Sort sort);
}

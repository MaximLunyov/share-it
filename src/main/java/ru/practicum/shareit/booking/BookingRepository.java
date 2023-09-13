package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select b from Booking b where b.booker.id = ?1 order by b.start DESC")
    List<Booking> findAllBookingsByBooker(Long id);

    @Query("select b from Booking b " +
            "where b.booker.id = ?1 and b.start < ?2 and b.end > ?2 " +
            "order by b.start DESC")
    List<Booking> findAllBookingsForBookerWithStartAndEndTime(
            Long id, LocalDateTime dateTime);

    @Query("select b from Booking b where b.item.userId = ?1 " +
            "order by b.start DESC")
    List<Booking> findAllByOwnerId(long ownerId);

    @Query("select b from Booking b" +
            " where b.item.userId = ?1 and b.start < ?2 and b.end > ?2")
    List<Booking> findAllByOwnerIdAndStartBeforeAndEndAfter(long bookerId, LocalDateTime dateTime);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime dateTime);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime dateTime);

    List<Booking> findAllByBookerIdAndStatus(long bookerId, BookingStatus status);


    @Query("select b from Booking b where b.item.userId = ?1 and b.status = ?2")
    List<Booking> findAllByOwnerIdAndStatus(long ownerId, BookingStatus status);

    @Query("select b from Booking b" +
            " where b.item.userId = ?1 and b.start > ?2" +
            " order by b.start DESC")
    List<Booking> findAllByOwnerIdAndStartAfter(long ownerId, LocalDateTime dateTime);

    @Query("select b from Booking b" +
            " where b.item.userId = ?1 and b.end < ?2" +
            " order by b.start DESC")
    List<Booking> findAllByOwnerIdAndEndBefore(long ownerId, LocalDateTime dateTime);

    Booking findFirstByItemIdAndEndBeforeOrderByEndDesc(Long itemId, LocalDateTime end);

    Booking findFirstByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime end);

    @Query("select b from Booking b" +
            " where b.item.id = ?1" +
            " order by b.start ASC") //b.start DESC
    List<Booking> test2(Long itemId, LocalDateTime end);
}

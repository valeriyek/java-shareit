package ru.practicum.shareit.booking;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.item.id = ?1 AND b.end < CURRENT_TIMESTAMP ORDER BY b.end DESC")
    Optional<Booking> findLastBooking(Long itemId);

    @Query("SELECT b FROM Booking b WHERE b.item.id = ?1 AND b.start > CURRENT_TIMESTAMP ORDER BY b.start ASC")
    Optional<Booking> findNextBooking(Long itemId);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId ORDER BY b.start DESC")
    List<Booking> findBookingsByBookerId(@Param("bookerId") Long bookerId);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findBookingsByOwnerId(@Param("ownerId") Long ownerId);

}

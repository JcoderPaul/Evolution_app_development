package me.oldboy.repository;

import me.oldboy.models.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query(value = "SELECT res.* " +
            "FROM coworking.all_reserves AS res " +
            "WHERE res.reserve_date = :date",
            nativeQuery = true)
    public Optional<List<Reservation>> findByDate(@Param("date") LocalDate date);

//    public <E extends CwEntity> Optional<List<Reservation>> findByCwEntity(E entity);

    @Query(value = "SELECT res.* " +
            "FROM coworking.all_reserves AS res " +
            "WHERE res.place_id = :placeId",
            nativeQuery = true)
    public Optional<List<Reservation>> findByPlaceId(@Param("placeId") Long placeId);

    @Query(value = "SELECT res.* " +
            "FROM coworking.all_reserves AS res " +
            "WHERE res.slot_id = :slotId",
            nativeQuery = true)
    public Optional<List<Reservation>> findBySlotId(@Param("slotId") Long slotId);

    @Query(value = "SELECT res.* " +
            "FROM coworking.all_reserves AS res " +
            "WHERE res.user_id = :userId",
            nativeQuery = true)
    public Optional<List<Reservation>> findByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT res.* " +
            "FROM coworking.all_reserves AS res " +
            "WHERE (res.reserve_date = :date " +
            "AND res.place_id = :placeId " +
            "AND res.slot_id =:slotId)",
            nativeQuery = true)
    public Optional<Reservation> findByDatePlaceAndSlot(@Param("date") LocalDate date,
                                                        @Param("placeId") Long placeId,
                                                        @Param("slotId") Long slotId);
}
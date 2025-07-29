package me.oldboy.repository;

import me.oldboy.models.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    public Optional<Slot> findBySlotNumber(@Param("slotNumber") Integer slotNumber);
}

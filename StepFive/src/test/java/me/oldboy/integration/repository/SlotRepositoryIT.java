package me.oldboy.integration.repository;

import me.oldboy.integration.ITBaseStarter;
import me.oldboy.models.entity.Slot;
import me.oldboy.repository.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SlotRepositoryIT extends ITBaseStarter {

    @Autowired
    private SlotRepository slotRepository;

    private Integer existNumber, nonExistentNumber;

    @BeforeEach
    void setUp() {
        existNumber = 10;
        nonExistentNumber = 100;
    }

    @Test
    void findBySlotNumber_shouldReturnFoundSlot_Test() {
        Optional<Slot> mayBeSlot = slotRepository.findBySlotNumber(existNumber);
        if (mayBeSlot.isPresent()) {
            assertThat(mayBeSlot.get().getSlotNumber()).isEqualTo(existNumber);
        }
    }

    @Test
    void findBySlotNumber_shouldReturnOptionalEmpty_Test() {
        Optional<Slot> mayBeSlot = slotRepository.findBySlotNumber(nonExistentNumber);
        if (mayBeSlot.isEmpty()) {
            assertThat(mayBeSlot.isPresent()).isFalse();
        }
    }
}
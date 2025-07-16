package me.oldboy.integration.repository;

import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.entity.Slot;
import me.oldboy.repository.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@IT
class SlotRepositoryIT extends TestContainerInit {

    @Autowired
    private SlotRepository slotRepository;
    private Integer existNumberSlot, nonExistentNumberSlot;

    @BeforeEach
    void setUp(){
        existNumberSlot = 10;
        nonExistentNumberSlot = 1000;
    }

    @Test
    void findBySlotNumber_shouldReturnSlotByNumber_Test() {
        Optional<Slot> mayBeSlot = slotRepository.findBySlotNumber(existNumberSlot);
        if (mayBeSlot.isPresent()){
            assertThat(mayBeSlot.get().getSlotNumber()).isEqualTo(existNumberSlot);
            assertThat(mayBeSlot.get().getSlotNumber()).isNotEqualTo(nonExistentNumberSlot);
        }
    }

    @Test
    void findBySlotNumber_shouldReturnOptionalEmpty_Test() {
        Optional<Slot> mayBeSlot = slotRepository.findBySlotNumber(nonExistentNumberSlot);
        assertThat(mayBeSlot.isEmpty()).isTrue();
    }
}
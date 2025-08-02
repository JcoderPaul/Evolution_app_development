package me.oldboy.controllers.admin_scope;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.auditor.core.annotation.Auditable;
import me.oldboy.auditor.core.entity.operations.AuditOperationType;
import me.oldboy.dto.slots.SlotCreateDeleteDto;
import me.oldboy.dto.slots.SlotReadUpdateDto;
import me.oldboy.exception.slot_exception.SlotControllerException;
import me.oldboy.services.SlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/api/admin/slots")
public class AdminSlotController {

    @Autowired
    private SlotService slotService;

    /* С - CRUD создаем новый слот */

    /**
     * Create new slot.
     *
     * @param createDto input data for create new time slot
     * @return slot read DTO with slot ID
     */
    @Auditable(operationType = AuditOperationType.CREATE_SLOT)
    @PostMapping("/create")
    public SlotReadUpdateDto createNewSlot(@Validated
                                           @RequestBody
                                           SlotCreateDeleteDto createDto) {
        Long createdSlotId = slotService.create(createDto);
        return slotService.findById(createdSlotId).get();
    }

    /* U - CRUD обновляем/изменяем данные по существующему слоту */

    /**
     * Update existent slot.
     *
     * @param updateDto input slot new data for update
     * @return true - if update success
     * false - if update fail
     */
    @Auditable(operationType = AuditOperationType.UPDATE_SLOT)
    @PostMapping("/update")
    public boolean updateSlot(@Validated
                              @RequestBody
                              SlotReadUpdateDto updateDto) {
        if (slotService.findById(updateDto.slotId()).isEmpty()) {
            throw new SlotControllerException("Слот с ID = " + updateDto.slotId() + " не найден!");
        }
        /* Остальные проверки проводятся на уровне сервисов */
        return slotService.update(updateDto);
    }

    /* D - CRUD удаляем данные о слоте из БД */

    @Auditable(operationType = AuditOperationType.DELETE_SLOT)
    @PostMapping("/delete")
    public boolean deleteSlot(@Validated
                              @RequestBody
                              SlotCreateDeleteDto deleteDto) {
        Optional<SlotReadUpdateDto> forDeleteSlot = slotService.findSlotByNumber(deleteDto.slotNumber());
        if (forDeleteSlot.isEmpty()) {
            throw new SlotControllerException("Слот для удаления не найден!");
        }
        return slotService.delete(forDeleteSlot.get().slotId());
    }
}
package me.oldboy.core.controllers;

import lombok.RequiredArgsConstructor;
import me.oldboy.annotations.Auditable;
import me.oldboy.annotations.Loggable;
import me.oldboy.core.dto.slots.SlotCreateDeleteDto;
import me.oldboy.core.dto.slots.SlotReadUpdateDto;
import me.oldboy.core.model.database.audit.operations.AuditOperationType;
import me.oldboy.core.model.service.SlotService;
import me.oldboy.exception.SlotControllerException;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    /* С - CRUD создаем новый слот */

    /**
     * Create new slot.
     *
     * @param createDto input data for create new time slot
     *
     * @return slot read DTO with slot ID
     */
    @Loggable
    @Auditable(operationType = AuditOperationType.CREATE_SLOT)
    public SlotReadUpdateDto createNewSlot(SlotCreateDeleteDto createDto, String userName) {
        /* Все валидации и проверки проводятся на уровне сервисов, для разнообразия */
        Long createdSlotId = slotService.create(createDto);
        return slotService.findById(createdSlotId).get();
    }

    /* R - CRUD читаем (получаем) слоты */

    /**
     * Read existing slot / Извлекаем данные по слоту из БД по ID
     *
     * @param slotId slot ID for find / ID искомого слота
     * @throws SlotControllerException if the slot non exist / выбрасывается в случае отсутствия искомого ID в БД
     *
     * @return reading slot
     */
    @Loggable
    public SlotReadUpdateDto readSlotById(Long slotId) throws SlotControllerException {
        Optional<SlotReadUpdateDto> mayBeSlot = slotService.findById(slotId);
        if(mayBeSlot.isEmpty()) {
            throw new SlotControllerException("Slot with ID: " + slotId + " not exist! " +
                                              "Слот с ID: " + slotId + " не существует!");
        } else
            return mayBeSlot.get();
    }

    /**
     * Read existing slot / Извлекаем слот из БД по номеру
     *
     * @param slotNumber slot number for find / номер слота который мы ищем
     * @throws SlotControllerException if the slot non exist / выбрасывается в случае отсутствия искомых данных в БД
     *
     * @return reading slot
     */
    @Loggable
    public SlotReadUpdateDto readSlotByNumber(Integer slotNumber) throws SlotControllerException {
        Optional<SlotReadUpdateDto> mayBeSlot = slotService.findSlotByNumber(slotNumber);
        if(mayBeSlot.isEmpty()) {
            throw new SlotControllerException("Slot with " + slotNumber + " number not found! " +
                                              "Слот с номером " + slotNumber + " не найден!");
        } else
            return mayBeSlot.get();
    }

    /**
     * Show all available time slots / Просмотр всех доступных временных диапазонов для брони
     */
    @Loggable
    public List<SlotReadUpdateDto> getAllSlots() {
        return slotService.findAll();
    }

    /* U - CRUD обновляем/изменяем данные по существующему слоту */

    /**
     * Update existent slot.
     *
     * @param updateDto input slot new data for update
     *
     * @return true - if update success
     *         false - if update fail
     */
    @Loggable
    @Auditable(operationType = AuditOperationType.UPDATE_SLOT)
    public boolean updateSlot(SlotReadUpdateDto updateDto, String userName) throws SlotControllerException {
        if (slotService.findById(updateDto.slotId()).isEmpty()){
            throw new SlotControllerException("Slot with ID = " + updateDto.slotId() + " not existent! " +
                                              "Слот с ID = "  + updateDto.slotId() + " не найден!" );
        }
        /* Остальные проверки и валидации проводятся на уровне сервисов */
        return slotService.update(updateDto.slotId(), updateDto);
    }

    /* D - CRUD удаляем данные о слоте из БД */

    @Loggable
    @Auditable(operationType = AuditOperationType.DELETE_SLOT)
    public boolean deleteSlot(SlotCreateDeleteDto deleteDto, String userName) throws SlotControllerException {
        Optional<SlotReadUpdateDto> forDeleteSlot = slotService.findSlotByNumber(deleteDto.slotNumber());
        if(forDeleteSlot.isEmpty()){
            throw new SlotControllerException("Have no slot to delete! Слот для удаления не найден!");
        }
        return slotService.delete(forDeleteSlot.get().slotId());
    }
}
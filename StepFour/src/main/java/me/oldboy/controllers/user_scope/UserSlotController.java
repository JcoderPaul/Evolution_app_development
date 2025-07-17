package me.oldboy.controllers.user_scope;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.annotations.Measurable;
import me.oldboy.dto.slots.SlotReadUpdateDto;
import me.oldboy.exception.slot_exception.SlotControllerException;
import me.oldboy.services.SlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/api/slots")
public class UserSlotController {

    @Autowired
    private SlotService slotService;

    /* R - CRUD читаем (получаем) слоты */

    /**
     * Read existing slot / Извлекаем данные по слоту из БД по ID
     *
     * @param slotId slot ID for find / ID искомого слота
     * @return reading slot
     * @throws SlotControllerException if the slot non exist / выбрасывается в случае отсутствия искомого ID в БД
     */
    @GetMapping("/id/{slotId}")
    public ResponseEntity<?> readSlotById(@PathVariable("slotId") Long slotId) throws SlotControllerException {
        Optional<SlotReadUpdateDto> mayBeSlot = slotService.findById(slotId);
        if (mayBeSlot.isEmpty()) {
            throw new SlotControllerException("Слот с ID: " + slotId + " не существует!");
        } else
            return ResponseEntity.ok().body(mayBeSlot.get());
    }

    /**
     * Read existing slot / Извлекаем слот из БД по номеру
     *
     * @param slotNumber slot number for find / номер слота который мы ищем
     * @return reading slot
     * @throws SlotControllerException if the slot non exist / выбрасывается в случае отсутствия искомых данных в БД
     */
    @GetMapping("/number/{slotNumber}")
    public ResponseEntity<?> readSlotByNumber(@PathVariable("slotNumber") Integer slotNumber) throws SlotControllerException {
        Optional<SlotReadUpdateDto> mayBeSlot = slotService.findSlotByNumber(slotNumber);
        if (mayBeSlot.isEmpty()) {
            throw new SlotControllerException("Слот с номером: " + slotNumber + " не найден!");
        } else
            return ResponseEntity.ok().body(mayBeSlot.get());
    }

    /**
     * Show all available time slots / Просмотр всех доступных временных диапазонов для брони
     */
    @GetMapping()
    public List<SlotReadUpdateDto> getAllSlots() {
        return slotService.findAll();
    }
}
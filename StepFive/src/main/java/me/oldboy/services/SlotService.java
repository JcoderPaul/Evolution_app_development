package me.oldboy.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.dto.slots.SlotCreateDeleteDto;
import me.oldboy.dto.slots.SlotReadUpdateDto;
import me.oldboy.exception.slot_exception.SlotServiceException;
import me.oldboy.logger.annotation.Measurable;
import me.oldboy.mapper.SlotMapper;
import me.oldboy.models.entity.Slot;
import me.oldboy.repository.SlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
@AllArgsConstructor
@Transactional(readOnly = true)
public class SlotService {

    @Autowired
    private SlotRepository slotRepository;

    @Transactional
    @Measurable
    public Long create(SlotCreateDeleteDto slotCreateDeleteDto) {
        /* Проверяем корректность заданных параметров */
        if (isNewSlotConflicts(slotCreateDeleteDto)) {
            throw new SlotServiceException("Конфликт временного диапазона слота бронирования!");
        } else if (findSlotByNumber(slotCreateDeleteDto.slotNumber()).isPresent()) {
            throw new SlotServiceException("Слот с номером '" + slotCreateDeleteDto.slotNumber() +
                    "' уже существует!");
        } else if (slotCreateDeleteDto.timeStart().isAfter(slotCreateDeleteDto.timeFinish())) {
            throw new SlotServiceException("Время начала: " + slotCreateDeleteDto.timeStart() +
                    " не может быть установлено позже времени окончания слота: " + slotCreateDeleteDto.timeFinish());
        }

        /* Если все проверки пройдены создаем новую сущность */
        Slot createSlot = SlotMapper.INSTANCE.mapToEntity(slotCreateDeleteDto);

        return slotRepository.save(createSlot).getSlotId();
    }

    @Measurable
    public Optional<SlotReadUpdateDto> findById(Long slotId) {
        return slotRepository.findById(slotId).map(SlotMapper.INSTANCE::mapToSlotReadDto);
    }

    @Measurable
    public List<SlotReadUpdateDto> findAll() {
        return slotRepository.findAll()
                .stream()
                .map(SlotMapper.INSTANCE::mapToSlotReadDto)
                .collect(Collectors.toList());
    }

    @Measurable
    public Optional<SlotReadUpdateDto> findSlotByNumber(Integer slotNumber) {
        return slotRepository.findBySlotNumber(slotNumber).map(SlotMapper.INSTANCE::mapToSlotReadDto);
    }

    @Transactional
    @Measurable
    public boolean delete(Long slotId) {
        Optional<Slot> mayBeSlot = slotRepository.findById(slotId);
        mayBeSlot.ifPresentOrElse(
                slot -> slotRepository.delete(slot),
                () -> {
                    throw new SlotServiceException("Slot with id - " + slotId + " not found!");
                }
        );
        return mayBeSlot.isPresent();
    }

    @Transactional
    @Measurable
    public boolean update(SlotReadUpdateDto slotReadUpdateDto) {
        /* Проверяем наличие записи в БД для изменения */
        Optional<Slot> mayBeSlot = slotRepository.findById(slotReadUpdateDto.slotId());

        /*
        Пусть обновлять временной диапазон можно в пределах текущего, хотя, был
        вариант удалить его и создать новый с применением глобальной проверки
        конфликта вновь созданного диапазона.
        */
        if (mayBeSlot.isPresent()) {
            if (mayBeSlot.get().getTimeStart().isAfter(slotReadUpdateDto.timeStart()) ||
                    mayBeSlot.get().getTimeFinish().isBefore(slotReadUpdateDto.timeFinish())) {
                throw new SlotServiceException("Обновить временной диапазон можно только в переделах текущего!");
            }
            if (slotReadUpdateDto.timeStart().isAfter(slotReadUpdateDto.timeFinish())) {
                throw new SlotServiceException("Время начала: " + slotReadUpdateDto.timeStart() +
                        " не может быть установлено позже времени окончания слота: " +
                        slotReadUpdateDto.timeFinish());
            }
            if (!(slotReadUpdateDto.slotNumber()).equals(mayBeSlot.get().getSlotNumber())) {
                if (slotRepository.findBySlotNumber(slotReadUpdateDto.slotNumber()).isPresent()) {
                    throw new SlotServiceException("Слот с номером '" + slotReadUpdateDto.slotNumber() +
                            "' уже существует!");
                }
            }
        }

        /*
            Если данные валидны и запись есть - меняем содержимое.
            Весь процесс можно реализовать через функционал метода
            *.ifPresent()

            mayBeSlot.ifPresent(slot -> {
                slot.setSlotNumber(slotReadUpdateDto.slotNumber());
                slot.setTimeStart(slotReadUpdateDto.timeStart());
                slot.setTimeFinish(slotReadUpdateDto.timeFinish());
                slotRepository.save(slot);
            });

            но для наглядности тестирования используем классику.
        */

        if(mayBeSlot.isPresent()){
            Slot updateSlot = mayBeSlot.get();
            updateSlot.setSlotNumber(slotReadUpdateDto.slotNumber());
            updateSlot.setTimeStart(slotReadUpdateDto.timeStart());
            updateSlot.setTimeFinish(slotReadUpdateDto.timeFinish());
            slotRepository.save(updateSlot);
        }

        /* Подтверждаем обновление */
        return mayBeSlot.isPresent();
    }

    /* Проверяем конфликтует ли вновь создаваемый слот (бронируемая единица) с уже существующими */
    private boolean isNewSlotConflicts(SlotCreateDeleteDto slotCreateDeleteDto) {
        List<Slot> allSlots = slotRepository.findAll();
        return allSlots.stream()
                .anyMatch(existSlot ->
                        ((existSlot.getTimeStart().isBefore(slotCreateDeleteDto.timeStart()) &&
                                existSlot.getTimeFinish().isAfter(slotCreateDeleteDto.timeStart())) ||
                                (existSlot.getTimeStart().isBefore(slotCreateDeleteDto.timeFinish()) &&
                                        existSlot.getTimeFinish().isAfter(slotCreateDeleteDto.timeFinish()))));
    }
}
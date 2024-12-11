package me.oldboy.core.model.service;

import me.oldboy.core.dto.slots.SlotCreateDeleteDto;
import me.oldboy.core.dto.slots.SlotReadUpdateDto;
import me.oldboy.core.model.database.repository.ReservationRepository;
import me.oldboy.exception.SlotServiceException;
import me.oldboy.core.mapper.SlotMapper;
import me.oldboy.core.model.database.entity.Slot;
import me.oldboy.core.model.database.repository.SlotRepository;
import me.oldboy.core.model.database.repository.crud.RepositoryBase;
import me.oldboy.validate.ValidatorDto;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

public class SlotService extends ServiceBase<Long, Slot>{

    private ReservationRepository reservationRepository;
    public SlotService(RepositoryBase<Long, Slot> repositoryBase,
                       ReservationRepository reservationRepository) {
        super(repositoryBase);
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Long create(SlotCreateDeleteDto slotCreateDeleteDto) {
        /* Валидируем входящие данные */
        ValidatorDto.getInstance().isValidData(slotCreateDeleteDto);

        /* Проверяем корректность заданных параметров */
        if(isNewSlotConflicts(slotCreateDeleteDto)){
            throw new SlotServiceException("Конфликт временного диапазона слота бронирования!");
        } else if(findSlotByNumber(slotCreateDeleteDto.slotNumber()).isPresent()){
            throw new SlotServiceException("Слот с номером " + "'" + slotCreateDeleteDto.slotNumber() + "' уже существует!");
        } else if(slotCreateDeleteDto.timeStart().isAfter(slotCreateDeleteDto.timeFinish())) {
            throw new SlotServiceException("Время начала: " + slotCreateDeleteDto.timeStart() +
                    " не может быть установлено позже времени окончания слота: " + slotCreateDeleteDto.timeFinish());
        }

        /* Если все проверки пройдены создаем новую сущность */
        Slot createSlot = SlotMapper.INSTANCE.mapToEntity(slotCreateDeleteDto);
        return getRepositoryBase().create(createSlot).getSlotId();
    }

    @Transactional
    public Optional<SlotReadUpdateDto> findById(Long slotId){
        return getRepositoryBase()
                .findById(slotId)
                .map(SlotMapper.INSTANCE::mapToSlotReadDto);
    }

    @Transactional
    public List<SlotReadUpdateDto> findAll(){
        return getRepositoryBase()
                .findAll()
                .stream()
                .map(SlotMapper.INSTANCE::mapToSlotReadDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<SlotReadUpdateDto> findSlotByNumber(Integer slotNumber) {
        SlotRepository slotRepository = (SlotRepository) getRepositoryBase();
        return slotRepository
                .findSlotByNumber(slotNumber)
                .map(SlotMapper.INSTANCE::mapToSlotReadDto);
    }

    @Transactional
    public boolean delete(Long slotId){
        Optional<Slot> mayBeSlot = getRepositoryBase().findById(slotId);
        mayBeSlot.ifPresent(slot -> getRepositoryBase().delete(slot.getSlotId()));
        return mayBeSlot.isPresent();
    }

    @Transactional
    public boolean update(Long slotId, SlotReadUpdateDto slotReadUpdateDto) {
        /* Валидируем входящие данные */
        ValidatorDto.getInstance().isValidData(slotReadUpdateDto);

        /* Проверяем наличие записи в БД для изменения */
        Optional<Slot> mayBeSlot = getRepositoryBase().findById(slotId);

        /*
        Пусть обновлять временной диапазон можно в пределах текущего, хотя, был
        вариант удалить его и создать новый с применением глобальной проверки
        конфликта вновь созданного диапазона.
        */
        if(mayBeSlot.isPresent()){
            if(mayBeSlot.get().getTimeStart().isAfter(slotReadUpdateDto.timeStart()) ||
               mayBeSlot.get().getTimeFinish().isBefore(slotReadUpdateDto.timeFinish())){
                    throw new SlotServiceException("Обновить временной диапазон можно только в переделах текущего!");
            }
            if(slotReadUpdateDto.timeStart().isAfter(slotReadUpdateDto.timeFinish())) {
                throw new SlotServiceException("Время начала: " + slotReadUpdateDto.timeStart() +
                                               " не может быть установлено позже времени окончания слота: " +
                                               slotReadUpdateDto.timeFinish());
            }
            if(!slotReadUpdateDto.slotNumber().equals(mayBeSlot.get().getSlotNumber())){
                if(((SlotRepository) getRepositoryBase())
                        .findSlotByNumber(slotReadUpdateDto.slotNumber()).isPresent()){
                    throw new SlotServiceException("Слот с номером " + "'" + slotReadUpdateDto.slotNumber() + "' уже существует!");
                }
            }
        }

        /* Если данные валидны и запись есть меняем содержимое */
        mayBeSlot.ifPresent(slot -> {
            slot.setSlotNumber(slotReadUpdateDto.slotNumber());
            slot.setTimeStart(slotReadUpdateDto.timeStart());
            slot.setTimeFinish(slotReadUpdateDto.timeFinish());
            getRepositoryBase().update(slot);});

        /* Подтверждаем обновление */
        return mayBeSlot.isPresent();
    }

    /* Проверяем конфликтует ли вновь создаваемый слот (бронируемая единица) с уже существующими */
    private boolean isNewSlotConflicts(SlotCreateDeleteDto slotCreateDeleteDto){
        return getRepositoryBase().findAll()
                .stream()
                .anyMatch(existSlot ->
                        ((existSlot.getTimeStart().isBefore(slotCreateDeleteDto.timeStart()) &&
                                existSlot.getTimeFinish().isAfter(slotCreateDeleteDto.timeStart())) ||
                         (existSlot.getTimeStart().isBefore(slotCreateDeleteDto.timeFinish()) &&
                                existSlot.getTimeFinish().isAfter(slotCreateDeleteDto.timeFinish()))));
    }
}

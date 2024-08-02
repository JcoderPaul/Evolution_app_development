package me.oldboy.cwapp.input.controllers;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.exceptions.controllers.SlotControllerException;
import me.oldboy.cwapp.input.entity.*;
import me.oldboy.cwapp.input.service.ReserveService;
import me.oldboy.cwapp.input.service.SlotService;
import me.oldboy.cwapp.input.service.UserService;

import java.time.LocalTime;
import java.util.Scanner;

@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;
    private final UserService userService;
    private final ReserveService reserveService;

    public Long createNewSlot(Scanner scanner, Long userId){
        Long newSlotGenerateId = null;
        User mayBeAdmin = userService.findUserById(userId);

        if(mayBeAdmin.getRole().equals(Role.ADMIN)) {
            System.out.print("Вы можете создать слот, введите номер: ");
            String slotCreatedNumber = scanner.nextLine();
            Integer slotNumber = Integer.parseInt(slotCreatedNumber);

            LocalTime[] enterTimeRange = setTimeRange(scanner);

            if(slotService.findSlotByNumber(slotNumber) != null) {
                throw new SlotControllerException("Слот с номером " + slotNumber + " уже существует");
            } else if(enterTimeRange[0].isAfter(enterTimeRange[1])) {
                throw new SlotControllerException("Время начала диапазона не может быть после его окончания!");
            } else {
                Slot mayByCreatedSlot = new Slot(slotNumber, enterTimeRange[0], enterTimeRange[1]);
                newSlotGenerateId = slotService.createSlot(mayByCreatedSlot);
            }
        } else {
            throw new SlotControllerException("У пользователя недостаточно прав!");
        }
        return newSlotGenerateId;
    }

    public void viewAllSlots(){
        slotService.findAllSlots().forEach(System.out::println);
    }

    public boolean updateSlot(Scanner scanner, Long userId){
        Boolean isSlotUpdated = false;
        User mayBeAdmin = userService.findUserById(userId);

        if(mayBeAdmin.getRole().equals(Role.ADMIN)) {
            System.out.print("Выберите слот из списка который планируете изменить: \n");
            viewAllSlots();

            System.out.print("Введите выбранный ID для изменения: ");
            String slotIdForUpdate = scanner.nextLine();
            Long slotId = Long.parseLong(slotIdForUpdate);

            System.out.print("Присвойте новый номер слоту: ");
            String slotNewNumberForUpdate = scanner.nextLine();
            Integer slotNumber = Integer.parseInt(slotNewNumberForUpdate);

            LocalTime[] enterTimeRange = setTimeRange(scanner);

            if(!reserveService.findReservationsBySlotId(slotId).isEmpty()){
                throw new SlotControllerException("Нельзя обновлять данные по зарезервированному слоту!");
            } else if(enterTimeRange[0].isAfter(enterTimeRange[1])){
                throw new SlotControllerException("Время начала диапазона не может быть после его окончания!");
            } else {
                Slot updateSlot = slotService.findSlotById(slotId);

                updateSlot.setSlotNumber(slotNumber);
                updateSlot.setTimeStart(enterTimeRange[0]);
                updateSlot.setTimeFinish(enterTimeRange[1]);

                isSlotUpdated = slotService.updateSlot(updateSlot);
            }
        } else {
            throw new SlotControllerException("У пользователя недостаточно прав!");
        }
        return isSlotUpdated;
    }

    public boolean deleteSlot(Scanner scanner, Long userId){
        Boolean isSlotDeleted = false;
        User mayBeAdmin = userService.findUserById(userId);

        if(mayBeAdmin.getRole().equals(Role.ADMIN)) {
            System.out.print("Выберите слот из списка которое планируете удалить: \n");
            viewAllSlots();

            System.out.print("Введите выбранный ID для удаления слота: ");
            String slotIdForDelete = scanner.nextLine();
            Long slotId = Long.parseLong(slotIdForDelete);

            if(!reserveService.findReservationsBySlotId(slotId).isEmpty()){
                throw new SlotControllerException("Нельзя удалять зарезервированный слот!");
            } else if(slotService.findSlotById(slotId) == null){
                throw new SlotControllerException("Слот для удаления не найден!");
            } else {
                isSlotDeleted = slotService.deleteSlot(slotId);
            }
        } else {
            throw new SlotControllerException("У пользователя недостаточно прав!");
        }
        return isSlotDeleted;
    }

    private LocalTime[] setTimeRange(Scanner scanner){
        LocalTime[] newTimeRange = new LocalTime[2];

        System.out.print("Выберите время начала диапазона (чч:мм): ");
        String slotStartTimeForUpdate = scanner.nextLine();
        LocalTime slotStartTime = LocalTime.parse(slotStartTimeForUpdate);
        newTimeRange[0] = slotStartTime;

        System.out.print("Выберите время конца диапазона (чч:мм): ");
        String slotFinishTimeForUpdate = scanner.nextLine();
        LocalTime slotFinishTime = LocalTime.parse(slotFinishTimeForUpdate);
        newTimeRange[1] = slotFinishTime;

        return newTimeRange;
    }
}

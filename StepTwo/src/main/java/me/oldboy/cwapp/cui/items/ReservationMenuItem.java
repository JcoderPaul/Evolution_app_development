package me.oldboy.cwapp.cui.items;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.core.controllers.PlaceController;
import me.oldboy.cwapp.core.controllers.ReserveController;
import me.oldboy.cwapp.core.entity.Place;
import me.oldboy.cwapp.core.entity.Reservation;
import me.oldboy.cwapp.core.entity.Slot;
import me.oldboy.cwapp.core.service.PlaceService;
import me.oldboy.cwapp.core.service.ReserveService;
import me.oldboy.cwapp.core.service.SlotService;


import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ReservationMenuItem {

    private final ReserveController reserveController;
    private final ReserveService reserveService;
    private final PlaceController placeController;
    private final PlaceService placeService;
    private final SlotService slotService;

    public void manageByReservationAndViewPlace(Scanner scanner, Long userId){
        Boolean leaveMenu = true;
        do{
            System.out.print("Выберите один из пунктов меню: " +
                    "\n1 - просмотр списка всех доступных рабочих мест и конференц-залов;" +
                    "\n2 - просмотр доступных слотов для бронирования на конкретную дату;" +
                    "\n3 - бронирование рабочего места или конференц-зала на определённое время и дату;" +
                    "\n4 - отмена бронирования;" +
                    "\n5 - просмотр всех бронирований с фильтрацией;" +
                    "\n6 - покинуть меню резервирования;\n\n" +
                    "Сделайте выбор и нажмите ввод: ");
            String choiceMenuItem = scanner.nextLine();

            switch (choiceMenuItem) {
                case "1":
                    placeController.showAllPlaces();
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "2":
                    showAllFreeSlotsByDate(scanner);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "3":
                    reserveController.reservationPlace(scanner, userId);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "4":
                    reserveController.deletePlaceReservation(scanner, userId);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "5":
                    viewReservationWithFilter(scanner, userId);
                    System.out.println("---------------------------------------------------------------");
                    break;
                case "6":
                    leaveMenu = false;
                    break;
                default:
                    break;
            }
        } while (leaveMenu);
    }

    private void viewReservationWithFilter(Scanner scanner, Long userId){
        System.out.print("Выберите в каком формате желаете просмотреть текущие брони: " +
                "\n1 - все без фильтрации;" +
                "\n2 - отфильтровать по дате;" +
                "\n3 - только ваши брони;" +
                "\n4 - по выбранному ресурсу;\n\n" +
                "Сделайте выбор и нажмите ввод: ");
        String choiceMenuItem = scanner.nextLine();

        switch (choiceMenuItem) {
            case "1":
                reserveController.showAllReservation();
                break;
            case "2":
                reserveController.showAllReservationByDate(scanner);
                break;
            case "3":
                reserveController.showAllReservationByUserId(userId);
                break;
            case "4":
                reserveController.showAllReservationByPlaceId(scanner);
                break;
            default:
                break;
        }
    }

    /* Обработка пункта меню - просмотр доступных для бронирования слотов на конкретную дату */
    private void showAllFreeSlotsByDate(Scanner scanner) {
        System.out.print("Введите дату на которую хотите посмотреть все свободные слоты (yyyy-mm-dd): ");
        String getDate = scanner.nextLine();
        /* Получаем все брони на конкретную дату */
        List<Reservation> allReservationByDate =
                reserveService.findReservationByDate(LocalDate.parse(getDate));
        /* Получаем список всех мест / залов, для наглядного отображения */
        List<Place> allPlaces = placeService.findAllPlaces();
        /* Сепарируем занятые слоты по местам Map: key - placeId, value - List<Reservation>*/
        Map<Long, List<Reservation>> separateReservation = collectReservationByPlaceId(allReservationByDate);
        separateReservationAndFreeSlot(allPlaces, separateReservation);
    }

    private void separateReservationAndFreeSlot(List<Place> allPlaces, Map<Long, List<Reservation>> separateReservation) {
        /*
        Нам нужно отобразить свободные слоты для этого отделим их от занятых,
        используем коллекцию выделенных броней для конкретного placeId на выбранную дату
        */
        for (Map.Entry<Long, List<Reservation>> entry: separateReservation.entrySet()) {
            /* Превратим List слотов в Map слотов (key - slotId, value - Slot), для каждого placeId он свой */
            Map<Long, Slot> freeSlotsMap = slotService.findAllSlots().stream()
                    .collect(Collectors.toMap(Slot::getSlotId, Function.identity()));
            /*
            На каждое placeId место / зал, в конкретную дату, может быть зарезервировано n-слотов, при
            существующих - x, где n >= x, удаляем из выделенного списка существующих слотов занятые и
            получаем список (или Map) свободных
            */
            entry.getValue().forEach(reservation -> freeSlotsMap.remove(reservation.getSlot().getSlotId()));
            /* По place ID получаем его 'красивое' название */
            Place placeToPrint = allPlaces.stream()
                                          .filter(p -> p.getPlaceId().equals(entry.getKey()))
                                          .findAny()
                                          .get();
            /* Отображаем результат */
            System.out.print("\nУ " + placeToPrint.getSpecies().getStrName() + " - " +
                                      placeToPrint.getPlaceNumber() + " свободы: ");
            freeSlotsMap.entrySet().forEach(slotEntry ->
                    System.out.print(" | слот № " + slotEntry.getValue().getSlotNumber() + ": " +
                                                    slotEntry.getValue().getTimeStart() + " - " +
                                                    slotEntry.getValue().getTimeFinish()));
        }
        System.out.println("\nЕсли в данном списке вы не нашли интересующий вас зал/рабочее место, " +
                           "\nзначит на выбранную дату они полностью доступны для бронирования в " +
                           "рабочее время! \nМилости просим!");
    }

    private Map<Long, List<Reservation>> collectReservationByPlaceId(List<Reservation> allReservationByConcreteDate) {
        /* Создаем коллекцию Map для картирования брони на ID места / зала (key - placeID, value - List<Reservation>) */
        Map<Long, List<Reservation>> reserveByPlaceIdMap = new HashMap<>();
        /*
        Перебираем полученную коллекцию броней на выбранную дату, чтобы
        получить для каждого ID места / зала свой List набор занятых слотов
        */
        for(Reservation res: allReservationByConcreteDate){
            /*
            Если в Map выборке для сепарирования броней по ID, нет такого ID места/зала,
            создаем для него отдельную List коллекцию броней, а сам ID будет ключом
            */
            if(!reserveByPlaceIdMap.containsKey(res.getPlace().getPlaceId())){
                /* Создаем List броней для конкретного ID места / зала */
                List<Reservation> concretePlaceIdList = new ArrayList<>();
                /* Добавляем бронь из выборки по дате в List соответствующего ID зала / места */
                concretePlaceIdList.add(res);
                /* Добавляем в нашу сепарирующую Map коллекцию: key - ID места/зала и value - его выборку слотов */
                reserveByPlaceIdMap.put(res.getPlace().getPlaceId(), concretePlaceIdList);
            } else {
                /*
                Если перебирая отфильтрованную по дате коллекцию броней, мы
                находим для конкретного ID места / зала новую бронь (слот),
                и это ID уже есть в сепарирующей Map, то вносим эту бронь в
                выборку List соответствующий этому ID
                */
                reserveByPlaceIdMap.get(res.getPlace().getPlaceId()).add(res);
            }
        }
        return reserveByPlaceIdMap;
    }
}

package me.oldboy.cwapp.handlers;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.entity.Place;
import me.oldboy.cwapp.entity.Reservation;
import me.oldboy.cwapp.services.PlaceService;
import me.oldboy.cwapp.services.ReservationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@RequiredArgsConstructor
public class FreeReservationSlotsHandler {

    private final ReservationService reservationService;
    private final PlaceService placeService;

    public void showAllFreeSlotsByDate(Scanner scanner){
        System.out.print("Для просмотра данных по свободным слотам введите дату (yyyy-mm-dd): ");
        LocalDate findFreeSlotsForThisDate = LocalDate.parse(scanner.nextLine());
        List<Reservation> allReservationByConcreteDate=
                reservationService.findReservationByDate(findFreeSlotsForThisDate);

        List<Place> allPlaces = placeService.getAllPlaces();
        Map<Long, List<Reservation>> separateReservation =
                collectReservationByPlaceId(allReservationByConcreteDate);
        for (Map.Entry<Long, List<Reservation>> entry: separateReservation.entrySet()) {
            Place placeToPrint = allPlaces.stream()
                                          .filter(p -> p.getPlaceId().equals(entry.getKey()))
                                          .findAny()
                                          .get();
            System.out.print("\nУ " + placeToPrint.getSpecies().getStrName() +
                             " - " + placeToPrint.getPlaceNumber() + ", ");
            List<Reservation> sortReservation = sortReserveByStartTime(entry.getValue());
            showFreeTimeSlots(sortReservation);
        }
        System.out.println("\nЕсли в данном списке вы не нашли интересующий вас зал/рабочее место, " +
                           "\nзначит на выбранную дату они полностью доступны для бронирования с " +
                           "00:00 до 23:59! \nМилости просим!");
    }

    private static Map<Long, List<Reservation>> collectReservationByPlaceId(List<Reservation> allReservationByConcreteDate) {
        Map<Long, List<Reservation>> reserveByPlaceIdMap = new HashMap<>();
        for(Reservation res: allReservationByConcreteDate){
            if(!reserveByPlaceIdMap.containsKey(res.getReservationPlaceId())){
                List<Reservation> concretePlaceIdList = new ArrayList<>();
                concretePlaceIdList.add(res);
                reserveByPlaceIdMap.put(res.getReservationPlaceId(), concretePlaceIdList);
            } else {
                reserveByPlaceIdMap.get(res.getReservationPlaceId()).add(res);
            }
        }
        return reserveByPlaceIdMap;
    }

    private static List<Reservation> sortReserveByStartTime(List<Reservation> listForSort){
        return listForSort.stream()
                .sorted(Comparator.comparing(reservation -> reservation.getStartTime()))
                .toList();
    }
    
    private static void showFreeTimeSlots(List<Reservation> sortListForShow){
        LocalTime startDayTime = LocalTime.of(00,00);
        LocalTime finishDayTime = LocalTime.of(23,59);
        System.out.println("доступны свободные временные диапазоны: ");
        for(int i = 0; i <= sortListForShow.size() - 1; i++){
            if(sortListForShow.size() == 1){
                oneElementListView(sortListForShow, startDayTime, finishDayTime, i);
            } else if(sortListForShow.size() > 1) {
                moreThenOneElementInListView(sortListForShow, startDayTime, finishDayTime, i);
            }
        }
    }

    private static void oneElementListView(List<Reservation> sortListForShow,
                                           LocalTime startDayTime,
                                           LocalTime finishDayTime,
                                           int i) {
        if(startDayTime.isBefore(sortListForShow.get(i).getStartTime()) &&
           finishDayTime.isAfter(sortListForShow.get(i).getFinishTime())){
            System.out.print(startDayTime +
                             " - " + sortListForShow.get(i).getStartTime() +
                             ", " + sortListForShow.get(i).getFinishTime() +
                             " - " + finishDayTime);
        }

        if (startDayTime.equals(sortListForShow.get(i).getStartTime()) &&
                   finishDayTime.isAfter(sortListForShow.get(i).getFinishTime())){
            System.out.print(sortListForShow.get(i).getFinishTime() +
                             " - " + finishDayTime);
        }

        if(startDayTime.isBefore(sortListForShow.get(i).getStartTime()) &&
                  finishDayTime.equals(sortListForShow.get(i).getFinishTime())){
            System.out.print(startDayTime +
                             " - " + sortListForShow.get(i).getStartTime());
        }

        if (startDayTime.equals(sortListForShow.get(i).getStartTime()) &&
                   finishDayTime.equals(sortListForShow.get(i).getFinishTime())){
            System.out.println("Свободных слотов нет!");
        }
    }

    private static void moreThenOneElementInListView(List<Reservation> sortListForShow,
                                                     LocalTime startDayTime,
                                                     LocalTime finishDayTime,
                                                     int i) {
        if (i == 0 && startDayTime.isBefore(sortListForShow.get(i).getStartTime())) {
            System.out.print(startDayTime + " - " +
                               sortListForShow.get(i).getStartTime() + ", " +
                               sortListForShow.get(i).getFinishTime());
        } else if (i == 0 && startDayTime.equals(sortListForShow.get(i).getStartTime())){
            System.out.print(sortListForShow.get(i).getFinishTime());
        }

        if (i > 0 && i < sortListForShow.size() - 1) {
            System.out.print(" - " + sortListForShow.get(i).getStartTime() +
                               ", " + sortListForShow.get(i).getFinishTime());
        }

        if (i == sortListForShow.size() - 1 && finishDayTime.equals(sortListForShow.get(i).getFinishTime())){
            System.out.print(" - " + sortListForShow.get(i).getStartTime());
        } else if (i == sortListForShow.size() - 1 && finishDayTime.isAfter(sortListForShow.get(i).getFinishTime())){
            System.out.print(" - " + sortListForShow.get(i).getStartTime() +
                               ", " + sortListForShow.get(i).getFinishTime() + " - " + finishDayTime);
        }
    }
}

package me.oldboy.cwapp.config.context;

import me.oldboy.cwapp.core.controllers.PlaceController;
import me.oldboy.cwapp.core.controllers.ReserveController;
import me.oldboy.cwapp.core.controllers.SlotController;
import me.oldboy.cwapp.core.controllers.UserController;
import me.oldboy.cwapp.core.repository.PlaceRepositoryImpl;
import me.oldboy.cwapp.core.repository.ReserveRepositoryImp;
import me.oldboy.cwapp.core.repository.SlotRepositoryImpl;
import me.oldboy.cwapp.core.repository.UserRepositoryImpl;
import me.oldboy.cwapp.core.repository.crud.PlaceRepository;
import me.oldboy.cwapp.core.repository.crud.ReservationRepository;
import me.oldboy.cwapp.core.repository.crud.SlotRepository;
import me.oldboy.cwapp.core.repository.crud.UserRepository;
import me.oldboy.cwapp.core.service.PlaceService;
import me.oldboy.cwapp.core.service.ReserveService;
import me.oldboy.cwapp.core.service.SlotService;
import me.oldboy.cwapp.core.service.UserService;

import java.sql.Connection;

public class CwAppContext {

    private static CwAppContext instance;

    private CwAppContext() {
    }

    private static PlaceRepository placeRepository;
    private static UserRepository userRepository;
    private static SlotRepository slotRepository;
    private static ReservationRepository reservationRepository;

    private static PlaceService placeService;
    private static ReserveService reservationService;
    private static UserService userService;
    private static SlotService slotService;
    private static UserController userController;
    private static PlaceController placeController;
    private static SlotController slotController;
    private static ReserveController reserveController;

    public static CwAppContext getInstance(Connection connection){
        if(instance == null){
            instance = new CwAppContext();
        }
        placeRepository = new PlaceRepositoryImpl(connection);
        userRepository = new UserRepositoryImpl(connection);
        slotRepository = new SlotRepositoryImpl(connection);
        reservationRepository =
                new ReserveRepositoryImp(connection, placeRepository, userRepository, slotRepository);
        placeService =
                new PlaceService(placeRepository, reservationRepository);
        reservationService =
                new ReserveService(reservationRepository, userRepository, slotRepository, placeRepository);
        userService = new UserService(userRepository, reservationRepository);
        slotService = new SlotService(reservationRepository, slotRepository);
        userController = new UserController(userService);
        placeController = new PlaceController(placeService, reservationService, userService);
        slotController = new SlotController(slotService, userService, reservationService);
        reserveController = new ReserveController(reservationService, placeService, slotService, userService);
        return instance;
    }

    public PlaceRepository getPlaceRepository() {
        return placeRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public SlotRepository getSlotRepository() {
        return slotRepository;
    }

    public ReservationRepository getReservationRepository() {
        return reservationRepository;
    }

    public PlaceService getPlaceService() {
        return placeService;
    }

    public ReserveService getReservationService() {
        return reservationService;
    }

    public UserService getUserService() {
        return userService;
    }

    public SlotService getSlotService() {
        return slotService;
    }

    public UserController getUserController() {
        return userController;
    }

    public PlaceController getPlaceController() {
        return placeController;
    }

    public SlotController getSlotController() {
        return slotController;
    }

    public ReserveController getReserveController() {
        return reserveController;
    }
}
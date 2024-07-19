package me.oldboy.cwapp.context;

import me.oldboy.cwapp.handlers.FreeReservationSlotsHandler;
import me.oldboy.cwapp.handlers.PlaceViewHandler;
import me.oldboy.cwapp.handlers.ReservationViewHandler;
import me.oldboy.cwapp.handlers.UserAuthenticationHandler;
import me.oldboy.cwapp.services.PlaceService;
import me.oldboy.cwapp.services.ReservationService;
import me.oldboy.cwapp.services.UserService;
import me.oldboy.cwapp.store.base.PlaceBase;
import me.oldboy.cwapp.store.base.ReservationBase;
import me.oldboy.cwapp.store.base.StartInitBases;
import me.oldboy.cwapp.store.base.UserBase;

public class CwAppContext {

    private static CwAppContext instance;

    private CwAppContext() {
    }

    private final static PlaceBase placeBase = new PlaceBase();
    private final static UserBase userBase = new UserBase();
    private final ReservationBase reservationBase = new ReservationBase();

    private final PlaceService placeService = new PlaceService(placeBase, reservationBase);
    private final ReservationService reservationService = new ReservationService(reservationBase);
    private final UserService userService = new UserService(userBase);

    private final UserAuthenticationHandler userAuthenticationHandler =
            new UserAuthenticationHandler(userService);
    private final ReservationViewHandler reservationViewHandler =
            new ReservationViewHandler(reservationService, placeService, userService);
    private final PlaceViewHandler placeViewHandler =
            new PlaceViewHandler(placeService, reservationService, userService);
    private final FreeReservationSlotsHandler freeReservationSlotsHandler =
            new FreeReservationSlotsHandler(reservationService, placeService);
    private final static StartInitBases startInitBases =
            new StartInitBases(placeBase, userBase);

    public static CwAppContext getInstance(){
        if(instance == null){
            instance = new CwAppContext();
            startInitBases.startInitBase();
        }
        return instance;
    }

    public PlaceBase getPlaceBase() {
        return placeBase;
    }

    public UserBase getUserBase() {
        return userBase;
    }

    public ReservationBase getReservationBase() {
        return reservationBase;
    }

    public PlaceService getPlaceService() {
        return placeService;
    }

    public ReservationService getReservationService() {
        return reservationService;
    }

    public UserService getUserService() {
        return userService;
    }

    public UserAuthenticationHandler getUserAuthenticationHandler() {
        return userAuthenticationHandler;
    }

    public ReservationViewHandler getReservationViewHandler() {
        return reservationViewHandler;
    }

    public PlaceViewHandler getPlaceViewHandler() {
        return placeViewHandler;
    }

    public FreeReservationSlotsHandler getFreeReservationSlotsHandler() {
        return freeReservationSlotsHandler;
    }
}

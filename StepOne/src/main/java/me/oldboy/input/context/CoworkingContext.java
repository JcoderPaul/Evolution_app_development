package me.oldboy.input.context;

import me.oldboy.input.controllers.CoworkingSpaceController;
import me.oldboy.input.controllers.UserController;
import me.oldboy.input.repository.HallBase;
import me.oldboy.input.repository.ReserveBase;
import me.oldboy.input.repository.UserBase;
import me.oldboy.input.repository.WorkplaceBase;
import me.oldboy.output.view.AllReserveWithFilterView;

public class CoworkingContext {

    private static CoworkingContext instance;

    private CoworkingContext() {
    }

    private final static HallBase hallBase = new HallBase();
    private final static WorkplaceBase workplaceBase = new WorkplaceBase();
    private final static UserBase userBase = new UserBase();
    private final static ReserveBase reserveBase = new ReserveBase(userBase);
    private final static UserController userController = new UserController(userBase);
    private final static CoworkingSpaceController coworkingSpaceController =
            new CoworkingSpaceController(reserveBase, userBase);
    private final static AllReserveWithFilterView allReserveWithFilterView =
            new AllReserveWithFilterView();

    public static CoworkingContext getInstance(){
        if(instance == null){
            instance = new CoworkingContext();
            hallBase.initHallBase();
            workplaceBase.initPlaceBase();
            userBase.initBaseAdmin();
            hallBase.setReserveBase(reserveBase);
            workplaceBase.setReserveBase(reserveBase);
        }
        return instance;
    }

    public static HallBase getHallBase() {
        return hallBase;
    }

    public static WorkplaceBase getWorkplaceBase() {
        return workplaceBase;
    }

    public static UserBase getUserBase() {
        return userBase;
    }

    public static ReserveBase getReserveBase() {
        return reserveBase;
    }

    public static UserController getUserController() {
        return userController;
    }

    public static CoworkingSpaceController getCoworkingSpaceController() {
        return coworkingSpaceController;
    }

    public static AllReserveWithFilterView getAllReserveWithFilterView() {
        return allReserveWithFilterView;
    }
}

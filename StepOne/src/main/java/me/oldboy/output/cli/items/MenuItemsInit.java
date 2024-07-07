package me.oldboy.output.cli.items;

public class MenuItemsInit {
    private static MenuItemsInit instance;

    private static PlaceCrudOperations placeCrudOperations;
    private static ViewReserveByFilter viewReserveByFilter;
    private static ViewPlacesAndSlots placesAndSlots;
    private static ReserveCrudOperation reserveCrudOperation;
    private static ExitMenu exitMenu;

    private MenuItemsInit() {
    }

    public static MenuItemsInit getInstance() {
        if(instance == null){
            instance = new MenuItemsInit();
            placeCrudOperations = new PlaceCrudOperations();
            viewReserveByFilter = new ViewReserveByFilter();
            placesAndSlots = new ViewPlacesAndSlots();
            reserveCrudOperation = new ReserveCrudOperation();
            exitMenu = new ExitMenu();
        }
        return instance;
    }

    public static PlaceCrudOperations getPlaceCrudOperations() {
        return placeCrudOperations;
    }

    public static ViewReserveByFilter getViewReserveByFilter() {
        return viewReserveByFilter;
    }

    public static ViewPlacesAndSlots getPlacesAndSlots() {
        return placesAndSlots;
    }

    public static ReserveCrudOperation getReserveCrudOperation() {
        return reserveCrudOperation;
    }

    public static ExitMenu getExitMenu() {
        return exitMenu;
    }
}

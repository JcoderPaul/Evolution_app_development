package me.oldboy.cwapp.store.base;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.entity.Place;
import me.oldboy.cwapp.entity.Role;
import me.oldboy.cwapp.entity.Species;
import me.oldboy.cwapp.entity.User;

@RequiredArgsConstructor
public class StartInitBases {

    private final PlaceBase placeBase;
    private final UserBase userBase;

    public void startInitBase(){
        placeBase.getAllPlaceBase().put(1L, new Place(1L, Species.HALL, 1));
        placeBase.getAllPlaceBase().put(2L, new Place(2L, Species.HALL, 2));
        placeBase.getAllPlaceBase().put(3L, new Place(3L, Species.HALL, 3));
        placeBase.getAllPlaceBase().put(4L, new Place(4L, Species.WORKPLACE, 1));
        placeBase.getAllPlaceBase().put(5L, new Place(5L, Species.WORKPLACE, 2));
        placeBase.getAllPlaceBase().put(6L, new Place(6L,Species.WORKPLACE, 3));

        userBase.getUserBase().put(1L, new User(1L, "Admin", "admin", Role.ADMIN));
        userBase.getUserBase().put(2L, new User(2L, "User", "user", Role.USER));
    }
}

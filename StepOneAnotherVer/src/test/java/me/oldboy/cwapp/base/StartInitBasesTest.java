package me.oldboy.cwapp.base;

import me.oldboy.cwapp.store.base.PlaceBase;
import me.oldboy.cwapp.store.base.StartInitBases;
import me.oldboy.cwapp.store.base.UserBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class StartInitBasesTest {

    private static PlaceBase placeBase;
    private static UserBase userBase;
    private static StartInitBases startInitBases;

    @BeforeEach
    public void setUp(){
        placeBase = new PlaceBase();
        userBase = new UserBase();
        startInitBases = new StartInitBases(placeBase, userBase);
    }

    @AfterEach
    public void killBase(){
        placeBase.getAllPlaceBase().clear();
        userBase.getUserBase().clear();
    }

    @Test
    void shouldReturnRightSizeOfPlaceBase_startInitBase() {
        startInitBases.startInitBase();
        assertThat(placeBase.getAllPlaceBase().size()).isEqualTo(6);
    }

    @Test
    void shouldReturnRightSizeOfUserBase_startInitBase() {
        startInitBases.startInitBase();
        assertThat(userBase.getUserBase().size()).isEqualTo(2);
    }
}
package me.oldboy.cwapp.base;

import me.oldboy.cwapp.entity.Place;
import me.oldboy.cwapp.exception.base_exception.PlaceBaseException;
import me.oldboy.cwapp.store.base.PlaceBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static me.oldboy.cwapp.entity.Species.HALL;
import static me.oldboy.cwapp.entity.Species.WORKPLACE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class PlaceBaseTest {

    public PlaceBase placeBase;

    @BeforeEach
    public void initBase(){
        placeBase = new PlaceBase();
    }

    @AfterEach
    public void killBase(){
        placeBase.getAllPlaceBase().clear();
    }

    /* Проверяем нормальную работу методов */

    @Test
    void createPlaceGoodTest() {
        Long newPlaceId = placeBase.create(new Place(HALL, 1));
        assertThat(newPlaceId).isEqualTo(1L);
        Long newPlaceId_2 = placeBase.create(new Place(WORKPLACE, 2));
        assertThat(newPlaceId_2).isEqualTo(2L);
        Long newPlaceId_3 = placeBase.create(new Place(HALL, 3));
        assertThat(newPlaceId_3).isEqualTo(3L);
    }

    @Test
    void updateExistingPlaceGoodTest() {
        Place newPlace = new Place(HALL, 3);
        Long newUserId = placeBase.create(newPlace);

        Place placeForUpdateWithId = placeBase.findById(newUserId).get();
        placeForUpdateWithId.setSpecies(WORKPLACE);
        placeForUpdateWithId.setPlaceNumber(5);

        assertThat(placeBase.update(placeForUpdateWithId)).isEqualTo(newPlace);
    }

    @Test
    void findByIdGoodTest() {
        Place newPlace = new Place(WORKPLACE, 12);
        Long createPlaceId = placeBase.create(newPlace);

        Boolean mayBePlace = placeBase.findById(createPlaceId).isPresent();
        assertThat(mayBePlace).isTrue();
        assertThat(placeBase.findById(createPlaceId).get()).isEqualTo(newPlace);
    }

    @Test
    void delete() {
        Place placeForCreate = new Place(WORKPLACE, 23);
        Long createdPlaceId = placeBase.create(placeForCreate);

        assertThat(placeBase.delete(createdPlaceId)).isTrue();
    }

    @Test
    void findBySpeciesAndPlaceNumberGoodTest() {
        Long placeId = placeBase.create(new Place(WORKPLACE, 2));
        Long placeId_2 = placeBase.create(new Place(HALL,4));
        Long placeId_3 = placeBase.create(new Place(WORKPLACE,6));

        assertThat(placeBase.findBySpeciesAndPlaceNumber(WORKPLACE, 2))
                .isEqualTo(placeBase.findById(placeId));
        assertThat(placeBase.findBySpeciesAndPlaceNumber(HALL, 4))
                .isEqualTo(placeBase.findById(placeId_2));
        assertThat(placeBase.findBySpeciesAndPlaceNumber(WORKPLACE, 6))
                .isEqualTo(placeBase.findById(placeId_3));
    }

    @Test
    void findNonExistentPlaceBySpeciesAndNumberExceptionTest() {
        Place findPlace = new Place(8L, WORKPLACE, 34);
        assertThat(placeBase.findBySpeciesAndPlaceNumber(findPlace.getSpecies(),
                                                         findPlace.getPlaceNumber())
                            .isEmpty()).isTrue();
    }

    @Test
    void findAllPlacesGoodTest() {
        Long placeId = placeBase.create(new Place(HALL, 1));
        Long placeId_2 = placeBase.create(new Place(HALL, 2));
        Long placeId_3 = placeBase.create(new Place(WORKPLACE, 1));
        Long placeId_4 = placeBase.create(new Place(WORKPLACE, 2));
        Long placeId_5 = placeBase.create(new Place(WORKPLACE, 3));

        assertThat(placeBase.findAll().size()).isEqualTo(5);

        assertThat(placeBase.findAll().contains(placeBase.findById(placeId).get())).isTrue();
        assertThat(placeBase.findAll().contains(placeBase.findById(placeId_2).get())).isTrue();
        assertThat(placeBase.findAll().contains(placeBase.findById(placeId_3).get())).isTrue();
        assertThat(placeBase.findAll().contains(placeBase.findById(placeId_4).get())).isTrue();
        assertThat(placeBase.findAll().contains(placeBase.findById(placeId_5).get())).isTrue();
    }

    @Test
    void findAllBySpeciesGoodTest() {
        Long placeId = placeBase.create(new Place(HALL, 1));
        Long placeId_2 = placeBase.create(new Place(HALL, 2));
        Long placeId_3 = placeBase.create(new Place(WORKPLACE, 1));
        Long placeId_4 = placeBase.create(new Place(WORKPLACE, 2));
        Long placeId_5 = placeBase.create(new Place(WORKPLACE, 3));

        assertThat(placeBase.findAllBySpecies(WORKPLACE).size()).isEqualTo(3);

        assertThat(placeBase.findAllBySpecies(WORKPLACE)
                .contains(placeBase.findById(placeId).get()))
                .isFalse();
        assertThat(placeBase.findAllBySpecies(WORKPLACE)
                .contains(placeBase.findById(placeId_2).get()))
                .isFalse();
        assertThat(placeBase.findAllBySpecies(WORKPLACE)
                .contains(placeBase.findById(placeId_3).get())).isTrue();
        assertThat(placeBase.findAllBySpecies(WORKPLACE)
                .contains(placeBase.findById(placeId_4).get())).isTrue();
        assertThat(placeBase.findAllBySpecies(WORKPLACE)
                .contains(placeBase.findById(placeId_5).get())).isTrue();
    }

    /* Проверяем броски исключений */

    @Test
    void createPlaceWithIdExceptionTest() {
        Place newPlace = new Place(1L,WORKPLACE, 2);
        assertThatThrownBy(()->placeBase.create(newPlace))
                .isInstanceOf(PlaceBaseException.class)
                .hasMessageContaining("Ошибка создания нового " + newPlace.getSpecies() + " !");
    }

    @Test
    void findNonExistentPlaceByIdExceptionTest() {
        Long nonExistentPlaceId = 32L;
        assertThat(placeBase.findById(nonExistentPlaceId)).isEqualTo(Optional.ofNullable(null));
    }

    @Test
    void updateNonExistentPlaceExceptionTest() {
        Place updatePlace = new Place(3L, WORKPLACE, 6);

        assertThatThrownBy(()->placeBase.update(updatePlace))
                .isInstanceOf(PlaceBaseException.class)
                .hasMessageContaining("Вы пытаетесь обновить несуществующий " + updatePlace.getSpecies() + " !");
    }

    @Test
    void deleteNonExistentPlaceExceptionTest() {
        Place deletePlace = new Place(5L, HALL, 3);
        assertThatThrownBy(()->placeBase.delete(deletePlace.getPlaceId()))
                .isInstanceOf(PlaceBaseException.class)
                .hasMessageContaining("Место/зал с ID: " +
                                                deletePlace.getPlaceId() +
                                                " в базе не найден!");
    }
}
package me.oldboy.cwapp.input.service;

import me.oldboy.cwapp.exceptions.repositorys.PlaceRepositoryException;
import me.oldboy.cwapp.exceptions.services.PlaceServiceException;
import me.oldboy.cwapp.input.entity.Place;
import me.oldboy.cwapp.input.entity.Reservation;
import me.oldboy.cwapp.input.entity.Species;
import me.oldboy.cwapp.input.repository.crud.PlaceRepository;
import me.oldboy.cwapp.input.repository.crud.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private PlaceService placeService;

    private List<Place> emptyPlaceBase;
    private List<Place> testPlaceBase;
    private List<Reservation> testReservationBase;
    private List<Reservation> emptyReservationBase;
    private Place testPlace;
    private Integer placeNumber;
    private Long generateId;
    private Species species;

    @BeforeEach
    public void setUp(){
        species = Species.WORKPLACE;
        placeNumber = 1;
        generateId = 1L;
        testPlace = new Place(species, placeNumber);
        emptyPlaceBase = new ArrayList<>();
        testPlaceBase = List.of(new Place(), new Place(), new Place());
        emptyReservationBase = new ArrayList<>();
        testReservationBase = List.of(new Reservation(), new Reservation(), new Reservation());

        MockitoAnnotations.openMocks(this);
    }

    /* Тестируем метод *.createPlace() условного уровня сервисов */

    @Test
    void shouldReturnGeneratedId_createPlaceTest() {
        testPlace.setPlaceId(generateId);
        when(placeRepository.findPlaceBySpeciesAndNumber(species, placeNumber))
                .thenReturn(Optional.empty());
        when(placeRepository.createPlace(testPlace))
                .thenReturn(Optional.of(testPlace));
        assertThat(placeService.createPlace(testPlace)).isEqualTo(generateId);
    }

    @Test
    void shouldReturnExceptionIfTryToCreateExistentPlace_createPlaceTest() {
        testPlace.setPlaceId(generateId);
        when(placeRepository.findPlaceBySpeciesAndNumber(species, placeNumber))
                .thenReturn(Optional.of(testPlace));
        assertThatThrownBy(() -> placeService.createPlace(testPlace))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("'" + testPlace.getSpecies() + "' - " +
                                                testPlace.getPlaceNumber() +
                                                " уже существует!");
    }

    /* Тестируем метод *.findPlaceById() условного уровня сервисов */

    @Test
    void shouldReturnFindPlaceIfExist_findPlaceByIdTest() {
        testPlace.setPlaceId(generateId);
        when(placeRepository.findPlaceById(generateId)).thenReturn(Optional.of(testPlace));
        assertThat(placeService.findPlaceById(generateId)).isEqualTo(testPlace);
    }

    @Test
    void shouldReturnExceptionIfPlaceNotExist_findPlaceByIdTest() {
        when(placeRepository.findPlaceById(generateId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> placeService.findPlaceById(generateId))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("Место/зал с ID - " + generateId + " не найден!");
    }

    /* Тестируем метод *.findAllPlaces() условного уровня сервисов */

    @Test
    void shouldReturnPlaceList_findAllPlacesTest() {
        when(placeRepository.findAllPlaces()).thenReturn(testPlaceBase);
        assertThat(placeService.findAllPlaces().size()).isEqualTo(testPlaceBase.size());
    }

    @Test
    void shouldReturnException_findAllPlacesTest() {
        when(placeRepository.findAllPlaces()).thenReturn(emptyPlaceBase);
        assertThatThrownBy(() -> placeService.findAllPlaces())
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("База мест и залов пуста!");
    }

    /* Тестируем метод *.updatePlace() условного уровня сервисов */

    @Test
    void shouldReturnTrueIfUpdateSuccess_updatePlaceTest() {
        testPlace.setPlaceId(generateId);
        Place updateFor = new Place(generateId, Species.WORKPLACE, 45);
        when(placeRepository.findPlaceById(generateId)).thenReturn(Optional.of(testPlace));
        when(placeRepository.updatePlace(updateFor)).thenReturn(true);
        assertThat(placeService.updatePlace(updateFor)).isTrue();
    }

    @Test
    void shouldReturnExceptionTryUpdateNonExistPlace_updatePlaceTest() {
        Place updateFor = new Place(generateId, Species.WORKPLACE, 45);
        when(placeRepository.findPlaceById(generateId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> placeService.updatePlace(updateFor))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("'" + updateFor.getSpecies() + "' - " +
                                                updateFor.getPlaceNumber() +
                                                " нельзя обновить, т.к. место/зал не существует!");
    }

    /* Тестируем метод *.deletePlace() условного уровня сервисов */

    @Test
    void shouldReturnTrueIfDeleteExistentPlace_deletePlaceTest() {
        Long placeId = 1L;

        when(reservationRepository.findReservationByPlaceId(placeId))
                .thenReturn(Optional.of(emptyReservationBase));
        when(placeRepository.findPlaceById(placeId)).thenReturn(Optional.of(testPlace));
        when(placeRepository.deletePlace(placeId)).thenReturn(true);

        assertThat(placeService.deletePlace(placeId)).isTrue();
    }

    @Test
    void shouldReturnExceptionTryToDeleteNonExistentPlace_deletePlaceTest() {
        when(reservationRepository.findReservationByPlaceId(generateId))
                .thenReturn(Optional.of(emptyReservationBase));
        when(placeRepository.findPlaceById(generateId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeService.deletePlace(generateId))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("Удаление несуществующего места/зала невозможно!");
    }

    @Test
    void shouldReturnExceptionTryToDeletePlaceWithReservation_deletePlaceTest() {
        when(reservationRepository.findReservationByPlaceId(generateId))
                .thenReturn(Optional.of(testReservationBase));
        when(placeRepository.findPlaceById(generateId))
                .thenReturn(Optional.of(testPlace));

        assertThatThrownBy(() -> placeService.deletePlace(generateId))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("Удаление зарезервированного места/зала невозможно!");
    }

    /* Тестируем метод *.findPlaceBySpeciesAndNumber() условного уровня сервисов */

    @Test
    void shouldReturnPlace_findPlaceBySpeciesAndNumberTest() {
        when(placeRepository.findPlaceBySpeciesAndNumber(species, placeNumber))
                .thenReturn(Optional.of(testPlace));
        assertThat(placeService.findPlaceBySpeciesAndNumber(species, placeNumber))
                .isEqualTo(testPlace);
    }

    @Test
    void shouldReturnExceptionTryToFindNonExistentSpeciesOrNumber_findPlaceBySpeciesAndNumberTest() {
        when(placeRepository.findPlaceBySpeciesAndNumber(species, placeNumber))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> placeService.findPlaceBySpeciesAndNumber(species, placeNumber))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("'" + species.getStrName() + "' - " +
                        placeNumber +
                        " не существует!");
    }

    /* Тестируем метод *.findAllPlacesBySpecies() условного уровня сервисов */

    @Test
    void shouldReturnPlaceList_findAllPlacesBySpeciesTest() {
        when(placeRepository.findAllPlacesBySpecies(species)).thenReturn(Optional.of(testPlaceBase));
        assertThat(placeService.findAllPlacesBySpecies(species).size()).isEqualTo(testPlaceBase.size());
    }

    @Test
    void shouldReturnExceptionTryFindNonExistentSpecies_findAllPlacesBySpeciesTest() {
        when(placeRepository.findAllPlacesBySpecies(species)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> placeService.findAllPlacesBySpecies(species))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("Списка" + "'" + species.getStrName() + "' - " +
                                                " не существует!");
    }

    /* Тестируем метод *.isPlaceExist() условного уровня сервисов */

    @Test
    void shouldReturnTrue_isPlaceExistTest() {
        when(placeRepository.findPlaceBySpeciesAndNumber(species, placeNumber))
                .thenReturn(Optional.of(testPlace));
        assertThat(placeService.isPlaceExist(species, placeNumber)).isTrue();
    }

    @Test
    void shouldReturnFalseIfNotFindPlaceBySpeciesOrNumber_isPlaceExistTest() {
        when(placeRepository.findPlaceBySpeciesAndNumber(species, placeNumber))
                .thenReturn(Optional.empty());
        assertThat(placeService.isPlaceExist(species, placeNumber)).isFalse();
    }

    @Test
    void shouldReturnTrueIfFindPlaceById_isPlaceExistTest() {
        when(placeRepository.findPlaceById(generateId))
                .thenReturn(Optional.of(testPlace));
        assertThat(placeService.isPlaceExist(generateId)).isTrue();
    }

    @Test
    void shouldReturnFalseIfNotFindPlaceById_isPlaceExistTest() {
        when(placeRepository.findPlaceById(generateId))
                .thenReturn(Optional.empty());
        assertThat(placeService.isPlaceExist(generateId)).isFalse();
    }
}
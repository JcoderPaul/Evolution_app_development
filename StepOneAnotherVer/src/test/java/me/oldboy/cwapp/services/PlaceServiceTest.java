package me.oldboy.cwapp.services;

import me.oldboy.cwapp.entity.Place;
import me.oldboy.cwapp.entity.Reservation;
import me.oldboy.cwapp.entity.Species;
import me.oldboy.cwapp.exception.service_exception.PlaceServiceException;
import me.oldboy.cwapp.store.repository.PlaceRepository;
import me.oldboy.cwapp.store.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

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

    /* C - Метод addNewPlace - добавление нового Place в БД */

    @Test
    void addNewWorkplaceToPlaceBaseGoodTest(){
        when(placeRepository.findBySpeciesAndPlaceNumber(species, placeNumber))
                .thenReturn(Optional.empty());
        when(placeRepository.create(testPlace))
                .thenReturn(generateId);
        assertThat(placeService.addNewPlace(testPlace)).isEqualTo(generateId);
    }

    @Test
    void addNewDuplicatePlaceToPlaceBaseExceptionTest(){
        when(placeRepository.findBySpeciesAndPlaceNumber(species, placeNumber))
                .thenReturn(Optional.of(testPlace));
        assertThatThrownBy(()->placeService.addNewPlace(testPlace))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("Создание дубликата рабочего места в БД запрещено!");
    }

    /* R - Метод getAllPlaces - чтение всех Places не зависимо от species в БД */

    @Test
    void shouldReturnAllPlacesFromBaseTest() {
        when(placeRepository.findAll()).thenReturn(testPlaceBase);
        assertThat(placeService.getAllPlaces().size()).isEqualTo(3);
    }

    @Test
    void getAllPlacesFromEmptyBaseExceptionTest() {
        when(placeRepository.findAll()).thenReturn(emptyPlaceBase);
        assertThatThrownBy(()->placeService.getAllPlaces())
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("База рабочих мест и залов пуста!");
    }

    /* R - Метод getPlacesBySpeciesAndPlaceNumber - чтение Place по species и номеру Place */

    @Test
    void shouldReturnPlace_GetBySpeciesAndPlaceNumber_ExistPlaceTest() {
        when(placeRepository.findBySpeciesAndPlaceNumber(testPlace.getSpecies(),
                                                         testPlace.getPlaceNumber()))
                .thenReturn(Optional.of(testPlace));
        assertThat(placeService.getPlaceBySpeciesAndPlaceNumber(testPlace.getSpecies(),
                                                                testPlace.getPlaceNumber()))
                .isEqualTo(testPlace);
    }

    @Test
    void shouldReturnException_GetBySpeciesAndPlaceNumber_NonExistPlaceTest() {
        when(placeRepository.findBySpeciesAndPlaceNumber(testPlace.getSpecies(),
                                                         testPlace.getPlaceNumber()))
                .thenReturn(Optional.ofNullable(null));
        assertThatThrownBy(()->placeService.getPlaceBySpeciesAndPlaceNumber(testPlace.getSpecies(),
                                                                            testPlace.getPlaceNumber()))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining(testPlace.getSpecies() + " с ID - " +
                                                testPlace.getPlaceNumber() + " не существует!");
    }

    /* R - Метод getPlaceById - чтение Place по ID в БД */

    @Test
    void shouldReturnPlace_GetPlaceById_ExistPlaceTest() {
        Long placeId = 1L;
        when(placeRepository.findById(placeId)).thenReturn(Optional.of(testPlace));
        assertThat(placeService.getPlaceById(placeId)).isEqualTo(testPlace);
    }

    @Test
    void shouldReturnException_GetPlaceById_NonExistPlaceTest() {
        Long placeId = 1L;
        when(placeRepository.findById(placeId)).thenReturn(Optional.empty());
        assertThatThrownBy(()->placeService.getPlaceById(placeId))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("Рабочее место/зал с ID - " + placeId + " не найден!");
    }

    /* R - Метод getPlaceById - чтение Place по виду (species) в БД */

    @Test
    void shouldReturnListOfPlaces_GetPlaceBySpecies_ExistPlacesTest() {
        when(placeRepository.findAllBySpecies(Species.HALL)).thenReturn(testPlaceBase);
        assertThat(placeService.getPlaceBySpecies(Species.HALL)).isEqualTo(testPlaceBase);
        assertThat(placeService.getPlaceBySpecies(Species.HALL).size()).isEqualTo(3);
    }

    @Test
    void shouldReturnException_GetPlaceBySpecies_NonExistPlaceOrSpeciesTest() {
        when(placeRepository.findAllBySpecies(species)).thenReturn(emptyPlaceBase);
        assertThatThrownBy(()->placeService.getPlaceBySpecies(species))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("В базе отсутствуют записи по любым: " + species + " !");
    }

    /* U - Метод updatePlace - обновление данных Place */

    @Test
    void shouldReturnUpdatedPlaces_UpdatePlace_ExistPlacesTest() {
        testPlace.setPlaceId(generateId);
        Place placeWithUpdatedInfo = new Place(generateId, Species.HALL, 5);
        when(placeRepository.findById(generateId)).thenReturn(Optional.of(testPlace));
        when(placeRepository.update(placeWithUpdatedInfo)).thenReturn(placeWithUpdatedInfo);
        assertThat(placeService.updatePlace(placeWithUpdatedInfo)).isEqualTo(placeWithUpdatedInfo);
    }

    @Test
    void shouldReturnException_UpdatePlace_NonExistPlaceTest() {
        testPlace.setPlaceId(generateId);
        when(placeRepository.findById(generateId)).thenReturn(Optional.empty());
        assertThatThrownBy(()->placeService.updatePlace(testPlace))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("ID - " + testPlace.getPlaceId() +
                                                " в базе не найден, обновление данных невозможно!");
    }

    /* D - Метод deletePlace - удаление найденного по ID Place в БД */

    @Test
    void shouldReturnTrue_DeleteExistPlaceGoodTest(){
        Long placeId = 1L;

        when(reservationRepository.findByPlaceId(placeId)).thenReturn(Optional.of(List.of()));
        when(placeRepository.findById(placeId)).thenReturn(Optional.of(testPlace));
        when(placeRepository.delete(placeId)).thenReturn(true);

        assertThat(placeService.deletePlace(placeId)).isTrue();
    }

    @Test
    void shouldReturnException_DeleteNonExistPlaceTest(){
        Long placeId = 1L;
        when(placeRepository.findById(placeId)).thenReturn(Optional.empty());
        assertThatThrownBy(()->placeService.deletePlace(placeId))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("Удаление несуществующего места/зала невозможно!");
    }

    @Test
    void shouldReturnException_DeleteExistButReservationPlaceTest(){
        Long placeId = 1L;

        when(reservationRepository.findByPlaceId(placeId)).thenReturn(Optional.of(testReservationBase));
        when(placeRepository.findById(placeId)).thenReturn(Optional.of(testPlace));

        assertThatThrownBy(()->placeService.deletePlace(placeId))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("Удаление зарезервированного места/зала невозможно!");
    }

    /* I - Метод isPlaceExist - проверяем по ID есть ли Place в БД */

    @Test
    void shouldReturnTrue_isPlaceExistFindBySpeciesAndPlaceNumberTest(){
        when(placeRepository.findBySpeciesAndPlaceNumber(any(Species.class), anyInt()))
                .thenReturn(Optional.of(testPlace));
        assertThat(placeService.isPlaceExist(testPlace.getSpecies(), testPlace.getPlaceNumber())).isTrue();
    }

    @Test
    void shouldReturnFalse_isPlaceNonExistentFindBySpeciesAndPlaceNumberTest(){
        when(placeRepository.findBySpeciesAndPlaceNumber(any(Species.class), anyInt()))
                .thenReturn(Optional.empty());
        assertThat(placeService.isPlaceExist(testPlace.getSpecies(), testPlace.getPlaceNumber())).isFalse();
    }

    @Test
    void shouldReturnTrue_isPlaceExistFindByPlaceIdTest(){
        when(placeRepository.findById(testPlace.getPlaceId()))
                .thenReturn(Optional.of(testPlace));
        assertThat(placeService.isPlaceExist(testPlace.getPlaceId())).isTrue();
    }

    @Test
    void shouldReturnFalse_isPlaceNonExistentFindByPlaceIdTest(){
        when(placeRepository.findById(testPlace.getPlaceId()))
                .thenReturn(Optional.empty());
        assertThat(placeService.isPlaceExist(testPlace.getPlaceId())).isFalse();
    }
}
package me.oldboy.integration.services;

import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.places.PlaceCreateDeleteDto;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.exception.place_exception.PlaceServiceException;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.entity.options.Species;
import me.oldboy.services.PlaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IT
class PlaceServiceIT extends TestContainerInit {

    @Autowired
    private PlaceService placeService;

    private PlaceCreateDeleteDto goodCreateDeleteDto, badCreateDeleteDto;
    private PlaceReadUpdateDto goodReadUpdateDto, badReadUpdateDto;
    private Long existId, nonExistentId;
    private Integer existNumber, nonExistentNumber;

    @BeforeEach
    void setUp(){
        existId = 1L;
        nonExistentId = 100L;

        existNumber = 1;
        nonExistentNumber = 100;

        goodCreateDeleteDto = PlaceCreateDeleteDto.builder()
                .species(Species.STUDIO)
                .placeNumber(existNumber)
                .build();
        badCreateDeleteDto = PlaceCreateDeleteDto.builder()
                .species(Species.HALL)
                .placeNumber(existNumber)
                .build();

        goodReadUpdateDto = PlaceReadUpdateDto.builder()
                .placeId(existId)
                .species(Species.STUDIO)
                .placeNumber(6)
                .build();
        badReadUpdateDto = PlaceReadUpdateDto.builder()
                .placeId(nonExistentId)
                .species(Species.WORKPLACE)
                .placeNumber(6)
                .build();
    }

    @Test
    void create_shouldReturnCreatedId_Test() {
        Long createdId = placeService.create(goodCreateDeleteDto);
        assertThat(createdId).isGreaterThan(9);
    }

    @Test
    void create_shouldReturnException_tryToDuplicatePlace_Test() {
        assertThatThrownBy(() -> placeService.create(badCreateDeleteDto))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining(badCreateDeleteDto.species().name() + " с " +
                        badCreateDeleteDto.placeNumber() + " уже существует!");
    }

    @Test
    void delete_shouldReturn_TrueAfterDelete_Test() {
        boolean isPlaceDelete = placeService.delete(existId);
        assertThat(isPlaceDelete).isTrue();
    }

    @Test
    void delete_shouldReturnException_haveNoId_Test() {
        assertThatThrownBy(() -> placeService.delete(nonExistentId))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("Рабочего места/зала с ID - " + nonExistentId + " не существует!");
    }

    @Test
    void update_shouldReturnTrue_afterUpdate_Test() {
        boolean isUpdated = placeService.update(goodReadUpdateDto);
        assertThat(isUpdated).isTrue();
    }

    @Test
    void update_shouldReturnFalse_afterUnSuccessUpdate_Test() {
        boolean isUpdated = placeService.update(badReadUpdateDto);
        assertThat(isUpdated).isFalse();
    }

    @Test
    void findById_shouldReturnOptionalDto_afterFindPlaceById_Test() {
        Optional<PlaceReadUpdateDto> mayBeFindDto = placeService.findById(existId);
        if(mayBeFindDto.isPresent()){
            assertThat(mayBeFindDto.get().placeId()).isEqualTo(existId);
        }
    }

    @Test
    void findById_shouldReturnOptionalEmpty_haveNoPlaceWithId_Test() {
        Optional<PlaceReadUpdateDto> mayBeFindDto = placeService.findById(nonExistentId);
        assertThat(mayBeFindDto.isEmpty()).isTrue();
    }

    @Test
    void findAll_shouldReturnAllPlacesList_Test() {
        List<PlaceReadUpdateDto> allPlace = placeService.findAll();
        assertThat(allPlace.size()).isGreaterThan(8);
    }

    @Test
    void findPlaceBySpeciesAndNumber_shouldReturnFindDto_Test() {
        Optional<PlaceReadUpdateDto> mayBePlace = placeService.findPlaceBySpeciesAndNumber(Species.HALL, existNumber);
        if(mayBePlace.isPresent()){
            assertThat(mayBePlace.get().placeNumber()).isEqualTo(existNumber);
            assertThat(mayBePlace.get().species()).isEqualTo(Species.HALL);
        }
    }

    @Test
    void findPlaceBySpeciesAndNumber_shouldReturnException_Test() {
        assertThatThrownBy(() -> placeService.findPlaceBySpeciesAndNumber(Species.HALL, nonExistentNumber))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("'" + Species.HALL.name() + "' - с номером '" + nonExistentNumber + "' не существует!");
    }

    @Test
    void findAllPlacesBySpecies_shouldReturnPlaceList_Test() {
        List<PlaceReadUpdateDto> findAllHalls = placeService.findAllPlacesBySpecies(Species.HALL);
        assertThat(findAllHalls.size()).isEqualTo(3);

        List<PlaceReadUpdateDto> findAllWorkPlace = placeService.findAllPlacesBySpecies(Species.WORKPLACE);
        assertThat(findAllWorkPlace.size()).isEqualTo(6);
    }

    @Test
    void findAllPlacesBySpecies_shouldReturnException_haveNoSpeciesInBase_Test() {
        assertThatThrownBy(() -> placeService.findAllPlacesBySpecies(Species.STUDIO))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("Списка для '" + Species.STUDIO.name() + "' не существует!");
    }

    @Test
    void isPlaceExist_shouldReturnTrue_Test() {
        boolean isPlaceExistAndTrue = placeService.isPlaceExist(existId);
        assertThat(isPlaceExistAndTrue).isTrue();
    }

    @Test
    void isPlaceExist_shouldReturnFalse_Test() {
        boolean isPlaceExistAndFalse = placeService.isPlaceExist(nonExistentId);
        assertThat(isPlaceExistAndFalse).isFalse();
    }

    @Test
    void IsPlaceExist_withSpeciesAndNumber_shouldReturnTrue_Test() {
        boolean isPlaceExistAndTrue = placeService.isPlaceExist(Species.HALL, existNumber);
        assertThat(isPlaceExistAndTrue).isTrue();
    }

    @Test
    void IsPlaceExist_withSpeciesAndNumber_shouldReturnFalse_Test() {
        boolean isPlaceExistAndFalse = placeService.isPlaceExist(Species.HALL, nonExistentNumber);
        assertThat(isPlaceExistAndFalse).isFalse();
    }
}
package me.oldboy.integration.services;

import me.oldboy.dto.places.PlaceCreateDeleteDto;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.exception.place_exception.PlaceServiceException;
import me.oldboy.integration.ITBaseStarter;
import me.oldboy.models.entity.options.Species;
import me.oldboy.services.PlaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaceServiceIT extends ITBaseStarter {

    @Autowired
    private PlaceService placeService;

    private PlaceCreateDeleteDto nonExistPlaceDto, existPlaceDto;
    private PlaceReadUpdateDto placeUpdateDto, placeNotCorrectUpdateDto, placeReadDto;
    private Long existId, nonExistentId;
    private Integer existNumber, nonExistentNumber;

    @BeforeEach
    void setUp() {
        existId = 1L;
        nonExistentId = 100L;

        existNumber = 5;
        nonExistentNumber = 100;

        nonExistPlaceDto = PlaceCreateDeleteDto.builder().species(Species.STUDIO).placeNumber(nonExistentNumber).build();
        existPlaceDto = PlaceCreateDeleteDto.builder().species(Species.WORKPLACE).placeNumber(existNumber).build();

        placeUpdateDto = PlaceReadUpdateDto.builder().placeId(existId).species(Species.STUDIO).placeNumber(nonExistentNumber).build();
        placeNotCorrectUpdateDto = PlaceReadUpdateDto.builder().placeId(nonExistentId).build();
    }

    @Test
    void create_shouldReturnGeneratedId_afterCreate_Test() {
        Long generatedId = placeService.create(nonExistPlaceDto);
        assertThat(generatedId).isGreaterThan(9);
    }

    @Test
    void create_shouldReturnException_tryDuplicatePlace_Test() {
        assertThatThrownBy(() -> placeService.create(existPlaceDto))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining(existPlaceDto.species().name() + " с " + existPlaceDto.placeNumber() + " уже существует!");
    }

    @Test
    void delete_shouldReturnTrue_afterDelete_Test() {
        assertThat(placeService.delete(existId)).isTrue();
    }

    @Test
    void delete_shouldReturnException_haveNoPlaceForRemove_Test() {
        assertThatThrownBy(() -> placeService.delete(nonExistentId))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("Рабочего места/зала с ID - " + nonExistentId + " не существует!");
    }

    @Test
    void update_shouldReturnTrue_afterUpdate_Test() {
        Optional<PlaceReadUpdateDto> placeReadBeforeUpdate = placeService.findById(existId);
        if (placeReadBeforeUpdate.isPresent()) {
            assertThat(placeService.update(placeUpdateDto)).isTrue();

            assertThat(placeReadBeforeUpdate.get().species()).isNotEqualTo(placeUpdateDto.species());
            assertThat(placeReadBeforeUpdate.get().placeNumber()).isNotEqualTo(placeUpdateDto.placeNumber());
        }

        Optional<PlaceReadUpdateDto> placeReadAfterUpdate = placeService.findById(existId);
        if (placeReadAfterUpdate.isPresent()) {
            assertThat(placeReadAfterUpdate.get().species()).isEqualTo(placeUpdateDto.species());
            assertThat(placeReadAfterUpdate.get().placeNumber()).isEqualTo(placeUpdateDto.placeNumber());
        }
    }

    @Test
    void update_shouldReturnFalse_haveNoPlaceForUpdate_Test() {
        assertThat(placeService.update(placeNotCorrectUpdateDto)).isFalse();
    }

    @Test
    void findById_shouldReturnFoundPlace_Test() {
        assertThat(placeService.findById(existId).isPresent()).isTrue();
    }

    @Test
    void findById_shouldReturnFalse_andOptionalEmpty_Test() {
        assertThat(placeService.findById(nonExistentId).isPresent()).isFalse();
    }

    @Test
    void findAll_shouldReturnBaseRecordList_Test() {
        assertThat(placeService.findAll().size()).isEqualTo(9);
    }

    @Test
    void findPlaceBySpeciesAndNumber_shouldReturnFoundPlace_Test() {
        assertThat(placeService.findPlaceBySpeciesAndNumber(Species.WORKPLACE, existNumber).isPresent()).isTrue();
    }

    @Test
    void findPlaceBySpeciesAndNumber_shouldReturnException_haveNoPlaceInBase_Test() {
        assertThatThrownBy(() -> placeService.findPlaceBySpeciesAndNumber(Species.STUDIO, nonExistentNumber))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("'" + Species.STUDIO.name() + "' - с номером '" + nonExistentNumber + "' не существует!");
    }

    @Test
    void findAllPlacesBySpecies_shouldReturnExistPlacesList_Test() {
        assertThat(placeService.findAllPlacesBySpecies(Species.WORKPLACE).size()).isEqualTo(6);
        assertThat(placeService.findAllPlacesBySpecies(Species.HALL).size()).isEqualTo(3);
    }

    @Test
    void findAllPlacesBySpecies_shouldReturnException_haveNoRecordsForCurrentSpecies_Test() {
        assertThatThrownBy(() -> placeService.findAllPlacesBySpecies(Species.STUDIO))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("Списка для '" + Species.STUDIO + "' не существует!");
    }

    @Test
    void isPlaceExist_withTwoParam_shouldReturnTrue_Test() {
        assertThat(placeService.isPlaceExist(Species.WORKPLACE, existNumber)).isTrue();
    }

    @Test
    void isPlaceExist_withTwoParam_shouldReturnFalse_Test() {
        assertThat(placeService.isPlaceExist(Species.STUDIO, nonExistentNumber)).isFalse();
    }

    @Test
    void isPlaceExist_withOneParam_shouldReturnTrue_Test() {
        assertThat(placeService.isPlaceExist(existId)).isTrue();
    }

    @Test
    void isPlaceExist_withOneParam_shouldReturnFalse_Test() {
        assertThat(placeService.isPlaceExist(nonExistentId)).isFalse();
    }
}
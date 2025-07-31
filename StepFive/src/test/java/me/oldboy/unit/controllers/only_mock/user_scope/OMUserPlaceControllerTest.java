package me.oldboy.unit.controllers.only_mock.user_scope;

import me.oldboy.controllers.user_scope.UserPlaceController;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.exception.place_exception.PlaceControllerException;
import me.oldboy.models.entity.options.Species;
import me.oldboy.services.PlaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OMUserPlaceControllerTest {

    @Mock
    private PlaceService placeService;
    @InjectMocks
    private UserPlaceController placeController;

    private Long existId, nonExistentId;
    private Integer existNumber, nonExistentNumber;
    private PlaceReadUpdateDto placeReadDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existId = 1L;
        nonExistentId = 100L;

        existNumber = 1;
        nonExistentNumber = 100;

        placeReadDto = PlaceReadUpdateDto.builder().placeId(existId).placeNumber(existNumber).species(Species.HALL).build();
    }

    @Test
    void readPlaceById_shouldReturnResponseStatusOk_Test() {
        when(placeService.findById(existId)).thenReturn(Optional.of(placeReadDto));

        ResponseEntity<?> response = placeController.readPlaceById(existId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(placeReadDto);

        verify(placeService, times(1)).findById(anyLong());
    }

    @Test
    void readPlaceById_shouldReturnException_canNotFindPlaceId_Test() {
        when(placeService.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeController.readPlaceById(nonExistentId))
                .isInstanceOf(PlaceControllerException.class)
                .hasMessageContaining("Конференц-зала / рабочего места с ID: " + nonExistentNumber + " не существует!");

        verify(placeService, times(1)).findById(anyLong());
    }

    @Test
    void readPlaceBySpeciesAndNumber_shouldReturnFindPlace_Test() {
        when(placeService.findPlaceBySpeciesAndNumber(placeReadDto.species(), placeReadDto.placeNumber()))
                .thenReturn(Optional.of(placeReadDto));

        ResponseEntity<?> response =
                placeController.readPlaceBySpeciesAndNumber(placeReadDto.species().name(), placeReadDto.placeNumber());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(placeReadDto);

        verify(placeService, times(1)).findPlaceBySpeciesAndNumber(any(Species.class), anyInt());
    }

    @Test
    void getAllPlaces_shouldReturnDtoList_Test() {
        List<PlaceReadUpdateDto> testDtoDList =
                List.of(PlaceReadUpdateDto.builder().placeId(existId).build(),
                        PlaceReadUpdateDto.builder().placeId(nonExistentId).build());
        when(placeService.findAll()).thenReturn(testDtoDList);

        assertThat(placeController.getAllPlaces().size()).isEqualTo(testDtoDList.size());
    }
}
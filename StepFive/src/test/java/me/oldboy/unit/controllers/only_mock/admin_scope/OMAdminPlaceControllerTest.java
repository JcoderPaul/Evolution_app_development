package me.oldboy.unit.controllers.only_mock.admin_scope;

import me.oldboy.controllers.admin_scope.AdminPlaceController;
import me.oldboy.dto.places.PlaceCreateDeleteDto;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OMAdminPlaceControllerTest {

    @Mock
    private PlaceService placeService;
    @InjectMocks
    private AdminPlaceController placeController;

    private PlaceCreateDeleteDto placeCreateNewDto, placeDeleteDto;
    private PlaceReadUpdateDto placeReadDto, placeUpdateDto;
    private Long existId, nonExistId;
    private Integer existNumber, nonExistNumber;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existId = 1L;
        nonExistId = 100L;

        existNumber = 1;
        nonExistNumber = 100;

        placeCreateNewDto = PlaceCreateDeleteDto.builder().species(Species.HALL).placeNumber(nonExistNumber).build();
        placeReadDto = PlaceReadUpdateDto.builder().placeId(nonExistId).species(Species.HALL).placeNumber(nonExistNumber).build();
        placeUpdateDto = placeReadDto;
        placeDeleteDto = placeCreateNewDto;
    }

    /* ------ Тестируем метод *.createNewPlace() ------ */
    @Test
    void createNewPlace_shouldReturnCreatedPlace_whenPlaceDoesNotExist_Test() {
        /* Готовим данные */
        when(placeService.isPlaceExist(placeCreateNewDto.species(), placeCreateNewDto.placeNumber())).thenReturn(false);
        when(placeService.create(placeCreateNewDto)).thenReturn(nonExistId);
        when(placeService.findById(nonExistId)).thenReturn(Optional.of(placeReadDto));

        /* Вызываем метод */
        ResponseEntity<?> response = placeController.createNewPlace(placeCreateNewDto);

        /* Получаем ответ и сравниваем с ожидаемым */
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(placeReadDto);

        /* Верифицируем вызовы */
        verify(placeService, times(1)).isPlaceExist(any(Species.class), anyInt());
        verify(placeService, times(1)).create(any(PlaceCreateDeleteDto.class));
        verify(placeService, times(1)).findById(anyLong());
    }

    @Test
    void createNewPlace_shouldReturnException_tryToDuplicate_Test() {
        /* Готовим данные */
        when(placeService.isPlaceExist(placeCreateNewDto.species(), placeCreateNewDto.placeNumber())).thenReturn(true);

        assertThatThrownBy(() -> placeController.createNewPlace(placeCreateNewDto))
                .isInstanceOf(PlaceControllerException.class)
                .hasMessageContaining("Попытка создать дубликат рабочего места/зала!");

        /* Верифицируем вызовы */
        verify(placeService, times(1)).isPlaceExist(any(Species.class), anyInt());
    }

    /* ------ Тестируем метод *.updatePlace() ------ */
    @Test
    void updatePlace_shouldReturnOk_whenPlaceUpdatedSuccess_Test() {
        when(placeService.isPlaceExist(placeUpdateDto.placeId())).thenReturn(true);
        when(placeService.isPlaceExist(placeUpdateDto.species(), placeUpdateDto.placeNumber())).thenReturn(false);
        when(placeService.update(placeUpdateDto)).thenReturn(true);

        ResponseEntity<?> response = placeController.updatePlace(placeUpdateDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"message\" : \"Update success!\"}");

        verify(placeService, times(1)).isPlaceExist(any(Species.class), anyInt());
        verify(placeService, times(1)).isPlaceExist(anyLong());
        verify(placeService, times(1)).update(any(PlaceReadUpdateDto.class));
    }

    @Test
    void updatePlace_shouldReturnException_haveNoPlaceId_Test() {
        when(placeService.isPlaceExist(placeUpdateDto.placeId())).thenReturn(false);

        assertThatThrownBy(() -> placeController.updatePlace(placeUpdateDto))
                .isInstanceOf(PlaceControllerException.class)
                .hasMessageContaining("Место или зал для обновления не найдены!");

        verify(placeService, times(1)).isPlaceExist(anyLong());
    }

    @Test
    void updatePlace_shouldReturnException_duplicateData_Test() {
        when(placeService.isPlaceExist(placeUpdateDto.placeId())).thenReturn(true);
        when(placeService.isPlaceExist(placeUpdateDto.species(), placeUpdateDto.placeNumber())).thenReturn(true);

        assertThatThrownBy(() -> placeController.updatePlace(placeUpdateDto))
                .isInstanceOf(PlaceControllerException.class)
                .hasMessageContaining("Обновления приведут к дублированию данных!");

        verify(placeService, times(1)).isPlaceExist(anyLong());
        verify(placeService, times(1)).isPlaceExist(any(Species.class), anyInt());
    }

    @Test
    void updatePlace_shouldReturnException_unExpectedError_Test() {
        when(placeService.isPlaceExist(placeUpdateDto.placeId())).thenReturn(true);
        when(placeService.isPlaceExist(placeUpdateDto.species(), placeUpdateDto.placeNumber())).thenReturn(false);
        when(placeService.update(placeUpdateDto)).thenReturn(false);

        ResponseEntity<?> response = placeController.updatePlace(placeUpdateDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("{\"message\" : \"Update failed!\"}");

        verify(placeService, times(1)).isPlaceExist(any(Species.class), anyInt());
        verify(placeService, times(1)).isPlaceExist(anyLong());
        verify(placeService, times(1)).update(any(PlaceReadUpdateDto.class));
    }

    /* ------ Тестируем метод *.deletePlace() ------ */
    @Test
    void deletePlace_shouldReturnOk_afterDeleteExistPlace_Test() {
        when(placeService.findPlaceBySpeciesAndNumber(placeDeleteDto.species(), placeDeleteDto.placeNumber())).thenReturn(Optional.of(placeReadDto));
        when(placeService.delete(placeReadDto.placeId())).thenReturn(true);

        ResponseEntity<?> response = placeController.deletePlace(placeDeleteDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"message\" : \"Remove successful!\"}");

        verify(placeService, times(1)).findPlaceBySpeciesAndNumber(any(Species.class), anyInt());
        verify(placeService, times(1)).delete(anyLong());
    }

    @Test
    void deletePlace_shouldReturnBadRequest_unExpectedError_Test() {
        when(placeService.findPlaceBySpeciesAndNumber(placeDeleteDto.species(), placeDeleteDto.placeNumber())).thenReturn(Optional.of(placeReadDto));
        when(placeService.delete(placeReadDto.placeId())).thenReturn(false);

        ResponseEntity<?> response = placeController.deletePlace(placeDeleteDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("{\"message\" : \"Remove failed!\"}");

        verify(placeService, times(1)).findPlaceBySpeciesAndNumber(any(Species.class), anyInt());
        verify(placeService, times(1)).delete(anyLong());
    }
}
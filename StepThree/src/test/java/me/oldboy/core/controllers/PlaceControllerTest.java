package me.oldboy.core.controllers;

import me.oldboy.core.dto.places.PlaceCreateDeleteDto;
import me.oldboy.core.dto.places.PlaceReadUpdateDto;
import me.oldboy.core.model.database.entity.options.Species;
import me.oldboy.core.model.service.PlaceService;
import me.oldboy.exception.PlaceControllerException;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlaceControllerTest {

    @Mock
    private PlaceService placeService;
    @InjectMocks
    private PlaceController placeController;

    private static PlaceReadUpdateDto updateDto, readCreatedDto;
    private static PlaceCreateDeleteDto createDto, deleteDto;
    private static String testUserName;
    private static Long existPlaceId, nonExistentPlaceId;

    @BeforeAll
    public static void initParam(){
        existPlaceId = 10L;
        nonExistentPlaceId = 45L;
        createDto = new PlaceCreateDeleteDto(Species.HALL, 14);
        readCreatedDto = new PlaceReadUpdateDto(existPlaceId,
                createDto.species(),
                createDto.placeNumber());
        updateDto = new PlaceReadUpdateDto(existPlaceId,
                createDto.species(),
                createDto.placeNumber());
        deleteDto = new PlaceCreateDeleteDto(createDto.species(), createDto.placeNumber());
        testUserName = "Admin";
    }

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    /* Блок тестов - выделим каждую группу тестов в отдельный вложенный класс, для удобства и наглядности */

    /* Тестируем метод *.createNewPlace() */

    @Nested
    @DisplayName("1 - PlaceController class *.createNewPlace method tests")
    class CreateNewPlaceMethodTests {

        @Test
        void shouldReturnDtoIfPlaceIsCreated_createNewPlaceTest() {
            when(placeService.isPlaceExist(createDto.species(), createDto.placeNumber())).thenReturn(false);
            when(placeService.create(createDto)).thenReturn(readCreatedDto.placeId());
            when(placeService.findById(readCreatedDto.placeId())).thenReturn(Optional.of(readCreatedDto));

            try {
                assertThat(placeController.createNewPlace(createDto, testUserName)).isEqualTo(readCreatedDto);
            } catch (PlaceControllerException e) {
                throw new RuntimeException(e);
            }

            verify(placeService, times(1)).isPlaceExist(any(Species.class), anyInt());
            verify(placeService, times(1)).create(any(PlaceCreateDeleteDto.class));
            verify(placeService, times(1)).findById(anyLong());
        }

        @Test
        void shouldThrowExceptionIfCreatePlaceDuplicated_createNewPlaceTest() {
            when(placeService.isPlaceExist(createDto.species(), createDto.placeNumber())).thenReturn(true);

            assertThatThrownBy(() -> placeController.createNewPlace(createDto, testUserName))
                    .isInstanceOf(PlaceControllerException.class)
                    .hasMessageContaining("Try to create duplicate place! " +
                                                    "Попытка создать дубликат рабочего места/зала!");

            verify(placeService, times(1)).isPlaceExist(any(Species.class), anyInt());
        }
    }

    /* Тестируем метод *.readPlaceById() */

    @Nested
    @DisplayName("2 - PlaceController class *.readPlaceById method tests")
    class ReadPlaceByIdMethodTests {

        @Test
        void shouldReturnPlaceReadDtoIfExistIt_readPlaceByIdTest() {
            when(placeService.findById(existPlaceId)).thenReturn(Optional.of(readCreatedDto));

            try {
                assertThat(placeController.readPlaceById(existPlaceId)).isEqualTo(readCreatedDto);
            } catch (PlaceControllerException e) {
                throw new RuntimeException(e);
            }

            verify(placeService, times(1)).findById(anyLong());
        }

        @Test
        void shouldThrowExceptionReadNonExistentPlace_readPlaceByIdTest() {
            when(placeService.findById(nonExistentPlaceId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> placeController.readPlaceById(nonExistentPlaceId))
                    .isInstanceOf(PlaceControllerException.class)
                    .hasMessageContaining("Конференц-зала / рабочего места с ID: " + nonExistentPlaceId + " не существует!");

            verify(placeService, times(1)).findById(anyLong());
        }
    }

    /* Тестируем метод *.readPlaceBySpeciesAndNumber() */

    @Nested
    @DisplayName("3 - PlaceController class *.readPlaceBySpeciesAndNumber method tests")
    class ReadPlaceBySpeciesAndNumberMethodTests {

        @Test
        void shouldReturnReadDto_readPlaceBySpeciesAndNumber() {
            when(placeService.findPlaceBySpeciesAndNumber(createDto.species(), createDto.placeNumber())).thenReturn(Optional.of(readCreatedDto));

            try {
                assertThat(placeController.readPlaceBySpeciesAndNumber(createDto.species(), createDto.placeNumber())).isEqualTo(readCreatedDto);
            } catch (PlaceControllerException e) {
                throw new RuntimeException(e);
            }

            verify(placeService, times(1)).findPlaceBySpeciesAndNumber(any(Species.class), anyInt());
        }

        @Test
        void shouldThrowExceptionNotFindPlace_readPlaceBySpeciesAndNumber() {
            when(placeService.findPlaceBySpeciesAndNumber(createDto.species(), createDto.placeNumber())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> placeController.readPlaceBySpeciesAndNumber(createDto.species(), createDto.placeNumber()))
                    .isInstanceOf(PlaceControllerException.class)
                    .hasMessageContaining("Place not found! Рабочее место/зал не найдены!");

            verify(placeService, times(1)).findPlaceBySpeciesAndNumber(any(Species.class), anyInt());
        }
    }

    /* Тестируем метод *.getAllPlaces() */

    @Test
    @DisplayName("4 - PlaceController class *.getAllPlaces method test")
    void shouldReturnSizeOfListPlaceDto_getAllPlacesTest() {
        when(placeService.findAll()).thenReturn(List.of(new PlaceReadUpdateDto(3L, Species.HALL, 16),
                                                        new PlaceReadUpdateDto(4L, Species.WORKPLACE, 16)));

        assertThat(placeController.getAllPlaces().size()).isEqualTo(2);
    }

    /* Тестируем метод *.updatePlace() */

    @Nested
    @DisplayName("5 - PlaceController class *.updatePlace method tests")
    class UpdatePlaceMethodTests {

        @Test
        void shouldReturnTrueIfUpdateIsSuccess_updatePlaceTest() {
            when(placeService.isPlaceExist(existPlaceId)).thenReturn(true);
            when(placeService.isPlaceExist(updateDto.species(), updateDto.placeNumber())).thenReturn(false);
            when(placeService.update(updateDto)).thenReturn(true);

            try {
                assertThat(placeController.updatePlace(updateDto, testUserName)).isTrue();
            } catch (PlaceControllerException e) {
                throw new RuntimeException(e);
            }

            verify(placeService, times(1)).isPlaceExist(anyLong());
            verify(placeService, times(1)).isPlaceExist(any(Species.class), anyInt());
            verify(placeService, times(1)).update(any(PlaceReadUpdateDto.class));
        }

        @Test
        void shouldThrowExceptionIfNoPlaceForUpdate_updatePlaceTest() {
            when(placeService.isPlaceExist(existPlaceId)).thenReturn(false);

            assertThatThrownBy(() -> placeController.updatePlace(updateDto, testUserName))
                    .isInstanceOf(PlaceControllerException.class)
                    .hasMessageContaining("Have no place for update! Место или зал для обновления не найдены!");

            verify(placeService, times(1)).isPlaceExist(anyLong());
        }

        @Test
        void shouldThrowExceptionDuplicatePlaceAfterUpdate_updatePlaceTest() {
            when(placeService.isPlaceExist(existPlaceId)).thenReturn(true);
            when(placeService.isPlaceExist(updateDto.species(), updateDto.placeNumber())).thenReturn(true);

            assertThatThrownBy(() -> placeController.updatePlace(updateDto, testUserName))
                    .isInstanceOf(PlaceControllerException.class)
                    .hasMessageContaining("Updates will result in data duplication! Обновления приведут к дублированию данных!");

            verify(placeService, times(1)).isPlaceExist(anyLong());
            verify(placeService, times(1)).isPlaceExist(any(Species.class), anyInt());
        }
    }

    /* Тестируем метод *.deletePlace() */

    @Nested
    @DisplayName("6 - PlaceController class *.deletePlace method tests")
    class DeletePlaceMethodTests {

        @Test
        void shouldReturnTrueDeleteSuccess_deletePlace() {
            when(placeService.findPlaceBySpeciesAndNumber(deleteDto.species(), deleteDto.placeNumber()))
                    .thenReturn(Optional.of(readCreatedDto));
            when(placeService.delete(readCreatedDto.placeId())).thenReturn(true);

            try {
                assertThat(placeController.deletePlace(deleteDto, testUserName)).isTrue();
            } catch (PlaceControllerException e) {
                throw new RuntimeException(e);
            }

            verify(placeService, times(1)).findPlaceBySpeciesAndNumber(any(Species.class), anyInt());
            verify(placeService, times(1)).delete(anyLong());
        }

        @Test
        void shouldThrowExceptionHaveNoPlaceForDelete_deletePlace() {
            when(placeService.findPlaceBySpeciesAndNumber(deleteDto.species(), deleteDto.placeNumber()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> placeController.deletePlace(deleteDto, testUserName))
                    .isInstanceOf(PlaceControllerException.class)
                    .hasMessageContaining("Have no place to delete! Нет рабочего места/зала для удаления!");

            verify(placeService, times(1)).findPlaceBySpeciesAndNumber(any(Species.class), anyInt());
        }
    }
}
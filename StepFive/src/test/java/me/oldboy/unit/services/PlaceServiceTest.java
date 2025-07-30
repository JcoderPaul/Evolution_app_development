package me.oldboy.unit.services;

import me.oldboy.dto.places.PlaceCreateDeleteDto;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.exception.place_exception.PlaceServiceException;
import me.oldboy.mapper.PlaceMapper;
import me.oldboy.models.entity.Place;
import me.oldboy.models.entity.options.Species;
import me.oldboy.repository.PlaceRepository;
import me.oldboy.services.PlaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;
    @InjectMocks
    private PlaceService placeService;

    @Captor
    private ArgumentCaptor<Place> placeCaptor;

    private PlaceCreateDeleteDto createNormalDto, createDuplicateDto, deleteExistDto;
    private PlaceReadUpdateDto updatePlaceDto;
    private Place toUpdatePlace, capturedPlace, toFindPlace;
    private long existId, nonExistentId;
    private int existNumber, nonExistentNumber;
    private List<Place> toTestList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existId = 1L;
        nonExistentId = 100L;

        existNumber = 1;
        nonExistentNumber = 100;

        createNormalDto = PlaceCreateDeleteDto.builder().species(Species.STUDIO).placeNumber(nonExistentNumber).build();
        createDuplicateDto = createNormalDto;
        deleteExistDto = createNormalDto;

        updatePlaceDto = PlaceReadUpdateDto.builder().placeId(existId).species(Species.WORKPLACE).placeNumber(nonExistentNumber).build();
        toUpdatePlace = Place.builder().placeId(existId).species(Species.HALL).placeNumber(existNumber).build();
        toFindPlace = toUpdatePlace;

        toTestList = new ArrayList<>();
        toTestList.add(new Place());
        toTestList.add(new Place());
        toTestList.add(new Place());
    }

    @Test
    void create_shouldReturnCreatedPlaceId_Test() {
        Place toSavePLace = PlaceMapper.INSTANCE.mapToEntity(createNormalDto);
        toSavePLace.setPlaceId(nonExistentId);

        when(placeRepository.findBySpeciesAndNumber(createNormalDto.species().name(), createNormalDto.placeNumber()))
                .thenReturn(Optional.empty());
        when(placeRepository.save(PlaceMapper.INSTANCE.mapToEntity(createNormalDto)))
                .thenReturn(toSavePLace);

        assertThat(placeService.create(createNormalDto)).isEqualTo(nonExistentId);

        verify(placeRepository, times(1)).findBySpeciesAndNumber(anyString(), anyInt());
        verify(placeRepository, times(1)).save(any(Place.class));
    }

    @Test
    void create_shouldReturnException_duplicateCreatedPlace_Test() {
        Place toSaveDuplicatePLace = PlaceMapper.INSTANCE.mapToEntity(createDuplicateDto);

        when(placeRepository.findBySpeciesAndNumber(toSaveDuplicatePLace.getSpecies().name(), toSaveDuplicatePLace.getPlaceNumber()))
                .thenReturn(Optional.of(toSaveDuplicatePLace));

        assertThatThrownBy(() -> placeService.create(createDuplicateDto))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining(toSaveDuplicatePLace.getSpecies().name() +
                        " с " + toSaveDuplicatePLace.getPlaceNumber() + " уже существует!");

        verify(placeRepository, times(1)).findBySpeciesAndNumber(anyString(), anyInt());
    }

    @Test
    void delete_shouldReturnTrue_Test() {
        Place toDeletePlace = PlaceMapper.INSTANCE.mapToEntity(deleteExistDto);
        toDeletePlace.setPlaceId(existId);
        when(placeRepository.findById(existId)).thenReturn(Optional.of(toDeletePlace));

        assertThat(placeService.delete(toDeletePlace.getPlaceId())).isTrue();
    }

    @Test
    void delete_shouldReturnException_Test() {
        when(placeRepository.findById(existId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeService.delete(existId))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("Рабочего места/зала с ID - " + existId + " не существует!");
    }

    @Test
    void update_shouldReturnTrue_afterUpdate_Test() {
        when(placeRepository.findById(updatePlaceDto.placeId())).thenReturn(Optional.of(toUpdatePlace));

        assertThat(updatePlaceDto.placeNumber()).isNotEqualTo(toUpdatePlace.getPlaceNumber());
        assertThat(updatePlaceDto.species()).isNotEqualTo(toUpdatePlace.getSpecies());

        assertThat(placeService.update(updatePlaceDto)).isTrue();

        /* Перехватываем "прилет" и извлекаем данные */
        verify(placeRepository).save(placeCaptor.capture());
        capturedPlace = placeCaptor.getValue();

        assertThat(capturedPlace.getPlaceNumber()).isEqualTo(updatePlaceDto.placeNumber());
        assertThat(capturedPlace.getSpecies()).isEqualTo(updatePlaceDto.species());
    }

    @Test
    void update_shouldReturnOptionalEmpty_orFalse_haveNoPlaceToUpdate_Test() {
        when(placeRepository.findById(updatePlaceDto.placeId())).thenReturn(Optional.empty());
        assertThat(placeService.update(updatePlaceDto)).isFalse();
    }

    @Test
    void findById_shouldReturn_optionalDto_Test() {
        when(placeRepository.findById(existId)).thenReturn(Optional.of(toFindPlace));

        Optional<PlaceReadUpdateDto> mayBePlace = placeService.findById(existId);
        if (mayBePlace.isPresent()) {
            assertThat(mayBePlace.get().placeNumber()).isEqualTo(toFindPlace.getPlaceNumber());
            assertThat(mayBePlace.get().species()).isEqualTo(toFindPlace.getSpecies());
        }
    }

    @Test
    void findById_shouldReturnEmptyList_Test() {
        when(placeRepository.findById(existId)).thenReturn(Optional.empty());
        assertThat(placeService.findById(existId).isPresent()).isFalse();
    }

    @Test
    void findAll_shouldReturnDtoList_Test() {
        when(placeRepository.findAll()).thenReturn(toTestList);
        List<PlaceReadUpdateDto> testDtoList = placeService.findAll();

        assertThat(testDtoList.size()).isEqualTo(toTestList.size());
    }

    @Test
    void findPlaceBySpeciesAndNumber_shouldReturnOptionalDto_Test() {
        when(placeRepository.findBySpeciesAndNumber(toFindPlace.getSpecies().name(), toFindPlace.getPlaceNumber())).thenReturn(Optional.of(toFindPlace));
        Optional<PlaceReadUpdateDto> mayBeFound = placeService.findPlaceBySpeciesAndNumber(toFindPlace.getSpecies(), toFindPlace.getPlaceNumber());
        if(mayBeFound.isPresent()){
            assertThat(mayBeFound.get().species()).isEqualTo(toFindPlace.getSpecies());
            assertThat(mayBeFound.get().placeNumber()).isEqualTo(toFindPlace.getPlaceNumber());
        }
    }

    @Test
    void findPlaceBySpeciesAndNumber_shouldReturnException_Test() {
        when(placeRepository.findBySpeciesAndNumber(toFindPlace.getSpecies().name(), toFindPlace.getPlaceNumber())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> placeService.findPlaceBySpeciesAndNumber(toFindPlace.getSpecies(), toFindPlace.getPlaceNumber()))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("'" + toFindPlace.getSpecies().name() + "' - с номером '" + toFindPlace.getPlaceNumber() + "' не существует!");
    }

    @Test
    void findAllPlacesBySpecies_shouldReturnDtoList_Test() {
        when(placeRepository.findAllBySpecies(Species.HALL.name())).thenReturn(Optional.of(toTestList));
        assertThat(placeService.findAllPlacesBySpecies(Species.HALL).size()).isEqualTo(toTestList.size());
    }

    @Test
    void findAllPlacesBySpecies_shouldReturnException_Test() {
        Species controlSpecies =Species.HALL;

        when(placeRepository.findAllBySpecies(controlSpecies.name())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> placeService.findAllPlacesBySpecies(controlSpecies))
                .isInstanceOf(PlaceServiceException.class)
                .hasMessageContaining("Списка для '" + controlSpecies.name() + "' не существует!");
    }

    @Test
    void isPlaceExist_shouldReturnTrue_Test() {
        when(placeRepository.findBySpeciesAndNumber(Species.HALL.name(), existNumber)).thenReturn(Optional.of(toFindPlace));
        assertThat(placeService.isPlaceExist(Species.HALL, existNumber)).isTrue();
    }

    @Test
    void isPlaceExist_shouldReturnFalse_Test() {
        when(placeRepository.findBySpeciesAndNumber(Species.STUDIO.name(), nonExistentNumber)).thenReturn(Optional.empty());
        assertThat(placeService.isPlaceExist(Species.STUDIO, nonExistentNumber)).isFalse();
    }

    @Test
    void testFindBuyId_IsPlaceExist_shouldReturnTrue_Test() {
        when(placeRepository.findById(existId)).thenReturn(Optional.of(toFindPlace));
        assertThat(placeService.isPlaceExist(existId)).isTrue();
    }

    @Test
    void testFindBuyId_IsPlaceExist_shouldReturnFalse_Test() {
        when(placeRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        assertThat(placeService.isPlaceExist(nonExistentId)).isFalse();
    }
}
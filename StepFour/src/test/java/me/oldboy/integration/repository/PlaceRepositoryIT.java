package me.oldboy.integration.repository;

import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.entity.Place;
import me.oldboy.models.entity.options.Species;
import me.oldboy.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@IT
class PlaceRepositoryIT extends TestContainerInit {

    @Autowired
    private PlaceRepository placeRepository;
    private String existInBaseSpecies, anotherExistSpecies, nonExistentSpecies;
    private Integer existNumber, anotherExistNumber, nonExistentNumber;

    @BeforeEach
    void setUp(){
        existInBaseSpecies = Species.HALL.name();
        anotherExistSpecies = Species.WORKPLACE.name();
        nonExistentSpecies = Species.STUDIO.name();

        existNumber = 1;
        anotherExistNumber = 5;
        nonExistentNumber = 100;
    }

    @Test
    void findBySpeciesAndNumber_correctCombination_shouldReturnPlace_Test() {
        Optional<Place> mayBePlace = placeRepository.findBySpeciesAndNumber(existInBaseSpecies, existNumber);
        if(mayBePlace.isPresent()){
            assertThat(mayBePlace.get().getSpecies().name()).isEqualTo(existInBaseSpecies);
            assertThat(mayBePlace.get().getPlaceNumber()).isEqualByComparingTo(existNumber);
        }
    }

    @Test
    void findBySpeciesAndNumber_haveNoCurrentCombinationInBase_shouldReturnFalse_Test() {
        Optional<Place> mayBePlace = placeRepository.findBySpeciesAndNumber(existInBaseSpecies, anotherExistNumber);
        assertThat(mayBePlace.isPresent()).isFalse();
    }

    @Test
    void findAllBySpecies_shouldReturnListSizeOfCurrentSpecies_Test() {
        Optional<List<Place>> allHalls = placeRepository.findAllBySpecies(existInBaseSpecies);
        if(allHalls.isPresent()){
            assertThat(allHalls.get().size()).isEqualTo(3);
        }

        Optional<List<Place>> allWorkplace = placeRepository.findAllBySpecies(anotherExistSpecies);
        if(allWorkplace.isPresent()){
            assertThat(allWorkplace.get().size()).isEqualTo(6);
        }
    }

    @Test
    void findAllBySpecies_shouldReturnZeroListSize_Test() {
        Optional<List<Place>> allHalls = placeRepository.findAllBySpecies(nonExistentSpecies);
        if(allHalls.isPresent()){
            assertThat(allHalls.get().size()).isEqualTo(0);
        }
    }
}
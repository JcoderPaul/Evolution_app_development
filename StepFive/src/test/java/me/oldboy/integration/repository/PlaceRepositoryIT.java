package me.oldboy.integration.repository;

import me.oldboy.integration.ITBaseStarter;
import me.oldboy.models.entity.Place;
import me.oldboy.models.entity.options.Species;
import me.oldboy.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceRepositoryIT extends ITBaseStarter {

    @Autowired
    private PlaceRepository placeRepository;
    private Species testExistSpecies, testNonExistSpecies;
    private Integer existNumber, nonExistNumber;

    @BeforeEach
    void setUp() {
        testExistSpecies = Species.HALL;
        testNonExistSpecies = Species.STUDIO;

        existNumber = 1;
        nonExistNumber = 20;
    }

    @Test
    @Rollback(value = false)
    void findBySpeciesAndNumber_success_Test() {
        Optional<Place> mayBePlaceFound = placeRepository.findBySpeciesAndNumber(testExistSpecies.name(), existNumber);
        if (mayBePlaceFound.isPresent()) {
            assertThat(mayBePlaceFound.get().getSpecies()).isEqualTo(testExistSpecies);
            assertThat(mayBePlaceFound.get().getPlaceNumber()).isEqualTo(existNumber);
        }
    }

    @Test
    @Rollback(value = false)
    void findBySpeciesAndNumber_fail_Test() {
        Optional<Place> mayBePlaceFound = placeRepository.findBySpeciesAndNumber(testNonExistSpecies.name(), nonExistNumber);
        if (mayBePlaceFound.isEmpty()) {
            assertThat(mayBePlaceFound.isPresent()).isFalse();
        }
    }

    @Test
    void findAllBySpecies_createOptionalList_Test() {
        Optional<List<Place>> mayBeFoundList = placeRepository.findAllBySpecies(testExistSpecies.name());
        if (mayBeFoundList.isPresent()) {
            assertThat(mayBeFoundList.get().size()).isEqualTo(3);
        }
    }

    @Test
    void findAllBySpecies_createOptionalEmptyList_Test() {
        Optional<List<Place>> mayBeFoundList = placeRepository.findAllBySpecies(testNonExistSpecies.name());
        if (mayBeFoundList.isPresent()) {
            /* Да, список найденных будет, но, он будет пустым */
            assertThat(mayBeFoundList.get().size()).isEqualTo(0);
        }
    }
}
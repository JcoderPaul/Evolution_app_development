package me.oldboy.repository;

import me.oldboy.models.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query(value = "SELECT p.* " +
            "FROM coworking.places AS p " +
            "WHERE (p.species =:species " +
            "AND p.place_number =:number)",
            nativeQuery = true)
    public Optional<Place> findBySpeciesAndNumber(@Param("species") String species,
                                                  @Param("number") Integer number);

    @Query(value = "SELECT p.* " +
            "FROM coworking.places AS p " +
            "WHERE p.species =:species",
            nativeQuery = true)
    public Optional<List<Place>> findAllBySpecies(@Param("species") String species);
}
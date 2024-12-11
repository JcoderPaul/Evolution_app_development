package me.oldboy.core.model.service;

import me.oldboy.core.dto.places.PlaceCreateDeleteDto;
import me.oldboy.core.dto.places.PlaceReadUpdateDto;
import me.oldboy.exception.PlaceServiceException;
import me.oldboy.core.mapper.PlaceMapper;
import me.oldboy.core.model.database.entity.Place;
import me.oldboy.core.model.database.entity.options.Species;
import me.oldboy.core.model.database.repository.PlaceRepository;
import me.oldboy.core.model.database.repository.crud.RepositoryBase;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlaceService extends ServiceBase<Long, Place>{

    public PlaceService(RepositoryBase<Long, Place> repositoryBase) {
        super(repositoryBase);
    }

    /**
     * Create (save) new Place to base.
     *
     * @param placeCreateDto the new place for creating
     *
     * @return new created (and save to base) place ID
     */
    @Transactional
    public Long create(PlaceCreateDeleteDto placeCreateDto){
        Place forCreatePlace = PlaceMapper.INSTANCE.mapToEntity(placeCreateDto);
        return getRepositoryBase().create(forCreatePlace).getPlaceId();
    }

    /**
     * Delete (remove) Place from base by placeId.
     *
     * @param placeId the place's ID for delete
     *
     * @return true if place existed and deleted
     *         false if place not exist and can not be deleted
     */
    @Transactional
    public boolean delete(Long placeId) {
        Optional<Place> mayBePlace = getRepositoryBase().findById(placeId);
        mayBePlace.ifPresent(place -> getRepositoryBase().delete(place.getPlaceId()));
        return mayBePlace.isPresent();
    }

    /**
     * Update Place data.
     *
     * @param placeUpdateDto data for update
     *
     * @return true if place existed and update success
     *         false if place not exist and can not be updated
     */
    @Transactional
    public boolean update(PlaceReadUpdateDto placeUpdateDto) {
        /* Проверяем наличие записи в БД для изменения и получаем его*/
        Optional<Place> maybeUser = getRepositoryBase().findById(placeUpdateDto.placeId());

        maybeUser.ifPresent(place -> { place.setSpecies(placeUpdateDto.species());
                                       place.setPlaceNumber(placeUpdateDto.placeNumber());
                                       getRepositoryBase().update(place);});

        /* Подтверждаем обновление */
        return maybeUser.isPresent();
    }

    /**
     * Find place (PlaceReadDto) by ID.
     *
     * @param placeId place ID in DB
     *
     * @return PlaceReadDto if find it
     */
    @Transactional
    public Optional<PlaceReadUpdateDto> findById(Long placeId){
        return getRepositoryBase().findById(placeId).
                map(PlaceMapper.INSTANCE::mapToPlaceReadDto);
    }

    /**
     * Find all places from DB (PlaceReadDto).
     *
     * @return List of PlaceReadDto
     */
    @Transactional
    public List<PlaceReadUpdateDto> findAll(){
        return getRepositoryBase()
                .findAll()
                .stream()
                .map(PlaceMapper.INSTANCE::mapToPlaceReadDto)
                .collect(Collectors.toList());
    }

    /**
     * Find place (PlaceReadDto) by species (HALL / WORKPLACE) and number.
     *
     * @param species detail (HALL / WORKPLACE)
     * @param placeNumber number of place (not ID)
     *
     * @return PlaceReadDto if find it
     */
    @Transactional
    public Optional<PlaceReadUpdateDto> findPlaceBySpeciesAndNumber(Species species, Integer placeNumber){
        Optional<Place> mayBePlace =
                ((PlaceRepository) getRepositoryBase()).findPlaceBySpeciesAndNumber(species, placeNumber);
        if(mayBePlace.isEmpty()) {
            throw new PlaceServiceException("'" + species.name() + "' - с номером '" + placeNumber + "' не существует!");
        } else
            return Optional.ofNullable(PlaceMapper.INSTANCE.mapToPlaceReadDto(mayBePlace.get()));
    }

    /**
     * Find place (PlaceReadDto) by species (HALL / WORKPLACE).
     *
     * @param species detail (HALL / WORKPLACE)
     *
     * @return List of place (PlaceReadDto) by one concrete species
     */
    @Transactional
    public List<PlaceReadUpdateDto> findAllPlacesBySpecies(Species species){
        Optional<List<Place>> mayBeList =
                ((PlaceRepository) getRepositoryBase()).findAllPlacesBySpecies(species);
        if(mayBeList.isEmpty() || mayBeList.get().size() == 0){
            throw new PlaceServiceException("Списка для '" + species.name() + "' не существует!");
        } else
            return mayBeList.get().stream()
                    .map(place -> PlaceMapper.INSTANCE.mapToPlaceReadDto(place))
                    .collect(Collectors.toList());
    }

    @Transactional
    public boolean isPlaceExist(Species species, Integer placeNumber) {
        Optional<Place> isPlaceExist =
                ((PlaceRepository) getRepositoryBase()).findPlaceBySpeciesAndNumber(species, placeNumber);
        if(isPlaceExist.isPresent()){
            return true;
        } else
            return false;
    }

    public boolean isPlaceExist(Long placeId) {
        Optional<PlaceReadUpdateDto> isPlaceExist = findById(placeId);
        if(isPlaceExist.isEmpty()){
            return false;
        } else
            return true;
    }
}
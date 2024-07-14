package me.oldboy.cwapp.store.base;

import me.oldboy.cwapp.entity.Place;
import me.oldboy.cwapp.entity.Species;
import me.oldboy.cwapp.exception.base_exception.PlaceBaseException;
import me.oldboy.cwapp.store.repository.PlaceRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlaceBase implements PlaceRepository {

    private final Map<Long, Place> allPlaceBase = new HashMap<>();

    /**
     * Создает новый Place (конференц-зал или рабочее место).
     *
     * Генерация ID идет на старте исходя из размера БД,
     * затем исходя из максимального значения ID имеющегося в БД.
     *
     * @param place место Place (Hall или Workplace) для создания
     * @throws PlaceBaseException если произошла ошибка генерации ID
     * @throws PlaceBaseException если произошла ошибка создания нового Place
     * @return значение ID созданного Place
     */
    @Override
    public Long create(Place place) {
        Long generatedPlaceId = null;
        if(place.getPlaceId() == null && allPlaceBase.size() == 0){
            generatedPlaceId = 1L;
            place.setPlaceId(generatedPlaceId);
            allPlaceBase.put(generatedPlaceId, place);
        } else if(place.getPlaceId() == null && allPlaceBase.size() > 0){
            generatedPlaceId = 1L + allPlaceBase.keySet()
                                             .stream()
                                             .mapToLong(k->k)
                                             .max()
                                             .orElseThrow(() -> new PlaceBaseException("Сбой в генерации ID!"));
            place.setPlaceId(generatedPlaceId);
            allPlaceBase.put(generatedPlaceId, place);
        } else {
            throw new PlaceBaseException("Ошибка создания нового " + place.getSpecies() + " !");
        }
        return generatedPlaceId;
    }

    /**
     * Обновляет данные уже существующего в базе Place (конференц-зал или рабочее место).
     *
     * @param place место Place (Hall или Workplace) для изменения
     * @throws PlaceBaseException если произошла ошибка в ходе обновления данных по записи
     * @return обновленные данные Place
     */
    @Override
    public Place update(Place place) {
        if(allPlaceBase.containsKey(place.getPlaceId())) {
            delete(place.getPlaceId());
            allPlaceBase.put(place.getPlaceId(), place);
        } else {
            throw new PlaceBaseException("Вы пытаетесь обновить несуществующий " + place.getSpecies() + " !");
        }
        return allPlaceBase.get(place.getPlaceId());
    }

    /**
     * Ищет данные Place (конференц-зал или рабочее место) по его ID.
     *
     * @param placeId id искомого Place (Hall или Workplace)
     * @throws PlaceBaseException если ID в БД не найден
     * @return возможное значение Place с искомым ID или null если таковой не найден
     */
    @Override
    public Optional<Place> findById(Long placeId) {
        return Optional.ofNullable(allPlaceBase.get(placeId));
    }

    /**
     * Удаляет Place (конференц-зал или рабочее место) по его ID.
     *
     * @param placeId id удаляемого Place (Hall или Workplace)
     * @throws PlaceBaseException если ID в БД не найден
     * @return true - если Place с ID успешно удален, и false - если ID в базе не найдено
     */
    @Override
    public boolean delete(Long placeId) {
        if(allPlaceBase.containsKey(placeId)){
            allPlaceBase.remove(placeId);
        } else {
            throw new PlaceBaseException("Место/зал с ID: " + placeId + " в базе не найден!");
        }
        return true;
    }

    /**
     * Ищет данные Place (конференц-зал или рабочее место) по его виду и номеру.
     *
     * @param species вид искомого Place (Hall или Workplace)
     * @param placeNumber номер искомого Place (1, 2, 3 ... n)
     * @throws PlaceBaseException если Place искомого вида с искомым номером (например: Hall 3) в БД не найден
     * @return возможное значение Place искомого вида с искомым номером или null если таковой не найден
     */
    @Override
    public Optional<Place> findBySpeciesAndPlaceNumber(Species species, Integer placeNumber) {
        return Optional.ofNullable(allPlaceBase.entrySet().stream()
                .map(k -> k.getValue())
                .filter(u -> (u.getSpecies().equals(species) && u.getPlaceNumber().equals(placeNumber)))
                .findAny()
                .orElse(null));
    }

    /**
     * Ищет все Place в БД.
     *
     * @return возвращает список всех существующих Place в БД
     */
    @Override
    public List<Place> findAll() {
        return allPlaceBase.entrySet().stream()
                .map(k -> k.getValue())
                .collect(Collectors.toList());
    }

    /**
     * Ищет все Place (конференц-зал или рабочее место) только по его виду.
     *
     * @param species вид искомого Place (Hall или Workplace)
     * @return возвращает список всех Place искомого вида
     */
    @Override
    public List<Place> findAllBySpecies(Species species) {
        return allPlaceBase.entrySet().stream()
                .map(k -> k.getValue())
                .filter(p->p.getSpecies().equals(species))
                .collect(Collectors.toList());
    }

    public Map<Long, Place> getAllPlaceBase() {
        return allPlaceBase;
    }
}
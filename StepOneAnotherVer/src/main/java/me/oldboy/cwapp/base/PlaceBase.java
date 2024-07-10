package me.oldboy.cwapp.base;

import me.oldboy.cwapp.entity.Place;
import me.oldboy.cwapp.entity.Species;
import me.oldboy.cwapp.exception.PlaceBaseException;
import me.oldboy.cwapp.repository.PlaceRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlaceBase implements PlaceRepository {

    private Map<Long, Place> placeBase = new HashMap<>();

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
        if(place.getPlaceId() == null && placeBase.size() == 0){
            generatedPlaceId = 1L;
            place.setPlaceId(generatedPlaceId);
            placeBase.put(generatedPlaceId, place);
        } else if(place.getPlaceId() == null && placeBase.size() > 0){
            generatedPlaceId = 1L + placeBase.keySet()
                                             .stream()
                                             .mapToLong(k->k)
                                             .max()
                                             .orElseThrow(() -> new PlaceBaseException("Сбой в генерации ID!"));
            place.setPlaceId(generatedPlaceId);
            placeBase.put(generatedPlaceId, place);
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
        if(placeBase.containsKey(place.getPlaceId())) {
            delete(place.getPlaceId());
            placeBase.put(place.getPlaceId(), place);
        } else {
            throw new PlaceBaseException("Вы пытаетесь обновить несуществующий " + place.getSpecies() + " !");
        }
        return placeBase.get(place.getPlaceId());
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
        return Optional.ofNullable(placeBase.get(placeId));
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
        if(placeBase.containsKey(placeId)){
            placeBase.remove(placeId);
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
        return Optional.ofNullable(placeBase.entrySet().stream()
                .map(k -> k.getValue())
                .filter(u -> (u.getSpecies().equals(species) && u.getPlaceNumber().equals(placeNumber)))
                .findAny()
                .orElseThrow(() -> new PlaceBaseException(species + " с номером " +
                                                          placeNumber + " не найден!")));
    }

    /**
     * Ищет все Place в БД.
     *
     * @return возвращает список всех существующих Place в БД
     */
    @Override
    public List<Place> findAll() {
        return placeBase.entrySet().stream()
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
        return placeBase.entrySet().stream()
                .map(k -> k.getValue())
                .filter(p->p.getSpecies().equals(species))
                .collect(Collectors.toList());
    }

    public Map<Long, Place> getPlaceBase() {
        return placeBase;
    }
}
package me.oldboy.core.controllers;

import lombok.RequiredArgsConstructor;
import me.oldboy.annotations.Auditable;
import me.oldboy.annotations.Loggable;
import me.oldboy.core.dto.places.PlaceCreateDeleteDto;
import me.oldboy.core.dto.places.PlaceReadUpdateDto;
import me.oldboy.core.model.database.audit.operations.AuditOperationType;
import me.oldboy.core.model.database.entity.options.Species;
import me.oldboy.core.model.service.PlaceService;
import me.oldboy.exception.PlaceControllerException;
import me.oldboy.validate.ValidatorDto;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    /* С - CRUD создаем новое рабочее место / зал */

    /**
     * Create new place (HALL and PLACE).
     *
     * @param createDto input data for create new place (species and place number)
     * @throws PlaceControllerException if the user try made duplicate place
     *
     * @return place read DTO with place ID, species and place number
     */
    @Loggable
    @Auditable(operationType = AuditOperationType.CREATE_PLACE)
    public PlaceReadUpdateDto createNewPlace(PlaceCreateDeleteDto createDto, String userName) throws PlaceControllerException {
        /* Проверяем входящие данные */
        ValidatorDto.getInstance().isValidData(createDto);

        Long createPlaceId;
        boolean isPlaceExist = placeService.isPlaceExist(createDto.species(), createDto.placeNumber());
        if(isPlaceExist) {
                throw new PlaceControllerException("Try to create duplicate place! " +
                                                   "Попытка создать дубликат рабочего места/зала!");
        } else {
                createPlaceId = placeService.create(createDto);
        }
        return placeService.findById(createPlaceId).get();
    }

    /* R - CRUD читаем (получаем) рабочие места / залы */

    /**
     * Read existing place (HALL and WORKPLACE) / Извлекаем рабочее место или зал из БД
     *
     * @param placeId place ID for find / ID искомого места / зала
     * @throws PlaceControllerException if the place non exist / выбрасывается в случае отсутствия искомого ID в БД
     *
     * @return reading place
     */
    @Loggable
    public PlaceReadUpdateDto readPlaceById(Long placeId) throws PlaceControllerException {
        Optional<PlaceReadUpdateDto> mayBePlace = placeService.findById(placeId);
        if(mayBePlace.isEmpty()) {
            throw new PlaceControllerException("Конференц-зала / рабочего места с ID: " + placeId + " не существует!");
        } else
            return mayBePlace.get();
    }

    /**
     * Read existing place (HALL and WORKPLACE) / Извлекаем рабочее место или зал из БД
     *
     * @param species place species for find / вид - место / зал
     * @param placeNumber place number for find / номер искомого зала / места
     * @throws PlaceControllerException if the place non exist / выбрасывается в случае отсутствия искомых данных в БД
     *
     * @return reading place
     */
    @Loggable
    public PlaceReadUpdateDto readPlaceBySpeciesAndNumber(Species species, Integer placeNumber) throws PlaceControllerException {
        Optional<PlaceReadUpdateDto> mayBePlace = placeService.findPlaceBySpeciesAndNumber(species, placeNumber);
        if(mayBePlace.isEmpty()) {
            throw new PlaceControllerException("Place not found! Рабочее место/зал не найдены!");
        } else
            return mayBePlace.get();
    }

    /**
     * Show all available HALLs and WORKPLACEs / Просмотр всех доступных рабочих места и залов
     */
    @Loggable
    public List<PlaceReadUpdateDto> getAllPlaces() {
        return placeService.findAll();
    }

    /* U - CRUD обновляем данные по рабочему месту / залу */

    /**
     * Update existent place (HALL and PLACE).
     *
     * @param updateDto input place new data from update
     *
     * @return true - if update success
     *         false - if update fail
     */
    @Loggable
    @Auditable(operationType = AuditOperationType.UPDATE_PLACE)
    public boolean updatePlace(PlaceReadUpdateDto updateDto, String userName) throws PlaceControllerException {
        /* Проверяем входящие данные */
        ValidatorDto.getInstance().isValidData(updateDto);

        /* Проверяем наличие ID в БД, т.е. есть ли вообще запись для изменений */
        if (!placeService.isPlaceExist(updateDto.placeId())){
            throw new PlaceControllerException("Have no place for update! Место или зал для обновления не найдены!");
        }

        /* Проверяем не приведут ли обновляющие данные к дублированию полей записей в БД */
        if (placeService.isPlaceExist(updateDto.species(), updateDto.placeNumber())){
            throw new PlaceControllerException("Updates will result in data duplication! " +
                                               "Обновления приведут к дублированию данных!");
        }
        return placeService.update(updateDto);
    }

    /* D - CRUD удаляем данные о рабочем месте / зале из БД */

    @Loggable
    @Auditable(operationType = AuditOperationType.DELETE_PLACE)
    public boolean deletePlace(PlaceCreateDeleteDto deleteDto, String userName) throws PlaceControllerException {
        /* Проверяем входящие данные */
        ValidatorDto.getInstance().isValidData(deleteDto);

        Optional<PlaceReadUpdateDto> forDeletePlace =
                placeService.findPlaceBySpeciesAndNumber(deleteDto.species(), deleteDto.placeNumber());
        if(forDeletePlace.isEmpty()){
            throw new PlaceControllerException("Have no place to delete! Нет рабочего места/зала для удаления!");
        }
        return placeService.delete(forDeletePlace.get().placeId());
    }
}
package me.oldboy.controllers.admin_scope;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.dto.places.PlaceCreateDeleteDto;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.exception.place_exception.PlaceControllerException;
import me.oldboy.services.PlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/api/admin/places")
public class AdminPlaceController {

    @Autowired
    private PlaceService placeService;

    /* С - CRUD создаем новое рабочее место / зал */

    /**
     * Create new place (HALL and PLACE).
     *
     * @param createDto input data for create new place (species and place number)
     * @return place read DTO with place ID, species and place number
     * @throws PlaceControllerException if the user try made duplicate place
     */
//    @Auditable(operationType = AuditOperationType.CREATE_PLACE)
    @PostMapping("/create")
    public ResponseEntity<?> createNewPlace(@Validated
                                            @RequestBody
                                            PlaceCreateDeleteDto createDto) throws PlaceControllerException {
        Long createPlaceId;
        boolean isPlaceExist = placeService.isPlaceExist(createDto.species(), createDto.placeNumber());
        if (isPlaceExist) {
            throw new PlaceControllerException("Попытка создать дубликат рабочего места/зала!");
        } else {
            createPlaceId = placeService.create(createDto);
        }
        return ResponseEntity.ok().body(placeService.findById(createPlaceId).get());
    }

    /* U - CRUD обновляем данные по рабочему месту / залу */

    /**
     * Update existent place (HALL and PLACE).
     *
     * @param updateDto input place new data from update
     * @return true - if update success
     * false - if update fail
     */
//    @Auditable(operationType = AuditOperationType.UPDATE_PLACE)
    @PostMapping("/update")
    public ResponseEntity<?> updatePlace(@Validated
                                         @RequestBody
                                         PlaceReadUpdateDto updateDto) throws PlaceControllerException {
        /* Проверяем наличие ID в БД, т.е. есть ли вообще запись для изменений */
        if (!placeService.isPlaceExist(updateDto.placeId())) {
            throw new PlaceControllerException("Место или зал для обновления не найдены!");
        }

        /* Проверяем не приведут ли обновляющие данные к дублированию полей записей в БД */
        if (placeService.isPlaceExist(updateDto.species(), updateDto.placeNumber())) {
            throw new PlaceControllerException("Обновления приведут к дублированию данных!");
        }
        if (placeService.update(updateDto)) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("Обновление рабочего места прошло успешно!");
        } else {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("Обновить рабочее место не удалось!");
        }
    }

    /* D - CRUD удаляем данные о рабочем месте / зале из БД */

    //    @Auditable(operationType = AuditOperationType.DELETE_PLACE)
    @PostMapping("/delete")
    public ResponseEntity<?> deletePlace(@Validated
                                         @RequestBody
                                         PlaceCreateDeleteDto deleteDto) throws PlaceControllerException {
        Optional<PlaceReadUpdateDto> forDeletePlace =
                placeService.findPlaceBySpeciesAndNumber(deleteDto.species(), deleteDto.placeNumber());

        if (placeService.delete(forDeletePlace.get().placeId())) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("Удаление рабочего места прошло успешно!");
        } else {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("Удаление рабочее место не удалось!");
        }
    }
}
package me.oldboy.controllers.user_scope;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.exception.place_exception.PlaceControllerException;
import me.oldboy.models.entity.options.Species;
import me.oldboy.services.PlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/api/places")
public class UserPlaceController {

    @Autowired
    private PlaceService placeService;

    /* R - CRUD читаем (получаем) рабочие места / залы */

    /**
     * Read existing place (HALL and WORKPLACE) / Извлекаем рабочее место или зал из БД
     *
     * @param placeId place ID for find / ID искомого места / зала
     * @return reading place
     * @throws PlaceControllerException if the place non exist / выбрасывается в случае отсутствия искомого ID в БД
     */
    @GetMapping("/{placeId}")
    public ResponseEntity<?> readPlaceById(@PathVariable("placeId") Long placeId) throws PlaceControllerException {
        Optional<PlaceReadUpdateDto> mayBePlace = placeService.findById(placeId);
        if (mayBePlace.isEmpty()) {
            throw new PlaceControllerException("Конференц-зала / рабочего места с ID: " + placeId + " не существует!");
        } else
            return ResponseEntity.ok().body(mayBePlace.get());
    }

    /**
     * Read existing place (HALL and WORKPLACE) / Извлекаем рабочее место или зал из БД
     *
     * @param species     place species for find / вид - место / зал
     * @param placeNumber place number for find / номер искомого зала / места
     * @return reading place
     * @throws PlaceControllerException if the place non exist / выбрасывается в случае отсутствия искомых данных в БД
     */
    @GetMapping("/species/{species}/number/{placeNumber}")
    public ResponseEntity<?> readPlaceBySpeciesAndNumber(@PathVariable("species")
                                                         String species,
                                                         @PathVariable("placeNumber")
                                                         Integer placeNumber) throws PlaceControllerException {
        Optional<PlaceReadUpdateDto> mayBePlace = placeService.findPlaceBySpeciesAndNumber(Species.valueOf(species), placeNumber);
        return ResponseEntity.ok().body(mayBePlace.get());
    }

    /**
     * Show all available HALLs and WORKPLACEs / Просмотр всех доступных рабочих места и залов
     */
    @GetMapping()
    public List<PlaceReadUpdateDto> getAllPlaces() {
        return placeService.findAll();
    }
}
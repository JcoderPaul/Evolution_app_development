package me.oldboy.integration.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.reservations.ReservationCreateDto;
import me.oldboy.dto.reservations.ReservationReadDto;
import me.oldboy.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.services.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class ReservationControllerIT extends TestContainerInit {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private WebApplicationContext appContext;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Long existId, nonExistId;
    private ReservationCreateDto newCreateDto, notValidCreateDto,
            notExistUserIdCreateDto, notExistSlotIdCreateDto,
            notExistPlaceIdCreateDto, duplicateCreateDto;
    private ReservationUpdateDeleteDto newUpdateDto, notValidUpdateDto,
            updateNotOwnerTryTo, updateNoReservationIdTry,
            newDeleteDto, notValidDeleteDto,
            deleteNotOwnerTryTo, deleteNoReservationIdTry;

    private LocalDate existDateInBase, nonExistDateInBase;

    @BeforeEach
    void setUp() {
        existId = 1L;
        nonExistId = 100L;

        existDateInBase = LocalDate.of(2029, 7, 28);
        nonExistDateInBase = LocalDate.of(2031, 9, 10);

        mockMvc = MockMvcBuilders.webAppContextSetup(appContext)
                .apply(springSecurity())
                .build();

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        newCreateDto = ReservationCreateDto.builder()
                .reservationDate(nonExistDateInBase)
                .userId(existId)
                .placeId(existId + 1)
                .slotId(existId + 2)
                .build();
        notValidCreateDto = ReservationCreateDto.builder()
                .reservationDate(LocalDate.of(1814, 9, 10))
                .userId(-existId)
                .placeId(-existId + 1)
                .build();
        duplicateCreateDto = ReservationCreateDto.builder()
                .reservationDate(existDateInBase)
                .userId(existId)
                .placeId(existId)
                .slotId(existId)
                .build();
        notExistUserIdCreateDto = ReservationCreateDto.builder()
                .reservationDate(nonExistDateInBase)
                .userId(nonExistId)
                .placeId(existId)
                .slotId(existId)
                .build();
        notExistSlotIdCreateDto = ReservationCreateDto.builder()
                .reservationDate(nonExistDateInBase)
                .userId(existId)
                .placeId(existId)
                .slotId(nonExistId)
                .build();
        notExistPlaceIdCreateDto = ReservationCreateDto.builder()
                .reservationDate(nonExistDateInBase)
                .userId(existId)
                .placeId(nonExistId)
                .slotId(existId)
                .build();

        newUpdateDto = ReservationUpdateDeleteDto.builder()
                .reservationId(existId + 3)    // На старте мы будем тестировать нормальное обновление брони "хозяином" оной, в БД это запись ID 4 и 5, см. далее
                .reservationDate(nonExistDateInBase)
                .userId(existId + 1)    // Т.е. записи для user-a с ID = 2, при таком варианте если в тесте @WithUserDetails(value = "User"), бронь обновится.
                .placeId(existId + 1)
                .slotId(existId + 1)
                .build();
        updateNotOwnerTryTo = ReservationUpdateDeleteDto.builder()
                .reservationId(existId + 5)    // Теперь зададим бронь "не хозяйскую" в БД это любые записи кроме reservationID 4 и 5, см. далее
                .reservationDate(nonExistDateInBase)
                .userId(existId + 1)    // Данная запись соответствует user-у с ID = 3, а не ID = 2, если в тесте @WithUserDetails(value = "User"), будет брошено исключение.
                .placeId(existId + 1)
                .slotId(existId + 1)
                .build();
        updateNoReservationIdTry = ReservationUpdateDeleteDto.builder()
                .reservationId(nonExistId)    // Теперь зададим не существующую бронь
                .reservationDate(nonExistDateInBase)
                .userId(existId)
                .placeId(existId)
                .slotId(existId)
                .build();
        notValidUpdateDto = ReservationUpdateDeleteDto.builder() // Тестируем валидацию при обновлении
                .reservationId(existId)
                .reservationDate(LocalDate.of(1432, 12, 12))    // Теперь зададим дату из прошлого
                .userId(-existId)   // И отрицательный ID
                .placeId(existId)
                .slotId(existId)
                .build();

        /* Удаление происходит не по reservationID, по сочетанию: даты, слота времени и места - смоделируем это (см. записи в БД) */
        newDeleteDto = ReservationUpdateDeleteDto.builder()
                .reservationId(existId + 3)
                .reservationDate(LocalDate.of(2029, 07, 28))
                .userId(existId + 1)
                .placeId(existId + 4)
                .slotId(existId + 5)
                .build();
        deleteNotOwnerTryTo = ReservationUpdateDeleteDto.builder()
                .reservationId(existId + 5)    // Теперь зададим бронь "не хозяйскую"
                .reservationDate(LocalDate.of(2029, 07, 29))
                .userId(existId)
                .placeId(existId)
                .slotId(existId + 5)
                .build();
        deleteNoReservationIdTry = updateNoReservationIdTry;
        notValidDeleteDto = notValidUpdateDto;
    }

    /* --- Тестируем метод *.createReservation() --- */
    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void createReservation_shouldReturnCreatedDto_Test() {
        String forCreateSlot = objectMapper.writeValueAsString(newCreateDto);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forCreateSlot))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        ReservationReadDto reservationReadDto = objectMapper.readValue(strReturn, ReservationReadDto.class);

        assertThat(reservationReadDto.reservationDate()).isEqualTo(newCreateDto.getReservationDate());
        /* Помним, что в методе, при бронировании, мы устанавливаем ID "залогиненного" пользователя, а не "абы какой прилетел" в CreateDto */
        assertThat(reservationReadDto.reservationId()).isNotEqualTo(newCreateDto.getUserId());
        assertThat(reservationReadDto.placeId()).isEqualTo(newCreateDto.getPlaceId());
        assertThat(reservationReadDto.slotId()).isEqualTo(newCreateDto.getSlotId());
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void createReservation_shouldReturnValidationError_Test() {
        String forCreateSlot = objectMapper.writeValueAsString(notValidCreateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forCreateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"slotId\" : \"Slot ID date can not be blank or negative\"")))
                .andExpect(content().string(containsString("\"reservationDate\" : \"Reservation date must be a date in the present or in the future\"")))
                .andExpect(content().string(containsString("\"userId\" : \"ID must be greater than or equal to 0\"")));
    }

    /*
        В рамках следующих 4-х тестов, опосредовано, проверим работу private методов которые
        бросают исключение в случае не корректных данных. Сделаем мы это один раз в рамках
        тестирования *.createReservation() метода.
    */

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void createReservation_shouldReturnException_nonExistentUserId_Test() {
        String forCreateSlot = objectMapper.writeValueAsString(notExistUserIdCreateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forCreateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Применен несуществующий идентификатор пользователя!")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void createReservation_shouldReturnException_nonExistentPlaceId_Test() {
        String forCreateSlot = objectMapper.writeValueAsString(notExistPlaceIdCreateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forCreateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Попытка использовать несуществующее место/зал!")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void createReservation_shouldReturnException_nonExistentSlotId_Test() {
        String forCreateSlot = objectMapper.writeValueAsString(notExistSlotIdCreateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forCreateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Попытка использовать несуществующий слот времени!")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void createReservation_shouldReturnException_duplicateReservation_Test() {
        String forCreateSlot = objectMapper.writeValueAsString(duplicateCreateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forCreateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Дублирование брони!")));
    }

    /* --- Тестируем метод *.updateReservation() --- */
    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void updateReservation_shouldReturnOk_reservationOwnerUpdateData_Test() {
        /* Достаем данные из БД перед обновлением */
        Optional<ReservationReadDto> resBeforeUpdate = reservationService.findById(newUpdateDto.reservationId());

        String forUpdateSlot = objectMapper.writeValueAsString(newUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdateSlot))
                .andExpect(status().isOk())
                .andExpect(content().string("Бронь обновлена"));

        /* Достаем данные после обновления */
        Optional<ReservationReadDto> resAfterUpdate = reservationService.findById(newUpdateDto.reservationId());

        /* Сравниваем, что было с тем что стало */
        if (resBeforeUpdate.isPresent() && resAfterUpdate.isPresent()) {
            /* Проверяем несоответствие старых данных с обновленными */
            assertThat(resBeforeUpdate.get().reservationDate()).isNotEqualTo(resAfterUpdate.get().reservationDate());
            assertThat(resBeforeUpdate.get().slotId()).isNotEqualTo(resAfterUpdate.get().slotId());
            assertThat(resBeforeUpdate.get().placeId()).isNotEqualTo(resAfterUpdate.get().placeId());

            /* Но userID остался прежним */
            assertThat(resBeforeUpdate.get().userId()).isEqualTo(resAfterUpdate.get().userId());

            /* Поверяем соответствие обновленных данных из БД с теми, что были переданы в метод */
            assertThat(resAfterUpdate.get().reservationDate()).isEqualTo(newUpdateDto.reservationDate());
            assertThat(resAfterUpdate.get().placeId()).isEqualTo(newUpdateDto.placeId());
            assertThat(resAfterUpdate.get().slotId()).isEqualTo(newUpdateDto.slotId());
        }
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void updateReservation_shouldReturnException_notOwnerTryToUpdateData_Test() {
        String forUpdateSlot = objectMapper.writeValueAsString(updateNotOwnerTryTo);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Недостаточно прав на обновление или удаление брони!")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "Admin", userDetailsServiceBeanName = "clientDetailsService")
        // Задаем ADMIN user-a для прохождения теста
    void updateReservation_shouldReturnOk_adminTryToUpdateData_Test() {
        String forUpdateSlot = objectMapper.writeValueAsString(updateNotOwnerTryTo);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdateSlot))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Бронь обновлена")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "Admin", userDetailsServiceBeanName = "clientDetailsService")
    void updateReservation_shouldReturnException_tryToUpdateNonExistRecord_Test() {
        String forUpdateSlot = objectMapper.writeValueAsString(updateNoReservationIdTry);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Бронь для обновления или удаления не найдена!")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "Admin", userDetailsServiceBeanName = "clientDetailsService")
    void updateReservation_shouldReturnValidationError_Test() {
        String forUpdateSlot = objectMapper.writeValueAsString(notValidUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"reservationDate\" : \"must be a date in the present or in the future\"")))
                .andExpect(content().string(containsString("\"userId\" : \"must be greater than or equal to 0\"")));
    }

    /* --- Тестируем метод *.deleteReservation() --- */
    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void deleteReservation_shouldReturnOk_ownerCanDeleteHisRecord_Test() {
        /* Достаем данные из БД перед обновлением */
        Optional<ReservationReadDto> resBeforeDelete = reservationService.findById(newDeleteDto.reservationId());

        String forDeleteSlot = objectMapper.writeValueAsString(newDeleteDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forDeleteSlot))
                .andExpect(status().isOk())
                .andExpect(content().string("Бронь удалена"));

        /* Достаем данные после обновления */
        Optional<ReservationReadDto> resAfterDelete = reservationService.findById(newDeleteDto.reservationId());

        /* Сравниваем, что было с тем что стало */
        assertThat(resBeforeDelete.isPresent()).isTrue();
        assertThat(resAfterDelete.isPresent()).isFalse();
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void deleteReservation_shouldReturnBadRequest_nonOwnerCanNotDeleteRecord_Test() {
        String forDeleteSlot = objectMapper.writeValueAsString(deleteNotOwnerTryTo);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forDeleteSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Недостаточно прав на обновление или удаление брони!")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "Admin", userDetailsServiceBeanName = "clientDetailsService")
    void deleteReservation_shouldReturnOk_adminCanDeleteNonOwnerRecord_Test() {
        String forDeleteSlot = objectMapper.writeValueAsString(deleteNotOwnerTryTo);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forDeleteSlot))
                .andExpect(status().isOk())
                .andExpect(content().string("Бронь удалена"));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "Admin", userDetailsServiceBeanName = "clientDetailsService")
    void deleteReservation_shouldReturnBadRequest_deleteNonExistentRecord_Test() {
        String forDeleteSlot = objectMapper.writeValueAsString(deleteNoReservationIdTry);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forDeleteSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Бронь для обновления или удаления не найдена!")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "Admin", userDetailsServiceBeanName = "clientDetailsService")
    void deleteReservation_shouldReturnValidationError_Test() {
        String forDeleteSlot = objectMapper.writeValueAsString(notValidDeleteDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forDeleteSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"reservationDate\" : \"must be a date in the present or in the future\"")))
                .andExpect(content().string(containsString("\"userId\" : \"must be greater than or equal to 0\"")));
    }

    /* --- Тестируем метод *.readAllReservation() --- */
    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void readAllReservation() {
        MvcResult result = mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        List<ReservationReadDto> reservationDtoList = objectMapper.readValue(strReturn, new TypeReference<List<ReservationReadDto>>() {
        });

        assertThat(reservationDtoList).isNotNull();
        assertThat(reservationDtoList.size()).isGreaterThan(7);
    }

    /* --- Тестируем метод *.getReservationByParam() --- */
    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void getReservationByParam_shouldReturnOk_dtoListFoundByDate_Test() {
        MvcResult result = mockMvc.perform(get("/api/reservations/booked")
                        .param("reservationDate", "2029-07-28"))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        List<ReservationReadDto> reservationDtoList = objectMapper.readValue(strReturn, new TypeReference<List<ReservationReadDto>>() {
        });

        assertThat(reservationDtoList).isNotNull();
        assertThat(reservationDtoList.size()).isGreaterThan(3);
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void getReservationByParam_shouldReturnOk_dtoListFoundByUserId_Test() {
        MvcResult result = mockMvc.perform(get("/api/reservations/booked")
                        .param("userId", String.valueOf(existId)))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        List<ReservationReadDto> reservationDtoList = objectMapper.readValue(strReturn, new TypeReference<List<ReservationReadDto>>() {
        });

        assertThat(reservationDtoList).isNotNull();
        assertThat(reservationDtoList.size()).isGreaterThan(2);
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void getReservationByParam_shouldReturnOk_dtoListFoundByPlaceId_Test() {
        MvcResult result = mockMvc.perform(get("/api/reservations/booked")
                        .param("placeId", String.valueOf(existId)))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        List<ReservationReadDto> reservationDtoList = objectMapper.readValue(strReturn, new TypeReference<List<ReservationReadDto>>() {});

        assertThat(reservationDtoList).isNotNull();
        assertThat(reservationDtoList.size()).isGreaterThan(2);
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void getReservationByParam_shouldReturnException_canNotWorkWithMultiParam_Test() {
        mockMvc.perform(get("/api/reservations/booked")
                        .param("placeId", String.valueOf(existId))
                        .param("userId", String.valueOf(existId)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Parse or unexpected error (check the entered parameters): " +
                        "Неверное сочетание параметров (достаточно одного параметра, комбинация не принимается)!")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void getReservationByParam_shouldReturnException_haveNoPlaceIdInBase_Test() {
        mockMvc.perform(get("/api/reservations/booked")
                        .param("placeId", String.valueOf(nonExistId)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Parse or unexpected error (check the entered parameters): " +
                        "Введенный параметр не найден в БД или отрицательный!")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void getReservationByParam_shouldReturnException_haveNoUserIdInBase_Test() {
        mockMvc.perform(get("/api/reservations/booked")
                        .param("userId", String.valueOf(nonExistId)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Parse or unexpected error (check the entered parameters): " +
                        "Введенный параметр не найден в БД или отрицательный!")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void getReservationByParam_shouldReturnException_negativePlaceId_Test() {
        mockMvc.perform(get("/api/reservations/booked")
                        .param("placeId", String.valueOf(- existId)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Parse or unexpected error (check the entered parameters): " +
                        "Введенный параметр не найден в БД или отрицательный!")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void getReservationByParam_shouldReturnException_negativeUserId_Test() {
        mockMvc.perform(get("/api/reservations/booked")
                        .param("userId", String.valueOf(- existId)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Parse or unexpected error (check the entered parameters): " +
                        "Введенный параметр не найден в БД или отрицательный!")));
    }

    /* --- Тестируем метод *.getFreeSlotsByDate() --- */
    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void getFreeSlotsByDate() {
        MvcResult result = mockMvc.perform(get("/api/reservations/free/date/{date}", existDateInBase))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        Map<Long, List<Long>> reservationDtoMap = objectMapper.readValue(strReturn, new TypeReference<Map<Long, List<Long>>>() {});

        assertThat(reservationDtoMap).isNotNull();
        assertThat(reservationDtoMap.size()).isGreaterThan(8);
        /*
            Мы помним, что сепарация была проведена по placeId, т.к. одно место на конкретную дату,
            можно зарезервировать ограниченное количество слотов (временных диапазонов). Нам известно,
            что на выбранную дату placeId = 1 (у нас это existId) было зарезервировано 2-а раза, т.е.
            freeReservationListSize может быть равен - 7 свободных слотов, извлекаем, проверяем.
        */
        assertThat(reservationDtoMap.get(existId).size()).isEqualTo(7);
    }
}
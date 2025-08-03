package me.oldboy.integration.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.config.security_details.ClientDetailsService;
import me.oldboy.dto.reservations.ReservationCreateDto;
import me.oldboy.dto.reservations.ReservationReadDto;
import me.oldboy.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.integration.ITBaseStarter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithUserDetails(value = "Admin", userDetailsServiceBeanName = "clientDetailsService")
class ReservationControllerIT extends ITBaseStarter {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClientDetailsService detailsService;

    private ObjectMapper objectMapper;
    private ReservationCreateDto reservationCreateDto, reservationCreateNoValidDto;
    private ReservationUpdateDeleteDto reservationUpdateDto, reservationUpdateNotValidDto,
            reservationDeleteDto, reservationDeleteNotValidDto;
    private Long existId, nonExistentId;
    private Integer existNumber;
    private LocalDate reservationDate, validReservationDate;
    private String enteredLogin, enteredPassword, stringValidDate, stringNotValidDate;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        existId = 1L;
        nonExistentId = 100L;

        existNumber = 10;

        enteredLogin = "Duglas Lind";
        enteredPassword = "1234";
        stringValidDate = "2029-12-03";
        stringNotValidDate = "12-03-2029";

        reservationDate = LocalDate.of(2031, 04, 02);
        validReservationDate = LocalDate.of(2029, 07, 29);

        reservationCreateDto = ReservationCreateDto.builder().reservationDate(reservationDate).userId(existId).placeId(existId).slotId(existId).build();
        reservationCreateNoValidDto = ReservationCreateDto.builder().userId(-existId).placeId(-existId).slotId(-existId).build();

        reservationUpdateDto = ReservationUpdateDeleteDto.builder()
                .reservationId(existId)
                .reservationDate(reservationDate)
                .userId(existId + 1)
                .slotId(existId + 1)
                .placeId(existId + 1)
                .build();
        reservationUpdateNotValidDto = ReservationUpdateDeleteDto.builder().userId(-existId).slotId(-existId).placeId(-existId).build();

        reservationDeleteDto = ReservationUpdateDeleteDto.builder()
                .reservationId(existId + 4)
                .reservationDate(validReservationDate)
                .userId(existId + 1)
                .slotId(existId + 5)
                .placeId(existId + 4)
                .build();
        reservationDeleteNotValidDto = reservationUpdateNotValidDto;
    }

    /* ----- Тестируем *.createReservation() ----- */
    @Test
    @SneakyThrows
    void createReservation_shouldReturnCreatedReservation_Test() {
        String strDtoForCreate = objectMapper.writeValueAsString(reservationCreateDto);

        MvcResult result = mockMvc.perform(post("/api/reservations/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForCreate))
                .andExpect(status().isOk())
                .andReturn();

        /* Получим результат запроса, обработаем до нужного формата и сравним с исходным */
        String strReturn = result.getResponse().getContentAsString();
        ReservationReadDto savedDto = objectMapper.readValue(strReturn, ReservationReadDto.class);

        assertAll(
                () -> assertThat(savedDto.reservationDate()).isEqualTo(reservationCreateDto.getReservationDate()),
                () -> assertThat(savedDto.slotId()).isEqualTo(reservationCreateDto.getSlotId()),
                () -> assertThat(savedDto.placeId()).isEqualTo(reservationCreateDto.getPlaceId()),
                /*
                    Нужно помнить, что в методе мы извлекаем userId залогиненного
                    пользователя из Security контекста и именно его передаем на
                    сохранение.
                */
                () -> assertThat(savedDto.userId()).isEqualTo(reservationCreateDto.getUserId())
        );
    }

    @Test
    @SneakyThrows
    void createReservation_shouldReturnValidationError_Test() {
        String strDtoForCreate = objectMapper.writeValueAsString(reservationCreateNoValidDto);

        mockMvc.perform(post("/api/reservations/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForCreate))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Place ID must be greater than or equal to 0")))
                .andExpect(content().string(containsString("Slot ID must be greater than or equal to 0")))
                .andExpect(content().string(containsString("Reservation date can not be blank or null")))
                .andExpect(content().string(containsString("User ID must be greater than or equal to 0")));
    }

    /* ----- Тестируем *.updateReservation() ----- */
    @Test
    @SneakyThrows
    void updateReservation_shouldReturnStatusOk_andSuccessMsg_Test() {
        String strDtoForUpdate = objectMapper.writeValueAsString(reservationUpdateDto);

        mockMvc.perform(post("/api/reservations/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\": \"Reservation updated\"}"));
    }

    @Test
    @SneakyThrows
    void updateReservation_shouldReturnValidationError_Test() {
        String strDtoForUpdate = objectMapper.writeValueAsString(reservationUpdateNotValidDto);

        mockMvc.perform(post("/api/reservations/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Place ID must be greater than or equal to 0")))
                .andExpect(content().string(containsString("Slot ID must be greater than or equal to 0")))
                .andExpect(content().string(containsString("Reservation date can not be blank or null")))
                .andExpect(content().string(containsString("User ID must be greater than or equal to 0")));
    }

    /* ----- Тестируем *.deleteReservation() ----- */
    @Test
    @SneakyThrows
    void deleteReservation_shouldReturnStatusOk_andSuccessMsg_Test() {
        String strDtoForDelete = objectMapper.writeValueAsString(reservationDeleteDto);

        mockMvc.perform(post("/api/reservations/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForDelete))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\": \"Reservation removed\"}"));
    }

    @Test
    @SneakyThrows
    void deleteReservation_shouldReturnValidationError_Test() {
        String strDtoForDelete = objectMapper.writeValueAsString(reservationDeleteNotValidDto);

        mockMvc.perform(post("/api/reservations/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForDelete))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Place ID must be greater than or equal to 0")))
                .andExpect(content().string(containsString("Slot ID must be greater than or equal to 0")))
                .andExpect(content().string(containsString("Reservation date can not be blank or null")))
                .andExpect(content().string(containsString("User ID must be greater than or equal to 0")));
    }

    /* ----- Тестируем *.readAllReservation() ----- */
    @Test
    @SneakyThrows
    void readAllReservation_shouldReturnListOfReservation_Test() {
        MvcResult result = mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<ReservationReadDto> listFromResponse = objectMapper.readValue(strResult, new TypeReference<List<ReservationReadDto>>() {
        });

        assertThat(listFromResponse.size()).isEqualTo(8);
    }

    /* ----- Тестируем *.getReservationByParam() ----- */
    @Test
    @SneakyThrows
    void getReservationByParam_shouldReturnReservationList_foundByDate_Test() {
        String formattedDate = validReservationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        MvcResult result = mockMvc.perform(get("/api/reservations/booked")
                        .param("reservationDate", formattedDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<ReservationReadDto> listFromResponse = objectMapper.readValue(strResult, new TypeReference<List<ReservationReadDto>>() {
        });

        assertThat(listFromResponse.size()).isEqualTo(4);
    }

    @Test
    @SneakyThrows
    void getReservationByParam_shouldReturnReservationList_foundByUserId_Test() {
        MvcResult result = mockMvc.perform(get("/api/reservations/booked")
                        .param("userId", String.valueOf(existId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<ReservationReadDto> listFromResponse = objectMapper.readValue(strResult, new TypeReference<List<ReservationReadDto>>() {
        });

        assertThat(listFromResponse.size()).isEqualTo(3);
    }

    @Test
    @SneakyThrows
    void getReservationByParam_shouldReturnReservationList_foundByPlaceId_Test() {
        MvcResult result = mockMvc.perform(get("/api/reservations/booked")
                        .param("placeId", String.valueOf(existId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<ReservationReadDto> listFromResponse = objectMapper.readValue(strResult, new TypeReference<List<ReservationReadDto>>() {
        });

        assertThat(listFromResponse.size()).isEqualTo(3);
    }

    @Test
    @SneakyThrows
    void getReservationByParam_shouldReturnException_moreThenOneParamOnRequest_Test() {
        mockMvc.perform(get("/api/reservations/booked")
                        .param("placeId", String.valueOf(existId))
                        .param("userId", String.valueOf(existId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json("{\"exceptionMsg\":\"Parse or unexpected error (check the entered parameters): " +
                                "Неверное сочетание параметров (достаточно одного параметра, комбинация не принимается)!\"}"));
    }

    @Test
    @SneakyThrows
    void getReservationByParam_shouldReturnException_notCorrectUserId_Test() {
        mockMvc.perform(get("/api/reservations/booked")
                        .param("userId", String.valueOf(-existId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json("{\"exceptionMsg\":\"Parse or unexpected error (check the entered parameters): " +
                                "Введенный параметр не найден в БД или отрицательный!\"}"));
    }

    @Test
    @SneakyThrows
    void getReservationByParam_shouldReturnException_notCorrectPlaceId_Test() {
        mockMvc.perform(get("/api/reservations/booked")
                        .param("placeId", String.valueOf(-existId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json("{\"exceptionMsg\":\"Parse or unexpected error (check the entered parameters): " +
                                "Введенный параметр не найден в БД или отрицательный!\"}"));
    }

    /* ----- Тестируем *.getFreeSlotsByDate() ----- */
    @Test
    @SneakyThrows
    void getFreeSlotsByDate() {
        String formattedDate = validReservationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        MvcResult result = mockMvc.perform(get("/api/reservations/free/date/{date}", formattedDate))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        Map<Long, List<Long>> mapFromResponse = objectMapper.readValue(strResult, new TypeReference<Map<Long, List<Long>>>() {
        });

        /* Общее количество мест/залов */
        assertThat(mapFromResponse.size()).isEqualTo(9);

        AtomicInteger count = new AtomicInteger();
        mapFromResponse.forEach((k, v) -> count.addAndGet(v.size()));

        assertThat(count.get()).isEqualTo(77);
    }
}
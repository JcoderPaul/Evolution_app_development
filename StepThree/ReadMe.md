### StepThree ([Servlet API](https://tomcat.apache.org/tomcat-5.5-doc/servletapi/index.html) и [AOP](https://en.wikipedia.org/wiki/AspectJ))

Основная задача проекта: Разработать приложение для управления коворкинг-пространством. Приложение должно позволять 
пользователям бронировать рабочие места, конференц-залы, а также управлять бронированиями и просматривать доступность 
ресурсов.

Т.е. все то, что было описано и проделано в:
- [StepOne](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne);
- [StepTwo](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo);

Чтобы не прыгать по описаниям вспомним, что на первом шаге мы использовали только возможности JAVA. На втором шаге 
мы добавили новые технологии (но, данные все еще возвращались в консоль и взаимодействие происходило через CLI):
- [JDBC](https://github.com/JcoderPaul/JDBC_Practice);
- Миграционный фреймворк для БД - [Liquibase](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_14);
- Docker для БД ([описание использования и примеры для Spring Boot](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_13), отличается от текущего);
- Test-containers ([описание использования и примеры для Spring Boot](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_13#lesson-65---%D1%82%D0%B5%D1%81%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5-postgresql-%D0%B1%D0%B4-%D1%80%D0%B0%D0%B7%D0%B2%D0%B5%D1%80%D0%BD%D1%83%D1%82%D0%BE%D0%B9-%D0%B2-docker-%D0%BA%D0%BE%D0%BD%D1%82%D0%B5%D0%B9%D0%BD%D0%B5%D1%80%D0%B5), реализация отличается от текущего);

#### Функциональные и технические требования предыдущего шага: 
- Репозитории должны писать ВСЕ сущности в БД [PostgreSQL](https://www.postgresql.org/) ([документация](https://www.postgresql.org/docs/));
- Идентификаторы при сохранении в БД должны выдаваться через [sequence](https://www.postgresql.org/docs/current/sql-createsequence.html);
- [DDL-скрипты](https://www.geeksforgeeks.org/sql-ddl-dql-dml-dcl-tcl-commands/) на создание таблиц и скрипты на предзаполнение таблиц должны выполняться только инструментом миграции [Liquibase](https://docs.liquibase.com/) ([Liquibase GitHub](https://github.com/liquibase));
- Скрипты миграции Liquibase должны быть написаны в нотации XML или YAML (SQL);
- Скриптов миграции должно быть несколько:
  - Создание всех таблиц;
  - Предзаполнение данными;
- Служебные таблицы должны быть в отдельной схеме;
- Таблицы сущностей хранить в схеме public запрещено (создать свою схему);
- В тестах необходимо использовать [test-containers](https://java.testcontainers.org/) ([short tutorial](https://www.baeldung.com/docker-test-containers));
- В приложении должен быть [docker-compose](https://docs.docker.com/compose/).yml:
  - В котором должны быть прописаны инструкции для развертывания [postgres БД в докере](https://hub.docker.com/_/postgres); 
  - Логин, пароль к БД должны быть отличными от тех, что прописаны в образе по-умолчанию;
  - Приложение должно работать с БД, развернутой в докере с указанными параметрами;
- Приложение должно поддерживать конфиг-файлы: 
  - Всё, что относится к подключению БД;
  - Настройки и инструкции к миграциям, должно быть сконфигурировано через конфиг-файл;

#### Текущий проект должен содержать и реализовывать:
- Взаимодействие с БД идет по средствам [Hibernate](https://hibernate.org/) ([Hibernate Tutorial](https://www.geeksforgeeks.org/hibernate-tutorial/));
- Взаимодействие с приложением должно осуществляться через отправку HTTP запросов;
- При запросах и ответах сервлеты ([servlets](https://en.wikipedia.org/wiki/Jakarta_Servlet)) должны принимать [JSON](https://ru.wikipedia.org/wiki/JSON) и возвращать JSON;
- Для сериализации и десериализации необходимо использовать библиотеку [Jackson](https://en.wikipedia.org/wiki/Jackson_(API)) [(статьи)](https://www.baeldung.com/jackson);
- Использовать понятное названия эндпоинтов (endpoint);
- Возвращать разные статус-коды ([HTTP-коды](https://ru.wikipedia.org/wiki/%D0%A1%D0%BF%D0%B8%D1%81%D0%BE%D0%BA_%D0%BA%D0%BE%D0%B4%D0%BE%D0%B2_%D1%81%D0%BE%D1%81%D1%82%D0%BE%D1%8F%D0%BD%D0%B8%D1%8F_HTTP));
- Добавить [DTO](https://www.baeldung.com/java-dto-pattern);
- Для маппинга сущностей в [DTO](https://www.baeldung.com/java-dto-pattern) использовать [MapStruct](https://mapstruct.org/documentation/installation/) ([Quick Guide](https://www.baeldung.com/mapstruct));
- Реализовать [валидацию входящих DTO](https://docs.oracle.com/javaee/7/api/javax/validation/package-summary.html);
- Реализовать аудит ключевых действий пользователя через [AOP](https://en.wikipedia.org/wiki/AspectJ) ([введение в AspectJ](https://www.baeldung.com/aspectj));
- Реализовать через AOP [логирование](https://docs.oracle.com/en/java/javase/11/core/java-logging-overview.html#GUID-B83B652C-17EA-48D9-93D2-563AE1FF8EDA) выполнения ключевых методов (с замером времени выполнения) ([short intro](https://www.baeldung.com/java-logging-intro));
- Покрыть сервлеты тестами (спорный момент);

________________________________________________________________________________________________________________________
#### Реализация:
- Функциональные требования выполнены полностью.
- Технические требования выполнены полностью.

________________________________________________________________________________________________________________________
#### Особенности:
Данная версия приложения взаимодействует с пользователем (сервисом или другим приложением) по средствам HTTP-запросов и 
использует в своей реализации Servlet API. Значит нам нужен или сервер приложений ([Payara](https://www.payara.fish/), [GlassFish](https://glassfish.org/)) или 
контейнер сервлетов ([TomCat](https://tomcat.apache.org/)) для запуска нашего приложения. Используем TomCat. 
Скрипты Liquibase при первичном старте создадут необходимые предзаполненные таблицы.

Структура проекта (это не Spring проект, но слоистую структуру можно применять где угодно, отсюда и названия):
- [annotation](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/annotations) - аннотации для аудирования и логирования;
- [aspects](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/aspects) - аспектные классы (аудит, логирование, транзакции);
- [config](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/config):
  - [connection](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/config/connection) - папка содержит классы отвечающие за связь с БД;
  - [liquibase](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/config/liquibase) - папка с классом управляющим работой миграционного фреймворка Liquibase;
  - [util](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/config/util) - папка с конфигуратором приложения и Hibernate;
- [core](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/core):
  - [controllers](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/core/controllers) - классы управляющие манипуляцией с сущностями на 'верхнем' уровне приложения;
  - [dto](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/core/dto) - классы для передачи данных со слоя на слой;
  - [mapper](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/core/mapper) - классы сопоставители;
  - [model](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/core/model):
    - [criteria.expander](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/core/model/criteria/expander) - класс дополнение для формирования запросов при помощи Criteria API (см. ком-ии внутри класса); 
    - [database](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/core/model/database):
      - [audit](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/core/model/database/audit) - класс работающий с функционалом аудирования;
      - [entity](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/core/model/database/entity) - основные рабочие элементы проекта с которыми происходят манипуляции (залы/рабочие места, слоты времени для
        резервирования, пользователи, записи о резервировании содержащие сведения о том, что, когда, и на сколько было зарезервировано);
      - [repository](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/core/model/database/repository) - классы для взаимодействия с БД;
    - [service](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/core/model/service) - классы отвечающие за обработку запросов с уровня контроллеров;
- [exceptions](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/exception) - папка с исключениями;
- [filter](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/filter) - фильтр задающий кодировку при обращении к сервлетам;
- [security](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/security) - классы для работы с JWT аутентификацией;
- [servlets](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/servlets) - классы HTTP интерфейса;
- [validates](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/java/me/oldboy/validate) - класс валидатор (для проверки входящих DTO);
- [AppContextBuilder](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/src/main/java/me/oldboy/AppContextBuilder.java) - класс формирующий общий контекст приложения;

- [resources](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/resources):
  - [db/changelog](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/main/resources/db/changelog) - файлы миграции Liquibase;
  - [application.properties](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/src/main/resources/application.properties) - файл настроек (см. комментарии);
  - [hibernate.cfg.xml](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/src/main/resources/hibernate.cfg.xml) - укороченный конфигуратор Hibernate (основная нагрузка на Java конфигурации);
  - [log4j.xml](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/src/main/resources/log4j.xml) - конфигурация логера;

________________________________________________________________________________________________________________________
#### Тесты (247 шт.) согласно расчетам IDE покрывают:
  - Классы 92% (234/252);
  - Методы 84% (742/874);
  - Строки кода 73% (2122/2904);

Тестирование проводилось в двух вариантах: 
- тестирование с применением Test-containers ([слой репозиториев](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/test/java/me/oldboy/core/database/repository) и [слой сервисов](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/test/java/me/oldboy/core/model/service));
- тестирование с применением фреймворка Mockito и AssertJ ([слой контроллеров](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/test/java/me/oldboy/core/controllers) и [сервлеты](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/src/test/java/me/oldboy/servlets));

Наиболее интересными моментами в тестировании сервлетов является тот факт, что разработчики того же Mockito не рекомендуют
использовать заглушки на классы, которые мы (разработчики) сами не создавали (т.е. [не mock-ать чужие библиотеки](https://site.mockito.org/), возможно я
и ошибаюсь):
  
    Remember
    Do not mock types you don’t own
    Don’t mock value objects
    Don’t mock everything
    Show love with your tests!

Любопытным показался вариант использования метода doAnswer [см. тест](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/src/test/java/me/oldboy/servlets/reservation/ReservationManageServletTest.java).

________________________________________________________________________________________________________________________
### Применение StreamAPI при работе с коллекциями:

Во всех реализациях приложения есть задача вывода свободных слотов на конкретную дату, при это не уточняется в каком формате.
Мы решили выводить информацию в формате коллекции - на конкретную дату будет существовать список наших Places (рабочих мест и 
залов) и каждый из них будет иметь ограниченное количество слотов, которые можно забронировать (возможно останутся и свободные) - 
вот их мы и выводим. Если у Place (места/зала) нет резервирования на конкретную дату - все слоты свободны - логично. Реализацию
данного вывода берет на себя метод *.findAllFreeSlotsByDate(LocalDate date) класса ReservationService.java. И тут полет фантазии
на реализацию обширен, просто на коллекциях и циклах (многословно и запутанно) или с применением StreamAPI. 

Один из вариантов ([можно сравнить с текущей реализацией](https://github.com/JcoderPaul/Evolution_app_development/blob/5f92d5dc124623a0a48d4f0cf5014d6136f9f391/StepThree/src/main/java/me/oldboy/core/model/service/ReservationService.java#L236)):

    List<Reservation> reservationList =
                reservationRepository.findReservationByDate(LocalDate.of(2029, 7, 28)).get();

        Map<Long, List<Long>> allFreeSlotsByDay = new HashMap<>();

        List<Long> allAvailableSlot = slotRepository.findAll().stream()
                                                              .map(Slot::getSlotId)
                                                              .toList();

        var placeReservationMap=
                reservationList.stream()
                               .collect(Collectors.groupingBy(Reservation::getPlace))
                               .entrySet().stream()
                                          .collect(Collectors.toMap(eKey -> eKey.getKey().getPlaceId(),
                                                                    eValue -> eValue.getValue().stream()
                                                                                               .map(r -> r.getSlot().getSlotId())
                                                                                               .collect(Collectors.toList())));

        placeReservationMap.forEach((key, value) -> {
            List<Long> freeSlotByPlace = new ArrayList<>(allAvailableSlot);
            value.forEach(freeSlotByPlace::remove);
            allFreeSlotsByDay.put(key, freeSlotByPlace);
        });

        System.out.println(allFreeSlotsByDay);
________________________________________________________________________________________________________________________
#### Параметры запросов к приложению (API)

Адрес хоста стандартный для локальной машины (порт исходя из настроек TomCat): http://localhost:8081

Естественно при развертке приложения в контейнере, или в локальном TomCat, в полном адресе запроса может появиться имя 
приложения (папки где оно развернуто), например **cw** и тогда между адресом хоста и endpoint-ом появится дополнение, 
например:

    http://localhost:8081/cw/cw_api/v1/places/

**Не забываем про это!** 

#### Запросы для работы с залами и рабочими местами (Place entity):

| Тип запроса (Servlet метод) | Endpoint                    | Полный запрос (пример)                                             | Тело запроса                                                  | Описание запроса                                     |
|-----------------------------|-----------------------------|--------------------------------------------------------------------|---------------------------------------------------------------|------------------------------------------------------|
| GET (doGet)                 | /cw_api/v1/places/available | http://localhost:8081/cw_api/v1/places/available                   |                                                               | Получить список всех Place-ов (залов/рабочих мест)   |
| POST (doPost)               | /cw_api/v1/places/          | http://localhost:8081/cw_api/v1/places/                            | {"species": "HALL", "placeNumber": "5"}                       | Создать Place с заданными параметрами                |
| PUT (doPut)                 | /cw_api/v1/places/          | http://localhost:8081/cw_api/v1/places/                            | {"placeId":"13", "species": "WORKPLACE", "placeNumber": "65"} | Обновить выбранный Place (Место/Зал)                 |
| DELETE (doDelete)           | /cw_api/v1/places/          | http://localhost:8081/cw_api/v1/places/                            | {"species": "HALL", "placeNumber": "5"}                       | Удаление Place (Место/Зал)                           |
| GET (doGet)                 | /cw_api/v1/places/          | http://localhost:8081/cw_api/v1/places/?placeId=1                  |                                                               | Посмотреть данные на Place (Место/Зал) по ID         |
| GET (doGet)                 | /cw_api/v1/places/          | http://localhost:8081/cw_api/v1/places/?species=HALL&placeNumber=3 |                                                               | Посмотреть данные Place (Место/Зал) по виду и номеру |

#### Запросы для работы с залами и рабочими местами (Slot entity):

| Тип запроса (Servlet метод) | Endpoint                      | Полный запрос (пример)                                             | Тело запроса                                                                     | Описание запроса                                |
|-----------------------------|-------------------------------|--------------------------------------------------------------------|----------------------------------------------------------------------------------|-------------------------------------------------|
| GET (doGet)                 | /cw_api/v1/slots/available    | http://localhost:8081/cw_api/v1/slots/available                    |                                                                                  | Получить список всех Slot-ов (отрезков времени) |
| POST (doPost)               | /cw_api/v1/slots/             | http://localhost:8081/cw_api/v1/slots/                             | {"slotNumber": "20", "timeStart": "20:00", "timeFinish": "21:00"}                | Создать Slot с заданными параметрами            |
| PUT (doPut)                 | /cw_api/v1/slots/             | http://localhost:8081/cw_api/v1/slots/                             | {"slotId":"19", "slotNumber": "20", "timeStart": "20:10", "timeFinish": "20:30"} | Обновить выбранный Slot                         |
| DELETE (doDelete)           | /cw_api/v1/slots/             | http://localhost:8081/cw_api/v1/slots/                             | {"slotNumber": "20", "timeStart": "20:00", "timeFinish": "21:00"}                | Удаление Slot-а                                 |
| GET (doGet)                 | /cw_api/v1/slots/             | http://localhost:8081/cw_api/v1/slots/?slotId=18                   |                                                                                  | Посмотреть данные Slot-a по ID                  |
| GET (doGet)                 | /cw_api/v1/slots/             | http://localhost:8081/cw_api/v1/slots/?slotNumber=28               |                                                                                  | Посмотреть данные Slot-a по его номеру          |
| GET (doGet)                 | /cw_api/v1/slots/free-by-date | http://localhost:8081/cw_api/v1/slots/free-by-date?date=2029-07-28 |                                                                                  | Посмотреть данные Slot-a по виду и номеру       |

#### Запросы для работы с пользователем (User entity):

| Тип запроса (Servlet метод) | Endpoint                      | Полный запрос (пример)                         | Тело запроса                                                                     | Описание запроса                      |
|-----------------------------|-------------------------------|------------------------------------------------|----------------------------------------------------------------------------------|---------------------------------------|
| POST (doPost)               | /cw_api/v1/users/register     | http://localhost:8081/cw_api/v1/users/register | {"userName": "nameOf", "password": "passOf", "role":"ADMIN"}                     | Регистрация пользователя              |
| POST (doPost)               | /cw_api/v1/users/login        | http://localhost:8081/cw_api/v1/users/login    | {"login": "nameOf", "password": "passOf"}                                        | Аутентификация пользователя в системе |
| GET (doGet)                 | /cw_api/v1/users              | http://localhost:8081/cw_api/v1/users          |                                                                                  | Получить список всех User-ов          |
| PUT (doPut)                 | /cw_api/v1/users              | http://localhost:8081/cw_api/v1/users          | {"userId": "1", "userName": "UserUpdate", "password": "4321End", "role":"ADMIN"} | Обновить данные по User-у             |
| DELETE (doDelete)           | /cw_api/v1/users              | http://localhost:8081/cw_api/v1/users          | {"userId": "4", "userName": "UserTwo", "password": "1234", "role":"USER"}        | Удалить User-a                        |

#### Запросы для работы с системой резервирования (Reservation entity): 

| Тип запроса (Servlet метод) | Endpoint                          | Полный запрос (пример)                                                   | Тело запроса                                                                                           | Описание запроса                              |
|-----------------------------|-----------------------------------|--------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|-----------------------------------------------|
| GET (doGet)                 | /cw_api/v1/reservations/available | http://localhost:8081/cw_api/v1/reservations/available                   |                                                                                                        | Посмотреть все существующие брони             |
| POST (doPost)               | /cw_api/v1/reservations/          | http://localhost:8081/cw_api/v1/reservations/                            | {"reservationDate": "2047-08-11", "userId": "3", "placeId": "5", "slotId": "4"}                        | Создание новой брони                          |
| PUT (doPut)                 | /cw_api/v1/reservations/          | http://localhost:8081/cw_api/v1/reservations/                            | {"reservationId":16, "reservationDate":"2048-03-10", "userId":2, "placeId":3, "slotId":3}              | Обновить данные по брони                      |
| DELETE (doDelete)           | /cw_api/v1/reservations/          | http://localhost:8081/cw_api/v1/reservations/                            | {"reservationId": "10", "reservationDate": "2027-06-12", "userId": "3", "placeId": "8", "slotId": "4"} | Удалить бронь                                 |
| GET (doGet)                 | /cw_api/v1/reservations/          | http://localhost:8081/cw_api/v1/reservations/?userId=3                   |                                                                                                        | Посмотреть все брони конкретного пользователя |
| GET (doGet)                 | /cw_api/v1/reservations/          | http://localhost:8081/cw_api/v1/reservations/?reservationDate=2029-07-28 |                                                                                                        | Посмотреть брони на конкретную дату           |
| GET (doGet)                 | /cw_api/v1/reservations/          | http://localhost:8081/cw_api/v1/reservations/?placeId=3                  |                                                                                                        | Посмотреть все брони конкретного места/зала   |

________________________________________________________________________________________________________________________
### Применение Docker.

После разработки приложения, и тестирования его в локальном окружении, у нас появилась мысль упаковать его в Docker-образ.
Более подробно процесс описан в разделе [docker-practice](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/docker-practice).
Рассмотрено несколько способов: 
  - с [применением уже готового TomCat образа](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-files/chapter/Dicker_part_one.md) без возможности быстрой сборки того;
  - с [применением меньшего образа TomCat](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-files/chapter/Dicker_part_two.md) и внедрением переменных окружения;
  - с [оптимизацией размера образа](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-files/chapter/Dicker_part_three.md) и [размещением его на DockerHub](https://hub.docker.com/r/jcoderpaul/webapp/tags);

Естественно мы не забыли про [docker-compose](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-compose-file/docker-compose.yaml) - теперь приложение и БД можно быстро развернуть практически на любой машине.
Отличие [текущего docker-compose файла](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-compose-file/docker-compose.yaml) от [более старой его версии](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepTwo/docker-compose.yaml), примененной для предпоследней версии приложения,
это размещение Volumes не в рабочей папке приложения запускающейся из Intellij IDEA, а размещение их внутри виртуальной 
машины Docker-a:

![VolumeLS](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/Doc/VolumeLS.jpg)
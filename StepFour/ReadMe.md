### StepFour (nonBoot Spring application)

Основная задача проекта: Разработать приложение для управления коворкинг-пространством. Приложение должно позволять
пользователям бронировать рабочие места, конференц-залы, а также управлять бронированиями и просматривать доступность
ресурсов.

Т.е. все то, что было описано и проделано на предыдущих шагах:
- [StepOne](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne);
- [StepTwo](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo);
- [StepThree](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree);

Снова нужно переписать, но уже с применением Spring (nonBoot).

#### Функциональные и технические требования текущего шага (обновления предыдущих решений):
- Java конфигурация приложения (простой пример, того, как в принципе можно конфигурировать приложения см. [SpringWebAppConfig](https://github.com/JcoderPaul/Evolution_app_development/tree/master/SpringWebAppConfig));
- Взаимодействие с внешними потребителями (пользователями, сторонними сервисами) реализовать с применением REST-контроллеров;
- Интегрировать систему документирования Swagger с приложением ([описание и варианты интеграции](https://github.com/JcoderPaul/Evolution_app_development/tree/master/SpringAndSwagger));
- Текущие аспекты адаптировать под [Spring AOP](https://docs.spring.io/spring-framework/reference/core/aop.html), краткую реализацию мы сделали на прошлом мини шаге - [SpringAOPAndCo](https://github.com/JcoderPaul/Evolution_app_development/tree/master/SpringAOPAndCo);
- Приложение полностью управляется Spring-ом, контроллеры покрыты тестами;

________________________________________________________________________________________________________________________
#### Реализация:
- Функциональные требования выполнены полностью.
- Технические требования выполнены полностью.
- Тестами (только интеграционные) покрыто более 87% кода.

________________________________________________________________________________________________________________________
#### Особенности:
Данная версия приложения взаимодействует с пользователем (сервисом или другим приложением) по средствам HTTP-запросов и
использует в своей реализации Spring (nonBoot). Приложение развертывается в контейнере сервлетов ([TomCat](https://tomcat.apache.org/)).

[Скрипты Liquibase](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/resources/db/changelog) при первичном старте создадут необходимые таблицы и предзаполнят их.

Структура проекта (это не Spring проект, но слоистую структуру можно применять где угодно, отсюда и названия):
- [annotation](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/java/me/oldboy/annotations) - аннотации для аудирования и логирования;
- [aspects](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/java/me/oldboy/aspects) - аспектные классы (аудит и логирование);
- [config](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/java/me/oldboy/config):
    - [data_source](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFour/src/main/java/me/oldboy/config/data_source/AppDataSourceConfig.java) - настройка источника данных (связь в БД и т.д.)
    - [jwt_config](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/java/me/oldboy/config/jwt_config) - файлы отвечающие за работу аутентификации с применением JWT токена ([фильтр](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFour/src/main/java/me/oldboy/config/jwt_config/JwtAuthFilter.java) и [генератор токена](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFour/src/main/java/me/oldboy/config/jwt_config/JwtTokenGenerator.java));
    - [main_config](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFour/src/main/java/me/oldboy/config/main_config/MainAppConfig.java) - папка с основным [конфигурационным файлом](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFour/src/main/java/me/oldboy/config/main_config/MainAppConfig.java);
    - [security_config](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/java/me/oldboy/config/security_config) - папка с настройкой системы безопасности Spring - [SecurityFilterChain](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFour/src/main/java/me/oldboy/config/security_config/FilterChainConfig.java);
    - [security_details](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/java/me/oldboy/config/security_details) - кастомные [UserDetails](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFour/src/main/java/me/oldboy/config/security_details/SecurityUserDetails.java) и [UserDetailsService](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFour/src/main/java/me/oldboy/config/security_details/ClientDetailsService.java);
    - [swagger](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/java/me/oldboy/config/swagger) - настройка Swagger-a;
    - [yaml_read_adapter](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFour/src/main/java/me/oldboy/config/yaml_read_adapter/YamlPropertySourceFactory.java) - адаптер для чтения yaml(yml) файлов;
- [controllers](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/java/me/oldboy/controllers) - классы управляющие сущностями на 'верхнем' уровне приложения;
- [dto](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/java/me/oldboy/dto) - классы для передачи данных со слоя на слой;
- [exception](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/java/me/oldboy/exception) - исключения и обработчики ошибок; 
- [mapper](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/java/me/oldboy/mapper) - классы сопоставители;
- [models](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/java/me/oldboy/models) - рабочие сущности приложения;
- [repository](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/java/me/oldboy/repository) - классы для взаимодействия с БД;
- [services](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/java/me/oldboy/services) - теоретически, данные классы должны аккумулировать основную "бизнес" логику приложения, но, у нас приложение не большое и логика ровненько размазана по всем слоям;
- [resources](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/resources):
    - [db/changelog](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour/src/main/resources/db/changelog) - файлы миграции Liquibase;
    - [application.yml](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFour/src/main/resources/application.yml) - файл настроек (см. комментарии);
    - [log4j.xml](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFour/src/main/resources/log4j.xml) - конфигурация логера;

________________________________________________________________________________________________________________________
#### Swagger

Используя наработки сделанные в предыдущем разделе - [SpringAndSwagger](https://github.com/JcoderPaul/Evolution_app_development/tree/master/SpringAndSwagger) подключаем Swagger.

Естественно, на сразу доступны две важные точки доступа:
- http://localhost:8080/swagger-ui/index.html
- http://localhost:8080/v3/api-docs

Что значительно упрощает и ускоряет описание (документирование) основных элементов текущего приложения.
________________________________________________________________________________________________________________________
#### Параметры запросов к приложению (API)

Адрес хоста стандартный для локальной машины (порт исходя из настроек TomCat): http://localhost:8080

Естественно при развертке приложения в контейнере, или в локальном TomCat, в полном адресе запроса может появиться имя
приложения (папки где оно развернуто), например **cw** и тогда между адресом хоста и endpoint-ом появится дополнение,
например:

    http://localhost:8080/cw/api/v1/places/

**Не забываем про это!**

________________________________________________________________________________________________________________________
#### Запросы для работы с залами и рабочими местами (Place entity):

| Тип запроса | Endpoint                                           | Полный запрос (пример)                                 | Тело запроса                                             | Описание запроса                                     |
|-------------|----------------------------------------------------|--------------------------------------------------------|----------------------------------------------------------|------------------------------------------------------|
| POST        | /api/admin/places/update                           | http://localhost:8080/api/admin/places/update          | {"placeId": 1, "species": "WORKPLACE", "placeNumber": 2} | Обновить выбранный Place (Место/Зал)                 |
| POST        | /api/admin/places/delete                           | http://localhost:8080/api/admin/places/delete          | {"species": "HALL", "placeNumber": 5}                    | Удаление Place (Место/Зал)                           |
| POST        | /api/admin/places/create                           | http://localhost:8080/api/admin/places/create          | {"species": "HALL", "placeNumber": 5}                    | Создать Place с заданными параметрами                |
| GET         | /api/places                                        | http://localhost:8080/api/places                       |                                                          | Получить список всех Place-ов (залов/рабочих мест)   |
| GET         | /api/places/{placeId}                              | http://localhost:8080/api/places/1                     |                                                          | Посмотреть данные на Place (Место/Зал) по ID         |
| GET         | /api/places/species/{species}/number/{placeNumber} | http://localhost:8080/api/places/species/HALL/number/3 |                                                          | Посмотреть данные Place (Место/Зал) по виду и номеру |

#### Запросы для работы с залами и рабочими местами (Slot entity):

| Тип запроса | Endpoint                       | Полный запрос (пример)                       | Тело запроса                                                                  | Описание запроса                       |
|-------------|--------------------------------|----------------------------------------------|-------------------------------------------------------------------------------|----------------------------------------|
| POST        | /api/admin/slots/update        | http://localhost:8080/api/admin/slots/update | {"slotId":"19", "slotNumber":"20", "timeStart":"20:10", "timeFinish":"20:30"} | Обновить выбранный Slot                |
| POST        | /api/admin/slots/delete        | http://localhost:8080/api/admin/slots/delete | {"slotNumber":"20", "timeStart":"20:00", "timeFinish":"21:00"}                | Удаление Slot-а                        |
| POST        | /api/admin/slots/create        | http://localhost:8080/api/admin/slots/create | {"slotNumber":"20", "timeStart":"20:00", "timeFinish":"21:00"}                | Создать Slot с заданными параметрами   |
| GET         | /api/slots                     | http://localhost:8080/api/slots              |                                                                               | Получить список всех Slot-ов           |
| GET         | /api/slots/number/{slotNumber} | http://localhost:8080/api/slots/number/3     |                                                                               | Посмотреть данные Slot-a по его номеру |
| GET         | /api/slots/id/{slotId}         | http://localhost:8080/api/slots/id/2         |                                                                               | Посмотреть данные Slot-a по ID         |

#### Запросы для работы с пользователем (User entity):

| Тип запроса | Endpoint                | Полный запрос (пример)                       | Тело запроса                                                               | Описание запроса                      |
|-------------|-------------------------|----------------------------------------------|----------------------------------------------------------------------------|---------------------------------------|
| POST        | /api/registration       | http://localhost:8080/api/registration       | {"login":"nameOf", "password":"passOf", "role":"ADMIN"}                    | Регистрация пользователя              |
| POST        | /api/login              | http://localhost:8080/api/login              | {"login":"nameOf", "password":"passOf"}                                    | Аутентификация пользователя в системе |
| POST        | /api/admin/users/update | http://localhost:8080/api/admin/users/update | {"userId":"1", "login":"UserUpdate", "password":"4321End", "role":"ADMIN"} | Обновить данные по User-у             |
| POST        | /api/admin/users/delete | http://localhost:8080/api/admin/users/delete | {"userId":"4", "login":"UserTwo", "password":"1234", "role":"USER"}        | Удалить User-a                        |
| GET         | /api/admin/users/all    | http://localhost:8080/api/admin/users/all    |                                                                            | Получить список всех User-ов          |

#### Запросы для работы с системой резервирования (Reservation entity):

| Тип запроса | Endpoint                           | Полный запрос (пример)                                  | Тело запроса                                                                                  | Описание запроса                                        |
|-------------|------------------------------------|---------------------------------------------------------|-----------------------------------------------------------------------------------------------|---------------------------------------------------------|
| POST        | /api/reservations/update           | http://localhost:8080/api/reservations/update           | {"reservationId": 16, "reservationDate":"2048-03-10", "userId": 2, "placeId": 3, "slotId": 3} | Обновить данные по брони                                |
| POST        | /api/reservations/delete           | http://localhost:8080/api/reservations/delete           | {"reservationId": 10, "reservationDate":"2027-06-12", "userId": 3, "placeId": 8, "slotId": 4} | Удалить бронь                                           |
| POST        | /api/reservations/create           | http://localhost:8080/api/reservations/create           | {"reservationDate": "2047-08-11", "userId": "3", "placeId": "5", "slotId": "4"}               | Создание новой брони                                    |
| GET         | /api/reservations                  | http://localhost:8080/api/reservations                  |                                                                                               | Посмотреть все существующие брони                       |
| GET         | /api/reservations/free/date/{date} | http://localhost:8080/api/reservations/free/date/{date} |                                                                                               | Посмотреть свободные слоты на конкретную дату           |
| GET         | /api/reservations/booked           | http://localhost:8080/api/reservations/booked           |                                                                                               | Посмотреть выборку броней по пользователю, дате и месту |

Для тестирования доступа можно использовать, как Postman, так и Swagger.

Приложение для аутентификации используем JWT ключ, посему не забываем его применять. После авторизации сервис (пользователь)
получает его в ответ, пример:

![JWT_Token_example](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/Doc/pic/JWT_Token_example.jpg)

Далее при формировании запроса через Postman выбираем тип аутентификации и добавляем полученный JWT ключ в соответствующее поле:

![Request_with_jwt_token](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/Doc/pic/Request_with_jwt_token.jpg)

В Swagger нужно проделать ту же операцию - вводим логин и пароль, получаем ответ:

![swagger_login_test.jpg](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFour/DOC/swagger_login_test.jpg)

И вводим в специальное поле, после нажатия кнопки Authorize - все защищенные endpoints теперь доступны для изучения:

![swagger_auth.jpg](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFour/DOC/swagger_auth.jpg)
________________________________________________________________________________________________________________________
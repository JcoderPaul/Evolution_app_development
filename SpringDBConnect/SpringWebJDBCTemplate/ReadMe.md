В разделе SpringWebAppConfig мы рассмотрели 4-и наиболее вероятные (но не единственные) способа конфигурирования 
Spring Web приложения теперь разберемся, как связать наше приложение со БД.
________________________________________________________________________________________________________________________
### 4 and 6/4 (Spring App with Java config and DataSource/JDBC Template)

На данном шаге мы хотим реализовать: 
- Связь нашего Spring MVC приложение с БД PostgreSQL (и в запросах [задействовать JdbcTemplate](https://docs.spring.io/spring-framework/docs/4.3.20.RELEASE/spring-framework-reference/html/jdbc.html));
- В методах контроллера применить аннотации специфичные для конкретного запроса;
- Получить данные из БД и вывести в браузере, удалить данные; 
- Отправить данные через тело запроса в БД;


Предварительно в docker-контейнере развернем базу и внесем туда немного данных:

    -- Создадим схему
    CREATE SCHEMA IF NOT EXISTS coworking;

    -- Создадим таблицу
    CREATE TABLE IF NOT EXISTS coworking.users
    (
        user_id BIGSERIAL PRIMARY KEY ,
        login VARCHAR(64) NOT NULL UNIQUE ,
        user_pass VARCHAR(128) NOT NULL ,
        role VARCHAR(32) NOT NULL
    );

    -- Подкинем данных в топку
    INSERT INTO coworking.users (login, user_pass, role)
    VALUES ('Admin', '1234', 'ADMIN'),
           ('User', '1234', 'USER'),
           ('UserThree', '4321', 'USER'),
           ('UserTwo', '1234', 'USER');

Будем использовать каркас приложения из раздела [SpringWebMvcConfig](https://github.com/JcoderPaul/Evolution_app_development/tree/master/SpringWebAppConfig/SpringWebMvcConfig)

Что мы добавим и что изменим:
- ConnectionManager.java - файл формирующий DataSource bean и отвечающий за настройку связи с БД;
- application.properties - файл настроек для JDBC драйвера;
- WebContextInitializer - файл формирующий web-контекст, существенное отличие от предыдущей версии наличие необходимых аннотаций;
- AppContextConfig.java - файл конфигурации приложения, для разнообразия мы вынесли сюда создание JdbcTemplate bean-a;
- UserRestController.java - контроллер обрабатывающий запросы: просмотр user-a по id, удаление user-a по id, просмотр всех user-ов, создание user-a;
- UserRepositoryImpl.java - файл получающий данные (теперь уже из PostgreSQL БД);

Конфигурация зависимостей:
- build.gradle;
- version.gradle - версии зависимостей;

Приложение разворачивается локально. Для запуска приложения нужен web-сервер приложений или контейнер сервлетов - 
используем TomCat.

Для тестирования запросов применяем [POSTMAN](https://www.postman.com/)): 
- http://localhost:8080/v2/users/5 (GET запрос с применением @GetMapping и @PathVariable - посмотреть user-a);
- http://localhost:8080/v1/users?userId=6 ("классический" GET запрос с применением @RequestParam  - посмотреть user-a);
- http://localhost:8080/v2/users (POST запрос с применением аннотаций @PostMapping и @RequestBody - создать user-a);
- http://localhost:8080/v2/users (GET запрос позволяющий получить всех пользователей из БД);
- http://localhost:8080/v2/users/5 (DELETE запрос с применением аннотации @DeleteMapping - удалить user-a по ID);
________________________________________________________________________________________________________________________
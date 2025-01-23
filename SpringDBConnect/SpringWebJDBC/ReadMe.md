В разделе SpringWebAppConfig мы рассмотрели 4-и наиболее вероятные (но не единственные) способа конфигурирования Spring Web
приложения теперь разберемся, как связать наше приложение с БД.
________________________________________________________________________________________________________________________
### 4 and 5/4 (Spring Web App with Java config and JDBC connection only)

На данном шаге мы хотим реализовать: 
- Связь нашего Spring MVC приложение с БД PostgreSQL (используя лишь JDBC и классические SQL запросы);
- Получить данные из БД и вывести в браузере; 
- Отправить данные через тело запроса в БД;

Предварительно в docker-контейнере развернем базу и внесем туда немного данных:

    -- Docker команда:
    docker run --name cw_db -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -p 5436:5432 -d postgres:13

    -- Создадим схему:
    CREATE SCHEMA IF NOT EXISTS coworking;

    -- Создадим таблицу:
    CREATE TABLE IF NOT EXISTS coworking.users
    (
        user_id BIGSERIAL PRIMARY KEY ,
        login VARCHAR(64) NOT NULL UNIQUE ,
        user_pass VARCHAR(128) NOT NULL ,
        role VARCHAR(32) NOT NULL
    );

    -- Подкинем данных в топку:
    INSERT INTO coworking.users (login, user_pass, role)
    VALUES ('Admin', '1234', 'ADMIN'),
           ('User', '1234', 'USER'),
           ('UserThree', '4321', 'USER'),
           ('UserTwo', '1234', 'USER');

Будем использовать каркас приложения из раздела [SpringWebMvcConfig](https://github.com/JcoderPaul/Evolution_app_development/tree/master/SpringWebAppConfig/SpringWebMvcConfig)

Что мы добавим и что изменим:
- ConnectionManager.java - файл отвечающий за настройку связи с БД;
- application.properties - файл настроек для JDBC драйвера;
- WebContextInitializer - файл формирующий web-контекст, существенное отличие от предыдущей версии наличие необходимых аннотаций;
- AppContextConfig.java - файл конфигурации приложения;
- UserRestController.java - контроллер обрабатывающий запросы, отличие от предыдущей версии - добавление метода записи данных в БД;
- UserRepositoryImpl.java - файл получающий данные (теперь уже из PostgreSQL БД);

Конфигурация зависимостей:
- build.gradle - для работы нам понадобились новые зависимости:

      /* Зависимость PostgreSQL */
      implementation "org.postgresql:postgresql:${versions.postgres}"
      /* Зависимость позволяющая в контроллерах принимать JSON запросы */
      implementation "com.fasterxml.jackson.core:jackson-databind:${versions.jackson}"

- version.gradle - версии зависимостей;

Приложение разворачивается локально. Для запуска приложения нужен web-сервер приложений или контейнер сервлетов - 
используем TomCat.

Основные отличия от предыдущих вариантов запросов (на этот раз для тестирования применяем [POSTMAN](https://www.postman.com/)): 
- http://localhost:8080/v2/users/5 (GET запрос с применением @PathVariable - получить данные по ID);
- http://localhost:8080/v1/users?userId=6 ("классический" GET запрос с применением @RequestParam  - получить данные по ID);
- http://localhost:8080/v2/users (POST запрос с применением method = RequestMethod.POST и аннотации @RequestBody - добавить данные в таблицу User-ов);

В тело POST запроса отдаем JSON вида:
    
    {
        "userName":"name_for_test",
        "password":"pass_for_test",
        "role":"ADMIN"
    }

И получаем подтверждающий регистрацию JSON объект из БД.

Более подробно о технологии JDBC можно [посмотреть в разделе JDBC_Practice](https://github.com/JcoderPaul/JDBC_Practice).
________________________________________________________________________________________________________________________
В разделе SpringWebAppConfig мы рассмотрели 4-и наиболее вероятные (но не единственные) способа конфигурирования Spring Web
приложения теперь разберемся, как связать наше приложение с БД.
________________________________________________________________________________________________________________________
### 4 and 7/4 (Spring Web App with Java configuration and Hibernate)

На данном шаге мы хотим реализовать: 
- Связь нашего Spring MVC приложение с БД PostgreSQL (по средствам Hibernate);
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
- [HibernateConfig.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringDBConnect/SpringDBConnect/SpringWebHibernate/src/main/java/me/oldboy/config/HibernateConfig.java) - файл отвечающий за настройку связи с БД;
- [hibernate.cfg.xml](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringDBConnect/SpringDBConnect/SpringWebHibernate/src/main/resources/hibernate.cfg.xml) - файл настроек для Hibernate (в данной реализации мы не используем файл application.properties);
- [WebContextInitializer](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringDBConnect/SpringDBConnect/SpringWebHibernate/src/main/java/me/oldboy/config/WebContextInitializer.java) - файл формирующий web-контекст, существенное отличие от предыдущей версии наличие необходимых аннотаций;
- [AppContextConfig.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringDBConnect/SpringDBConnect/SpringWebHibernate/src/main/java/me/oldboy/config/AppContextConfig.java) - файл конфигурации приложения;
- [controller](https://github.com/JcoderPaul/Evolution_app_development/tree/SpringDBConnect/SpringDBConnect/SpringWebHibernate/src/main/java/me/oldboy/controller) - папка контроллеров обрабатывающих запросы;
- [UserRepositoryImpl.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringDBConnect/SpringDBConnect/SpringWebHibernate/src/main/java/me/oldboy/repository/UserRepositoryImpl.java) - файл получающий данные (используем несколько вариантов запросов);

Конфигурация зависимостей:
- [build.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringDBConnect/SpringDBConnect/SpringWebHibernate/build.gradle) - для работы нам понадобились новые зависимости:

      /* Подключаем функционал Hibernate */
      implementation "org.hibernate:hibernate-core:${versions.hibernate}"
  
- [version.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringDBConnect/SpringDBConnect/SpringWebHibernate/version.gradle) - версии зависимостей;

Приложение разворачивается локально. Для запуска приложения нужен web-сервер приложений или контейнер сервлетов - 
используем TomCat.

Для тестирования применяем [POSTMAN](https://www.postman.com/)): 
- http://localhost:8080/v2/users/5 (GET запрос с применением @GetMapping и @PathVariable - посмотреть user-a);
- http://localhost:8080/v1/users?userId=6 ("классический" GET запрос с применением @RequestParam  - посмотреть user-a);
- http://localhost:8080/v2/users (POST запрос с применением аннотаций @PostMapping и @RequestBody - создать user-a);
- http://localhost:8080/v2/users (GET запрос позволяющий получить всех пользователей из БД);
- http://localhost:8080/v2/users/5 (DELETE запрос с применением аннотации @DeleteMapping - удалить user-a по ID);

Более подробно работу с Hibernate [можно посмотреть тут - Hibernate_Lessons](https://github.com/JcoderPaul/Hibernate_Lessons).
________________________________________________________________________________________________________________________
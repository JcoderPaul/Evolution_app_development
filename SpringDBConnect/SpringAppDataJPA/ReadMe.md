И так, мы уже рассмотрели, как минимум, три возможных варианта связи нашего web приложения с БД. Как это ни странно, но 
каждый из способов связки приложения с БД приводил к тому, что мы вынуждены были переписывать код слоя репозиториев (DAO) -
базовые понятия и алгоритмы оставались классическими - реализация методов взаимодействия с БД применялись сообразно 
используемой технологии.

Теперь попробуем вариант, который выглядит почти магией, поскольку технология наиболее простых запросов к БД реализована 
за нас и "сокрыта от рядового разработчика". Естественно, нам никто не мешает писать свои реализации, используя весь 
примененный ранее арсенал и даже сверх того.
________________________________________________________________________________________________________________________
### 4 and 8/4 (Spring Web App with Hibernate and Spring Data JPA)

На данном шаге мы хотим реализовать: 
- Все настройки взаимодействия приложения с БД прописать в *.YML (*.YAML) файле;
- Связь нашего Spring MVC приложение с БД PostgreSQL реализовать с применением Spring Data Jpa;
- Получить данные из БД и вывести в браузере; 
- Отправить данные через тело запроса в БД;

Повторимся, как и ранее, предварительно в docker-контейнере развернем базу и внесем туда немного данных:

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

Будем использовать чуть видоизмененный каркас приложения из раздела [SpringWebMvcConfig](https://github.com/JcoderPaul/Evolution_app_development/tree/master/SpringWebAppConfig/SpringWebMvcConfig)

Что мы добавим и что изменим:
- config - раздел конфигурации всего приложения:
  - YamlPropertySourceFactory.java - файл позволяющий читать свойства из application.yml;
  - AppContextConfig.java - файл конфигурации приложения;
  - WebContextInitializer - файл формирующий web-контекст;
  - DataSourceConfig.java - файл отвечающий за настройку связи с БД, фактически, тут формируется источник данных для 
всего приложения и настраивается менеджер транзакций;
- application.yml - файл настроек;
- controller - папка контроллеров обрабатывающих запросы;
- User.java - класс (сущность, model), "схема" ключевого объекта которым манипулирует наше приложение;
- UserReadDto.java - т.н. "межслойный" объект передачи данных, в нем мы отсекаем "лишнюю" информацию для передачи отображению;
- UserRepository.java - самый "магический" из слоев приложения, т.к. не имеет видимой реализации применяемых с него 
методов, файл получающий данные из БД;
- UserService.java - слой бизнес логики приложения (в текущем приложении она минимальна);

Конфигурация зависимостей:
- build.gradle - для работы нам понадобились новые зависимости:

      /* Зависимость позволяющая использовать функционал Spring JPA */
      implementation "org.springframework.data:spring-data-jpa:${versions.data_jpa}"
  
      /* Зависимость позволяющая работать с JSON форматом в приложении */
      implementation "com.fasterxml.jackson.core:jackson-databind:${versions.jackson}"

      /* Зависимость позволяющая читать YAML файлы */
      implementation 'org.yaml:snakeyaml:2.3'
  
- version.gradle - версии зависимостей;

Основную хитрость в данном мини-приложении составлял момент чтения *.YML файла, т.к. в Spring Boot эту возможность мы 
получаем "из коробки", то в Spring Framework (используемой версии) должны написать некую реализацию "конвертера" - 
в [документации по Spring](https://docs.spring.io/spring-boot/docs/1.1.0.M1/reference/html/boot-features-external-config.html) 
данных мало, но определенные подсказки есть - для начала нам нужно подтянуть зависимость описанную в build.gradle. 
Более информативной оказалась статья ["@PropertySource with YAML Files in Spring Boot"](https://www.baeldung.com/spring-yaml-propertysource) 
и самыми полезными: 
- ["External Properties"](https://github.com/dkkahm/study-springfamework5/wiki/030.External-Properties);
- ["PropertySource support .yml file"](https://github.com/Andy-Shao/Gear/wiki/PropertySource-support-.yml-file);
- ["PropertySource"](https://github.com/Neethahiremath/Wiki/wiki/PropertySource);

из разделов GitHub WiKi других разработчиков.

Приложение разворачивается локально, для его запуска нужен web-сервер приложений или контейнер сервлетов - 
используем TomCat.

Для тестирования прохождения запросов применяем [POSTMAN](https://www.postman.com/)): 
- http://localhost:8080/v2/users/5 (GET запрос с применением @GetMapping и @PathVariable - посмотреть user-a);
- http://localhost:8080/v1/users?userId=6 ("классический" GET запрос с применением @RequestParam  - посмотреть user-a);
- http://localhost:8080/v2/users (POST запрос с применением аннотаций @PostMapping и @RequestBody - создать user-a);
- http://localhost:8080/v2/users (GET запрос позволяющий получить всех пользователей из БД);
- http://localhost:8080/v2/users/5 (DELETE запрос с применением аннотации @DeleteMapping - удалить user-a по ID);

Более подробно работу с Spring JPA можно посмотреть тут, применительно к Spring Boot - [Data JPA и Data JPA Transactions](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_9).
________________________________________________________________________________________________________________________
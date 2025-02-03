И так, на шаге три - [StepThree](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree) используя 
Servlet API мы написали некое приложение, которое управляло "виртуальным коворкинг-центром", на следующем четвертом шаге 
нам необходимо перенести всю его логику под управление (nonBoot) Spring Web MVC приложения, чем мы и занимаемся последние 
несколько микро-шажков. Ранее мы рассмотрели: 
- несколько классических вариантов [конфигурации Spring приложения](https://github.com/JcoderPaul/Evolution_app_development/tree/master/SpringWebAppConfig);
- несколько вариантов [взаимодействия Spring приложения и PostgreSQL БД;](https://github.com/JcoderPaul/Evolution_app_development/tree/master/SpringDBConnect)
- пару вариантов [интеграции Spring приложения и OpenAPI (Swagger)](https://github.com/JcoderPaul/Evolution_app_development/tree/master/SpringAndSwagger);

Теперь нам надо провести несколько небольших доработок, чтобы двигаться дальше.
________________________________________________________________________________________________________________________
### 4 and 11/4 ((non Boot) Spring application with AOP and some useful modifications)

На данном шаге нам нужно:
- Интегрировать технологию AOP в наше приложение (рассчитать скорость выполнения методов контроллера).
- Настроить работу миграционного фреймворка Liquibase и нашего приложения (все таблицы создаются только им).
- Настроить валидацию входящих данных (в нашем случае, необходимо отловить неверное заполнение полей UserCreateDto).
- Применить библиотеку MapStruct (для преобразования UserCreateDto в User, и User в UserReadDto).

Структура проекта:
- annotations - папка с аннотациями ([@Measurable](https://github.com/JcoderPaul/Evolution_app_development/blob/master/SpringAOPAndCo/src/main/java/me/oldboy/annotations/Measurable.java) - ей мы аннотируем методы скорость которых хотим рассчитать);
- aspects - папка с аспектами ([MethodSpeedCalcAspect.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/SpringAOPAndCo/src/main/java/me/oldboy/aspects/MethodSpeedCalcAspect.java) - класс аспект описывающий логику работы и подключения "измерителя скорости методов");
- config:
  - [YamlPropertySourceFactory](https://github.com/JcoderPaul/Evolution_app_development/blob/master/SpringAOPAndCo/src/main/java/me/oldboy/config/yml_properties_reader/YamlPropertySourceFactory.java) - файл позволяющий читать данные из нашего application.yml;
  - [DataSourceConfig.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/SpringAOPAndCo/src/main/java/me/oldboy/config/DataSourceConfig.java) - файл конфигурирующий связь приложения с БД;
  - [OpenApiConfig.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/SpringAOPAndCo/src/main/java/me/oldboy/config/OpenApiConfig.java) - файл конфигурирующий настройки OpenApi (и Swagger UI);
  - [WebContextInitializer.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/SpringAOPAndCo/src/main/java/me/oldboy/config/WebContextInitializer.java) - файл конфигурирующий контекст приложения;
  - [LiquibaseConfig.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/SpringAOPAndCo/src/main/java/me/oldboy/config/LiquibaseConfig.java) - файл конфигуратор подключенного миграционного фреймворка Liquibase;
- [UserRestController.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/SpringAOPAndCo/src/main/java/me/oldboy/controller/UserRestController.java) - файл контроллера - взаимодействие пользователя приложением и нашего сервиса (приложения);
- dto:
  - [UserReadDto.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/SpringAOPAndCo/src/main/java/me/oldboy/dto/UserReadDto.java) - класс передачи данных между слоями приложения (условно со слоя работы с данными "наверх");
  - [UserCreateDto.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/SpringAOPAndCo/src/main/java/me/oldboy/dto/UserCreateDto.java) - класс передачи данных между слоями (условно со слоя контроллеров (отображения) на "нижние" слои сервисов/данных);
- [User.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/SpringAOPAndCo/src/main/java/me/oldboy/entity/User.java) - сущность с которой работает приложение (модель);
- [UserRepository](https://github.com/JcoderPaul/Evolution_app_development/blob/master/SpringAOPAndCo/src/main/java/me/oldboy/repository/UserRepository.java) - "магический" интерфейс репозитория - взаимодействие приложения и БД;
- [UserService.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/SpringAOPAndCo/src/main/java/me/oldboy/service/UserService.java) - слой бизнес логики;

Конфигурация зависимостей:
- [build.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/master/SpringAOPAndCo/build.gradle) - описание зависимостей (из нового нам понадобились):

      /* Зависимость отвечающая за работу AOP функционала */
      implementation "org.aspectj:aspectjweaver:1.9.22.1"
  
      /* Подключаем функционал провайдера валидации */
      implementation "org.hibernate.validator:hibernate-validator:8.0.1.Final"
  
      /* Подключаем функционал миграционного фреймворка Liquibase */
      implementation "org.liquibase:liquibase-core:4.31.0"
  
      /* Подключим библиотеку Mapstruct */
      implementation "org.projectlombok:lombok-mapstruct-binding:0.2.0"
      implementation "org.mapstruct:mapstruct:1.6.3"
      implementation "org.mapstruct:mapstruct-processor:1.6.3"
      annotationProcessor "org.mapstruct:mapstruct-processor:1.6.3",
                          "org.projectlombok:lombok-mapstruct-binding:0.2.0"

- [version.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/master/SpringAOPAndCo/version.gradle) - версии зависимостей;

Как и ранее приложение разворачивается локально. Для запуска нужен web-сервер приложений или контейнер сервлетов - используем TomCat 10 или выше.

Для тестирования прохождения запросов применяем POSTMAN или Swagger UI (у нас 4-и конечных точки):

- http://localhost:8080/v2/users/5 (GET запрос с применением @GetMapping и @PathVariable - посмотреть user-a);
- http://localhost:8080/v2/users (POST запрос с применением аннотаций @PostMapping - создать user-a);
- http://localhost:8080/v2/users (GET запрос позволяющий получить всех пользователей из БД);
- http://localhost:8080/v2/users/5 (DELETE запрос с применением аннотации @DeleteMapping - удалить user-a по ID);
________________________________________________________________________________________________________________________
Интересные материалы по текущему вопросу (оф.док и т.д.):
- [Spring AOP](https://docs.spring.io/spring-framework/reference/core/aop.html);
- [Aspect Oriented Programming with Spring](https://docs.spring.io/spring-framework/docs/4.3.15.RELEASE/spring-framework-reference/html/aop.html);
- [Introduction to Spring AOP](https://www.baeldung.com/spring-aop);
- [Аспектно-ориентированное программирование и АОП в Spring Framework](https://www.geeksforgeeks.org/aspect-oriented-programming-and-aop-in-spring-framework/);
- [Liquibase Documentation](https://docs.liquibase.com/home.html);
- [GitHub liquibase-docs](https://github.com/liquibase/liquibase-docs);
- [Liquibase (liquibase)](https://docs.spring.io/spring-boot/api/rest/actuator/liquibase.html);
- [MapStruct Doc](https://mapstruct.org/documentation/installation/);
- [Validation with Spring Boot - the Complete Guide](https://reflectoring.io/bean-validation-with-spring-boot/#a-custom-validator-with-spring-boot);

Вопрос-ответ, близко по теме (https://stackoverflow.com/):
- [Understanding Spring AOP](https://stackoverflow.com/questions/5589319/understanding-spring-aop);
- [Spring boot - disable Liquibase at startup](https://stackoverflow.com/questions/37708145/spring-boot-disable-liquibase-at-startup);
- [How to create schema in Postgres DB, before liquibase start to work](https://stackoverflow.com/questions/52517529/how-to-create-schema-in-postgres-db-before-liquibase-start-to-work);

Примеры из пройденного:
- [Spring Boot lessons part 24 - AOP в Spring](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_24);
- [Spring Boot lessons part 14 - Database Migrations](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_14);
- [Spring Boot lessons part 18 - Validation Starter](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_18);
________________________________________________________________________________________________________________________
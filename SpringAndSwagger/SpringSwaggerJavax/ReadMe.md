Одним из вариантов документирования (и не только) конечных точек (Endpoint) нашего REST API может быть интеграция приложения
его с фреймворком Swagger UI. Спецификация OpenAPI предоставляет один из способов описания REST API, а Swagger и подобные
ему фреймворки предоставляют нам удобный способ реализации этой спецификации. 
________________________________________________________________________________________________________________________
### 4 and 9/4 ((non Boot) Spring service integration with SpringFox - "Swagger")

На данном шаге используем устаревший набор библиотек Java Springfox, который полностью посвящен автоматизации генерации 
машинных спецификаций для API JSON, написанных с использованием семейства проектов Spring ([см. документацию](https://springfox.github.io/springfox/docs/current/)).
Судя по состоянию [страницы проекта на GitHub](https://github.com/springfox/springfox), структура оного не обновлялась 
более 5-и лет, в своем коде он использует библиотеку [javax.servlet-api](https://docs.oracle.com/javaee/7/api/javax/servlet/package-summary.html), которая слегка не совместима с
текущей версией [Spring Framework 6.2.2](https://spring.io/projects/spring-framework) и новичку будет затруднительно их состыковать в одном приложении. 
И конечно же, запустить такое приложение на стареньком TomCat 9 и ниже окажется интересной задачей. 

Поэтому для изучения вопроса немного отойдем в прошлое и перепишем часть классов с применением пакета Javax и более старой
версии [Spring Framework 5.3.39](https://mvnrepository.com/artifact/org.springframework/spring-context/5.3.39). Версия контейнера сервлетов TomCat тут условно не важна, т.к. существует обратная совместимость 
(т.е. с пакетом Javax в коде наше приложение легко стыкуется со [SpringFox 3.0.0](https://mvnrepository.com/artifact/io.springfox/springfox-swagger2/3.0.0) и заводится на [TomCat 9-10](https://tomcat.apache.org/whichversion.html)).

На данном шаге нам нужно:
- Интегрировать SpringFox и наше SpringWebMvc (non Boot) приложение.
- Настроить и получить доступ к JSON описанию API наших конечных точек - http://localhost:8080/v2/api-docs
- Настроить и получить доступ к интерфейсу Swagger UI - http://localhost:8080/swagger-ui/index.html
- Проверить работоспособность нашего контроллера используя интерфейс Swagger-a;

Структура проекта:
- config:
  - [YamlPropertySourceFactory](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJavax/src/main/java/me/oldboy/config/yml_properties_reader/YamlPropertySourceFactory.java) - файл позволяющий читать данные из нашего application.yml;
  - [DataSourceConfig.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJavax/src/main/java/me/oldboy/config/DataSourceConfig.java) - файл конфигурирующий связь приложения с БД;
  - [SwaggerConfig.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJavax/src/main/java/me/oldboy/config/SwaggerConfig.java) - файл конфигурирующий настройки Swagger-а;
  - [WebContextInitializer.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJavax/src/main/java/me/oldboy/config/WebContextInitializer.java) - файл конфигурирующий контекст приложения;
- [UserRestController.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJavax/src/main/java/me/oldboy/controller/UserRestController.java) - файл контроллера - взаимодействие пользователя приложением и нашего сервиса (приложения);
- [User.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJavax/src/main/java/me/oldboy/entity/User.java) - сущность с которой работает приложение;
- [UserReadDto.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJavax/src/main/java/me/oldboy/dto/UserReadDto.java) - объект передачи данных между слоями приложения;
- [UserRepository](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJavax/src/main/java/me/oldboy/repository/UserRepository.java) - "магический" интерфейс репозитория - взаимодействие приложения и БД;
- [UserService.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJavax/src/main/java/me/oldboy/service/UserService.java) - слой бизнес логики;

Конфигурация зависимостей:
- [build.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJavax/build.gradle) - описание зависимостей (из нового нам понадобились):

      /* Подключим зависимость Swagger-a */
      implementation 'io.springfox:springfox-swagger2:3.0.0'
      implementation 'io.springfox:springfox-swagger-ui:3.0.0'
      implementation 'io.springfox:springfox-oas:3.0.0'

      /* Зависимость отвечающая за работу аннотаций и сервлетов */
      compileOnly 'javax.servlet:javax.servlet-api:4.0.1'
      implementation 'javax.servlet:javax.servlet-api:4.0.1'
      implementation 'javax.validation:validation-api:2.0.1.Final'

- [version.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJavax/version.gradle) - версии зависимостей;

Приложение разворачивается локально. Для запуска приложения нужен web-сервер приложений или контейнер сервлетов -
используем TomCat 9 или 10.

Для тестирования прохождения запросов применяем POSTMAN или Swagger UI (у нас 4-и конечных точки):

- http://localhost:8080/v2/users/5 (GET запрос с применением @GetMapping и @PathVariable - посмотреть user-a);
- http://localhost:8080/v2/users (POST запрос с применением аннотаций @PostMapping - создать user-a);
- http://localhost:8080/v2/users (GET запрос позволяющий получить всех пользователей из БД);
- http://localhost:8080/v2/users/5 (DELETE запрос с применением аннотации @DeleteMapping - удалить user-a по ID);
________________________________________________________________________________________________________________________
Интересные материалы по сути вопроса:
- [Springfox Documentation](https://springfox.github.io/springfox/docs/current/);
- [Automated JSON API documentation for API's built with Spring (со ссылками на документацию и примеры)](https://springfox.github.io/springfox/);

Вопрос-ответ (https://stackoverflow.com/):
- [A 'simple' way to implement Swagger in a Spring MVC application.](https://stackoverflow.com/questions/26720090/a-simple-way-to-implement-swagger-in-a-spring-mvc-application)
- [Added Springfox Swagger-UI and it's not working, what am I missing?](https://stackoverflow.com/questions/46151540/added-springfox-swagger-ui-and-its-not-working-what-am-i-missing/64333853#64333853)
- [Swagger and Spring MVC (non-spring-boot).](https://stackoverflow.com/questions/77350821/swagger-and-spring-mvc-non-spring-boot)
________________________________________________________________________________________________________________________
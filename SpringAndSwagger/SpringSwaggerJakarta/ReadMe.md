Одним из вариантов документирования (и не только) конечных точек (Endpoint) нашего REST API может быть интеграция приложения
его с фреймворком Swagger UI. Спецификация OpenAPI предоставляет один из способов описания REST API, а Swagger и подобные
ему фреймворки предоставляют нам удобный способ реализации этой спецификации. 
________________________________________________________________________________________________________________________
### 4 and 10/4 ((non Boot) Spring application integration with SpringDoc OpenApi 3.0 - "Swagger")

На данном шаге применим более свежий набор библиотек SpringDoc-OpenApi, который помогает автоматизировать создание 
документации API с использованием проектов Spring Framework и Spring Boot. ([см. документацию](https://springdoc.org/)).
Судя по состоянию [страницы проекта на GitHub](https://github.com/springdoc/springdoc-openapi), проект поддерживается 
разработчиками и обновлялась. На этот раз в своем коде библиотека использует функционал пакета [jakarta.servlet](https://jakarta.ee/specifications/servlet/), которая совместима с
текущей версией [Spring Framework 6.2.2](https://spring.io/projects/spring-framework) и следуя документации, подсказкам на [Stack Overflow](https://stackoverflow.com/), 
начинающий разработчик сможет настроить свой Spring (non Boot) проект на взаимодействие с библиотекой SpringDoc. Естественно, 
теперь выбор контейнера сервлетов для развертывания нашего приложения однозначен - не ниже TomCat 10. 

На данном шаге (как и ранее) нам нужно:
- Интегрировать SpringDoc-OpenApi и наше SpringWebMvc (non Boot) приложение.
- Настроить и получить доступ к JSON описанию API наших конечных точек - http://localhost:8080/v3/api-docs
- Настроить и получить доступ к интерфейсу Swagger UI - http://localhost:8080/swagger-ui/index.html или http://localhost:8080/swagger-ui.html
- Проверить работоспособность нашего контроллера используя интерфейс Swagger-a;

Структура проекта:
- config:
  - [YamlPropertySourceFactory](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJakarta/src/main/java/me/oldboy/config/yml_properties_reader/YamlPropertySourceFactory.java) - файл позволяющий читать данные из нашего application.yml;
  - [DataSourceConfig.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJakarta/src/main/java/me/oldboy/config/DataSourceConfig.java) - файл конфигурирующий связь приложения с БД;
  - [OpenApiConfig.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJakarta/src/main/java/me/oldboy/config/OpenApiConfig.java) - файл конфигурирующий настройки OpenApi (и Swagger UI);
  - [WebContextInitializer.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJakarta/src/main/java/me/oldboy/config/WebContextInitializer.java) - файл конфигурирующий контекст приложения;
- [UserRestController.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJakarta/src/main/java/me/oldboy/controller/UserRestController.java) - файл контроллера - взаимодействие пользователя приложением и нашего сервиса (приложения);
- [User.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJakarta/src/main/java/me/oldboy/entity/User.java) - сущность с которой работает приложение;
- [UserReadDto.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJakarta/src/main/java/me/oldboy/dto/UserReadDto.java) - объект передачи данных между слоями приложения;
- [UserRepository](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJakarta/src/main/java/me/oldboy/repository/UserRepository.java) - "магический" интерфейс репозитория - взаимодействие приложения и БД;
- [UserService.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJakarta/src/main/java/me/oldboy/service/UserService.java) - слой бизнес логики;

Конфигурация зависимостей:
- [build.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJakarta/build.gradle) - описание зависимостей (из нового нам понадобились):

      plugins {
        id "org.springframework.boot" version "3.1.2"
        id "org.springdoc.openapi-gradle-plugin" version "1.9.0"
      }

      /* Подключим зависимость Swagger-a */
      implementation "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.4.0"

      /* Зависимость отвечающая за работу аннотаций и сервлетов */
      implementation "jakarta.servlet:jakarta.servlet-api:6.1.0"
      implementation "jakarta.persistence:jakarta.persistence-api:3.2.0"
      implementation "jakarta.annotation:jakarta.annotation-api:3.0.0"

- [version.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringAndSwagger/SpringAndSwagger/SpringSwaggerJakarta/version.gradle) - версии зависимостей;

Из особенностей настройки взаимосвязи (non Boot) Spring приложения со SpringDoc-OpenApi это, то что мы применяем
библиотеку, которая использует и транзитивно подтягивает Spring Boot зависимости, что может слегка обескуражить новичка. 
Отсюда вытекает интересный момент - мы можем применять аннотации авто-конфигурирования в файлах настроек приложения:

    @Slf4j
    @Configuration
    @EnableWebMvc
    @EnableAutoConfiguration
    @ComponentScan(basePackages = "me.oldboy.controller")
    public class OpenApiConfig implements WebMvcConfigurer {
      // some code
    }

Так же нам необходимо в файл build.gradle добавить два важных плагина позволяющих взаимодействовать с применяемой библиотекой см. выше. 

Приложение разворачивается локально. Для запуска приложения нужен web-сервер приложений или контейнер сервлетов - используем TomCat 10 или выше.

Для тестирования прохождения запросов применяем POSTMAN или Swagger UI (у нас 4-и конечных точки):

- http://localhost:8080/v2/users/5 (GET запрос с применением @GetMapping и @PathVariable - посмотреть user-a);
- http://localhost:8080/v2/users (POST запрос с применением аннотаций @PostMapping - создать user-a);
- http://localhost:8080/v2/users (GET запрос позволяющий получить всех пользователей из БД);
- http://localhost:8080/v2/users/5 (DELETE запрос с применением аннотации @DeleteMapping - удалить user-a по ID);
________________________________________________________________________________________________________________________
Интересные материалы по текущему вопросу:
- [SpringDoc-OpenApi Documentation](https://springdoc.org/);
- [GitHub проекта](https://github.com/springdoc);
- [OpenAPI Specification](https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md);
- [Примеры использования](https://github.com/springdoc/springdoc-openapi-demos);
- [Documenting a Spring REST API Using OpenAPI 3.0](https://www.baeldung.com/spring-rest-openapi-documentation);

Вопрос-ответ, близко по теме (https://stackoverflow.com/):
- [Spring Boot 3 springdoc-openapi-ui doesn't work.](https://stackoverflow.com/questions/74701738/spring-boot-3-springdoc-openapi-ui-doesnt-work)
- [How to Integrate Open API 3 with Spring project (not Spring Boot) using springdoc-openapi.](https://stackoverflow.com/questions/59871209/how-to-integrate-open-api-3-with-spring-project-not-spring-boot-using-springdo)
- [How to run Swagger 3 on Spring Boot 3.](https://stackoverflow.com/questions/74614369/how-to-run-swagger-3-on-spring-boot-3)
________________________________________________________________________________________________________________________

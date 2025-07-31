### StepFive (Spring boot application)
Основная сквозная задача проекта: Разработать приложение для управления коворкинг-пространством. Приложение должно 
позволять пользователям бронировать рабочие места, конференц-залы, а также управлять бронированиями и просматривать 
доступность ресурсов (в смысле свободные рабочие места и залы).

Теперь, то что было описано и проделано на предыдущих шагах:

- [StepOne](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne);
- [StepTwo](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo);
- [StepThree](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree);
- [StepFour](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour);

Снова нужно переписать, но уже с применением Spring Boot технологии.

#### Функциональные и технические требования текущего шага (обновления предыдущих решений):
- Spring Boot 3.2 (и выше);
- Интегрировать систему документирования Swagger (Spring Doc);
- Aspect-s аудита и логирования (расчета времени работы метода) вынести в отдельные стартеры, сделать отдельными модулями;
- Один стартер должен автоматически подключаться, второй через аннотацию;
- Приложение полностью управляется Spring-ом, максимально возможное покрытие тестами;

________________________________________________________________________________________________________________________
#### Реализация:
- Функциональные требования выполнены полностью.
- Технические требования выполнены полностью.
- [Тестами покрыто](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFive/src/test) более 97% кода.

________________________________________________________________________________________________________________________
#### Особенности:
К реализации стартеров мы подошли "слегка фривольно". Поскольку у нас один стартер должен все делать автоматически, это
означает следующее - его аспект(ты), запускаются реагируя на некое заранее известное сочетание правил настройки pointcut-ов.
Например, наши точки среза будут работать со слоем сервисов и значит нам нужно четко описать "правило среза". Далее, 
настройка - автоматическое подключение означает, что наш стартер начнет сразу работать с основным приложением, без каких
либо настроек в properties.yml (application.properties), т.е. у нас не будет явно выделенного файла настроек и условий.

Поскольку мы понимаем, что должно происходить, мы отступим от задачи и сделаем все то-же, но с внешними настройками и 
аннотациями, т.е. наши аспекты будут активироваться, как и раньше - по выставленной над методом аннотацией и будут 
отключаться и подключаться по желанию пользователя.
________________________________________________________________________________________________________________________
#### Описание

- Структура основного проекта не изменилась (только выделены аспекты) - ["основной проект"](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFive/src/main/java/me/oldboy).
- Тесты [интеграционные](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFive/src/test/java/me/oldboy/integration), [unit (2-a вида)](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFive/src/test/java/me/oldboy/unit).
- [Swagger, как и ранее](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour#swagger).
- Ключевые endpoint-ы можно изучить и протестировать в Swagger-e или использовать [Postman и приведенные ссылки](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFour#%D0%BF%D0%B0%D1%80%D0%B0%D0%BC%D0%B5%D1%82%D1%80%D1%8B-%D0%B7%D0%B0%D0%BF%D1%80%D0%BE%D1%81%D0%BE%D0%B2-%D0%BA-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D1%8E-api).

Модуль-стартер (измеритель времени работы метода):
- [time-logger-spring-boot-starter](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepFive/time-logger-spring-boot-starter):
  - annotation:
    - [Measurable.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFive/time-logger-spring-boot-starter/src/main/java/me/oldboy/logger/annotation/Measurable.java) - аннотация для пометки методов требующих расчета времени выполнения; 
  - config:
    - [LoggerAutoConfiguration.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFive/time-logger-spring-boot-starter/src/main/java/me/oldboy/logger/config/LoggerAutoConfiguration.java) - файл авто-конфигурации в нем создаются bean-ы нашего стартера (см. аннотации);
    - [LoggerProperties.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFive/time-logger-spring-boot-starter/src/main/java/me/oldboy/logger/config/LoggerProperties.java) - файл свойств, определяющий работу стартера сообразно настройкам application.properties (*.yml); 
  - measurer:
    - [MethodSpeedCalcAspect.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFive/time-logger-spring-boot-starter/src/main/java/me/oldboy/logger/measurer/MethodSpeedCalcAspect.java) - класс определяющий наши "точки среза" и advise-ы;
- resources:
  - META-INF:
    - [spring.factories](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFive/time-logger-spring-boot-starter/src/main/resources/META-INF/spring.factories) - файл метаданных, определяет классы авто-конфигурации, в нем указываются файлы описывающие структуру bean-ов нашего стартера, прописываем все @Configuration файлы стартера; 
- [build.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepFive/time-logger-spring-boot-starter/build.gradle) - файл настроек стартера;

Модуль-стартер (аудитор действий пользователя, аннотируем слой сервисов):
- audit-writer-spring-boot-starter:
  - config:
      - AuditAutoConfiguration.java - файл авто-конфигурации (см. выше или MakeLoggerFor.md в качестве примера); 
      - AuditProperties.java - класс определяющий свойства настройки стартера;
  - core:
    - annotation:
      -  Auditable.java - аннотация которой помечаются "удируемые" методы (создание, удаление, изменения и т.д.) слоя сервисов;
    - entity:
      - operations:
        - AuditOperationResult.java - enum описывающий возможные результаты действий пользователя;
        - AuditOperationType.java - enum описывающий возможные типы действий пользователя;
      -  Audit.java - сущность, которая будет сохраняться в БД;
    - auditing:
      - AuditingAspect.java - класс описывающий логику аспекта аудита;
    - repository:
      - AuditRepository.java - интерфейс определяющий связь с БД;
    - service:
      - AuditService.java - сервис описывающий логику сохранения данных в БД;
- resources:
  - META-INF.spring:
    - org.springframework.boot.autoconfigure.AutoConfiguration.imports - файл метаданных (еще один вариант см. MakeLoggerFor.md), его вид зависит от версии Spring-a, описывает файлы авто-конфигурации для "сканера" Spring-a;
- build.gradle - файл настроек стартера;

________________________________________________________________________________________________________________________
И так, у нас есть многомодульный проект коворкинг-сервиса описанный по шагам с простого Java проекта, до Spring приложения.

Что дальше? Микросервисная архитектура.
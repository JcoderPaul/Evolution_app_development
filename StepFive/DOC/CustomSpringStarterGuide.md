### Создание custom Spring Starter-a (пошаговый пример)

Кастомный Spring Starter — это библиотека, которая упрощает настройку Spring Boot приложений, автоматически подключая 
зависимости, конфигурации и бины. Это особенно полезно для повторного использования функциональности в нескольких 
проектах, например, для настройки логирования, безопасности или интеграции с внешними сервисами. В этом руководстве 
мы создадим простой стартер для сервиса приветственных сообщений (`GreeterService`), который будет автоматически 
настраиваться в приложении.

________________________________________________________________________________________________________________________
### Предварительные требования
- **JDK**: 17 или выше.
- **Система сборки**: Maven или Gradle (в примере используется Maven).
- **IDE**: IntelliJ IDEA.
- **Знания**: Базовое понимание Spring Boot и аннотаций, таких как `@Configuration`, `@Bean`.

________________________________________________________________________________________________________________________
### Обзор структуры
Кастомный стартер обычно состоит из двух модулей:

1. **Модуль автоконфигурации** (`library-name-spring-boot-autoconfigure`): Содержит логику автоконфигурации, включая классы свойств и бины.
2. **Модуль стартера** (`library-name-spring-boot-starter`): Содержит зависимости, необходимые для работы автоконфигурации.

Для нашего примера структура проекта будет следующей:

```
greeter-spring-boot-starter-project
├── greeter-spring-boot-autoconfigure
│   ├── src/main/java/com/example/greeter/autoconfigure
│   │   ├── GreeterProperties.java
│   │   ├── GreeterService.java
│   │   ├── GreeterAutoConfiguration.java
│   ├── src/main/resources
│   │   ├── META-INF/spring
│   │   │   ├── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   ├── pom.xml
├── greeter-spring-boot-starter
│   ├── pom.xml
├── pom.xml (родительский)
```

________________________________________________________________________________________________________________________
### Шаг 1: Создание многомодульного проекта
Создайте многомодульный Maven проект с помощью Spring Initializr или вручную. Родительский `pom.xml` определяет модули и общие зависимости.

________________________________________________________________________________________________________________________
#### Родительский POM-файл

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>greeter-spring-boot-starter-project</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>greeter-spring-boot-autoconfigure</module>
        <module>greeter-spring-boot-starter</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <spring-boot.version>3.3.2</spring-boot.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.10.1</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```
________________________________________________________________________________________________________________________
### Шаг 2: Создание модуля автоконфигурации
Модуль `greeter-spring-boot-autoconfigure` содержит логику автоконфигурации, включая классы свойств, сервиса и конфигурации.

________________________________________________________________________________________________________________________
#### POM-файл модуля автоконфигурации

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.example</groupId>
        <artifactId>greeter-spring-boot-starter-project</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>greeter-spring-boot-autoconfigure</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```
________________________________________________________________________________________________________________________
#### Класс свойств `GreeterProperties`
Этот класс обрабатывает пользовательские настройки из `application.properties`.

      package com.example.greeter.autoconfigure;
      
      import org.springframework.boot.context.properties.ConfigurationProperties;
      
      @ConfigurationProperties(prefix = "greeter")
      public class GreeterProperties {
          private String message;
      
          public String getMessage() {
              return message;
          }
      
          public void setMessage(String message) {
              this.message = message;
          }
      }

________________________________________________________________________________________________________________________
#### Класс сервиса `GreeterService`
Этот класс содержит основную функциональность стартера.

      package com.example.greeter.autoconfigure;
      
      public class GreeterService {
          private final String message;
      
          public GreeterService(String message) {
              this.message = message;
          }
      
          public String greet() {
              return message != null ? message : "Hello, World!";
          }
      }

________________________________________________________________________________________________________________________
#### Класс автоконфигурации `GreeterAutoConfiguration`
Этот класс создает бин `GreeterService` на основе свойств.

      package com.example.greeter.autoconfigure;
      
      import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
      import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
      import org.springframework.boot.context.properties.EnableConfigurationProperties;
      import org.springframework.context.annotation.Bean;
      import org.springframework.context.annotation.Configuration;
      
      @Configuration
      @ConditionalOnClass(GreeterService.class)
      @EnableConfigurationProperties(GreeterProperties.class)
      public class GreeterAutoConfiguration {
      
          private final GreeterProperties properties;
      
          public GreeterAutoConfiguration(GreeterProperties properties) {
              this.properties = properties;
          }
      
          @Bean
          @ConditionalOnMissingBean
          public GreeterService greeterService() {
              return new GreeterService(properties.getMessage());
          }
      }

________________________________________________________________________________________________________________________
#### Регистрация автоконфигурации
Для Spring Boot 2.7 и выше создайте файл `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` с содержимым:

      com.example.greeter.autoconfigure.GreeterAutoConfiguration

Для более ранних версий используйте `src/main/resources/META-INF/spring.factories`:

      org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.example.greeter.autoconfigure.GreeterAutoConfiguration

________________________________________________________________________________________________________________________
### Шаг 3: Создание модуля стартера
Модуль `greeter-spring-boot-starter` — это точка входа для пользователей, включающая зависимости.

________________________________________________________________________________________________________________________
#### POM-файл стартера

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.example</groupId>
        <artifactId>greeter-spring-boot-starter-project</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>greeter-spring-boot-starter</artifactId>

    <dependencies>
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>greeter-spring-boot-aut atonfigure</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
    </dependencies>
</project>
```
________________________________________________________________________________________________________________________
### Шаг 4: Использование стартера в приложении

1. **Добавьте зависимость** в `pom.xml` вашего Spring Boot приложения:

```xml   
<dependency>
    <groupId>com.example</groupId>
    <artifactId>greeter-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

2. **Настройте свойства** в `application.properties`:

```
greeter.message=Hello from Custom Starter!
```

3. **Используйте сервис** в приложении:


      package com.example.demo;
      
      import com.example.greeter.autoconfigure.GreeterService;
      import org.springframework.beans.factory.annotation.Autowired;
      import org.springframework.boot.SpringApplication;
      import org.springframework.boot.autoconfigure.SpringBootApplication;
      import org.springframework.web.bind.annotation.GetMapping;
      import org.springframework.web.bind.annotation.RestController;
      
      @SpringBootApplication
      public class DemoApplication {
      
          @Autowired
          private GreeterService greeterService;
      
          public static void main(String[] args) {
              SpringApplication.run(DemoApplication.class, args);
          }
      
          @GetMapping("/greet")
          public String greet() {
              return greeterService.greet();
          }
      }

________________________________________________________________________________________________________________________
### Шаг 5: Сборка и тестирование
1. **Соберите проект**:
   - Выполните команду `mvn clean install` в корне проекта.
2. **Запустите приложение**:
   - Проверьте, что эндпоинт `/greet` возвращает настроенное сообщение (например, "Hello from Custom Starter!").

________________________________________________________________________________________________________________________
### Шаг 6: Дополнительные рекомендации
- **Именование**: Следуйте конвенциям, называя модули `library-name-spring-boot-autoconfigure` и `library-name-spring-boot-starter`. Избегайте префикса `spring-boot` в названии.
- **Условные бины**: Используйте аннотации `@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty` для гибкой настройки.
- **Документация**: Создайте `README.md` с инструкциями по использованию стартера.
- **Публикация**: Опубликуйте стартер в Maven Central или локальном репозитории для удобного подключения.
- **Метаданные**: Используйте `spring-boot-configuration-processor` для генерации метаданных свойств, чтобы обеспечить поддержку IDE.

________________________________________________________________________________________________________________________
### Возможные проблемы и решения
| Проблема                                   | Решение                                                                                                                                                               |
|--------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Бин `ConfigurationProperties` не создается | Убедитесь, что `@EnableConfigurationProperties` добавлен в класс автоконфигурации, и проверьте правильность префикса в `@ConfigurationProperties`.                    |
| Автоконфигурация не подключается           | Проверьте наличие и правильность файла `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (или `spring.factories` для старых версий). |
| Ошибка зависимостей                        | Убедитесь, что все зависимости в стартере помечены как `optional`, если они не обязательны.                                                                           |

________________________________________________________________________________________________________________________
### Ресурсы для дальнейшего изучения
- [Baeldung: Creating a Custom Starter with Spring Boot](https://www.baeldung.com/spring-boot-custom-starter)
- [GeeksforGeeks: Spring Boot 3 - Creating a Custom Starter](https://www.geeksforgeeks.org/spring-boot-3-creating-a-custom-starter/)
- [Spring Boot Documentation: Auto-Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.auto-configuration)
- [GitHub: Spring Boot Master Auto-Configuration Demo](https://github.com/snicoll-demos/spring-boot-master-auto-configuration)
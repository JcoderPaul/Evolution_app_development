### StepTwo

Задача: Разработайте приложение для управления коворкинг-пространством. Приложение должно позволять пользователям 
бронировать рабочие места, конференц-залы, а также управлять бронированиями и просматривать доступность ресурсов.
Т.е. все то, что было описано и проделано в [StepOne](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne).

Но теперь мы добавляем новые технологии:
- [JDBC](https://github.com/JcoderPaul/JDBC_Practice);
- Миграционный фреймворк для БД - [Liquibase](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_14);
- Docker для БД ([описание использования и примеры для Spring Boot](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_13), отличается от текущего);
- Test-containers ([описание использования и примеры для Spring Boot](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_13#lesson-65---%D1%82%D0%B5%D1%81%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5-postgresql-%D0%B1%D0%B4-%D1%80%D0%B0%D0%B7%D0%B2%D0%B5%D1%80%D0%BD%D1%83%D1%82%D0%BE%D0%B9-%D0%B2-docker-%D0%BA%D0%BE%D0%BD%D1%82%D0%B5%D0%B9%D0%BD%D0%B5%D1%80%D0%B5), реализация отличается от текущего);

Время на выполнение 2.5 дня.

#### Функциональные и технические требования для обновленного проекта см. [ReadMe от StepOne](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepOne/ReadMe.md): 
- Репозитории теперь должны писать ВСЕ сущности в БД PostgreSQL;
- Идентификаторы при сохранении в БД должны выдаваться через sequence;
- DDL-скрипты на создание таблиц и скрипты на предзаполнение таблиц должны выполняться только инструментом миграции Liquibase;
- Скрипты миграции Liquibase должны быть написаны в нотации XML или YAML (SQL);
- Скриптов миграции должно быть несколько:
  - Создание всех таблиц;
  - Предзаполнение данными;
- Служебные таблицы должны быть в отдельной схеме;
- Таблицы сущностей хранить в схеме public запрещено;
- В тестах необходимо использовать test-containers;
- В приложении должен быть docker-compose.yml:
  - В котором должны быть прописаны инструкции для развертывания postgres БД в докере; 
  - Логин, пароль к БД должны быть отличными от тех, что прописаны в образе по-умолчанию;
  - Приложение должно работать с БД, развернутой в докере с указанными параметрами;
- Приложение должно поддерживать конфиг-файлы: 
  - Всё, что относится к подключению БД;
  - Настройки и инструкции к миграциям, должно быть сконфигурировано через конфиг-файл;

#### Реализация:
- Слот в текущей реализации имеет длину 60 мин. и таковых 9 шт. (взяли лучшее из обеих версий StepOne).
- Функциональные требования выполнены полностью.
- Технические требования выполнены полностью.
- После запуска системы в ней уже присутствует пользователь с логином Admin, с его помощью можно проверить работу 
функционала приложения в разделе CRUD операций, теперь только Admin может создавать/удалять/обновлять:
  - места/залы (ресурс для резервирования);
  - слоты (время резервирования);
- Первый вход в систему с придуманным именем автоматом регистрирует пользователя как user-a без прав админа, 
CRUD операции ему не доступны;
- Для работы с приложением следовать инструкциям меню;

Структура проекта (это не Spring проект, но слоистую структуру можно применять где угодно, отсюда и названия):
- [config](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo/src/main/java/me/oldboy/cwapp/config):
  - [connection](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo/src/main/java/me/oldboy/cwapp/config/connection) - папка содержит классы отвечающие за связь с БД по JDBC;
  - [context](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo/src/main/java/me/oldboy/cwapp/config/context) - папка с классом 'связкой' всех зависимостей в проекте;
  - [liquibase](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo/src/main/java/me/oldboy/cwapp/config/liquibase) - папка с классом управляющим работой миграционного фреймворка Liquibase;
- [core](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo/src/main/java/me/oldboy/cwapp/core):
  - [controllers](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo/src/main/java/me/oldboy/cwapp/core/controllers) - классы управляющие манипуляцией с сущностями на 'верхнем' уровне приложения;
  - [entity](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo/src/main/java/me/oldboy/cwapp/core/entity) - основные рабочие элементы проекта с которыми происходят манипуляции (залы/рабочие места, 
слоты времени для резервирования, пользователи, записи о резервировании содержащие сведения о том, что, когда, 
и на сколько было зарезервировано);
  - [service](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo/src/main/java/me/oldboy/cwapp/core/service) - классы отвечающие за обработку запросов с уровня контроллеров;
  - [repository](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo/src/main/java/me/oldboy/cwapp/core/repository) - классы работающие с БД PostgeSQL, через JDBC;
- [cui](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo/src/main/java/me/oldboy/cwapp/cui) (консольный интерфейс пользователя):
  - [items](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo/src/main/java/me/oldboy/cwapp/cui/items) - классы описывающие разделы общего меню;
  - [MainMenu.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepTwo/src/main/java/me/oldboy/cwapp/cui/MainMenu.java) - класс основного меню;
- [exceptions](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo/src/main/java/me/oldboy/cwapp/exceptions) - папка с исключениями;
- [CwMainApp.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepTwo/src/main/java/me/oldboy/cwapp/CwMainApp.java) - запускаемый модуль.

#### [Тесты](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo/src/test/java/me/oldboy/cwapp) согласно расчетам IDE покрывают:
  - Классы 97% (70/72);
  - Методы 97% (392/402);
  - Строки кода 91% (2042/2240);

[Тестирование проводилось](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo/src/test/java/me/oldboy/cwapp) в двух вариантах: 
- тестирование с применением [Test-containers](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo/src/test/java/me/oldboy/cwapp/core/repository);
- тестирование с применением [фреймворка Mockito](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepTwo/src/test/java/me/oldboy/cwapp/core/controllers);

Наиболее интересными моментами в тестировании являются ситуации когда Unit тесты вроде бы покрывают большинство возможных 
ситуаций, однако при проведении ручного тестирования (или интеграционных тестов, без Mock-заглушек) выявляются некие 
проблемы при обращении к БД. Например, данные при обращении с уровня репозиториев четко ложатся в базу, однако при 
отправлении тех же запросов (добавление user-a, slot-a и т.п.) с уровня контроллеров - фиксируются БД, но не заносятся в 
нее или другим языком не коммитятся. 

Проблема решается просто! 

### Хочешь быть уверенным в четкой реализации твоих действий, сам: открой транзакцию, сделай запрос, закрой транзакцию!

В коде это выглядит так:

    @Override
    public boolean deletePlace(Long placeId) {
      Boolean isDeleteCorrect = false;
        try(PreparedStatement prepareStatement = connection.prepareStatement(DELETE_PLACE_BY_ID_SQL)) {
          prepareStatement.setLong(1, placeId);
          isDeleteCorrect = prepareStatement.executeUpdate() > 0;
          connection.commit(); // Без данной строки мы можем и не зафиксировать изменения вносимые в БД
        } catch (SQLException sqlException) {
          sqlException.printStackTrace();
        }
      return isDeleteCorrect;
    }

### Применение Docker.

В данном случае нам сразу намекнули, что нужен docker-compose файл, а если без него, то наши действия будут следующими:
- получение образа БД PostgreSQL 13, с установкой имени и передача в него наших login/password, а так же проброска портов:

      docker run --name cw_db -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -p 5436:5432 -d postgres:13

- получение образа wui PgAdmin4, с установкой имени и передача в него наших login/password, а так же проброска портов:

      docker run --name pgadmin_ui_db -e PGADMIN_DEFAULT_EMAIL=admin@pgadmin.com -e PGADMIN_DEFAULT_PASSWORD=password -p 5050:80 -d dpage/pgadmin4
  
- далее стандартные настройки.

### Применение Docker-Compose.

Применение декларативного подхода упрощает настройку проекта - [docker-compose.yaml](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepTwo/docker-compose.yaml).
Фактически в одном файле мы описываем, что будет происходить при его запуске (см. комментарии) командой: 

      docker-compose up

Файл [.env](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepTwo/.env) описывает переменные, 
которые будут переданы в контейнеры (login / password)

Самым полезным и интересным является элемент [Volumes](https://docs.docker.com/compose/compose-file/07-volumes/), в котором мы можем настроить 'проекцию' нашей базы и нашего PgAdmin4
в структуре текущего проекта. Это позволяет сохранить данные базы и настройки UI в случае удаления контейнеров. Как это сделать,
чуть подробнее ниже:

- Изучим структуру нашего Docker контейнера с БД, используем команду:
  
      docker inspect cw_db

Получаем данные в формате JSON описывающие наш контейнер. Нас интересует раздел Volumes, и где он определен:

    "Volumes": {
    "/var/lib/postgresql/data": {}
    }

Именно по этому пути будут храниться данные нашей базы внутри контейнера. Теперь нам нужно посмотреть где будет 
смонтирована (спроецирована) база внутри виртуальной машины Docker-a, это раздел Mount:

    "Mounts": [
        {
              "Type": "bind",
              "Source": "E:\\EvolutionOfJavaApp\\StepTwo\\db-data",
              "Destination": "/var/lib/postgresql/data",
              "Mode": "rw",
              "RW": true,
              "Propagation": "rprivate"
        }
    ],

Поскольку мы уже прописали необходимый путь в [docker-compose.yaml](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepTwo/docker-compose.yaml), то его мы и видим в разделе "Source". Но обычно, если 
предварительных настроек раздела Volumes не проводилось, то можно увидеть другую картину в секции "Source", например:

    "Mounts": [
        {
              "Type": "volume",
              "Name": "00568d19d90a7015efb42e1e8a47c3baaf58c4a39763287ca29fd69449a22855",
              "Source": "/var/lib/docker/volumes/00568d19d90a7015efb42e1e8a47c3baaf58c4a39763287ca29fd69449a22855/_data",
              "Destination": "/var/lib/postgresql/data",
              "Driver": "local",
              "Mode": "",
              "RW": true,
              "Propagation": ""
        }
    ],

Таким образом используя синтаксис [docker-compose файла](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepTwo/docker-compose.yaml): 

    volumes:
      - ./db-data:/var/lib/postgresql/data

Мы настраиваем проекцию (монтаж) нашей рабочей базы в конкретное интересующее нас место, в данном случае в папку 'db-data'
корня нашего проекта. Для проверки работоспособности или полезности данной опции можно удалить контейнер с БД в которую уже 
внесены некие данные, а затем повторно смонтировать новый контейнер, после обратиться к БД и мы увидим, что все ранее 
внесенные данные сохранились и доступны. Теперь тот же фокус проделаем с контейнером в котором находится PgAdmin4 и так же
проводим настройку раздела Volumes в [compose.yaml файле](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepTwo/docker-compose.yaml) с указанием папки куда будет смонтирован образ нужных нам разделов.

### Настройка соединения PgAdmin4 с БД PostgreSQL 13 развернутых в контейнере.

И так, у нас есть два контейнера с БД PostgreSQL 13 и программой WebUI PgAdmin4 для работы с теми же БД. Контейнер с PgAdmin4 
тоже требовал предварительной настройки: инициализация логина и пароля (см. [файл .env](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepTwo/.env)), проброска портов во вне, 
в нашем случае, для данного контейнера это:

    ports:
      - "5050:80"

Т.е. при обращении через браузер к локальному адресу и порту: [http://127.0.0.1:5050/](http://127.0.0.1:5050/) мы попадем 
на станицу аутентификации PgAdmin4, где вводим нами же заданный логин/пароль и получаем доступ web интерфейсу позволяющему
легко и удобно работать с нашей БД. Осталось ее подключить:
- Вводим email и пароль из .env файла:

![Auth menu](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepTwo/JPG/PgAdminMenu.png)


### 2. Docker контейнер с переменными окружения (надфилем и шкуркой)

При создании контейнера PostgreSQL мы имеем возможность через переменные среды [-e](https://docs.docker.com/reference/cli/docker/container/create/) задавать пароли и логины к БД. Нам бы хотелось,
чтобы и наше приложение упакованное в контейнер тоже позволяло, как-то взаимодействовать с собой и принимать некие параметры,
для более гибкого взаимодействия с пользователем и внешней БД.

Шаг 1. - Переписываем [temurin.Dockerfile](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-files/temurin.Dockerfile) (добавляем строки с переменными и копированием содержимого WAR архива в папку webapps TomCat-a):

    ENV HIBERNATE_USERNAME=admin HIBERNATE_PASSWORD=admin POSTGRESQL_CONTAINER_NAME=cw_db DB_CONTAINER_PORT=5432 POSTGRES_DB=coworking_db
    COPY cw /usr/local/tomcat/webapps/cw

Тут мы определили и задали параметры по умолчанию, если вдруг пользователь не станет их задавать при монтаже контейнера.
Теперь нам нужно изменить код в нескольких классах и файлах свойств, чтобы получать параметры для нашего приложения из
переменных окружения смонтированного контейнера.

Шаг 2. - Изменяем ConnectionManager используем [System.getenv()](https://docs.oracle.com/javase/tutorial/essential/environment/env.html) (привожу отличия от оригинала настроенного на localhost):

    ...
    private final static String BASEURL_KEY = "jdbc:postgresql://" +
                                              System.getenv("POSTGRESQL_CONTAINER_NAME") + ":" +
                                              System.getenv("DB_CONTAINER_PORT") + "/" +
                                              System.getenv("POSTGRES_DB");
    private final static String LOGIN_KEY = System.getenv("HIBERNATE_USERNAME");
    private final static String PASS_KEY = System.getenv("HIBERNATE_PASSWORD");
    ...

Шаг 3. - Изменяем [HibernateUtil](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/src/main/java/me/oldboy/config/util/HibernateUtil.java) используем [System.getenv()](https://docs.oracle.com/javase/tutorial/essential/environment/env.html) (привожу отличия от оригинала настроенного на localhost и файл свойств):

    ...
    private final static String BASEURL_KEY = "jdbc:postgresql://" +
                                              System.getenv("POSTGRESQL_CONTAINER_NAME") + ":" +
                                              System.getenv("DB_CONTAINER_PORT") + "/" +
                                              System.getenv("POSTGRES_DB");
    private final static String LOGIN_KEY = System.getenv("HIBERNATE_USERNAME");
    private final static String PASS_KEY = System.getenv("HIBERNATE_PASSWORD");

    ...
    
    public static Configuration buildConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.url", BASEURL_KEY);
        configuration.setProperty("hibernate.connection.username", LOGIN_KEY);
        configuration.setProperty("hibernate.connection.password", PASS_KEY);
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Place.class);
        configuration.addAnnotatedClass(Reservation.class);
        configuration.addAnnotatedClass(Slot.class);
        configuration.addAnnotatedClass(Audit.class);

        return configuration;
    }

Шаг 4. - В файле [application.properties](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/src/main/resources/application.properties) удаляем строки жестко задающие параметры соединения с БД (адрес, пароль и логин).

Шаг 5. - В файле [log4j.xml](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/src/main/resources/log4j.xml) переписываем путь для логирования работы приложения в файл внутри контейнера:

    <param name="file" value="logs/app_log_files/logging.log"/>

Шаг 6. - Пересобираем наш WAR архив и забрасываем его содержимое в папку cw.

Шаг 7. - Добавляем в /cw/META-INF/ файл для логирования стека вызовов исключений [logging.properties](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/logging.properties)

Шаг 8. - Запускаем сборку образа из [temurin.Dockerfile](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-files/temurin.Dockerfile) (он меньше чем предыдущий [noble.Dockerfile](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-files/noble.Dockerfile)):

    docker build -t jcoderpaul/webapp:3.0 -f temurin.Dockerfile .

Теперь у нас есть возможность использовать docker-compose файл для сборки всех наших контейнеров, связки их и настройки
последовательности их запуска. После монтажа контейнеров, контейнер БД данных должен запускаться первым, затем наше
приложение - ведь оно должно подключиться к БД, контейнер PgAdmin тоже должен цепляться к БД, но его "падение" не критично.

Если первый [docker-compose](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepTwo/docker-compose.yaml) файл в процессе своего выполнения монтировал volume-ы к БД и PgAdmin в рабочую папку приложения,
то теперь мы настроем [docker-compose](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-compose-file/docker-compose.yaml) файл так, чтобы при создании контейнера БД и PgAdmin их volume-ы монтировались в
/var/lib/docker/volumes виртуальной машины, где запущен Docker сервер (у нас Windows).

Готовим файл [.ENV](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-compose-file/.env) с соответствующими параметрами запуска приложения и доступа к БД (отличных от дефолтных прописанных в Dockerfile).

Шаг 9. - Запускаем наш docker-compose файл:

    docker-compose up

Шаг 10. - Проверяем работоспособность приложения через браузер и Postman. Красота!

Теперь если внести некие изменения в БД: добавить/удалить/изменить пользователя, слот и т.д., а затем намеренно удалить
наш только что созданный compose stack - т.е. все три связных контейнера. А затем снова запустить наш docker-compose файл
мы получим доступ к БД будто никто ничего не удалял. Т.к. все данные записанные в volume разделах соответствующего контейнера,
остаются в сохранности после его удаления. Естественно если никто намеренно не удалил volume раздел БД специальной командой.

### Особенности и недостатки!

Данный метод лучше первого, т.к. мы получаем возможность взаимодействовать с нашим приложением в процессе монтажа контейнера,
а значит можем варьировать входные параметры, такие как адрес, порт, пароль, логин при соединении с БД. Да, при сборке образа
нам приходится использовать некие заготовки и вот тут всплывает интересный недостаток - размер образа (первый вариант образа
был > 760 Мб, текущий > 499 Мб).

Хочется собрать образ еще более компактный, хоть на маненько!

Тут две составляющие: размер базового образа и размер нашего приложения. Мы можем оптимизировать размер кода, и количество
используемых сторонних библиотек. Так же мы можем собрать свой образ с нуля, используя компактные компоненты для каждого слоя.
Выберем второй путь - собираем свой образ!
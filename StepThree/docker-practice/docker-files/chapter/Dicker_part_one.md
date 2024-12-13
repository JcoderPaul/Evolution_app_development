### 1. Docker TomCat 10.1.33 (молотком и стамеской)

Для запуска нашего web-приложения используется контейнер сервелетов TomCat - значит нам нужен образ с данным пакетом
"работающий из коробки" версии 10.0.23 или выше. Идем на [Docker Hub в раздел официальных образов TomCat](https://hub.docker.com/_/tomcat) и выбираем
необходимую нам версию контейнера сервлетов - вполне подойдет версия 10.1.33 (можно выбрать с [JDK17 - она больше](https://github.com/docker-library/tomcat/blob/master/10.1/jdk17/temurin-noble/Dockerfile), или с
[JRE17 - она компактнее](https://github.com/docker-library/tomcat/blob/master/10.1/jre17/temurin-noble/Dockerfile) в два раза) берем ту что больше и роскошнее.  
При чем, это не образы, которые придется тянуть с Docker хаба (когда мы применяем команду [docker pull](https://docs.docker.com/reference/cli/docker/image/pull/)). Это Dockerfile-ы,
которые содержат [набор инструкций для создания Docker-образов](https://docs.docker.com/reference/dockerfile/), вот из них мы и будем собрать нужные нам образы.
Они будут храниться у нас на виртуальной машине, где развернут Docker-сервер (Docker-daemon).

Шаг 1. - Подготовим наше приложение для залива в контейнер, для этого при помощи сборщика проектов, у нас это Gradle,
создадим файл с расширением WAR, например - cw.war. Т.к. у нас Community edition версия IDEA, то для получения такой
"привилегии" нам пришлось использовать соответствующий плагин:

    plugins {
        id 'war'
    }

Шаг 2. - Создаем свой [noble.Dockerfile используя данные с GitHub.](https://github.com/docker-library/tomcat/blob/master/10.1/jdk17/temurin-noble/Dockerfile)

Шаг 3. - Используем команду docker build (задаем TAG):

    docker build -t webapp:3.0 -f noble.Dockerfile .

Шаг 4. - Для корректной работы нашего приложения придется его немного подрихтовать, берем тяжелый инструмент. Сам WAR файл
нам не нужен - это фактически *.rar архив - нам нужно его содержимое. В рабочей папке, которая является еще и контекстом при
создании Docker образов, создаем пакет cw и распаковываем туда содержимое нашего cw.war.

Перед созданием нашего WAR файла мы не вносили изменений в [application.properties](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/src/main/resources/application.properties) и [hibernate.cfg.xml](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/src/main/resources/hibernate.cfg.xml) в разрезе связи с БД - делаем.
Мы точно знаем, что имя контейнера БД - cw_db, а порт 5432 стандартный (нам не понадобится порт проброшенный во вне, т.к. оба
контейнера будут в рамках одной подсети внутри виртуальной машины), поэтому переписываем соответствующие строки в файлах:
- [application.properties](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/src/main/resources/application.properties):

        # Database url
        db.url=jdbc:postgresql://cw_db:5432/coworking_db

- [hibernate.cfg.xml](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/src/main/resources/hibernate.cfg.xml):

        <hibernate-configuration>
            <session-factory>
                <property name="connection.url">jdbc:postgresql://cw_db:5432/coworking_db</property>
                . . .
            </session-factory>
        </hibernate-configuration>

Шаг 5. - Создаем контейнер [из образа созданного на третьем шаге](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-files/noble.Dockerfile) (пробрасываем порт во вне, чтобы достучаться к нашему
приложению из браузера или Postman-a):

    docker run --name WebAppCw -p 8081:8080 -d webapp:3.0

Шаг 6. - Проверяем есть ли вообще созданный нами образ:

    docker ps

Шаг 7. - Глянем на содержимое созданного контейнера (ПОМНИМ ЧТО МЫ ВНУТРИ - WORKDIR, значит мы в /usr/local/tomcat):

    docker exec [container_id у нас это: f817727bb832] f81 ls

Шаг 8. - Проверяем есть ли оболочка shell в контейнере:

    docker exec f81 which sh

Шаг 9. - Заходим внутрь контейнера в интерактивном режиме (т.е. запуск в режиме -it):

    docker exec -it f81 sh

Шаг 10. - Насладившись видами попок, пардон папок и файлов выходим из интерактивного режима во внешний терминал (у нас не Linux):

    exit

Шаг 11. - Копируем пакет cw из контекстной папки внутрь созданного контейнера:

    docker cp cw_v3 f81:/usr/local/tomcat/webapps

Шаг 12. - Подключаем контейнер (WebAppCw - имя даем сами, любое) с развернутым приложением к сети в которой работает наш
PostgreSQL контейнер (ну, да, название сети то еще):

    docker network connect oldboy_coworking_center_step_two_default WebAppCw

Шаг 13. - Перезапускаем контейнер. Заходим в браузер и обращаемся к странице HelloWorld нашего приложения:

    http://localhost:8081/cw/cw_api/hello

Все работает - контейнер приложения связывается с контейнером БД по имени и отдает данные во вне по проброшенному порту.
Дальнейшие тесты приложения работающего из Docker контейнра можно проводить при помощи Postman-a. Грубо, долго, зато понятно,
что происходит.

### Особенности и недостатки!

У данного метода есть масса недостатков и главные из них - многошаговость и "непереносимость"! При случайном удалении
контейнера придется все проделывать с самого начала. Для проделывания всех этих шагов, сторонний разработчик должен
получить наше приложение в виде WAR файла и должен точно знать, что и как настроено в его Docker окружении - не беда,
но тут ведь еще есть и БД со своими особенностями. Короче - не кошерный вариант!

### <span style="color: red"> Логирование ошибок в TomCat </span>

При первичном размещения приложения в контейнере с TomCat оно запускалось, но тут же "падало", без внятных объяснений причин.
Однако на Stackoverflow в статье ["Tomcat: One or more listeners failed to start"](https://stackoverflow.com/questions/48639816/tomcat-one-or-more-listeners-failed-to-start) предложен вариант дополнительного логирования процессов (полная трассировка стека ошибок):

Шаг 1. - В WEB-INF/classes каталоге приложения создайте новый файл: [logging.properties](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/logging.properties).

Шаг 2. - Добавьте в этот файл следующие строки:

    org.apache.catalina.core.ContainerBase.[Catalina].level=INFO
    org.apache.catalina.core.ContainerBase.[Catalina].handlers=java.util.logging.ConsoleHandler

Шаг 3. - Перезапустите tomcat.

Как и положено мы разместили наше приложение в: /usr/local/tomcat/webapps/cw - контейнера, проделываем эти три шага и получаем
наглядные данные что происходит с нашим приложением и почему оно "ложиться" при запуске. В нашем случае это была проблема связи
с контейнером БД, т.к. изначально в приложении использовался "connection.url" с локальным адресом и проброшенным во вне из
контейнера портом, а не подсетью и портами Docker-a, что мы исправили и учли при написании [docker-compose файла](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-compose-file/docker-compose.yaml) и создании нового
WAR архива.
### 3. Оптимизация размера Docker образа (икебана это искусство)

Большую часть работы мы проделали ранее в первых двух наших попытках, осталось "докрутить" до кондиции проделанное. Продолжим
работать с уже подготовленным контекстом для Dockerfile-ов. Наше приложение собрано, для сборки собственного образа нам нужен
базовый слой, и слой с TomCat контейнером сервлетов. Далее нужно написать собственный небольшой Dockerfile, и снова несколько
вариантов:

### 3.1. Вариант первый

Шаг 1. - Качаем архив TomCat и [распаковываем его в](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepThree/docker-practice/docker-files/tomcat) контекстной папке (туда же где и наше приложение).

Шаг 2. - Компонуем наш [my.Dockerfile](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-files/my.Dockerfile) в контекстной папке (базовый слой [alpine:3.21](https://hub.docker.com/_/alpine)):

    FROM alpine:3.21
    RUN apk add openjdk17-jre
    
    COPY tomcat /tomcat
    COPY cw /tomcat/webapps/cw
    
    EXPOSE 8080
    ENTRYPOINT ["/tomcat/bin/catalina.sh"]
    CMD ["run"]

Шаг 3. - Собираем образ из получившегося [Dockerfile-a](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-files/my.Dockerfile):

    docker build -t jcoderpaul/webapp:3.0 -f my.Dockerfile .

Шаг 4. - Монтируем контейнер из полученного образа:

    docker run --name WebApp -p 8081:8080 -d jcoderpaul/webapp:3.0

Шаг 5. - Поскольку мы не пробросили никаких дополнительных параметров, то соединение с БД и т.д. идет по умолчанию,
соединяем с уже существующей сетью, в которой работает контейнер БД (настройки [docker-compose](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepTwo/docker-compose.yaml) из прошлого проекта):

    docker network connect oldboy_coworking_center_step_two_default WebApp

Шаг 6. - Останавливаем контейнер и снова запускаем, чтобы он подключился к БД. Проверяем работоспособность нашего
приложения в браузере и Postman-ом. Все отлично!

В итоге мы получили образ размером **404 Мб.** Экономия **100 Мб** тоже хорошо!

### 3.2. Вариант второй

Предварительно удалим все контейнеры и образы собранные при первой попытке оптимизировать размер образа см. выше.

Шаг 1. - Качаем архив [TomCat](https://dlcdn.apache.org/tomcat/tomcat-10/v10.1.34/bin/apache-tomcat-10.1.34.tar.gz) и распаковываем его в контекстной папке (туда же где и наше приложение). Это шаг мы уже
проделали перед написанием Dockerfile-a для первого варианта см. выше.

Шаг 2. - Компонуем наш [myshort.Dockerfile](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-files/myshort.Dockerfile) в контекстной папке (базовый слой [openjdk:17-alpine](https://hub.docker.com/layers/library/openjdk/17-alpine/images/sha256-a996cdcc040704ec6badaf5fecf1e144c096e00231a29188596c784bcf858d05)).

Шаг 3. - Собираем образ из получившегося [Dockerfile-a](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-files/myshort.Dockerfile):

    docker build -t jcoderpaul/webapp:3.0 -f my.Dockerfile .

Шаг 4. - Монтируем контейнер из полученного образа:

    docker run --name WebApp -p 8081:8080 -d jcoderpaul/webapp:3.0

Шаг 5. - Поскольку мы не пробросили никаких дополнительных параметров, то соединение с БД и т.д. идет по умолчанию,
соединяем с уже существующей сетью, в которой работает контейнер БД (настройки [docker-compose](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepTwo/docker-compose.yaml) из прошлого проекта):

    docker network connect oldboy_coworking_center_step_two_default WebApp

Шаг 6. - Не забываем! Останавливаем контейнер и снова запускаем, чтобы он подключился к БД. Теперь проверяем
работоспособность приложения в браузере и Postman-ом. Все снова работает!

И теперь мы уже получили образ размером **283 Мб.** Экономия составила еще порядка **200 Мб.**
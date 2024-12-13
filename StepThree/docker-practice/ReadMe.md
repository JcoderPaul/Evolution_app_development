### Запуск web-приложения из Docker контейнера.

Наше web приложение готово и теперь нам интересно, как бы его затолкать в контейнер и запустить на любой машине, где 
установлен Docker. Вариантов реализовать данное желание несколько, но начнем с простого - как вообще запустить наше 
приложение в Docker контейнере, хотя бы на той машине где оно написано, оттестировано и гарантировано работает (пока
только из под Intellij IDEA Community edition, но зато наш PostgreSQL развернут в контейнере, прекрасно себя чувствует,
и с приложением чудно взаимодействует). У нас Windows 10, TomCat 10.0.23 и Docker Desktop - ну как говориться, что уж 
есть! Поехали!

1. [Docker TomCat 10.1.33](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-files/chapter/Dicker_part_one.md)
2. [Docker контейнер с переменными окружения](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-files/chapter/Dicker_part_two.md)
3. [Оптимизация размера Docker образа](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-files/chapter/Dicker_part_three.md)

Оптимизированный образ заливаем на [DockerHub](https://hub.docker.com/):

Шаг 1. - Логинимся в системе Docker-a из терминала (из под Windows Docker Desktop залить образ пока проблема):

    docker login

Шаг 2. - Пушим образ в свой репозиторий:

    docker push jcoderpaul/webapp:3.0

Теперь если мы захотим собрать наше приложение на другой машине (отправить его другу, заказчику) мы просто достанем 
образ из DockerHub-a и смонтируем его. А что бы процесс был еще более дружественным используем [docker-compose](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepThree/docker-practice/docker-compose-file/docker-compose.yaml).

Конечно варианты описанные выше так же можно доработать. Например, мы можем разместить на GitHub-e наше приложение. 
Далее создать Dockerfile который бы доставал его оттуда, как и архив TomCat-а с сайта разработчика, далее предварительно 
проделывал бы операции их взаимной компоновки и размещения в отдельный слой образа и сборки того. Но на данном этапе 
задача выполнена.
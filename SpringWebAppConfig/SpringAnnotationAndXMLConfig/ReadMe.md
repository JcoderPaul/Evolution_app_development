Наше WEB приложение, работающее на базе Servlet-ов, должно превратиться в полноценное Spring MVC приложение.
Прежде чем перейти к полноформатному четвертому шагу, т.е. реализовать задуманное, сделаем еще несколько мелких
шажков.
________________________________________________________________________________________________________________________
### 4 and 2/4 (Servlet and Spring Context with XML and annotation config)

На предыдущем маленьком шажке мы использовали для формирования контекста приложения только XML файл. Все связывание 
компонентов и конфигурирование происходило исключительно в нем.

Особенности данного шага: 
- Сконфигурировать наше Spring приложение при помощи аннотаций, на XML файл ложиться минимум (возможность многовариантного 
конфигурирования Spring-а, а также наследование настроек конфигурации, крайне удобная особенность фреймворка).
- Просто получить имя пользователя из некоего источника (в данном случае Java коллекция). 
- Для получения и отображения информации используем функционал Servlet-ов.

Как и на первом микро шаге, демонстрационное приложение - имитация работы более масштабного WEB сервиса. 

Структура проекта осталась неизменной, как и было отмечено ранее:
- [LikeBase.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringAnnotationAndXMLConfig/src/main/java/me/oldboy/base_imitation/LikeBase.java) - имитация БД (добавили аннотацию @Component);
- [UserDao.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringAnnotationAndXMLConfig/src/main/java/me/oldboy/dao_imitation/UserDao.java) - имитация слоя DAO (применили аннотацию @Component и @Autowired);
- [User.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringAnnotationAndXMLConfig/src/main/java/me/oldboy/like_entity/User.java) - сущность с которой работает приложение;
- [ContextBuilder.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringAnnotationAndXMLConfig/src/main/java/me/oldboy/listeners/ContextBuilder.java) - фактически это слушатель, который будет запущен контейнером сервлетов в первую очередь, именно в нем будет вызван Spring контекст;
- [UserService.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringAnnotationAndXMLConfig/src/main/java/me/oldboy/service_imitation/UserService.java) - имитация слоя сервисов (применили аннотацию @Component и @Autowired);
- [GetServletWithSpringContext.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringAnnotationAndXMLConfig/src/main/java/me/oldboy/servlets/GetServletWithSpringContext.java) - сервлет обрабатывающий запрос от пользователя на получение имени User-a по его ID;
- [context.xml](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringAnnotationAndXMLConfig/src/main/resources/context.xml) - файл, как и в прошлом микро проекте задает структуру bean-ов, в текущей версии просто указывает каким 
образом это делать - сканированием содержимого проекта для поиска необходимых аннотаций, хотя мы все еще можем описывать 
тут bean-ы классическим способом;

Конфигурация зависимостей:
- [build.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringAnnotationAndXMLConfig/build.gradle) - для работы нам понадобились две зависимости, как и в прошлый раз;
- [version.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringAnnotationAndXMLConfig/version.gradle) - версии зависимостей;

Приложение разворачивается локально. Для запуска приложения нужен web-сервер приложений или контейнер сервлетов - 
используем TomCat.

Запустив приложение в TomCat и задав в браузере запрос: http://localhost:8080/user?userId=1, получаем ответ в виде 
имени пользователя из БД (коллекции List) если таковой есть: "USER NAME: John". В случае запроса не существующего ID
получаем сообщение вида: "USER NAME: Have no User (Unexpected ID)!". Тут мы не бросаем исключения и соответствующего 
кода, мы исследуем методы формирования контекста.

Более развернутый [пример и описание конфигурирования при помощи аннотаций можно посмотреть тут](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_3).
________________________________________________________________________________________________________________________
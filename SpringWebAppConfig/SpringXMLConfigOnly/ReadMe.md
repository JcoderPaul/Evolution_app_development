Наше WEB приложение, работающее на базе Servlet-ов, должно превратиться в полноценное Spring MVC приложение.
Прежде чем перейти к полноформатному четвертому шагу, т.е. реализовать задуманное, сделаем еще несколько мелких
шажков.
________________________________________________________________________________________________________________________
### 4 and 1/4 (Servlet and Spring Context with XML config only)

Любое Spring приложение можно сконфигурировать несколькими способами (от классического - используя только 
XML, до конфигурирования исключительно средствами Java). При всех вариантах конфигурации ядро приложения 
будет оставаться неизменным, с небольшими вариациями. Поэтому тут, в качестве примера, мы разместим варианты 
мини-приложений имитирующих работу большего, но с упором на наиболее возможные варианты конфигурации оных.

Особенности данного шага: 
- Использовать только XML конфигурацию Spring контекста.
- Просто получить имя пользователя из некоего источника (в данном случае Java коллекция). 
- Для получения и отображения информации используем функционал Servlet-ов.

Еще раз, это имитация работы WEB приложения и основная задача посмотреть как работает XML конфигурация Spring-a, как 
описываются bean-ы, как они интегрируются друг с другом и т.п. 

Структура проекта:
- [LikeBase.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringXMLConfigOnly/src/main/java/me/oldboy/base_imitation/LikeBase.java) - имитация БД;
- [UserDao.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringXMLConfigOnly/src/main/java/me/oldboy/dao_imitation/UserDao.java) - имитация слоя DAO;
- [User.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringXMLConfigOnly/src/main/java/me/oldboy/like_entity/User.java) - сущность с которой работает приложение;
- [ContextBuilder.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringXMLConfigOnly/src/main/java/me/oldboy/listeners/ContextBuilder.java) - фактически это слушатель, который будет запущен контейнером сервлетов в первую очередь, именно в нем будет вызван Spring контекст;
- [UserService.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringXMLConfigOnly/src/main/java/me/oldboy/service_imitation/UserService.java) - имитация слоя сервисов;
- [GetServletWithSpringContext.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringXMLConfigOnly/src/main/java/me/oldboy/servlets/GetServletWithSpringContext.java) - сервлет обрабатывающий запрос от пользователя на получение имени User-a по его ID;
- [context.xml](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringXMLConfigOnly/src/main/resources/context.xml) - файл описывающий структуру bean-ов и их взаимосвязь (XML конфигурация контекста приложения);

Конфигурация зависимостей:
- [build.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringXMLConfigOnly/build.gradle) - для работы нам понадобились две зависимости;
- [version.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringXMLConfigOnly/version.gradle) - версии зависимостей;

Приложение разворачивается локально. Для запуска приложения нужен web-сервер приложений или контейнер сервлетов - 
используем TomCat.

Запустив приложение в TomCat и задав в браузере запрос: http://localhost:8080/user?userId=2, получаем ответ в виде 
имени пользователя из БД (коллекции List) если таковой есть.

Более развернутый [пример и описание конфигурирования при помощи только XML можно посмотреть тут](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_1).
________________________________________________________________________________________________________________________
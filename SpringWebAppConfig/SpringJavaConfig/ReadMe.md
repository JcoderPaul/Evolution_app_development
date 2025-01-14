Наше WEB приложение, работающее на базе Servlet-ов, должно превратиться в полноценное Spring MVC приложение.
Прежде чем перейти к полноформатному четвертому шагу, т.е. реализовать задуманное, сделаем еще несколько мелких
шажков.
________________________________________________________________________________________________________________________
### 4 and 3/4 (Servlet and Spring Context with Java config file and annotation)

И так мы кратко увидели, как можно сконфигурировать наше WEB приложение при помощи Spring-a, как минимум двумя способами:
- Используя только XML файл;
- Используя аннотации и XML;

Теперь на данном шаге нам нужно: 
- Использовать только Java конфигурацию Spring контекста и аннотации.
- Как и ранее, получить имя пользователя из источника (Java коллекции). 
- Для получения и отображения информации все еще используем функционал Servlet-ов.

Структура проекта немного изменилась:
- [AppContextConfig.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringJavaConfig/src/main/java/me/oldboy/config/AppContextConfig.java) - файл конфигуратор Spring контекста (аннотируем его, как @Configuration и задаем пакет для 
поиска соответствующих аннотаций @ComponentScan);
- [LikeBase.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringJavaConfig/src/main/java/me/oldboy/base_imitation/LikeBase.java) - имитация БД (убираем аннотацию @Component и в качестве демонстрации возлагаем заботу о создании bean-a
на конфигурационный класс приложения и аннотацию @Bean, см. класс выше);
- [UserDao.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringJavaConfig/src/main/java/me/oldboy/dao_imitation/UserDao.java) - имитация слоя DAO (применяем аннотацию @Component);
- [User.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringJavaConfig/src/main/java/me/oldboy/like_entity/User.java) - сущность с которой работает приложение;
- [ContextBuilder.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringJavaConfig/src/main/java/me/oldboy/listeners/ContextBuilder.java) - слушатель, который будет запущен контейнером сервлетов в первую очередь, именно в нем будет 
вызван Spring контекст и задан контекст сервлетов;
- [UserService.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringJavaConfig/src/main/java/me/oldboy/service_imitation/UserService.java) - имитация слоя сервисов (применяем аннотацию @Component);
- [GetServletWithSpringContext.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringJavaConfig/src/main/java/me/oldboy/servlets/GetServletWithSpringContext.java) - сервлет обрабатывающий запрос от пользователя на получение имени User-a по его ID;

Конфигурация зависимостей (те же, что и на первых двух малых шагах):
- [build.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringJavaConfig/build.gradle) - для работы нам понадобились две зависимости;
- [version.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringJavaConfig/version.gradle) - версии зависимостей;

Приложение разворачивается локально. Для запуска приложения нужен web-сервер приложений или контейнер сервлетов - 
используем TomCat.

Запустив приложение в TomCat и задав в браузере запрос: http://localhost:8080/user?userId=3, получаем ответ в виде 
имени пользователя из БД (коллекции List) если таковой есть.

Более развернутый [пример и описание конфигурирования при помощи Java класса можно посмотреть тут](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_4).
________________________________________________________________________________________________________________________
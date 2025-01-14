Spring MVC приложение работает по тем же принципам и имеет под капотом те же Servlet-ы. Прежде чем перейти к нашему 
четвертому шагу, т.е. реализовать функционал Коворкинг-центра, сделаем последний маленький шажок - перепишем наши 
прежние три маленьких шажочка с применением только средств Spring-a (при этом, будь у нас желание оставить в текущем
приложении папки [servlets](https://github.com/JcoderPaul/Evolution_app_development/tree/SpringWebAppConfig/SpringWebAppConfig/SpringJavaConfig/src/main/java/me/oldboy/servlets) и [listeners](https://github.com/JcoderPaul/Evolution_app_development/tree/SpringWebAppConfig/SpringWebAppConfig/SpringJavaConfig/src/main/java/me/oldboy/listeners), мы бы с легкостью смогли использовать сервлеты параллельно возможностям Spring-a).
________________________________________________________________________________________________________________________
### 4 and 4/4 (Spring MVC Context with Java config file and annotation)

На данном шаге мы реализовали: 
- Небольшое Spring MVC приложение с применением только Java конфигурации и аннотаций.
- Как и ранее, оно возвращает имя пользователя из источника (Java коллекции) по запросу из браузера. 
- Для получения и отображения информации используется функционал Spring-a.

Фактически это некий упрощенный каркас нашего будущего приложения.

Структура проекта немного изменилась:
- [WebInitializer.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringWebMvcConfig/src/main/java/me/oldboy/app_initializer/WebInitializer.java) - файл формирует WEB контейнер (сервлет контейнер), создает Деспатчер-сервлет (супер-сервлет, 
который будет принимать все прилетающие в приложение запросы) и связывает их;
- [LikeBase.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringWebMvcConfig/src/main/java/me/oldboy/base_imitation/LikeBase.java) - имитация БД (помечен как @Component);
- [User.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringWebMvcConfig/src/main/java/me/oldboy/entity/User.java) - сущность с которой работает приложение;
- [AppContextConfig.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringWebMvcConfig/src/main/java/me/oldboy/config/AppContextConfig.java) - файл конфигурации приложения;
- [controller](https://github.com/JcoderPaul/Evolution_app_development/tree/SpringWebAppConfig/SpringWebAppConfig/SpringWebMvcConfig/src/main/java/me/oldboy/controller) - папка с файлами контроллерами (обработчиками запросов, помечены как @RestController или @Controller);
- [UserRepositoryImpl.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringWebMvcConfig/src/main/java/me/oldboy/repository/UserRepositoryImpl.java) - файл получающий необработанные данные из БД (помечен как @Repository);
- [UserService.java](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringWebMvcConfig/src/main/java/me/oldboy/service/UserService.java) - имитация слоя сервисов (помечен как @Service);

Конфигурация зависимостей (те же, что и на первых двух малых шагах):
- [build.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringWebMvcConfig/build.gradle) - для работы нам понадобились две зависимости (тут нам понадобилась зависимость: 
[implementation "org.springframework:spring-webmvc:${versions.spring}"](https://mvnrepository.com/artifact/org.springframework/spring-webmvc));
- [version.gradle](https://github.com/JcoderPaul/Evolution_app_development/blob/SpringWebAppConfig/SpringWebAppConfig/SpringWebMvcConfig/version.gradle) - версии зависимостей (['spring' : '6.2.0'](https://mvnrepository.com/artifact/org.springframework/spring-webmvc/6.2.0));

Приложение разворачивается локально. Для запуска приложения нужен web-сервер приложений или контейнер сервлетов - 
используем TomCat.

Основные отличия от предыдущих вариантов отображения запрошенной информации, мы можем, сравнительно просто, предложить
пользователю различные варианты её получения. Запустив приложение в TomCat и задав в браузере запрос вида: 
- http://localhost:8080/v1/users?userId=1 (применение в контроллере аннотации @RequestMapping и @ResponseBody);
- http://localhost:8080/v2/users?userId=2 (применение в контроллере аннотации @GetMapping, интерфейса Model и отображение с помощью user_name.jsp страницы);
- http://localhost:8080/v3/users?userId=3 (применение в контроллере аннотации @RestController над классом);

пользователь может получаем ответ в виде имени пользователя из БД если таковой есть.

Более развернутый [пример и описание конфигурирования посмотреть тут](https://github.com/JcoderPaul/Spring_Framework_Lessons/tree/master/Spring_part_15).
________________________________________________________________________________________________________________________
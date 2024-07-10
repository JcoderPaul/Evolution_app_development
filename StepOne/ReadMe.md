### StepOne

Задача: Разработайте приложение для управления коворкинг-пространством. Приложение должно позволять пользователям 
бронировать рабочие места, конференц-залы, а также управлять бронированиями и просматривать доступность ресурсов.

Время на выполнение 2.5 дня.

Функциональные требования: 
- регистрация и авторизация пользователя; 
- просмотр списка всех доступных рабочих мест и конференц-залов; 
- просмотр доступных слотов для бронирования на конкретную дату; 
- бронирование рабочего места или конференц-зала на определённое время и дату; 
- отмена бронирования; 
- добавление новых рабочих мест и конференц-залов, а также управление существующими; 
- просмотр всех бронирований и их фильтрация по дате, пользователю или ресурсу.

Технические требования: 
- приложение должно быть написано на pure Java (без использования Spring); 
- приложение должно быть консольным;
- приложение должно хранить данные в коллекциях (в памяти);
- реализуйте CRUD (Create, Read, Update, Delete) операции для управления бронированиями и ресурсами; 
- реализуйте авторизацию и аутентификацию пользователей; 
- реализуйте обработку конфликтов бронирований; 
- unit-тесты, покрытие 75% (junit5 + Mockito + assertJ).

Дополнения от разработчика: как и любое учебное, а значит не конкретное, данное задание вызывает массу 
вопросов, на которые "заказчик" не отвечает. Обычно в проектах подобного плана, "заказчик", судя по всему 
ищет тех, кто пишет максимально простой и компактный код, пусть и не реализующий пару требований, однако 
полностью соответствующий его взгляду на то, как должен выглядеть "чистый" код. Я встречал варианты, когда 
использования Lombok были запрещены в команде ведущей поиски нужного специалиста. Ни кто не спорит, что если
код компактен и прост - это преимущество - и тестировать такой код проще и быстрее. А некие негласные 
требования к проекту нужны, чтобы сразу отсеивать недогадливых. 

И так, тут приходится домысливать варианты реализации, например, что такое слот в понимании "заказчика". 
Например, это могут быть временные отрезки фиксированной длинны, пусть по 60 мин. И тогда для бронирования 
рабочего места или зала клиент делает бронь нескольких слотов - почасовая бронь. И это один вариант реализации
приложения. У нас может быть вариант, при котором в процессе бронирования задается не фиксированный заранее 
сервисом временной диапазон, а выбираемый заказчиком диапазон условно любой длинны - от startTime до finishTime.
И это другая реализация логики, не сильно отличная от первой, но все же.  

Реализация:
- Слот в текущей реализации имеет длину 60 мин. и таковых 9 шт.
- Функциональные требования выполнены полностью.
- Технические требования выполнены полностью.
- После запуска системы в ней уже присутствует пользователь с логином Admin,
с его помощью можно проверить работу функционала приложения в разделе CRUD 
операций;
- Первый вход в систему с придуманным именем автоматом регистрирует пользователя
как user-a без прав админа, CRUD операции ему не доступны;
- Для работы с приложением следовать инструкциям меню;

Структура проекта:
- [input](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne/src/main/java/me/oldboy/input):
  - [context](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne/src/main/java/me/oldboy/input/context) - класс связывающий между собой остальные классы проекта;
  - [controllers](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne/src/main/java/me/oldboy/input/controllers) - классы управляющие созданием пользователей и резервированием слотов;
  - [entity](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne/src/main/java/me/oldboy/input/entity) - основные рабочие элементы проекта с которыми происходят манипуляции (залы, рабочие места, 
слот времени для резервирования, пользователи, записи о резервировании содержащие сведения о том, что, когда, 
и на сколько было зарезервировано);
  - [exceptions](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne/src/main/java/me/oldboy/input/exeptions) - папа с исключениями;
  - [repository](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne/src/main/java/me/oldboy/input/repository) - базы данных хранящие существующие в нашем коворкин-центре: залы, рабочие места, 
зарегистрированные пользователей, записи о бронировании залов и рабочих мест.

CRUD операции предусмотрены для управления залами и рабочими местами.

- [output](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne/src/main/java/me/oldboy/output):
  - [cli](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne/src/main/java/me/oldboy/output/cli) - консольный интерфейс пользователя;
  - [view](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne/src/main/java/me/oldboy/output/view) - сущности для наглядного отображения данных пользователю по запросу с фильтром через меню;
- [CoworkingApp.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepOne/src/main/java/me/oldboy/CoworkingApp.java) - запускаемый модуль.

Тесты согласно расчетам IDE покрывают 86% кода. 
Тестирование проводилось в двух вариантах: 
- [классические модульные и "интеграционные" JUnit тесты](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne/src/test/java/me/oldboy/junit);
- [тестирование с применением фреймворка Mockito](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne/src/test/java/me/oldboy/mockito);

Наиболее интересные тесты проводились над [void методами](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepOne/src/test/java/me/oldboy/mockito/output/view/AllPlacesViewMockitoTest.java) и методами, где применялся [класс Scanner](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne/src/test/java/me/oldboy/junit/output/cli/items).
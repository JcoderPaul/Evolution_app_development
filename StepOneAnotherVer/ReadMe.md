### StepOneAnotherVer

Задача осталась той же, что и в [StepOne](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepOne/ReadMe.md).

Однако данная версия отличается от предыдущей тем, что в качестве слота брони был взят плавающий временной диапазон, 
т.е. мы допустили возможность, что наш коворкинг-центр работает круглосуточно (в первой версии с 10:00 до 19:00) и 
клиент может резервировать (бронировать) место/зал хоть поминутно в диапазоне от 00:00 до 23:59 выбранной даты, хоть в 
прошлом, хоть в текущем, хоть в будущем (у нас прогрессивный вневременной коворкинг-центр и не имеет темпоральной 
дискриминации).

Так же была переработана структура программы и ее пошаговая реализация:
- Шаг 1 - Создаем [сущности](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/entity) с которыми будет работать наше приложение.
- Шаг 2 - Прописываем слой ['репозиториев' - интерфейсы](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/store/repository).
- Шаг 3 - Прописываем их реализацию ['базы данных'](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/store/base), которые, как и в первом случае все хранят в коллекциях.
- Шаг 4 - Впервые была применена практика - написал метод, тут же оттестировал его реализацию (метод - тест и т.д.), 
что позволило следить за % покрытия и быстрым исправлением ошибок в коде (итоговая 96% покрытия всего кода), 
как это ни странно методы и тесты на них в 'нижних уровнях' стали короче и пишутся быстрее.
- Шаг 5 - Далее поднимаемся по уровням вверх (все уровни условны, тут нет реальных DAO, Repository и т.д), но логика
происходящего сопоставима со слоистой структурой MVC подхода, и слой [services](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/services) - [тесты на слой сервисов](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/test/java/me/oldboy/cwapp/services) 
(применяем интерфейсы), и слой [controllers или handlers](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/handlers) - [тесты на слой обработчиков](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/test/java/me/oldboy/cwapp/handlers) и т.д.
- Шаг 6 - Добравшись до этого шага, мы все еще не имеем интерфейса взаимодействия с пользователем CLI (в первой версии 
программы, можно сказать - пляска шла от него). У нас уже есть: сущности, базы, бизнес логика согласно требованиям 
задания, и все уже покрыто тестами. Делаем CLI интерфейс. В отличие от первой версии программы [StepOne](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne)
структура меню как бы размазана по [слою обработчиков](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/handlers) и
['слою меню'](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/out).
- Шаг 7 - Меню готово. Покрываем тестами меню, общее покрытие 96% всего проекта, из них: 
классы 96% - (62/64), методы 98% - (308/314), строки 96% (1356/1400).

Как и в [StepOne](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOne):
- Функциональные требования выполнены полностью.
- Технические требования выполнены полностью.
- После запуска системы в ней уже присутствует пользователь с логином/паролем - User/user и Admin/admin,
с его помощью можно проверить работу функционала приложения в разрезе CRUD операций над залами и местами;

Структура проекта:
  - [context](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/context) - класс связывающий между собой остальные классы проекта;
  - [handlers](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/handlers) - классы управляющие созданием пользователей, резервированием слотов и т.д., т.е. обработчики бизнес процесса;
  - [entity](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/entity) - основные рабочие элементы проекта с которыми происходят манипуляции ([Place](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/entity/Place.java) - залы/рабочие места (в отличие от 
первой версии программы это одна сущность, различия задаем [через ENUM](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/entity/Species.java), как и [роль пользователя](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/entity/Role.java)), [User](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/entity/User.java) - пользователи, 
[Reservation](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/entity/Reservation.java) - записи о резервировании содержащие сведения о том, что, когда, и на сколько было зарезервировано);
  - [exceptions](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/exception) - папка с исключениями;
  - [store](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/store) - основные [рабочие интерфейсы](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/store/repository) и [базы данных](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/store/base) хранящие существующие в нашем коворкин-центре: [залы и рабочие места](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/store/base/PlaceBase.java), 
[зарегистрированные пользователей](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/store/base/UserBase.java), [записи о бронировании залов и рабочих мест](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/store/base/ReservationBase.java).
  - [services](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/services) - слой обрабатывающий запросы с верхних уровней и получающий ответы от слоя БД;
- [out](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/out) - консольное меню:
  - [items](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/out/items) - отдельные пункты меню;
  - [MainMenu.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/out/MainMenu.java) - основное меню 
(как мне намекнули, [вариант меню в первой версии](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepOne/src/main/java/me/oldboy/output/cli/CoworkingCli.java) имел максимальную 'responsibility - ответственность' и был слишком длинным, с чем сложно не согласиться);
- [CwApp.java](https://github.com/JcoderPaul/Evolution_app_development/blob/master/StepOneAnotherVer/src/main/java/me/oldboy/cwapp/CwApp.java) - запускаемый модуль.

Тесты согласно расчетам IDE покрывают 96% кода. Тестирование проводилось в основном с [использованием Mockito и AssertJ](https://github.com/JcoderPaul/Evolution_app_development/tree/master/StepOneAnotherVer/src/test/java/me/oldboy/cwapp).

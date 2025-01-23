- [JDBC](https://ru.wikipedia.org/wiki/Java_Database_Connectivity) (Java DataBase Connectivity — соединение с базами данных на Java) — платформенно независимый промышленный 
стандарт взаимодействия Java-приложений с различными СУБД, реализованный в виде пакета [java.sql](https://docs.oracle.com/javase/8/docs/api/java/sql/package-summary.html), входящего в состав Java SE.
- [ORM](https://ru.wikipedia.org/wiki/ORM) (Object-Relational Mapping - объектно-реляционное отображение, или преобразование) — технология программирования, 
которая связывает базы данных с концепциями объектно-ориентированных языков программирования, создавая «виртуальную объектную 
базу данных».
- JPA (Java Persistence API) это спецификация Java EE и Java SE, описывающая систему управления сохранением java объектов 
в таблицы реляционных баз данных в удобном виде. Сама Java не содержит реализации JPA, однако существует много реализаций 
данной спецификации, как с закрытым, так и с открытым кодом.
- [Hibernate](https://hibernate.org/orm/) — библиотека для языка программирования Java, решающая задачи объектно-реляционного отображения (ORM) - 
реализация спецификации JPA. JPA только описывает правила и API, а Hibernate реализует их. Hibernate, как и другие 
реализации JPA дает дополнительные возможности, не описанные в JPA (и не переносимые на другие реализации JPA).
________________________________________________________________________________________________________________________

Соединить наше Spring Web MVC приложение и PostgreSQL БД, для сохранения и получения данных, можно по-разному (технологически обрабатывать запросы и ответы к/из БД), приведем 4-и возможных варианта:
- [SpringWebJDBC](https://github.com/JcoderPaul/Evolution_app_development/tree/SpringWebAppConfig/SpringDBConnect/SpringWebJDBC) - приложение использует "прямое соединение" по JDBC, не использует технологию ORM, слой взаимодействия с БД использует SQL запросы и PreparedStatement;
- [SpringWebJDBCTemplate](https://github.com/JcoderPaul/Evolution_app_development/tree/SpringWebAppConfig/SpringDBConnect/SpringWebJDBCTemplate) - проложение использует JDBC без применения технологии ORM, как и предыдущее, но теперь мы используем JDBC шаблоны - JdbcTemplate, из пакета org.springframework.jdbc.core;
- [SpringWebHibernate](https://github.com/JcoderPaul/Evolution_app_development/tree/SpringWebAppConfig/SpringDBConnect/SpringWebHibernate) - приложение взаимодействует с БД с применением ORM Framework Hibernate;
- [SpringAppDataJPA](https://github.com/JcoderPaul/Evolution_app_development/tree/SpringWebAppConfig/SpringDBConnect/SpringAppDataJPA) - приложение взаимодействует с БД используя возможности [Spring Data Jpa](https://spring.io/projects/spring-data-jpa) и [Hibernate](https://hibernate.org/orm/documentation/6.6/);  
В Spring Framework семейство аннотаций `@Conditional` используется для условной регистрации bean-ов в контексте 
приложения на основе определённых условий. Основная аннотация `@Conditional` позволяет разработчику определять 
собственные условия через реализацию интерфейса `Condition`. 

Однако Spring предоставляет несколько готовых аннотаций, основанных на `@Conditional`, для распространённых сценариев. 
Вот список основных аннотаций семейства `@Conditional` и их назначение:

________________________________________________________________________________________________________________________
1. **@ConditionalOnBean**  
   - **Назначение**: Регистрирует бин, только если указанный бин (или бины) уже присутствует в контексте приложения.
   - **Пример использования**: 

         @ConditionalOnBean(DataSource.class)
         @Bean
         public MyBean myBean() {
             return new MyBean();
         }

Бин `MyBean` будет создан, только если в контексте уже есть бин типа `DataSource`.

________________________________________________________________________________________________________________________
2. **@ConditionalOnMissingBean**  
   - **Назначение**: Регистрирует бин, только если указанного бина (или бинов) нет в контексте приложения.
   - **Пример использования**:

         @ConditionalOnMissingBean(MyService.class)
         @Bean
         public MyService myService() {
             return new DefaultMyService();
         }

Если бин `MyService` отсутствует, будет создан `DefaultMyService`.

________________________________________________________________________________________________________________________
3. **@ConditionalOnClass**  
   - **Назначение**: Регистрирует бин, только если указанный класс доступен в classpath.
   - **Пример использования**:

         @ConditionalOnClass(RedisConnectionFactory.class)
         @Bean
         public RedisTemplate redisTemplate() {
             return new RedisTemplate();
         }

Бин `RedisTemplate` будет создан, только если класс `RedisConnectionFactory` найден в classpath.

________________________________________________________________________________________________________________________
4. **@ConditionalOnMissingClass**  
   - **Назначение**: Регистрирует бин, только если указанный класс отсутствует в classpath.
   - **Пример использования**:


         @ConditionalOnMissingClass("com.example.SomeClass")
         @Bean
         public FallbackBean fallbackBean() {
             return new FallbackBean();
         }

Если класс `SomeClass` не найден, создаётся `FallbackBean`.

________________________________________________________________________________________________________________________
5. **@ConditionalOnProperty**  
   - **Назначение**: Регистрирует бин, если указанное свойство в конфигурации (например, в `application.properties`) имеет определённое значение или существует.
   - **Пример использования**:

         @ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
         @Bean
         public FeatureBean featureBean() {
             return new FeatureBean();
         }

Бин `FeatureBean` будет создан, если свойство `feature.enabled=true`.

________________________________________________________________________________________________________________________
6. **@ConditionalOnResource**  
   - **Назначение**: Регистрирует бин, если указанный ресурс (например, файл) доступен в classpath.
   - **Пример использования**:

         @ConditionalOnResource(resources = "classpath:my-config.properties")
         @Bean
         public ConfigBean configBean() {
             return new ConfigBean();
         }

Бин создаётся, только если файл `my-config.properties` присутствует.

________________________________________________________________________________________________________________________
7. **@ConditionalOnExpression**  
   - **Назначение**: Регистрирует бин, если указанное SpEL-выражение (Spring Expression Language) возвращает `true`.
   - **Пример использования**:

         @ConditionalOnExpression("${feature.enabled} && ${another.condition}")
         @Bean
         public MyBean myBean() {
             return new MyBean();
         }

Бин создаётся, если выражение возвращает `true`.

________________________________________________________________________________________________________________________
8. **@ConditionalOnJava**  
   - **Назначение**: Регистрирует бин, если версия Java соответствует указанным требованиям.
   - **Пример использования**:

         @ConditionalOnJava(JavaVersion.EIGHT)
         @Bean
         public Java8Bean java8Bean() {
             return new Java8Bean();
         }

Бин создаётся, если приложение работает на Java 8.

________________________________________________________________________________________________________________________
9. **@ConditionalOnWebApplication**  
   - **Назначение**: Регистрирует бин, только если приложение является веб-приложением (например, Spring MVC или Spring WebFlux).
   - **Пример использования**:

         @ConditionalOnWebApplication
         @Bean
         public WebController webController() {
             return new WebController();
         }

Бин создаётся только для веб-приложений.

________________________________________________________________________________________________________________________
10. **@ConditionalOnNotWebApplication**  
    - **Назначение**: Регистрирует бин, если приложение НЕ является веб-приложением.
    - **Пример использования**:

          @ConditionalOnNotWebApplication
          @Bean
          public BatchProcessor batchProcessor() {
              return new BatchProcessor();
          }

Бин создаётся для не-веб-приложений.

________________________________________________________________________________________________________________________
11. **@ConditionalOnSingleCandidate**  
    - **Назначение**: Регистрирует бин, если в контексте есть ровно один бин указанного типа (или его подтипа).
    - **Пример использования**:

          @ConditionalOnSingleCandidate(DataSource.class)
          @Bean
          public DataSourceDependentBean dataSourceDependentBean() {
              return new DataSourceDependentBean();
          }

Бин создаётся, если в контексте ровно один бин типа `DataSource`.

________________________________________________________________________________________________________________________
12. **@ConditionalOnCloudPlatform**  
    - **Назначение**: Регистрирует бин, если приложение работает на указанной облачной платформе (например, AWS, Azure).
    - **Пример использования**:

          @ConditionalOnCloudPlatform(CloudPlatform.AWS)
          @Bean
          public AwsService awsService() {
              return new AwsService();
          }

Бин создаётся, если приложение развёрнуто на AWS.

________________________________________________________________________________________________________________________
### Дополнительно
- **@Conditional**: Базовая аннотация, которая принимает класс, реализующий интерфейс `Condition`. Позволяет создавать пользовательские условия.

          @Conditional(MyCustomCondition.class)
          @Bean
          public MyBean myBean() {
              return new MyBean();
          }

Здесь `MyCustomCondition` — пользовательский класс, реализующий логику условия.

________________________________________________________________________________________________________________________
- **Комбинирование условий**: Аннотации можно комбинировать для создания сложных условий. Например:


          @ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
          @ConditionalOnClass(SomeClass.class)
          @Bean
          public MyBean myBean() {
              return new MyBean();
          }

________________________________________________________________________________________________________________________
### Заключение

Эти аннотации широко используются в Spring Boot для реализации автоконфигурации, позволяя гибко управлять созданием 
бинов в зависимости от окружения, доступных классов, свойств или других факторов.
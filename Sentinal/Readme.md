1. Handled ansync calls for better customer experience.
2. handled distributed system logic for clear speration of concerns
3. handled retry logic for status update and added a retry limit so system doesn't encounter any memory overload or resources depletion problem.
4. TODO: In memory queue, can be replaced with kafka, PriorityQueue ordered by nextRetryTime, DelayQueue (best Java solution)
5. TODO: Feign fancy client for call from payment service to fraud engine
6. TransctionsService create UUID. Fraud treats it as a external Reference. No ownership should be there in Fraud Service.
7. Service needs to be idempotent so when status = in review or new status == old status then no need to update.
8. Retry logic is for situation when Fraud Engine tries to connect with transaction service to update the status but for some reason it is down. So it does an exponential timeslot callback attempt.
9. Force RestTemplate to treat non-2xx as failure. Spring RestTemplate does NOT throw exception for 404 by default in my setup (or it gets swallowed depending on error handler).
10. Springboot has something called autoconfiguration. when we add the dependency : spring-boot-starter-security it automatically creates SecurityFilterChain. Even if you write 0 security code, spring silently adds FilterChainProxy which can lead to unauthorized if we dont set the correct security credentials while sending request. Now by adding manual filter , my request passes through the filter(filterChain), which executes before the authentication due to addFilterBefore
11. Spring primarily injects by type, not by name. So after startup when it searches for a autowired class or object it searches among the available beans and dependencies and when it find that type, it injects it. That is what happens in case of PasswordEncoder and bean of Bcrypt Encoder.
12. Creating two beans with the same return type of password encoder would have caused ambiguity and spring would hae thrown no unique bean found exception. In such case we need to inject by name using @Qualifier annotation. ex: @Qualifier("encoder1")
13. Spring creates all singleton beans when the application context is initialized. @SpringBootApplication kicks off auto-configuration. Component scanning begins -> Application context gets built -> singleton beans get created -> dependency injected -> app ready.
14. Bcrypt is a secure, slow, and computationally intensive password-hashing function based on the Blowfish block cipher. A salt is a random, unique string added to each password before hashing, which prevents rainbow table attacks and ensures that identical passwords produce different hashes, significantly enhancing security. Slow is good for security. SHA256 is fast → bad for passwords. BCrypt is adaptive & slow → resistant to brute force
15. Never trust request data, Always trust DB state
16. Services are part of business logic and do not participate in security lifecycle.
17. Spring expects roles prefixed with ROLE\_. So, USER → ROLE_USER , ADMIN → ROLE_ADMIN
18. Method-level security is evaluated after authentication but before method execution. Is closer to business logic.
19. Authentication needs roles immediately. So eager fetch.
20. In stateless authentication, the server does not store session information. Therefore, each request must be independently authenticated. The JWT filter extracts and validates the token on every request and sets the authentication in the SecurityContext so that Spring Security can authorize the request.
21. Flyway : we can use this framework to continuously remodel our application’s database schema reliably and easily. Flyway updates a database from one version to the next using migrations. We can write migrations either in SQL with database-specific syntax, or in Java for advanced database transformations. Naming format MUST be:V<version>\_\_<description>.sql for the files.

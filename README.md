# jwt-client-authentitation-filter


This is an implemntation of a jwt authentication filter for spring boot. 
There are many tutorials that describe how to build one of these but none of them provide an artifact that can be used. This allows you to avoid copying and pasting code into your own projects. 
Example tutoirals: https://auth0.com/blog/implementing-jwt-authentication-on-spring-boot/, https://www.freecodecamp.org/news/how-to-setup-jwt-authorization-and-authentication-in-spring/, https://dzone.com/articles/implementing-jwt-authentication-on-spring-boot-api


Each microservice that needs to validate a token can use this by including it in their webSecurityConfigurationAdapter. 


maven coordinates 

```
<dependency>
  <groupId>io.github.murphp15</groupId>
  <artifactId>jwt-client-authentitation-filter</artifactId>
  <version>1.0.9</version>
</dependency>
```

```
@Configuration
class WebSecurityConfig(val defaultUserDetailsRepo: UserDetailsCreator,
                        val tokenCreator: TokenCreator,
                        @Value("\${jwt.secret}") private val jwtSecret: String) : WebSecurityConfigurerAdapter() {


    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .addFilter(JwtAuthorizationFilter(authenticationManager(), defaultUserDetailsRepo, jwtSecret))
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable();
    }
```


By default a token is mapped to a org.springframework.security.core.userdetails.User with the username and the roles pulled from the jwt token. 
However if further augmentation of the authenticated user object is needed a custom version of user UserDetailsCreator can be provided.

e.g

```
interface UserDetailsCreator {
    fun createFromToken(username: String, roles: List<String>): UserDetails?
}


class  MyComplicatedCustomUserDetailsCreator : UserDetailsCreator {
    fun createFromToken(username: String, roles: List<String>): UserDetails = MyCustomUserObject("blah", roles = "ROLE_CAN_DO_STUFF")
}
```

 

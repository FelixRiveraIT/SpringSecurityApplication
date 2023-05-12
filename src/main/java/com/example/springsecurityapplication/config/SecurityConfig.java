package com.example.springsecurityapplication.config;
import com.example.springsecurityapplication.services.PersonDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.Security;

@Configuration
public class SecurityConfig {

    private final PersonDetailsService personDetailsService;

    @Bean
    public PasswordEncoder getPasswordEncode(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{
//        .csrf().disable() // отключаем защиту от межсайтовой подделки запросов
        // конфигурируем работу Spring Security
        httpSecurity
                .authorizeHttpRequests() // указываем что все страницы должны быть защищены аутентификацией
                .requestMatchers("/admin").hasRole("ADMIN") // Указываем на то, что страница /admin доступна пользователю с ролью ADMIN
                .requestMatchers("/authentication", "/error", "/registration", "/resources/**", "/static/**", "/css/**", "/js/**", "/img/**").permitAll() // Указываем что не аутентифицированные пользователи могут зайти на страницу аутентификации и на объект ошибки
                // С помощью permitAll указываем, что не аутентифицированные пользователи могут заходить на перечисленные страницы
                .anyRequest().hasAnyRole("USER", "ADMIN")
//                .anyRequest().authenticated() // Указываем что для всех остальных страниц необходимо вызывать метод authenticated(), который открывает форму аутентификации
                .and() // Указываем что дальше настраивается аунтефикация и соединяем её с настройкой доступа
                .formLogin().loginPage("/authentication") // Указываем какой uel запрос будет отправляться при заходе на защищенные страницы
                .loginProcessingUrl("/process_login") // Указываем на какой адрес будут отправляться данные с формы. Нам уже не нужно будет создавать метод в контроллере и обрабатывать данные с формы. Мы задали url, который используется по умолчанию для обработки формы аутентификации по средствам Spring Security. Он будет ждать объект с формы аутентификации и затем сверять логин и пароль с данными в БД
                .defaultSuccessUrl("/index", true) // Указываем на какой url необходимо направить пользователя после успешной аутентификации. Вторым аргументом указывается true, чтобы перенапревление шло в любом случае после успешной аутентификации
                .failureUrl("/authentication?error") // Указываем куда необходимо перенаправить пользователя при проваленной аутентификации. В запрос будет передан объект error, который будет проверяться на форме и при наличии данного объекта в запросе выводится сообщение "Неправильный логин или пароль"
                .and()
                .logout().logoutUrl("/logout").logoutSuccessUrl("/authentication");
        return httpSecurity.build();
    }
    public SecurityConfig(PersonDetailsService personDetailsService) {
        this.personDetailsService = personDetailsService;
    }
//    private final AuthenticationProvider authenticationProvider;

//    public SecurityConfig(AuthenticationProvider authenticationProvider) {
//        this.authenticationProvider = authenticationProvider;
//    }

    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
//        authenticationManagerBuilder.authenticationProvider(authenticationProvider);
        authenticationManagerBuilder.userDetailsService(personDetailsService)
                .passwordEncoder(getPasswordEncode());
    }
}

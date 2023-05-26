package com.example.springsecurityapplication.config;
import com.example.springsecurityapplication.services.PersonDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {
    private final PersonDetailsService personDetailsService;

    //Определяем метод хэширования паролей хэш-функцией BCrypt
    @Bean
    public PasswordEncoder getPasswordEncode(){
        return new BCryptPasswordEncoder();
    }

    //HttpSecurity отвечет за объект аутентификации
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //Так можно отключить csrf токены (защиту от межсайтовой подделки запросов), но лучше не делать
        //  http.csrf().disable();
        http
                // указываем, что все страницы должны быть защищены аутентификацией
                .authorizeHttpRequests()
                // Указываем на то, что страница /admin доступна пользователю с ролью ADMIN ("ROLE_" - отбрасывается, не указываем)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                //Указываем, что указанные страницы доступны всем пользователям. Добавляем в этот список также таблицы стилей, JS, папки с картинками и тд (т.к. в момент, когда у пользователя не будет роли - ему все это будет недоступно)
                .requestMatchers("/authentication", "/registration", "/error", "/resources/**", "/static/**", "/css/**", "/js/**", "/img/**", "product", "/product/info/{id}", "/product/search").permitAll()
                // указываем, что все остальные страницы доступны user и admin
                .anyRequest().hasAnyRole("USER", "ADMIN")

                // указываем, что для всех остальных страниц необходимо вызывать метод authenticated(), который открывает форму аутентификации. использовали до внедрения ролей
//                .anyRequest().authenticated() использовали до внедрения ролей
                // --- конец настройки доступа --- //
                .and() // указываем, что дальше настраивается аутентификация и соединяем ее с настройкой доступа
                // --- Начало настройки аутентификации --- //
                //указываем какой url запрос будет отправляться при заходе на защищенные страницы
                .formLogin().loginPage("/authentication")
                //указываем на какой адрес будут отправляться данные с формы. Нам уже не нужно будет создавать метод в контроллере и обрабатывать данные с формы. Мы задали url, который используется по умолчанию для обработки формы аутентификации по средствам SS. SS. будет ждать объект с формы аутентиф и затем сверять логин и пароль с данными в БД
                .loginProcessingUrl("/process_login")
                //Указываем на какой url необходимо направить пользователя после успешной аутентификации. Вторым аргументом указывается true чтобы перенаправление шло в любом случае после успешной аутентификации
                .defaultSuccessUrl("/person_account", true)
                // Указываем, куда перенаправлять пользователя при неудачной аутентификации. В запрос будет передан объект error, который будет проверятся на форме и при наличии данного объекта в запросе выводится сообщение "Неправильный логин или пароль".
                .failureUrl("/authentication?error")
                // --- добавляем логаут --- //
                //указываем по какому url будет осуществлён логаут и куда направит после успешного логаута
                //всё это реализуется стандартными средствами и методами SS.
                .and()
                .logout().logoutUrl("/logout").logoutSuccessUrl("/authentication");
        return http.build();
    }

    @Autowired
    public SecurityConfig(PersonDetailsService personDetailsService) {
        this.personDetailsService = personDetailsService;
    }




    //Указываем, что аутентификация приложения будет осуществляться с помощью базовых настроек SS: authenticationManagerBuilder
    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(personDetailsService)
                .passwordEncoder(getPasswordEncode());
    }
}

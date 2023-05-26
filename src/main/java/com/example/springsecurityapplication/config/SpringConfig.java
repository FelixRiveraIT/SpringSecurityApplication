package com.example.springsecurityapplication.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

//Нужен для сканирования папки util. Добавлена аннтотация @Component в PersonalValidator = работает без этого класса конфиграции
@Configuration
@ComponentScan("com.example.springsecurityapplication.util")
public class SpringConfig {


}

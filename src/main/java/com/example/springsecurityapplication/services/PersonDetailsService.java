package com.example.springsecurityapplication.services;

import com.example.springsecurityapplication.models.Person;
import com.example.springsecurityapplication.repositories.PersonRepository;
import com.example.springsecurityapplication.security.PersonDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

//Имплементируем интерфейс UserDetailsService, который позволяет проверять корректность логина и при
// корректном логине возвращать объект пользователя
@Service
public class PersonDetailsService implements UserDetailsService {
    private final PersonRepository personRepository;

    public PersonDetailsService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Получаем пользоателя из таблицы по логину с формы аутентификации
        Optional<Person> person = personRepository.findByLogin(username);
        // Если пользователь не был найден
        if (person.isEmpty()) {
        // выбрасываем исключение, что пользователь не найден.
        // Данное исключение будет поймано SS и сообщение будет выведено на страницу
            throw new UsernameNotFoundException("Пользователь не найден");
        }
        //Если найден, то создаем новый объект PersonDetails
        return new PersonDetails(person.get());
    }
}

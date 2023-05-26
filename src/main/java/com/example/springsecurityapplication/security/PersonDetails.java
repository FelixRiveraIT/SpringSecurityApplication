package com.example.springsecurityapplication.security;

import com.example.springsecurityapplication.models.Person;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class PersonDetails implements UserDetails {
    private final Person person;

    public PersonDetails(Person person) {
        this.person = person;
    }

    //Получение пользователя
    public Person getPerson() {
        return this.person;
    }

    // Получаем роль пользователя
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(person.getRole()));
    }

    @Override
    public String getPassword() {
        return this.person.getPassword();
    }

    @Override
    public String getUsername() {
        return this.person.getLogin();
    }

    //Аккаунт действителен?
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    //Аккаунт заблокирован?
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    //Пароль действителен (срок действия не истёк)?
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    //Аккаунт активен?
    @Override
    public boolean isEnabled() {
        return true;
    }
}

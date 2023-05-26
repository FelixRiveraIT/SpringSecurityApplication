package com.example.springsecurityapplication.repositories;

import com.example.springsecurityapplication.models.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person,Integer> {

    //Option используется, потому что пользователь может быть как найден, так и нет
    Optional<Person> findByLogin(String login);

    @Override
    List<Person> findAll();
}

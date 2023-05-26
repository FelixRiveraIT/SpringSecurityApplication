package com.example.springsecurityapplication.repositories;

import com.example.springsecurityapplication.models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//опущен сервисный слой, который очень желательно чтобы был
@Repository
@Transactional
public interface CartRepository extends JpaRepository<Cart, Integer> {
    //метод для получения корзины товаров конкретного пользователя
    List<Cart> findByPersonId(int id);

    //удаление товара из корзины по id Товара
    @Modifying
    @Query(value = "delete from product_cart where product_id=?1", nativeQuery = true)
    void deleteCartByProductId(int id);

}

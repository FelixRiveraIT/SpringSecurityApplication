package com.example.springsecurityapplication.repositories;

import com.example.springsecurityapplication.models.Image;
import com.example.springsecurityapplication.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer>{
    //Удаление всех фотографий товара по id товара
    @Modifying
    @Query(value = "delete from image where product_id=?1", nativeQuery = true)
    void deleteImageByProductId(int id);

    @Query(value = "select * from image where product_id=?1", nativeQuery = true)
    List<Image> findAllImageForProductId (int id);

}



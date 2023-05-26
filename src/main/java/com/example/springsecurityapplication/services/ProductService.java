package com.example.springsecurityapplication.services;

import com.example.springsecurityapplication.models.Category;
import com.example.springsecurityapplication.models.Product;
import com.example.springsecurityapplication.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    public Product getProductById(long id) {
        return productRepository.findById((int) id).orElse(null);
    }

    //Данный метод позволяет получить список всех товаров
    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    //Данный метод позволяет получить продукт по ID или вернуть null, если не будет найден
    public Product getProductById(int id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        return optionalProduct.orElse(null);
    }

    //Даннный метод позволяет сохранить товар
    @Transactional
    public void saveProduct (Product product, Category category) {
        product.setCategory(category);
        productRepository.save(product);
    }

    //Данный метод позволяет обновить данные о товаре
    @Transactional
    public void updateProduct (int id, Product product) {
        product.setId(id);
        productRepository.save(product);
    }

    //Данный метод для удаления товара по id
    @Transactional
    public void deleteProduct(int id) {
        productRepository.deleteById(id);
    }

}

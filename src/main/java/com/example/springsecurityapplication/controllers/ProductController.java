package com.example.springsecurityapplication.controllers;

import com.example.springsecurityapplication.models.Product;
import com.example.springsecurityapplication.repositories.ProductRepository;
import com.example.springsecurityapplication.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    //    Эти методы нужно
//    было продублировать в сервисном слое, но этот момент тут опущен
    private final ProductRepository productRepository;

    public ProductController(ProductService ProductService, ProductRepository ProductRepository) {
        this.productService = ProductService;
        this.productRepository = ProductRepository;
    }

    //Позволит получить все товары и вывести представление product из папки product
    @GetMapping("")
    public String getAllProduct(Model model){
        List<Product> products = productService.getAllProduct();
        //Проверяем полученное значение на null и возвращаем пустой список, если результат запроса пустой
        if(products == null) {
            products = new ArrayList<Product>();
        }
        model.addAttribute("products", products);
        return "/product/product";
    }

    //Метод для получения информации о конкретном продукте (при переходе по ссылке с карточки товара со страницы /product)
    @GetMapping("/info/{id}")
    public String infoProduct(@PathVariable("id") int id, Model model) {
        model.addAttribute("product", productService.getProductById(id));
        return "product/infoProduct";
    }

    //Обработка формы поиска товара (с представлений product,
    @PostMapping("/search")
    public String productSearch(@RequestParam("search") String search, @RequestParam("ot") String ot, @RequestParam("do") String Do, @RequestParam(value = "price", required = false, defaultValue = "") String price, @RequestParam(value = "contract", required = false, defaultValue = "") String contract, Model model){
        model.addAttribute("products", productService.getAllProduct());

        //Логика обработки формы поиска и фильтрации товаров
        //Реализована только часть возможных случаев
        if(!ot.isEmpty() & !Do.isEmpty()){ //Если графы "цена от", "цена до" не пустые
            if(!price.isEmpty()){ //Если радиокнопка сортировки ("по возрастанию цены"/по "убыванию цены") не пустая

                if(price.equals("sorted_by_ascending_price")) { //если выбрано значение "сортировка по возрастанию цены
                    if (!contract.isEmpty()) { //Если радиокнопка "Категория товара" не пустое значение
                        if (contract.equals("furniture")) {//Если выбрана категория мебель(id=1, смотрим id по БД категорий), то:
                            //Кладем в модель "search_product" и в качестве значения кладем туда то, что возвращает соответствующий метод(в метод передаём то, что пришло с формы, дополнительно поисковый запрос приводим к нижнему регистру, значения полей Цена "От"/"До" приводим ко флоат
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceAsc(search.toLowerCase(), Float.parseFloat(ot), Float.parseFloat(Do), 1));
                        } else if (contract.equals("appliances")) {//Если выбрана бытовая техника
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceAsc(search.toLowerCase(), Float.parseFloat(ot), Float.parseFloat(Do), 3));
                        } else if (contract.equals("clothes")) {//Если выбрана одежда
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceAsc(search.toLowerCase(), Float.parseFloat(ot), Float.parseFloat(Do), 2));
                        } else if (contract.equals("shoes")) {//Если выбрана обувь
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceAsc(search.toLowerCase(), Float.parseFloat(ot), Float.parseFloat(Do), 4));
                        }
                    } else {
                        //Если категория товара не выбрана, то вызываем соответствующий метод для поиска по наименованию и сортировке по возрастанию цены
                        model.addAttribute("search_product", productRepository.findByTitleOrderByPriceAsc(search.toLowerCase(), Float.parseFloat(ot), Float.parseFloat(Do)));
                    }
                    //Если установлен сорт по уменьшению цены, то алгоритм как выше, только используем методы с фильтром от высокой к низкой цене
                } else if(price.equals("sorted_by_descending_price")){
                    if(!contract.isEmpty()){ //если
                        System.out.println(contract);
                        if(contract.equals("furniture")){
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(ot), Float.parseFloat(Do), 1));
                        }else if (contract.equals("appliances")) {
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(ot), Float.parseFloat(Do), 3));
                        } else if (contract.equals("clothes")) {
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(ot), Float.parseFloat(Do), 2));
                        } else if (contract.equals("shoes")) {//Если выбрана обувь
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceAsc(search.toLowerCase(), Float.parseFloat(ot), Float.parseFloat(Do), 4));
                        }
                    }  else {
                        model.addAttribute("search_product", productRepository.findByTitleOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(ot), Float.parseFloat(Do)));
                    }
                }
            } else {//Если радиокнопка сортировки цены не указана,
                //ищем по наименованию и диапазону указанной цены
                model.addAttribute("search_product", productRepository.findByTitleAndPriceGreaterThanEqualAndPriceLessThanEqual(search.toLowerCase(), Float.parseFloat(ot), Float.parseFloat(Do)));
            }
        } else { //Если поля Цена "От"/"До" не заполнены, то ищем по наименованию
            model.addAttribute("search_product", productRepository.findByTitleContainingIgnoreCase(search));
        }
        //Кладём в модель обратно полученные значения с формы для того, чтобы после отправки формы (произойдёт перезагрузка
        // страницы) отправить в форму эти значения для автозаполнения полей по ключу "attributeName" ("value_search" и тд)
        model.addAttribute("value_search",search);
        model.addAttribute("value_price_ot",ot);
        model.addAttribute("value_price_do",Do);
        return "/product/product";
    }
}
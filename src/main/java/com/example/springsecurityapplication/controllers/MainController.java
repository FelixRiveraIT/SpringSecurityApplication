package com.example.springsecurityapplication.controllers;

import com.example.springsecurityapplication.enumm.Status;
import com.example.springsecurityapplication.models.*;
import com.example.springsecurityapplication.repositories.CartRepository;
import com.example.springsecurityapplication.repositories.OrderRepository;
import com.example.springsecurityapplication.repositories.ProductRepository;
import com.example.springsecurityapplication.security.PersonDetails;
import com.example.springsecurityapplication.services.PersonService;
import com.example.springsecurityapplication.services.ProductService;
import com.example.springsecurityapplication.util.PersonValidator;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//В этом контроллере прописаны методы для регистрации и авторизации пользователей,
//а также методы для зарегистрированного пользователя. Хотя пользователя можно было бы вынести отдельно

@Controller
public class MainController {
    private final PersonValidator personValidator;
    private final PersonService personService;

    private final ProductService productService;

    private final ProductRepository productRepository;

    //тут должен быть сервисный слой, но он опущен в рамках данной работы
    private final CartRepository cartRepository;

    private final OrderRepository orderRepository;

    public MainController(PersonValidator personValidator, PersonService personService, ProductService productService, ProductRepository productRepository, CartRepository cartRepository, OrderRepository orderRepository) {
        this.personValidator = personValidator;
        this.personService = personService;
        this.productService = productService;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/person_account")
    public String index(Model model){
        // Получаем объект аутентификации. Далее, с помощью SpringContextHolder обращаемся к контексту ина нем вызываем метод аутентификации. Из сессии текущего пользователя получаем объект, который был положен в данную сессию после аутентификации пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        //получаем роль аутентифицированного пользователя от полученного объекта сессии
        String role = personDetails.getPerson().getRole();
        // Если у аутентиф. пользователя роль Админ, то отправляем его по ссылке :/admin", иначе отправляем на index
        if (role.equals("ROLE_ADMIN")) {
            return "redirect:/admin";
        }
        model.addAttribute("products", productService.getAllProduct());
        return "/user/index";
    }

    @GetMapping("/registration")
    public String registration(@ModelAttribute("person") Person person) {
        return "/registration";
    }

    @PostMapping("/registration")
    public String resultRegistration(@ModelAttribute("person") @Valid Person person, BindingResult bindingResult){
        //Дополнительная валидация на наличие регистрируемого логина в БД (в дополнение к валидации, указанной в модели Person). Если пользователь уже существует, валидатор внедрит ошибку в bindingResult
        personValidator.validate(person,bindingResult);
        if(bindingResult.hasErrors()){
            return "/registration";
        }
        personService.register(person);
        return "redirect:/person_account";
    }

    //Метод для получения информации о конкретном продукте (при переходе по ссылке с карточки товара со страницы /product)
    //Считываем динамический id и по нему передаем в модель из репозитория продукт с этим id.
    //И возвращаем представление product/infoProduct
    @GetMapping("/person_account/product/info/{id}")
    public String infoProduct(@PathVariable("id") int id, Model model) {
        model.addAttribute("product", productService.getProductById(id));
        return "user/infoProduct";
    }

    //Обработка формы поиска товара (с представления index,
    @PostMapping("/person account/product/search")
    public String productSearch(@RequestParam("search") String search,
                                @RequestParam("ot") String ot,
                                @RequestParam("do") String Do,
                                //required = false обозначает, что параметр не обязателен, в таком случае указываем ещё
                                // defaultValue - это значение будет присвоено, если в price ничего не придёт
                                @RequestParam(value = "price", required = false, defaultValue = "") String price,
                                @RequestParam(value = "contract", required = false, defaultValue = "") String contract,
                                Model model){
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
                        }else if (contract.equals("shoes")) {//Если выбрана обувь
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
                        }else if (contract.equals("shoes")) {//Если выбрана обувь
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

    //Обрабатываем ссылку "добавить в корзину" из представления index
    @GetMapping("/cart/add/{id}")
    public String addProductInCart(@PathVariable("id") int id, Model model) {
        //Получаем продукт по id
        Product product = productService.getProductById(id);
        //Извлекаем объект аутентифицированного пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //вытаскиваем из объекта аутентификации объект модели person
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        //извлекаем id пользователя из объекта
        int id_person = personDetails.getPerson().getId();
//создаем объект корзины (id пользователя и id продукта)
        Cart cart = new Cart(id_person, product.getId());
        //Записываем объект в репозиторий (в БД)
        cartRepository.save(cart);
        return "redirect:/cart";


    }

    //Обрабатываем редирект после добавления в корзину
    @GetMapping("/cart")
    public String cart (Model model){
        //Когда пользователь заходит в корзину, мы должны получить объкт аутентификации, чтобы передать корину
        // конкретно для этого пользователя. Извлекаем объект аутентифицированного пользователя:
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //вытаскиваем из объекта аутентификации объект модели person
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        //извлекаем id пользователя из объекта
        int id_person = personDetails.getPerson().getId();
        List<Cart> cartList = cartRepository.findByPersonId(id_person);
        List<Product> productList = new ArrayList<>();
        //перебираем продукты в картлисте
        for (Cart cart:cartList){
            //добавляем продукт в лист. Продукт добавляем по id, который берём из cart.getProductId().
            // (в объекте корзины у нас храниться id продукта и id пользователя, которому принадлежит этот продукт)
            productList.add(productService.getProductById((cart.getProductId())));
        }
        //Вычисление итоговой цены товаров в корзине
        float price=0;
        for (Product product: productList) {
            price += product.getPrice();
        }
        model.addAttribute("price", price);
        model.addAttribute("cart_product", productList);
        return "/user/cart";
    }

    // Данный метод обрабатывает ссылку "удалить товар из корзины" из представления Index
    @GetMapping("/cart/delete/{id}")
    public String deleteProductFromCart(Model model, @PathVariable("id") int id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //вытаскиваем из объекта аутентификации объект модели person
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        //извлекаем id пользователя из объекта
        int id_person = personDetails.getPerson().getId();
        List<Cart> cartList = cartRepository.findByPersonId(id_person);
        List<Product> productList = new ArrayList<>();
        //перебираем продукты в картлисте
        for (Cart cart:cartList){
            productList.add(productService.getProductById((cart.getProductId())));
        }
        cartRepository.deleteCartByProductId(id);
        return "redirect:/cart";
    }

    //Обрабатываем нажатие на ссылку "оформить заказ"
    @GetMapping("/order/create")
    public String order (){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //вытаскиваем из объекта аутентификации объект модели person
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        //извлекаем id пользователя из объекта
        int id_person = personDetails.getPerson().getId();
        List<Cart> cartList = cartRepository.findByPersonId(id_person);
        List<Product> productList = new ArrayList<>();
        //перебираем продукты в картлисте
        for (Cart cart:cartList){
            //добавляем продукт в лист. Продукт добавляем по id, который берём из cart.getProductId().
            // (в объекте корзины у нас храниться id продукта и id пользователя, которому принадлежит этот продукт)
            productList.add(productService.getProductById((cart.getProductId())));
        }
        //Вычисление итоговой цены товаров в корзине
        float price=0;
        for (Product product: productList) {
            price += product.getPrice();
        }
        //генерируем уникальный номер заказа
        String uuid = UUID.randomUUID().toString();
        //
        for (Product product: productList){
            //Для каждого из товара в заказе генерируем запись в сущности Order
            Order newOrder = new Order(uuid, product, personDetails.getPerson(),1, product.getPrice(), Status.Оформлен);
            //сохраняем заказ в сущность order
            orderRepository.save(newOrder);
            //удаляем заказанный товар из корзины
            cartRepository.deleteCartByProductId(product.getId());
        }
        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String orderUser (Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        List<Order> orderList = orderRepository.findByPerson(personDetails.getPerson());
        model.addAttribute("orders", orderList);
        return "/user/orders";
    }
}


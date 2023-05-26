package com.example.springsecurityapplication.controllers;

import com.example.springsecurityapplication.enumm.Role;
import com.example.springsecurityapplication.enumm.Status;
import com.example.springsecurityapplication.models.*;
import com.example.springsecurityapplication.repositories.CategoryRepository;
import com.example.springsecurityapplication.repositories.ImageRepository;
import com.example.springsecurityapplication.repositories.OrderRepository;
import com.example.springsecurityapplication.repositories.ProductRepository;
import com.example.springsecurityapplication.services.ImageService;
import com.example.springsecurityapplication.services.OrderService;
import com.example.springsecurityapplication.services.PersonService;
import com.example.springsecurityapplication.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Controller
public class AdminController {

    private final ProductService productService;
    private final ImageService imageService;
    private final OrderService orderService;

    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;
    private final OrderRepository orderRepository;
    private final PersonService personService;

    // Место хранения добавленных файлов
    @Value("${upload.path}")
    private String uploadPath;

    private final CategoryRepository categoryRepository;

    public AdminController(ProductService productService, ImageService imageService, OrderService orderService, ProductRepository productRepository, ImageRepository imageRepository, OrderRepository orderRepository, PersonService personService, CategoryRepository categoryRepository) {
        this.productService = productService;
        this.imageService = imageService;
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.orderRepository = orderRepository;
        this.personService = personService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin/admin";
    }

    @GetMapping("/admin/product")
    public String admin(Model model) {
        //по переходу по ссылке /admin/product передаем также все продукты, которые есть в репозитории
        model.addAttribute("products", productService.getAllProduct());
        return "admin/infoProduct";
    }

    //получаем все категории товаров из репозитория категорий - их будет использовать select в представлении
    @GetMapping("admin/product/add")
    public String addProduct(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("category", categoryRepository.findAll());
        return "product/addProduct";
    }


    @PostMapping("/admin/product/add")
    public String addProduct(@ModelAttribute("product") @Valid Product product, BindingResult bindingResult,
                             @RequestParam("file_one") MultipartFile file_one,
                             @RequestParam("file_two") MultipartFile file_two,
                             @RequestParam("file_three") MultipartFile file_three,
                             @RequestParam("file_four") MultipartFile file_four,
                             @RequestParam("file_five") MultipartFile file_five,
                             @RequestParam("category") int category, Model model) throws IOException {
        Category category_db = categoryRepository.findById(category).orElseThrow();
        if (bindingResult.hasErrors()) {
            model.addAttribute("category", categoryRepository.findAll());
            return "product/addProduct";
        }
        //Создаем массив файлов и добавляем в него все пять параметров MultipartFile
        MultipartFile[] files = {file_one, file_two, file_three, file_four, file_five};
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdir();
                }
                String uuidFile = UUID.randomUUID().toString();
                String resultFile = uuidFile + "." + file.getOriginalFilename();
                file.transferTo(new File(uploadPath + "/" + resultFile));
                Image image = new Image();
                image.setProduct(product);
                image.setFileName(resultFile);
                product.addImageToProduct(image);
            }
        }
        product.setCategory(category_db);
        productService.saveProduct(product, category_db);
        return "redirect:/admin/product";
    }

    @GetMapping("admin/product/delete/{id}")
    public String deleteProduct(@PathVariable("id") int id){
        productService.deleteProduct(id);
        return "redirect:/admin";
    }

    @GetMapping("admin/product/edit/{id}")
    public String editProduct(Model model, @PathVariable("id") int id){
        model.addAttribute("product", productService.getProductById(id));
        model.addAttribute("category", categoryRepository.findAll());
        return "product/editProduct";
    }
    @PostMapping("/admin/product/edit/{id}")
    public String editProduct(@ModelAttribute("product")
                              @Valid Product product, BindingResult bindingResult,
                              @PathVariable("id") int id,
                              @RequestParam("file_one") MultipartFile file_one,
                              @RequestParam("file_two") MultipartFile file_two,
                              @RequestParam("file_three") MultipartFile file_three,
                              @RequestParam("file_four") MultipartFile file_four,
                              @RequestParam("file_five") MultipartFile file_five,
                              Model model) throws IOException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("category", categoryRepository.findAll());
            return "product/editProduct";
        }

        //Если ошибок нет, то Обрабатываем загружаемые файлы с файлИнпутов формы.
        //Удаляем старые фотографии редактируемого товара из БД
        imageService.deleteImageByProductId(product.getId());
        if (!file_one.isEmpty()) {
            //Превращаем ссылку на файл в объект файла
            File uploadDir = new File(uploadPath);
            //Если директория не найдена, то создаём её
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            //Генерируем UUID для файла и создаем новое уникальное имя файла UUID + исходное название файла
            String uuidFile = UUID.randomUUID().toString();
            String resultFile = uuidFile + "." + file_one.getOriginalFilename();
            //Отправляем файл file_one с новым сгенерированным наименованием в папку с загрузками (ссылка на которую
            // хранится в uploadPath и указана в app. prop.). Не забываем выбросить Exception.
            file_one.transferTo(new File(uploadPath + "/" + resultFile));
            Image image = new Image(); //создаем объект модели Image (фото)
            image.setProduct(product); //к фотографии привязываем продукт
            image.setFileName(resultFile); //присваиваем наименование файлу
            product.addImageToProduct(image); //добавляем фото в лист с фото, привязанный к этому продукту
        }
        if (!file_two.isEmpty()) {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFile = uuidFile + "." + file_two.getOriginalFilename();
            file_two.transferTo(new File(uploadPath + "/" + resultFile));
            Image image = new Image(); //создаем объёкт модели Image (фото)
            image.setProduct(product); //к фотографии привязываем продукт
            image.setFileName(resultFile); //присваиваем наименование файлу
            product.addImageToProduct(image); //добавляем фото в лист с фото, привязанный к этому продукту
        }
        if (!file_three.isEmpty()) {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFile = uuidFile + "." + file_three.getOriginalFilename();
            file_three.transferTo(new File(uploadPath + "/" + resultFile));
            Image image = new Image(); //создаем объёкт модели Image (фото)
            image.setProduct(product); //к фотографии привязываем продукт
            image.setFileName(resultFile); //присваиваем наименование файлу
            product.addImageToProduct(image); //добавляем фото в лист с фото, привязанный к этому продукту
        }
        if (!file_four.isEmpty()) {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFile = uuidFile + "." + file_four.getOriginalFilename();
            file_four.transferTo(new File(uploadPath + "/" + resultFile));
            Image image = new Image(); //создаем объёкт модели Image (фото)
            image.setProduct(product); //к фотографии привязываем продукт
            image.setFileName(resultFile); //присваиваем наименование файлу
            product.addImageToProduct(image); //добавляем фото в лист с фото, привязанный к этому продукту
        }
        if (!file_five.isEmpty()) {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFile = uuidFile + "." + file_five.getOriginalFilename();
            file_five.transferTo(new File(uploadPath + "/" + resultFile));
            Image image = new Image(); //создаем объёкт модели Image (фото)
            image.setProduct(product); //к фотографии привязываем продукт
            image.setFileName(resultFile); //присваиваем наименование файлу
            product.addImageToProduct(image); //добавляем фото в лист с фото, привязанный к этому продукту
        }
        productService.updateProduct(id, product);
        return "redirect:/product/info/{id}";
    }

    //Отображение страницы со всеми заказами
    @GetMapping("/admin/orders")
    public String showAllOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("status", Status.values());
        return "/admin/orders";
    }

    //Подробная информация о заказе и изменение его статуса
    @GetMapping("/admin/order/{id}")
    public String orderInfo(@PathVariable("id") int id, Model model) {
        model.addAttribute("order", orderService.getOrderById(id));
        model.addAttribute("status", Status.values());
        return "/admin/orderInfo";
    }

    //Изменение статуса заказа на странице с заказами
    @PostMapping("/admin/order/{id}")
    public String ChangeOrderStatus(@ModelAttribute("status") Status status,
                                    @PathVariable("id") int id)
    {
        Order order = orderService.getOrderById(id); //получаем объект заказа из БД
        order.setStatus(status); //меняем статус на выбранный в селекте
        orderService.updateOrder(id, order); //обновляем данные заказа в БД
        return "redirect:/admin/order/{id}";
    }

    //Поиск заказов по последним символам
    @PostMapping("/admin/order/search")
    public String orderSearch(@RequestParam("search") String search, Model model){
        //кладем в модель все заказы
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("search_order", orderRepository.findOrderByPartOfNumber(search));
        //Кладём в модель обратно полученные значения с формы для того, чтобы после отправки формы (произойдёт перезагрузка
        // страницы) отправить в инпут ранее введённое значение
        model.addAttribute("value_search", search);
        return "/admin/orders";
    }

    //Отображение страницы со всеми пользователями
    @GetMapping("/admin/users")
    public String showAllUsers(Model model) {
        model.addAttribute("users", personService.findAllUsers());
        model.addAttribute("role", Role.values());
        return "/admin/users";
    }

    //Подробная информация о выбранном пользователе
    @GetMapping("/admin/user/{id}")
    public String userInfo(@PathVariable("id") int id, Model model) {
        model.addAttribute("user", personService.findById(id));
        model.addAttribute("role", Role.values());
        return "/admin/userInfo";
    }

    @PostMapping("/admin/user/{id}")
    public String setUserRole(@PathVariable("id") int id, @ModelAttribute("user") Person user) {
        // Создаем список из всех ролей, которые используются в проекте
        List<String> validRoles = List.of("ROLE_ADMIN", "ROLE_USER", "ROLE_SELLER");
        // Проверяем, что значение роли находится в списке допустимых ролей
        if (!validRoles.contains(user.getRole())) {
            throw new IllegalArgumentException("Invalid role: " + user.getRole());
        }
        // Получаем пользователя из базы данных
        Person userToEdit = personService.findById(id);
        if (userToEdit == null) {
            throw new IllegalArgumentException("Invalid user id: " + id);
        }
        // Обновляем роль пользователя и сохраняем его в базе данных
        userToEdit.setRole(user.getRole());
        personService.updatePerson(id, userToEdit);
        return "redirect:/admin/user/{id}";
    }
    // Удаление заказа
//    @PostMapping("/admin/order/{id}/delete")
//    public String deleteOrder(@PathVariable int id) {
//        // Получаем заказ из БД
//        Optional<Order> order = orderRepository.findById(id);
//        // Проверяем, существует ли заказ
//        if (order.isPresent()) {
//            // Получаем список продуктов, связанных с заказом
//            List<Product> products = (List<Product>) order.get().getProduct();
//            // Удаляем все продукты
//            productRepository.deleteAll(products);
//            // Получаем список изображений, связанных с заказом
//            List<Image> images = new ArrayList<>();
//            for (Product product : products) {
//                images.addAll(imageRepository.findAllImageForProductId(product.getId()));
//            }
//            // Удаляем все изображения товаров, связанных с заказом
//            imageRepository.deleteAll(images);
//            // Удаляем сам заказ
//            orderRepository.delete(order.get());
//        }
//        return "redirect:/admin/order/";
//    }
}
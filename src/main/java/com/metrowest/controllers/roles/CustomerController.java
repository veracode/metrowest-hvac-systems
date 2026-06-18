package com.metrowest.controllers.roles;

import com.metrowest.entity.Order;
import com.metrowest.entity.OrderEntry;
import com.metrowest.entity.OrderItems;
import com.metrowest.entity.OrderStatus;
import com.metrowest.entity.Product;
import com.metrowest.repo.OrderRepository;
import com.metrowest.repo.ProductRepository;
import com.metrowest.repo.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Controller
@RequestMapping("/customer")
public class CustomerController
{
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public CustomerController(UserRepository userRepository,
                              ProductRepository productRepository,
                              OrderRepository orderRepository)
    {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    private List<OrderEntry> convert_items(Order order, OrderItems items)
    {
        var entries = new ArrayList<OrderEntry>();
        for (var entry : items.getQuantities().entrySet())
        {
            var product = productRepository.findByName(entry.getKey())
                .orElseThrow(() -> new IllegalArgumentException("unknown product: " + entry.getKey()));
            var order_entry = new OrderEntry();
            order_entry.setOrder(order);
            order_entry.setProduct(product);
            order_entry.setQuantity(entry.getValue());
            order_entry.setUnitPrice(product.getPrice());
            entries.add(order_entry);
        }
        return entries;
    }

    @PostMapping("/new_order")
    public String new_order(Model model, Authentication authentication,
                            @RequestParam("items") OrderItems items)
    {
        var user = userRepository.findByUsername(authentication.getName());
        if (user.isEmpty())
        {
            model.addAttribute("error", "user not found: " + authentication.getName());
            return "error";
        }
        var order = new Order();
        var saved = order;
        try
        {
            var entries = convert_items(order, items);
            order.getItems().addAll(entries);
            order.setCustomer(user.get());
            order.setStatus(OrderStatus.NEW);
            saved = orderRepository.save(order);
            orderRepository.flush();
        }
        catch (Exception e)
        {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
        model.addAttribute("message", "order created: " + saved.getId());
        return "customer/dashboard";
    }

    @GetMapping("/dashboard")
    public String root(Model model, Authentication authentication)
    {
        var user = userRepository.findByUsername(authentication.getName());
        if (user.isEmpty())
        {
            model.addAttribute("error", "user not found: " + authentication.getName());
            return "error";
        }

        var products = productRepository.findAll().stream()
            .map(Product::getName)
            .toList();

        var orders = orderRepository.findByCustomer(user.get());
        model.addAttribute("orders", orders);
        model.addAttribute("products", products);
        return "customer/dashboard";
    }
}

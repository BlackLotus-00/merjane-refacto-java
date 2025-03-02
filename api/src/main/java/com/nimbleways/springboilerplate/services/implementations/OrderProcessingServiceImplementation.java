package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.OrderProcessingService;
import com.nimbleways.springboilerplate.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

@Service
public class OrderProcessingServiceImplementation implements OrderProcessingService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    public ProcessOrderResponse processOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        Set<Product> products = order.getItems();
        for (Product p : products) {
            switch (p.getType()) {
                case "NORMAL" -> handleNormalProduct(p);
                case "SEASONAL" -> handleSeasonalProduct(p);
                case "EXPIRABLE" -> handleExpirableProduct(p);
                default -> {
                    System.out.println("Unknown product type: " + p.getType());
                }
            }
        }

        return new ProcessOrderResponse(order.getId());
    }

    private void handleNormalProduct(Product p) {
        if (p.getAvailable() > 0) {
            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);
            return;
        }
        int leadTime = p.getLeadTime();
        if (leadTime > 0) {
            productService.notifyDelay(leadTime, p);
        }
    }

    private void handleSeasonalProduct(Product p) {
        if (LocalDate.now().isAfter(p.getSeasonStartDate())
                && LocalDate.now().isBefore(p.getSeasonEndDate())
                && p.getAvailable() > 0) {
            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);
        } else {
            productService.handleSeasonalProduct(p);
        }
    }

    private void handleExpirableProduct(Product p) {
        if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);
        } else {
            productService.handleExpiredProduct(p);
        }
    }
}

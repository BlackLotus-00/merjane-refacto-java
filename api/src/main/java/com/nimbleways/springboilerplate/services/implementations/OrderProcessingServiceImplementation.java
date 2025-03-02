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

        order.getItems().forEach(this::processProduct);

        return new ProcessOrderResponse(order.getId());
    }

    private void processProduct(Product product) {
        switch (product.getType()) {
            case "NORMAL" -> handleNormalProduct(product);
            case "SEASONAL" -> handleSeasonalProduct(product);
            case "EXPIRABLE" -> handleExpirableProduct(product);
            default -> System.out.println("Unknown product type: " + product.getType());
        }
    }

    private void handleNormalProduct(Product product) {
        if (product.getAvailable() > 0) {
            decrementStock(product);
        } else if (product.getLeadTime() > 0) {
            productService.notifyDelay(product.getLeadTime(), product);
        }
    }

    private void handleSeasonalProduct(Product product) {
        if (isInSeason(product) && product.getAvailable() > 0) {
            decrementStock(product);
        } else {
            productService.handleSeasonalProduct(product);
        }
    }

    private void handleExpirableProduct(Product product) {
        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now())) {
            decrementStock(product);
        } else {
            productService.handleExpiredProduct(product);
        }
    }

    private void decrementStock(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }

    private boolean isInSeason(Product product) {
        LocalDate today = LocalDate.now();
        return today.isAfter(product.getSeasonStartDate()) && today.isBefore(product.getSeasonEndDate());
    }
}
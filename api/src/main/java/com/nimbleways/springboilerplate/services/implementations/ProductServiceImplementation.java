package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;

import com.nimbleways.springboilerplate.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
public class ProductServiceImplementation implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private NotificationService notificationService;

    public void notifyDelay(int leadTime, Product product) {
        product.setLeadTime(leadTime);
        productRepository.save(product);
        notificationService.sendDelayNotification(leadTime, product.getName());
    }

    public void handleSeasonalProduct(Product product) {
        if (isOutOfSeason(product)) {
            markAsUnavailable(product);
        } else if (isNotYetInSeason(product)) {
            notifyOutOfStock(product);
        } else {
            notifyDelay(product.getLeadTime(), product);
        }
    }

    public void handleExpiredProduct(Product product) {
        if (isNotExpired(product)) {
            decrementStock(product);
        } else {
            markAsExpired(product);
        }
    }

    private boolean isOutOfSeason(Product product) {
        return LocalDate.now().plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate());
    }

    private boolean isNotYetInSeason(Product product) {
        return product.getSeasonStartDate().isAfter(LocalDate.now());
    }

    private boolean isNotExpired(Product product) {
        return product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now());
    }

    private void markAsUnavailable(Product product) {
        notificationService.sendOutOfStockNotification(product.getName());
        product.setAvailable(0);
        productRepository.save(product);
    }

    private void notifyOutOfStock(Product product) {
        notificationService.sendOutOfStockNotification(product.getName());
        productRepository.save(product);
    }

    private void decrementStock(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }

    private void markAsExpired(Product product) {
        notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
        product.setAvailable(0);
        productRepository.save(product);
    }
}
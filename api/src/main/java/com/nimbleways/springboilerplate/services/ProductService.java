package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.entities.Product;

public interface ProductService {
        void notifyDelay(int leadTime, Product p);
        void handleSeasonalProduct(Product p);
        void handleExpiredProduct(Product p);
}

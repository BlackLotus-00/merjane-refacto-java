package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;

public interface OrderProcessingService {
    ProcessOrderResponse processOrder(Long orderId);
}

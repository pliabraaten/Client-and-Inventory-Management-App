package com.PL.pig_ranch.service;

import com.PL.pig_ranch.model.Order;
import java.util.List;
import java.util.Optional;

public interface OrderService {

    List<Order> getAllOrders();

    Optional<Order> getOrderById(Long id);

    Order saveOrder(Order order);

    void deleteOrder(Long id);

    Order completeOrder(Long id);
}

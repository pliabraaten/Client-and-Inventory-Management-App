package com.PL.pig_ranch.service.impl;

import com.PL.pig_ranch.model.Order;
import com.PL.pig_ranch.model.OrderItem;
import com.PL.pig_ranch.repository.OrderRepository;
import com.PL.pig_ranch.service.OrderService;
import com.PL.pig_ranch.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Order completeOrder(Long id) {
        return orderRepository.findById(id).map(order -> {
            if (order.getStatus() == Order.OrderStatus.OPEN) {
                // Deduct inventory for each item in the order
                for (OrderItem orderItem : order.getOrderItems()) {
                    inventoryService.updateStock(
                            orderItem.getItem().getId(),
                            -orderItem.getQuantity(),
                            com.PL.pig_ranch.model.InventoryTransaction.TransactionType.ORDER,
                            "Order #" + order.getId() + " completed");
                }
                order.setStatus(Order.OrderStatus.COMPLETED);
                return orderRepository.save(order);
            }
            return order;
        }).orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }
}

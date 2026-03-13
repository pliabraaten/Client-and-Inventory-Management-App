package com.PL.pig_ranch.service.impl;

import com.PL.pig_ranch.model.Order;
import com.PL.pig_ranch.model.OrderItem;
import com.PL.pig_ranch.repository.OrderRepository;
import com.PL.pig_ranch.service.OrderService;
import com.PL.pig_ranch.service.InventoryService;
import com.PL.pig_ranch.exception.EntityNotFoundException;
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
        order.evaluateStatus();
        if (!order.isWasFulfilled() && order.getStatus() == Order.OrderStatus.FULFILLED) {
            deductInventory(order);
        }
        order.setWasFulfilled(order.getStatus() == Order.OrderStatus.FULFILLED);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Order markAsPaid(Long id) {
        return orderRepository.findById(id).map(order -> {
            order.setPaid(true);
            return saveOrder(order);
        }).orElseThrow(() -> new EntityNotFoundException("Order", id));
    }

    @Override
    @Transactional
    public Order markAsShipped(Long id) {
        return orderRepository.findById(id).map(order -> {
            order.setShipped(true);
            return saveOrder(order);
        }).orElseThrow(() -> new EntityNotFoundException("Order", id));
    }

    private void deductInventory(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            inventoryService.updateStock(
                    orderItem.getItem().getId(),
                    -orderItem.getQuantity(),
                    com.PL.pig_ranch.model.InventoryTransaction.TransactionType.ORDER,
                    "Order #" + order.getId() + " fulfilled");
        }
    }
}

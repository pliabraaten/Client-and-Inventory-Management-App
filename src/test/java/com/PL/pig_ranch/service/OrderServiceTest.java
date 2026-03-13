package com.PL.pig_ranch.service;

import com.PL.pig_ranch.exception.EntityNotFoundException;
import com.PL.pig_ranch.model.InventoryItem;
import com.PL.pig_ranch.model.Order;
import com.PL.pig_ranch.model.OrderItem;
import com.PL.pig_ranch.repository.OrderRepository;
import com.PL.pig_ranch.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderItems(new ArrayList<>());
    }

    @Test
    void testGetAllOrders() {
        orderService.getAllOrders();
        verify(orderRepository).findAll();
    }

    @Test
    void testGetOrderById_Found() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        Optional<Order> result = orderService.getOrderById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void testGetOrderById_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<Order> result = orderService.getOrderById(1L);
        assertFalse(result.isPresent());
    }

    @Test
    void testSaveOrder_StatusEvaluation() {
        testOrder.setPaid(true);
        testOrder.setShipped(true);
        // saveOrder calls evaluateStatus internally

        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order saved = orderService.saveOrder(testOrder);

        assertEquals(Order.OrderStatus.FULFILLED, saved.getStatus());
        verify(orderRepository).save(testOrder);
    }

    @Test
    void testSaveOrder_InventoryDeduction_OnFirstFulfillment() {
        testOrder.setPaid(true);
        testOrder.setShipped(true);
        testOrder.setWasFulfilled(false); // Flag indicates it wasn't fulfilled before

        InventoryItem item = new InventoryItem();
        item.setId(10L);
        item.setName("Test Item");

        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setQuantity(5);
        testOrder.getOrderItems().add(orderItem);

        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.saveOrder(testOrder);

        // Verify inventory deduction was called
        verify(inventoryService).updateStock(eq(10L), eq(-5), any(), anyString());
        assertTrue(testOrder.isWasFulfilled());
    }

    @Test
    void testSaveOrder_NoInventoryDeduction_IfAlreadyWasFulfilled() {
        testOrder.setPaid(true);
        testOrder.setShipped(true);
        testOrder.setStatus(Order.OrderStatus.FULFILLED);
        testOrder.setWasFulfilled(true); // Flag indicates it was already fulfilled

        InventoryItem item = new InventoryItem();
        item.setId(10L);

        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setQuantity(5);
        testOrder.getOrderItems().add(orderItem);

        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.saveOrder(testOrder);

        // Verify inventory deduction NOT called again
        verify(inventoryService, never()).updateStock(anyLong(), anyInt(), any(), anyString());
    }

    @Test
    void testMarkAsPaid_Success() {
        testOrder.setPaid(false);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.markAsPaid(1L);

        assertTrue(result.isPaid());
        verify(orderRepository).save(testOrder);
    }

    @Test
    void testMarkAsPaid_NotFound_ThrowsEntityNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> orderService.markAsPaid(1L));
    }

    @Test
    void testMarkAsShipped_Success() {
        testOrder.setShipped(false);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.markAsShipped(1L);

        assertTrue(result.isShipped());
        verify(orderRepository).save(testOrder);
    }

    @Test
    void testMarkAsShipped_NotFound_ThrowsEntityNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> orderService.markAsShipped(1L));
    }

    @Test
    void testDeleteOrder_Success() {
        when(orderRepository.existsById(1L)).thenReturn(true);
        orderService.deleteOrder(1L);
        verify(orderRepository).deleteById(1L);
    }

    @Test
    void testDeleteOrder_NotFound_ThrowsEntityNotFoundException() {
        when(orderRepository.existsById(1L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> orderService.deleteOrder(1L));
    }
}

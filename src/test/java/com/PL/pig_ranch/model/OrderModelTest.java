package com.PL.pig_ranch.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.ArrayList;

class OrderModelTest {

    @Test
    void testOrderTypeDefaultsToStandard() {
        Order order = new Order();
        assertEquals(Order.OrderType.STANDARD, order.getType());
    }

    @Test
    void testSetOrderTypeHog() {
        Order order = new Order();
        order.setType(Order.OrderType.HOG);
        assertEquals(Order.OrderType.HOG, order.getType());
    }

    @Test
    void testTotalPriceWithHog() {
        Order order = new Order();
        order.setType(Order.OrderType.HOG);

        Hog hog = new Hog();
        hog.setProcessingCost(new BigDecimal("150.00"));

        order.setHogs(new ArrayList<>());
        order.getHogs().add(hog);

        assertEquals(0, new BigDecimal("150.00").compareTo(order.getTotalPrice()));
    }
}

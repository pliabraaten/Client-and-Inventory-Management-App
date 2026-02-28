package com.PL.pig_ranch.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
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
        hog.setProcessingCost(150.0);

        order.setHogs(new ArrayList<>());
        order.getHogs().add(hog);

        assertEquals(150.0, order.getTotalPrice());
    }
}

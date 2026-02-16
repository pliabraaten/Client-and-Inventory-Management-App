package com.PL.pig_ranch.controller;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;

@Component
public class HomeController {

    private final ApplicationEventPublisher eventPublisher;

    public HomeController(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @FXML
    public void handleClientsClick() {
        eventPublisher.publishEvent(new NavigationEvent(this, "CLIENTS"));
    }

    @FXML
    public void handleInventoryClick() {
        // Placeholder for Inventory navigation
        System.out.println("Inventory clicked");
    }
}

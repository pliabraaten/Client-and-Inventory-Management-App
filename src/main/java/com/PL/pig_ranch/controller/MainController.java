package com.PL.pig_ranch.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;

/**
 * JavaFX controller for the main application window (main.fxml).
 * Managed by Spring so it can inject services later.
 */
@Component
public class MainController {

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        statusLabel.setText("Pig Ranch — Ready");
    }
}

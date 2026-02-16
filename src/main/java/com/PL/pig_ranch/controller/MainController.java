package com.PL.pig_ranch.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MainController implements ApplicationListener<NavigationEvent> {

    private final ApplicationContext context;

    @FXML
    private BorderPane mainLayout;

    public MainController(ApplicationContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        loadView("/fxml/home.fxml");
    }

    @Override
    public void onApplicationEvent(NavigationEvent event) {
        switch (event.getViewName()) {
            case "CLIENTS":
                loadView("/fxml/client_view.fxml");
                break;
            case "HOME":
                loadView("/fxml/home.fxml");
                break;
            default:
                System.out.println("Unknown view: " + event.getViewName());
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(context::getBean);
            Parent view = loader.load();
            mainLayout.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

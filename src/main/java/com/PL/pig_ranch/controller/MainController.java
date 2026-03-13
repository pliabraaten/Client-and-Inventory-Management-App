package com.PL.pig_ranch.controller;

import com.PL.pig_ranch.model.Order;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MainController implements ApplicationListener<NavigationEvent> {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    private final ApplicationContext context;

    @FXML
    private BorderPane mainLayout;

    public MainController(ApplicationContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        loadView("/fxml/home.fxml");
        updateWindowTitle("Home");
    }

    @Override
    public void onApplicationEvent(NavigationEvent event) {
        switch (event.getViewName()) {
            case "CLIENTS":
                loadView("/fxml/client_view.fxml");
                updateWindowTitle("Clients");
                break;
            case "INVENTORY":
                loadView("/fxml/inventory.fxml");
                updateWindowTitle("Inventory");
                break;
            case "ORDERS":
                loadView("/fxml/order_view.fxml");
                updateWindowTitle("Order History");
                break;
            case "PENDING_ORDERS":
                loadView("/fxml/pending_orders.fxml");
                updateWindowTitle("Pending Orders");
                break;
            case "NEW_ORDER":
                showOrderDialog(null, false);
                break;
            case "HOME":
                loadView("/fxml/home.fxml");
                updateWindowTitle("Home");
                break;
            default:
                log.warn("Unknown view requested: {}", event.getViewName());
        }
    }

    private void updateWindowTitle(String viewName) {
        Stage stage = (Stage) mainLayout.getScene().getWindow();
        if (stage != null) {
            stage.setTitle("Pig Ranch — " + viewName);
        }
    }

    private void showOrderDialog(Order order, boolean readOnly) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order_dialog.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            OrderDialogController controller = loader.getController();
            controller.setOrder(order);
            controller.setReadOnly(readOnly);

            Stage stage = new Stage();
            stage.setTitle(order == null ? "New Order" : "Edit Order #" + order.getId());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showErrorAlert("Failed to open order dialog", e);
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(context::getBean);
            Parent view = loader.load();
            mainLayout.setCenter(view);
        } catch (IOException e) {
            showErrorAlert("Failed to load view: " + fxmlPath, e);
        }
    }

    private void showErrorAlert(String message, Exception e) {
        log.error(message, e);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Application Error");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage() != null ? e.getMessage() : "An unexpected error occurred.");
        alert.showAndWait();
    }
}

package com.PL.pig_ranch.controller;

import com.PL.pig_ranch.model.Order;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
            case "INVENTORY":
                loadView("/fxml/inventory.fxml");
                break;
            case "ORDERS":
                loadView("/fxml/order_view.fxml");
                break;
            case "PENDING_ORDERS":
                loadView("/fxml/pending_orders.fxml");
                break;
            case "NEW_ORDER":
                showOrderDialog(null, false);
                break;
            case "HOME":
                loadView("/fxml/home.fxml");
                break;
            default:
                System.out.println("Unknown view: " + event.getViewName());
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
            e.printStackTrace();
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

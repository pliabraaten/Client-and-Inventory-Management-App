package com.PL.pig_ranch.controller;

import com.PL.pig_ranch.model.Order;
import com.PL.pig_ranch.service.OrderService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;
    private final ApplicationContext context;

    private ObservableList<Order> allOrders;
    private FilteredList<Order> filteredOrders;

    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, Long> colId;
    @FXML
    private TableColumn<Order, String> colClient;
    @FXML
    private TableColumn<Order, String> colDate;
    @FXML
    private TableColumn<Order, String> colType;
    @FXML
    private TableColumn<Order, String> colStatus;
    @FXML
    private TableColumn<Order, BigDecimal> colTotal;
    @FXML
    private TableColumn<Order, Void> colActions;
    @FXML
    private TextField searchField;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    public OrderController(OrderService orderService, ApplicationEventPublisher eventPublisher,
            ApplicationContext context) {
        this.orderService = orderService;
        this.eventPublisher = eventPublisher;
        this.context = context;
    }

    @FXML
    public void initialize() {
        setupTable();
        setupSearch();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClient.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getClient() != null ? cellData.getValue().getClient().getName() : "N/A"));
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getOrderDate() != null ? cellData.getValue().getOrderDate().format(formatter)
                        : ""));
        colType.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getType() != null ? cellData.getValue().getType().name() : "STANDARD"));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().name() : ""));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        setupActionsColumn();

        // Double-click also opens order details in read-only mode
        orderTable.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Order clickedOrder = row.getItem();
                    showDialog(clickedOrder, true);
                }
            });
            return row;
        });
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");

            {
                viewBtn.getStyleClass().add("btn-info");
                viewBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    showDialog(order, true);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewBtn);
                }
            }
        });
    }

    private void setupSearch() {
        allOrders = FXCollections.observableArrayList();
        filteredOrders = new FilteredList<>(allOrders, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
        });

        orderTable.setItems(filteredOrders);
    }

    private void applyFilters() {
        if (filteredOrders == null)
            return;

        filteredOrders.setPredicate(order -> {
            // Only show FULFILLED and CANCELLED orders
            boolean isHistory = order.getStatus() == Order.OrderStatus.FULFILLED
                    || order.getStatus() == Order.OrderStatus.CANCELLED;
            if (!isHistory)
                return false;

            String search = searchField.getText();
            if (search == null || search.isEmpty())
                return true;

            String lower = search.toLowerCase();
            String clientName = order.getClient() != null ? order.getClient().getName().toLowerCase() : "";
            String type = order.getType() != null ? order.getType().name().toLowerCase() : "";
            String status = order.getStatus() != null ? order.getStatus().name().toLowerCase() : "";

            return clientName.contains(lower) || type.contains(lower) || status.contains(lower);
        });
    }

    private void loadData() {
        List<Order> orders = orderService.getAllOrders();
        allOrders.setAll(orders);
        applyFilters();
    }

    private void showDialog(Order order, boolean readOnly) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order_dialog.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            OrderDialogController controller = loader.getController();
            controller.setOrder(order);
            controller.setReadOnly(readOnly);

            Stage stage = new Stage();
            stage.setTitle("Order #" + order.getId() + " Details");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showErrorAlert("Failed to open order details", e);
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

    @FXML
    public void handleBackClick() {
        eventPublisher.publishEvent(new NavigationEvent(this, "HOME"));
    }
}

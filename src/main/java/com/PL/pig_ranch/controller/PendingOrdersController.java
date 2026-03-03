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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class PendingOrdersController {

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
    private TableColumn<Order, Double> colTotal;
    @FXML
    private TableColumn<Order, Void> colActions;
    @FXML
    private TextField searchField;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    public PendingOrdersController(OrderService orderService, ApplicationEventPublisher eventPublisher,
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

        orderTable.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Order clickedOrder = row.getItem();
                    showDialog(clickedOrder, false);
                    loadData();
                }
            });
            return row;
        });

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button paidBtn = new Button("Mark Paid");
            private final Button shippedBtn = new Button("Mark Shipped");
            private final HBox container = new HBox(5, editBtn, deleteBtn, paidBtn, shippedBtn);

            {
                editBtn.setStyle("-fx-background-color: #4444ff; -fx-text-fill: white;");
                editBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleEdit(order);
                });

                deleteBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleDelete(order);
                });

                paidBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                paidBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    orderService.markAsPaid(order.getId());
                    loadData();
                });

                shippedBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
                shippedBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    orderService.markAsShipped(order.getId());
                    loadData();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    if (order != null) {
                        paidBtn.setVisible(!order.isPaid());
                        paidBtn.setManaged(!order.isPaid());
                        shippedBtn.setVisible(!order.isShipped());
                        shippedBtn.setManaged(!order.isShipped());
                    }
                    setGraphic(container);
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
            // Only show OPEN and PENDING orders
            boolean isActive = order.getStatus() == Order.OrderStatus.OPEN
                    || order.getStatus() == Order.OrderStatus.PENDING;
            if (!isActive)
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

    private void handleEdit(Order order) {
        showDialog(order, false);
        loadData();
    }

    private void handleDelete(Order order) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Order #" + order.getId());
        alert.setContentText("Are you sure? This will also delete all associated items.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                orderService.deleteOrder(order.getId());
                loadData();
            }
        });
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
            stage.setTitle(order == null ? "New Order" : "Edit Order #" + order.getId());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBackClick() {
        eventPublisher.publishEvent(new NavigationEvent(this, "HOME"));
    }

    @FXML
    public void handleNewOrderClick() {
        showDialog(null, false);
        loadData();
    }
}

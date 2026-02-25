package com.PL.pig_ranch.controller;

import com.PL.pig_ranch.model.InventoryItem;
import com.PL.pig_ranch.service.InventoryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryController {

    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;
    private final ApplicationContext context;

    private ObservableList<InventoryItem> allItems;
    private FilteredList<InventoryItem> filteredItems;

    @FXML
    private TableView<InventoryItem> inventoryTable;
    @FXML
    private TableColumn<InventoryItem, Long> colId;
    @FXML
    private TableColumn<InventoryItem, String> colName;
    @FXML
    private TableColumn<InventoryItem, String> colType;
    @FXML
    private TableColumn<InventoryItem, Integer> colQuantity;
    @FXML
    private TableColumn<InventoryItem, String> colDescription;
    @FXML
    private TableColumn<InventoryItem, Void> colActions;
    @FXML
    private TextField searchField;

    @Autowired
    public InventoryController(InventoryService inventoryService, ApplicationEventPublisher eventPublisher,
            ApplicationContext context) {
        this.inventoryService = inventoryService;
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
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button adjustBtn = new Button("Adjust");
            private final Button editBtn = new Button("Edit");
            private final Button historyBtn = new Button("Logs");
            private final HBox container = new HBox(5, adjustBtn, editBtn, historyBtn);

            {
                adjustBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                adjustBtn.setOnAction(event -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    handleAdjust(item);
                });

                editBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
                editBtn.setOnAction(event -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    handleEdit(item);
                });

                historyBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                historyBtn.setOnAction(event -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    handleHistory(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void setupSearch() {
        allItems = FXCollections.observableArrayList();
        filteredItems = new FilteredList<>(allItems, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredItems.setPredicate(item -> {
                if (newVal == null || newVal.isEmpty())
                    return true;
                String lower = newVal.toLowerCase();
                return (item.getName() != null && item.getName().toLowerCase().contains(lower)) ||
                        (item.getType() != null && item.getType().toLowerCase().contains(lower));
            });
        });

        inventoryTable.setItems(filteredItems);
    }

    private void loadData() {
        List<InventoryItem> items = inventoryService.getAllItems();
        allItems.setAll(items);
    }

    private void handleAdjust(InventoryItem item) {
        showDialog(item, InventoryDialogController.DialogMode.ADJUST);
        loadData();
    }

    private void handleEdit(InventoryItem item) {
        showDialog(item, InventoryDialogController.DialogMode.EDIT);
        loadData();
    }

    private void handleHistory(InventoryItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/inventory_history.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            TransactionHistoryController controller = loader.getController();
            controller.setItem(item);

            Stage stage = new Stage();
            stage.setTitle("Transaction History: " + item.getName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDialog(InventoryItem item, InventoryDialogController.DialogMode mode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/inventory_dialog.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            InventoryDialogController controller = loader.getController();
            controller.setItem(item, mode);

            Stage stage = new Stage();
            String title = "Inventory Item";
            if (mode == InventoryDialogController.DialogMode.ADD)
                title = "Add Item";
            else if (mode == InventoryDialogController.DialogMode.ADJUST)
                title = "Adjust Stock";
            else if (mode == InventoryDialogController.DialogMode.EDIT)
                title = "Edit Details";

            stage.setTitle(title);
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
    public void handleAddItemClick() {
        showDialog(null, InventoryDialogController.DialogMode.ADD);
        loadData();
    }
}

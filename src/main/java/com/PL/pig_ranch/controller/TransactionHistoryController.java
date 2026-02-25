package com.PL.pig_ranch.controller;

import com.PL.pig_ranch.model.InventoryItem;
import com.PL.pig_ranch.model.InventoryTransaction;
import com.PL.pig_ranch.service.InventoryService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@Scope("prototype")
public class TransactionHistoryController {

    private final InventoryService inventoryService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML private Label titleLabel;
    @FXML private TableView<InventoryTransaction> transactionTable;
    @FXML private TableColumn<InventoryTransaction, String> colTimestamp;
    @FXML private TableColumn<InventoryTransaction, InventoryTransaction.TransactionType> colType;
    @FXML private TableColumn<InventoryTransaction, Integer> colAmount;
    @FXML private TableColumn<InventoryTransaction, String> colNotes;

    @Autowired
    public TransactionHistoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @FXML
    public void initialize() {
        colTimestamp.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTimestamp().format(FORMATTER)));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("changeAmount"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
    }

    public void setItem(InventoryItem item) {
        titleLabel.setText("History: " + item.getName());
        transactionTable.setItems(FXCollections.observableArrayList(inventoryService.getTransactionHistory(item.getId())));
    }

    @FXML
    public void handleClose() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }
}

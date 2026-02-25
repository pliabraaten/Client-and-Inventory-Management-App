package com.PL.pig_ranch.controller;

import com.PL.pig_ranch.model.InventoryItem;
import com.PL.pig_ranch.model.InventoryTransaction;
import com.PL.pig_ranch.service.InventoryService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class InventoryDialogController {

    private final InventoryService inventoryService;
    private InventoryItem editingItem;
    private boolean isAdjustmentMode = false;

    @FXML
    private Label titleLabel;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label quantityLabel;
    @FXML
    private TextField quantityField;
    @FXML
    private HBox adjustmentBox;
    @FXML
    private ComboBox<InventoryTransaction.TransactionType> adjTypeComboBox;

    @Autowired
    public InventoryDialogController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList("Feed", "Medication", "Tool", "Livestock", "Other"));
        adjTypeComboBox.setItems(FXCollections.observableArrayList(InventoryTransaction.TransactionType.values()));
        adjTypeComboBox.setValue(InventoryTransaction.TransactionType.ADJUSTMENT);
    }

    public void setItem(InventoryItem item) {
        this.editingItem = item;
        if (item != null) {
            isAdjustmentMode = true;
            titleLabel.setText("Adjust Inventory: " + item.getName());
            nameField.setText(item.getName());
            nameField.setEditable(false);
            typeComboBox.setValue(item.getType());
            typeComboBox.setDisable(true);
            descriptionArea.setText(item.getDescription());
            descriptionArea.setEditable(false);

            quantityLabel.setText("Amount (+/-):");
            quantityField.setText("");
            adjustmentBox.setVisible(true);
            adjustmentBox.setManaged(true);
        } else {
            isAdjustmentMode = false;
            titleLabel.setText("Add New Inventory Item");
        }
    }

    @FXML
    public void handleSave() {
        try {
            if (isAdjustmentMode) {
                int amount = Integer.parseInt(quantityField.getText());
                inventoryService.updateStock(editingItem.getId(), amount, adjTypeComboBox.getValue(),
                        "Manual update via UI");
            } else {
                InventoryItem newItem = new InventoryItem();
                newItem.setName(nameField.getText());
                newItem.setType(typeComboBox.getValue());
                newItem.setDescription(descriptionArea.getText());
                newItem.setQuantity(Integer.parseInt(quantityField.getText()));
                inventoryService.saveItem(newItem);
            }
            close();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a valid number for quantity.");
            alert.showAndWait();
        }
    }

    @FXML
    public void handleCancel() {
        close();
    }

    private void close() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}

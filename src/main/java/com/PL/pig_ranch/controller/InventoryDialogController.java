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

    public enum DialogMode {
        ADD, ADJUST, EDIT
    }

    private DialogMode currentMode = DialogMode.ADD;

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
    private Label typeLabel;
    @FXML
    private Label descLabel;
    @FXML
    private Label nameLabel;
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

    public void setItem(InventoryItem item, DialogMode mode) {
        this.editingItem = item;
        this.currentMode = mode;

        if (mode == DialogMode.ADJUST) {
            titleLabel.setText("Adjust Inventory: " + item.getName());

            // Hide metadata fields
            toggleMetadataFields(false);

            quantityLabel.setText("Amount (+/-):");
            quantityField.setText("");
            adjustmentBox.setVisible(true);
            adjustmentBox.setManaged(true);

        } else if (mode == DialogMode.EDIT) {
            titleLabel.setText("Edit Item: " + item.getName());

            // Show metadata fields as editable
            toggleMetadataFields(true);
            nameField.setText(item.getName());
            typeComboBox.setValue(item.getType());
            descriptionArea.setText(item.getDescription());

            // Hide adjustment fields
            quantityLabel.setVisible(false);
            quantityLabel.setManaged(false);
            quantityField.setVisible(false);
            quantityField.setManaged(false);
            adjustmentBox.setVisible(false);
            adjustmentBox.setManaged(false);

        } else { // ADD
            titleLabel.setText("Add New Inventory Item");
            toggleMetadataFields(true);
            quantityLabel.setText("Initial Quantity:");
            quantityField.setText("0");
            adjustmentBox.setVisible(false);
            adjustmentBox.setManaged(false);
        }
    }

    private void toggleMetadataFields(boolean visible) {
        nameLabel.setVisible(visible);
        nameLabel.setManaged(visible);
        nameField.setVisible(visible);
        nameField.setManaged(visible);

        typeLabel.setVisible(visible);
        typeLabel.setManaged(visible);
        typeComboBox.setVisible(visible);
        typeComboBox.setManaged(visible);

        descLabel.setVisible(visible);
        descLabel.setManaged(visible);
        descriptionArea.setVisible(visible);
        descriptionArea.setManaged(visible);
    }

    @FXML
    public void handleSave() {
        try {
            if (currentMode == DialogMode.ADJUST) {
                int amount = Integer.parseInt(quantityField.getText());
                inventoryService.updateStock(editingItem.getId(), amount, adjTypeComboBox.getValue(),
                        "Manual stock adjustment");
            } else if (currentMode == DialogMode.EDIT) {
                editingItem.setName(nameField.getText());
                editingItem.setType(typeComboBox.getValue());
                editingItem.setDescription(descriptionArea.getText());
                inventoryService.saveItem(editingItem);
            } else { // ADD
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

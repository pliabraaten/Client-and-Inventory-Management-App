package com.PL.pig_ranch.controller;

import com.PL.pig_ranch.model.Client;
import com.PL.pig_ranch.model.Hog;
import com.PL.pig_ranch.model.InventoryItem;
import com.PL.pig_ranch.model.Order;
import com.PL.pig_ranch.model.OrderItem;
import com.PL.pig_ranch.service.ClientService;
import com.PL.pig_ranch.service.InventoryService;
import com.PL.pig_ranch.service.InvoiceService;
import com.PL.pig_ranch.service.OrderService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
public class OrderDialogController {

    private final OrderService orderService;
    private final ClientService clientService;
    private final InventoryService inventoryService;
    private final InvoiceService invoiceService;
    private final ApplicationContext applicationContext;
    private Order editingOrder;

    private ObservableList<Client> allClients;
    private FilteredList<Client> filteredClients;
    private ObservableList<OrderItem> orderItemsList;

    @FXML
    private Label titleLabel;
    @FXML
    private ComboBox<Client> clientComboBox;
    @FXML
    private CheckBox paidCheckBox;
    @FXML
    private CheckBox shippedCheckBox;
    @FXML
    private TextArea notesArea;

    @FXML
    private Button newClientButton;
    @FXML
    private Button addItemButton;
    @FXML
    private Button removeItemButton;
    @FXML
    private Button saveButton;

    // Item table
    @FXML
    private TableView<OrderItem> itemTable;
    @FXML
    private TableColumn<OrderItem, String> colItemName;
    @FXML
    private TableColumn<OrderItem, Number> colItemQuantity;
    @FXML
    private TableColumn<OrderItem, Number> colItemPrice;

    // Hog section
    @FXML
    private TitledPane hogTitledPane;
    @FXML
    private TextField hogNumberField;
    @FXML
    private ComboBox<Hog.HogType> hogTypeComboBox;
    @FXML
    private TextField processorField;
    @FXML
    private TextField liveWeightField;
    @FXML
    private TextField hangingWeightField;
    @FXML
    private TextField processingCostField;
    @FXML
    private CheckBox inspectedCheckBox;

    @Autowired
    public OrderDialogController(OrderService orderService, ClientService clientService,
            InventoryService inventoryService, InvoiceService invoiceService,
            ApplicationContext applicationContext) {
        this.orderService = orderService;
        this.clientService = clientService;
        this.inventoryService = inventoryService;
        this.invoiceService = invoiceService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        setupClientComboBox();
        setupItemTable();
        setupHogSection();
    }

    // ── Client ComboBox (editable + searchable) ──────────────────────────

    private void setupClientComboBox() {
        allClients = FXCollections.observableArrayList(clientService.getAllClients());
        filteredClients = new FilteredList<>(allClients, p -> true);

        clientComboBox.setEditable(true);
        clientComboBox.setItems(filteredClients);

        clientComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Client item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        clientComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Client client) {
                return client != null ? client.getName() : "";
            }

            @Override
            public Client fromString(String string) {
                if (string == null || string.isEmpty())
                    return null;
                return allClients.stream()
                        .filter(c -> c.getName().equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        // Filter as user types
        clientComboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            javafx.application.Platform.runLater(() -> {
                Client selected = clientComboBox.getSelectionModel().getSelectedItem();
                if (selected == null || !clientComboBox.getEditor().getText().equals(selected.getName())) {
                    filteredClients.setPredicate(client -> {
                        if (newVal == null || newVal.isEmpty())
                            return true;
                        return client.getName().toLowerCase().contains(newVal.toLowerCase());
                    });
                }
            });
            if (!clientComboBox.isShowing() && newVal != null && !newVal.isEmpty()) {
                clientComboBox.show();
            }
        });
    }

    // ── Item Table ────────────────────────────────────────────────────────

    private void setupItemTable() {
        orderItemsList = FXCollections.observableArrayList();
        itemTable.setItems(orderItemsList);

        colItemName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem() != null
                ? cellData.getValue().getItem().getName()
                : ""));
        colItemQuantity
                .setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity() != null
                        ? cellData.getValue().getQuantity()
                        : 0));
        colItemPrice.setCellValueFactory(
                cellData -> new SimpleDoubleProperty(cellData.getValue().getPriceAtTimeOfOrder() != null
                        ? cellData.getValue().getPriceAtTimeOfOrder()
                        : 0.0));
    }

    // ── Hog Section ──────────────────────────────────────────────────────

    private void setupHogSection() {
        hogTypeComboBox.setItems(FXCollections.observableArrayList(Hog.HogType.values()));
        hogTypeComboBox.setValue(Hog.HogType.WHOLE);
        hogTitledPane.setExpanded(false);
    }

    // ── Set existing order for editing ────────────────────────────────────

    public void setOrder(Order order) {
        this.editingOrder = order;
        if (order != null) {
            titleLabel.setText("Edit Order #" + order.getId());
            clientComboBox.setValue(order.getClient());
            paidCheckBox.setSelected(order.isPaid());
            shippedCheckBox.setSelected(order.isShipped());
            notesArea.setText(order.getNotes());

            // Load existing order items
            if (order.getOrderItems() != null) {
                orderItemsList.setAll(order.getOrderItems());
            }

            // Load existing hog data
            if (order.getHogs() != null && !order.getHogs().isEmpty()) {
                hogTitledPane.setExpanded(true);
                Hog hog = order.getHogs().get(0);
                hogNumberField.setText(hog.getHogNumber());
                hogTypeComboBox.setValue(hog.getHogType());
                processorField.setText(hog.getProcessor());
                liveWeightField.setText(hog.getLiveWeight() != null ? hog.getLiveWeight().toString() : "");
                hangingWeightField.setText(hog.getHangingWeight() != null ? hog.getHangingWeight().toString() : "");
                processingCostField.setText(hog.getProcessingCost() != null ? hog.getProcessingCost().toString() : "");
                inspectedCheckBox.setSelected(Boolean.TRUE.equals(hog.getInspected()));
            }
        }
    }

    public void setReadOnly(boolean readOnly) {
        clientComboBox.setDisable(readOnly);
        paidCheckBox.setDisable(readOnly);
        shippedCheckBox.setDisable(readOnly);
        notesArea.setEditable(!readOnly);

        hogNumberField.setEditable(!readOnly);
        hogTypeComboBox.setDisable(readOnly);
        processorField.setEditable(!readOnly);
        liveWeightField.setEditable(!readOnly);
        hangingWeightField.setEditable(!readOnly);
        processingCostField.setEditable(!readOnly);
        inspectedCheckBox.setDisable(readOnly);

        newClientButton.setVisible(!readOnly);
        newClientButton.setManaged(!readOnly);
        addItemButton.setVisible(!readOnly);
        addItemButton.setManaged(!readOnly);
        removeItemButton.setVisible(!readOnly);
        removeItemButton.setManaged(!readOnly);
        saveButton.setVisible(!readOnly);
        saveButton.setManaged(!readOnly);
    }

    // ── New Client ───────────────────────────────────────────────────────

    @FXML
    public void handleNewClientClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/client_form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("New Client");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh client list and select the newest client
            List<Client> updatedClients = clientService.getAllClients();
            allClients.setAll(updatedClients);
            if (!updatedClients.isEmpty()) {
                // Select last added (highest ID)
                Client newest = updatedClients.stream()
                        .reduce((a, b) -> a.getId() > b.getId() ? a : b)
                        .orElse(null);
                if (newest != null) {
                    clientComboBox.setValue(newest);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Add / Remove Items ───────────────────────────────────────────────

    @FXML
    public void handleAddItem() {
        List<InventoryItem> inventoryItems = inventoryService.getAllItems();
        if (inventoryItems.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No inventory items available. Add items in Inventory first.")
                    .showAndWait();
            return;
        }

        // Build a small dialog for selecting an item + quantity
        Dialog<OrderItem> dialog = new Dialog<>();
        dialog.setTitle("Add Item");
        dialog.setHeaderText("Select an inventory item and quantity");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<InventoryItem> itemCombo = new ComboBox<>(FXCollections.observableArrayList(inventoryItems));
        itemCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(InventoryItem item) {
                return item != null ? item.getName() + " ($" + String.format("%.2f", item.getPrice()) + ")" : "";
            }

            @Override
            public InventoryItem fromString(String string) {
                return null;
            }
        });
        itemCombo.setMaxWidth(Double.MAX_VALUE);

        Spinner<Integer> quantitySpinner = new Spinner<>(1, 9999, 1);
        quantitySpinner.setEditable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Item:"), 0, 0);
        grid.add(itemCombo, 1, 0);
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(quantitySpinner, 1, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK && itemCombo.getValue() != null) {
                OrderItem oi = new OrderItem();
                oi.setItem(itemCombo.getValue());
                oi.setQuantity(quantitySpinner.getValue());
                oi.setPriceAtTimeOfOrder(itemCombo.getValue().getPrice());
                return oi;
            }
            return null;
        });

        Optional<OrderItem> result = dialog.showAndWait();
        result.ifPresent(orderItemsList::add);
    }

    @FXML
    public void handleRemoveItem() {
        OrderItem selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            orderItemsList.remove(selected);
        } else {
            new Alert(Alert.AlertType.INFORMATION, "Please select an item to remove.").showAndWait();
        }
    }

    // ── Save ─────────────────────────────────────────────────────────────

    @FXML
    public void handleSave() {
        // Validate client
        Client selectedClient = clientComboBox.getValue();
        if (selectedClient == null) {
            // Try to resolve from editor text
            String text = clientComboBox.getEditor().getText();
            if (text != null && !text.isEmpty()) {
                selectedClient = allClients.stream()
                        .filter(c -> c.getName().equalsIgnoreCase(text))
                        .findFirst().orElse(null);
            }
            if (selectedClient == null) {
                new Alert(Alert.AlertType.WARNING, "Please select a client or create a new one.").showAndWait();
                return;
            }
        }

        try {
            Order order = (editingOrder != null) ? editingOrder : new Order();
            order.setClient(selectedClient);
            order.setPaid(paidCheckBox.isSelected());
            order.setShipped(shippedCheckBox.isSelected());
            order.evaluateStatus();
            order.setNotes(notesArea.getText());

            // ── Order items ──
            // Clear and re-add from observable list
            order.getOrderItems().clear();
            for (OrderItem oi : orderItemsList) {
                oi.setOrder(order);
                order.getOrderItems().add(oi);
            }

            // ── Auto-set order type ──
            boolean hasHogData = hogTitledPane.isExpanded() && isHogDataFilled();
            if (hasHogData) {
                order.setType(Order.OrderType.HOG);
                Hog hog;
                if (order.getHogs().isEmpty()) {
                    hog = new Hog();
                    hog.setOrder(order);
                    order.setHogs(new ArrayList<>());
                    order.getHogs().add(hog);
                } else {
                    hog = order.getHogs().get(0);
                }
                hog.setHogNumber(hogNumberField.getText());
                hog.setHogType(hogTypeComboBox.getValue());
                hog.setProcessor(processorField.getText());
                hog.setInspected(inspectedCheckBox.isSelected());

                String liveText = liveWeightField.getText();
                hog.setLiveWeight(liveText != null && !liveText.isEmpty() ? Double.parseDouble(liveText) : null);
                String hangText = hangingWeightField.getText();
                hog.setHangingWeight(hangText != null && !hangText.isEmpty() ? Double.parseDouble(hangText) : null);
                String costText = processingCostField.getText();
                hog.setProcessingCost(costText != null && !costText.isEmpty() ? Double.parseDouble(costText) : null);
            } else {
                order.setType(Order.OrderType.STANDARD);
                // Remove any orphaned hogs if user collapsed the section
                order.getHogs().clear();
            }

            orderService.saveOrder(order);
            handleInvoiceGeneration(order);
            close();
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Please enter valid numbers for weights and costs.").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error saving order: " + e.getMessage()).showAndWait();
        }
    }

    private boolean isHogDataFilled() {
        return (hogNumberField.getText() != null && !hogNumberField.getText().isEmpty())
                || (processorField.getText() != null && !processorField.getText().isEmpty())
                || (liveWeightField.getText() != null && !liveWeightField.getText().isEmpty())
                || (hangingWeightField.getText() != null && !hangingWeightField.getText().isEmpty())
                || (processingCostField.getText() != null && !processingCostField.getText().isEmpty());
    }

    @FXML
    public void handleCancel() {
        close();
    }

    private void handleInvoiceGeneration(Order order) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Generate Invoice");
        alert.setHeaderText("Order Saved Successfully");
        alert.setContentText("Would you like to generate and print a PDF invoice for this order?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                fileChooser.setTitle("Save Invoice PDF");
                fileChooser.setInitialFileName("Invoice_Order_" + order.getId() + ".pdf");
                fileChooser.getExtensionFilters().add(
                        new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

                File file = fileChooser.showSaveDialog(saveButton.getScene().getWindow());
                if (file != null) {
                    try {
                        invoiceService.generateAndOpenInvoice(order, file.getAbsolutePath());
                    } catch (Exception e) {
                        new Alert(Alert.AlertType.ERROR, "Error generating invoice: " + e.getMessage()).showAndWait();
                    }
                }
            }
        });
    }

    private void close() {
        ((Stage) clientComboBox.getScene().getWindow()).close();
    }
}

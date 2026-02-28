package com.PL.pig_ranch.controller;

import com.PL.pig_ranch.model.Client;
import com.PL.pig_ranch.model.Hog;
import com.PL.pig_ranch.model.Order;
import com.PL.pig_ranch.service.ClientService;
import com.PL.pig_ranch.service.OrderService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@Scope("prototype")
public class OrderDialogController {

    private final OrderService orderService;
    private final ClientService clientService;
    private Order editingOrder;

    @FXML
    private Label titleLabel;
    @FXML
    private ComboBox<Client> clientComboBox;
    @FXML
    private ComboBox<Order.OrderType> typeComboBox;
    @FXML
    private ComboBox<Order.OrderStatus> statusComboBox;
    @FXML
    private TextArea notesArea;

    @FXML
    private VBox standardOrderBox;
    @FXML
    private VBox hogOrderBox;

    // Hog Fields
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
    public OrderDialogController(OrderService orderService, ClientService clientService) {
        this.orderService = orderService;
        this.clientService = clientService;
    }

    @FXML
    public void initialize() {
        clientComboBox.setItems(FXCollections.observableArrayList(clientService.getAllClients()));
        clientComboBox.setConverter(new StringConverter<Client>() {
            @Override
            public String toString(Client client) {
                return client != null ? client.getName() : "";
            }

            @Override
            public Client fromString(String string) {
                return null;
            }
        });

        typeComboBox.setItems(FXCollections.observableArrayList(Order.OrderType.values()));
        typeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateTypeVisibility(newVal);
        });

        statusComboBox.setItems(FXCollections.observableArrayList(Order.OrderStatus.values()));
        hogTypeComboBox.setItems(FXCollections.observableArrayList(Hog.HogType.values()));

        typeComboBox.setValue(Order.OrderType.STANDARD);
        statusComboBox.setValue(Order.OrderStatus.OPEN);
        hogTypeComboBox.setValue(Hog.HogType.WHOLE);
    }

    private void updateTypeVisibility(Order.OrderType type) {
        if (type == Order.OrderType.HOG) {
            standardOrderBox.setVisible(false);
            standardOrderBox.setManaged(false);
            hogOrderBox.setVisible(true);
            hogOrderBox.setManaged(true);
        } else {
            standardOrderBox.setVisible(true);
            standardOrderBox.setManaged(true);
            hogOrderBox.setVisible(false);
            hogOrderBox.setManaged(false);
        }
    }

    public void setOrder(Order order) {
        this.editingOrder = order;
        if (order != null) {
            titleLabel.setText("Edit Order #" + order.getId());
            clientComboBox.setValue(order.getClient());
            typeComboBox.setValue(order.getType());
            statusComboBox.setValue(order.getStatus());
            notesArea.setText(order.getNotes());

            if (order.getType() == Order.OrderType.HOG && !order.getHogs().isEmpty()) {
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

    @FXML
    public void handleSave() {
        try {
            Order order = (editingOrder != null) ? editingOrder : new Order();
            order.setClient(clientComboBox.getValue());
            order.setType(typeComboBox.getValue());
            order.setStatus(statusComboBox.getValue());
            order.setNotes(notesArea.getText());

            if (order.getType() == Order.OrderType.HOG) {
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
            }

            orderService.saveOrder(order);
            close();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter valid numbers for weights and costs.");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error saving order: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void handleCancel() {
        close();
    }

    @FXML
    public void handleAddItem() {
        // Placeholder for adding item to standard order
    }

    @FXML
    public void handleRemoveItem() {
        // Placeholder for removing item from standard order
    }

    private void close() {
        ((Stage) clientComboBox.getScene().getWindow()).close();
    }
}

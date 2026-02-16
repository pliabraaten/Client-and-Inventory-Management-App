package com.PL.pig_ranch.controller;

import com.PL.pig_ranch.model.Client;
import com.PL.pig_ranch.model.Household;
import com.PL.pig_ranch.service.ClientService;
import com.PL.pig_ranch.service.HouseholdService;
import com.PL.pig_ranch.util.ClientUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClientFormController {

    private final ClientService clientService;
    private final HouseholdService householdService;
    private ObservableList<Household> allHouseholds;
    private FilteredList<Household> filteredHouseholds;
    private Client editingClient;

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField typeField;
    @FXML
    private ComboBox<Household> householdComboBox;

    @Autowired
    public ClientFormController(ClientService clientService, HouseholdService householdService) {
        this.clientService = clientService;
        this.householdService = householdService;
    }

    @FXML
    public void initialize() {
        allHouseholds = FXCollections.observableArrayList(householdService.getAllHouseholds());
        filteredHouseholds = new FilteredList<>(allHouseholds, p -> true);

        setupHouseholdComboBox();
        setupPhoneFieldFormatting();
    }

    public void setClient(Client client) {
        this.editingClient = client;
        if (client != null) {
            nameField.setText(client.getName());
            emailField.setText(client.getEmail());
            phoneField.setText(client.getPhoneNumber());
            typeField.setText(client.getNotes());
            householdComboBox.setValue(client.getHousehold());
        }
    }

    private void setupPhoneFieldFormatting() {
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                return;

            // Strip non-digits
            String digits = newValue.replaceAll("\\D", "");

            // Limit to 10 digits
            if (digits.length() > 10) {
                digits = digits.substring(0, 10);
            }

            // Build formatted string
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i == 3 || i == 6) {
                    formatted.append("-");
                }
                formatted.append(digits.charAt(i));
            }

            String finalFormatted = formatted.toString();

            // Only update if changed to avoid loops/cursor issues
            if (!newValue.equals(finalFormatted)) {
                phoneField.setText(finalFormatted);
                phoneField.positionCaret(finalFormatted.length()); // Move cursor to end
            }
        });
    }

    private void setupHouseholdComboBox() {
        householdComboBox.setEditable(true);
        householdComboBox.setItems(filteredHouseholds);

        // Define how Households are displayed in the list
        householdComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Household item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getSurname());
                }
            }
        });

        // String converter for editable state
        householdComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Household object) {
                return object == null ? "" : object.getSurname();
            }

            @Override
            public Household fromString(String string) {
                if (string == null || string.isEmpty())
                    return null;
                return allHouseholds.stream()
                        .filter(h -> h.getSurname().equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        // Filter logic as user types
        householdComboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            // Use runLater to avoid issues with modifying list during event
            javafx.application.Platform.runLater(() -> {
                if (householdComboBox.getSelectionModel().getSelectedItem() == null
                        || !householdComboBox.getEditor().getText()
                                .equals(householdComboBox.getSelectionModel().getSelectedItem().getSurname())) {
                    filteredHouseholds.setPredicate(household -> {
                        if (newVal == null || newVal.isEmpty()) {
                            return true;
                        }
                        return household.getSurname().toLowerCase().contains(newVal.toLowerCase());
                    });
                }
            });

            if (!householdComboBox.isShowing() && !newVal.isEmpty()) {
                householdComboBox.show();
            }
        });
    }

    @FXML
    public void handleSave() {
        String rawName = nameField.getText();
        // Basic Validation
        if (rawName == null || rawName.trim().isEmpty()) {
            // TODO: Show alert
            System.out.println("Name is required");
            return;
        }

        String formattedName = ClientUtils.formatName(rawName);
        String formattedPhone = ClientUtils.formatPhoneNumber(phoneField.getText());

        // DUPLICATE CHECK - Skip if it matches the client we are CURRENTLY editing
        if (clientService.isDuplicate(formattedName, formattedPhone)) {
            boolean isSelf = editingClient != null
                    && formattedName.equals(editingClient.getName())
                    && formattedPhone.equals(editingClient.getPhoneNumber());

            if (!isSelf) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Duplicate Entry");
                alert.setHeaderText("Client Already Exists");
                alert.setContentText(
                        "A client with the name '" + formattedName + "' and phone number '" + formattedPhone
                                + "' already exists.");
                alert.showAndWait();
                return;
            }
        }

        Client clientToSave = (editingClient != null) ? editingClient : new Client();
        clientToSave.setName(formattedName);
        clientToSave.setEmail(emailField.getText());
        clientToSave.setPhoneNumber(formattedPhone);
        clientToSave.setNotes(typeField.getText()); // Using notes as Type

        Household selectedHousehold = householdComboBox.getValue();
        if (selectedHousehold == null) {
            // Auto-create household from surname
            String surname = ClientUtils.extractSurname(formattedName);
            Household newHousehold = new Household();
            newHousehold.setSurname(ClientUtils.generateHouseholdName(surname));
            // Save household first to get ID
            selectedHousehold = householdService.saveHousehold(newHousehold);
        }
        clientToSave.setHousehold(selectedHousehold);

        clientService.saveClient(clientToSave);

        closeDialog();
    }

    @FXML
    public void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}

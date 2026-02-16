package com.PL.pig_ranch.controller;

import com.PL.pig_ranch.model.Client;
import com.PL.pig_ranch.model.Household;
import com.PL.pig_ranch.service.ClientService;
import com.PL.pig_ranch.service.HouseholdService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
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
        setupHouseholdComboBox();
        loadHouseholds();
    }

    private void setupHouseholdComboBox() {
        // Define how Households are displayed in the list and selected value
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

        // Define how the selected Household is converted to a String for the box
        householdComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Household object) {
                return object == null ? null : object.getSurname();
            }

            @Override
            public Household fromString(String string) {
                return householdComboBox.getItems().stream()
                        .filter(h -> h.getSurname().equals(string))
                        .findFirst().orElse(null);
            }
        });
    }

    private void loadHouseholds() {
        householdComboBox.setItems(FXCollections.observableArrayList(householdService.getAllHouseholds()));
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

        String formattedName = formatName(rawName);

        Client newClient = new Client();
        newClient.setName(formattedName);
        newClient.setEmail(emailField.getText());
        newClient.setPhoneNumber(phoneField.getText());
        newClient.setNotes(typeField.getText()); // Using notes as Type

        Household selectedHousehold = householdComboBox.getValue();
        if (selectedHousehold == null) {
            // Auto-create household from surname
            String surname = extractSurname(formattedName);
            Household newHousehold = new Household();
            newHousehold.setSurname("The " + surname + " Family");
            // Save household first to get ID
            selectedHousehold = householdService.saveHousehold(newHousehold);
        }
        newClient.setHousehold(selectedHousehold);

        clientService.saveClient(newClient);

        closeDialog();
    }

    private String formatName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder formatted = new StringBuilder();
        for (String part : parts) {
            if (part.length() > 0) {
                formatted.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return formatted.toString().trim();
    }

    private String extractSurname(String fullName) {
        String trimmed = fullName.trim();
        int lastSpace = trimmed.lastIndexOf(" ");
        if (lastSpace != -1) {
            return trimmed.substring(lastSpace + 1);
        }
        return trimmed;
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

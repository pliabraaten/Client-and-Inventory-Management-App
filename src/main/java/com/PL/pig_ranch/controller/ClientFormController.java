package com.PL.pig_ranch.controller;

import com.PL.pig_ranch.model.Client;
import com.PL.pig_ranch.model.Household;
import com.PL.pig_ranch.service.ClientService;
import com.PL.pig_ranch.service.HouseholdService;
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

        String formattedName = formatName(rawName);
        String formattedPhone = formatPhoneNumber(phoneField.getText());

        // DUPLICATE CHECK
        if (clientService.isDuplicate(formattedName, formattedPhone)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Duplicate Entry");
            alert.setHeaderText("Client Already Exists");
            alert.setContentText("A client with the name '" + formattedName + "' and phone number '" + formattedPhone
                    + "' already exists.");
            alert.showAndWait();
            return;
        }

        Client newClient = new Client();
        newClient.setName(formattedName);
        newClient.setEmail(emailField.getText());
        newClient.setPhoneNumber(formattedPhone);
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

    private String formatPhoneNumber(String rawPhone) {
        if (rawPhone == null) {
            return "";
        }
        // Strip everything except digits
        String digits = rawPhone.replaceAll("\\D", "");

        // If we have exactly 10 digits, format as XXX-XXX-XXXX
        if (digits.length() == 10) {
            return digits.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
        }
        // Otherwise return original (or bare digits) to avoid losing data
        return rawPhone;
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

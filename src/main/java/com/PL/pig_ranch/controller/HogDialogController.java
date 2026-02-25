package com.PL.pig_ranch.controller;

import com.PL.pig_ranch.model.Hog;
import com.PL.pig_ranch.service.HogService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class HogDialogController {

    private final HogService hogService;
    private Hog editingHog;

    @FXML
    private Label titleLabel;
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
    public HogDialogController(HogService hogService) {
        this.hogService = hogService;
    }

    @FXML
    public void initialize() {
        hogTypeComboBox.setItems(FXCollections.observableArrayList(Hog.HogType.values()));
        hogTypeComboBox.setValue(Hog.HogType.WHOLE);
    }

    public void setHog(Hog hog) {
        this.editingHog = hog;
        if (hog != null) {
            titleLabel.setText("Edit Hog #" + hog.getHogNumber());
            hogNumberField.setText(hog.getHogNumber());
            hogTypeComboBox.setValue(hog.getHogType());
            processorField.setText(hog.getProcessor());
            liveWeightField.setText(hog.getLiveWeight() != null ? hog.getLiveWeight().toString() : "");
            hangingWeightField.setText(hog.getHangingWeight() != null ? hog.getHangingWeight().toString() : "");
            processingCostField.setText(hog.getProcessingCost() != null ? hog.getProcessingCost().toString() : "");
            inspectedCheckBox.setSelected(Boolean.TRUE.equals(hog.getInspected()));
        }
    }

    @FXML
    public void handleSave() {
        try {
            Hog hog = (editingHog != null) ? editingHog : new Hog();
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

            hogService.saveHog(hog);
            close();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter valid numbers for weight and cost fields.");
            alert.showAndWait();
        }
    }

    @FXML
    public void handleCancel() {
        close();
    }

    private void close() {
        ((Stage) hogNumberField.getScene().getWindow()).close();
    }
}

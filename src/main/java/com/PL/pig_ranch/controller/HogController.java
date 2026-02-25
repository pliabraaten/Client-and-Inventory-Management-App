package com.PL.pig_ranch.controller;

import com.PL.pig_ranch.model.Hog;
import com.PL.pig_ranch.service.HogService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HogController {

    private final HogService hogService;
    private final ApplicationEventPublisher eventPublisher;
    private final ApplicationContext context;

    private ObservableList<Hog> allHogs;
    private FilteredList<Hog> filteredHogs;

    @FXML
    private TableView<Hog> hogTable;
    @FXML
    private TableColumn<Hog, String> colHogNumber;
    @FXML
    private TableColumn<Hog, String> colHogType;
    @FXML
    private TableColumn<Hog, String> colInspected;
    @FXML
    private TableColumn<Hog, String> colProcessor;
    @FXML
    private TableColumn<Hog, Double> colLiveWeight;
    @FXML
    private TableColumn<Hog, Double> colHangingWeight;
    @FXML
    private TableColumn<Hog, String> colPercentHanging;
    @FXML
    private TableColumn<Hog, Double> colProcessingCost;
    @FXML
    private TableColumn<Hog, Void> colActions;
    @FXML
    private TextField searchField;

    @Autowired
    public HogController(HogService hogService, ApplicationEventPublisher eventPublisher, ApplicationContext context) {
        this.hogService = hogService;
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
        colHogNumber.setCellValueFactory(new PropertyValueFactory<>("hogNumber"));
        colHogType.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getHogType() != null ? cellData.getValue().getHogType().name() : ""));
        colInspected.setCellValueFactory(cellData -> new SimpleStringProperty(
                Boolean.TRUE.equals(cellData.getValue().getInspected()) ? "Yes" : "No"));
        colProcessor.setCellValueFactory(new PropertyValueFactory<>("processor"));
        colLiveWeight.setCellValueFactory(new PropertyValueFactory<>("liveWeight"));
        colHangingWeight.setCellValueFactory(new PropertyValueFactory<>("hangingWeight"));
        colPercentHanging.setCellValueFactory(cellData -> {
            Double pct = cellData.getValue().getPercentHanging();
            return new SimpleStringProperty(pct != null ? String.format("%.1f%%", pct) : "—");
        });
        colProcessingCost.setCellValueFactory(new PropertyValueFactory<>("processingCost"));

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #4444ff; -fx-text-fill: white;");
                editBtn.setOnAction(event -> {
                    Hog hog = getTableView().getItems().get(getIndex());
                    handleEdit(hog);
                });

                deleteBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    Hog hog = getTableView().getItems().get(getIndex());
                    handleDelete(hog);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void setupSearch() {
        allHogs = FXCollections.observableArrayList();
        filteredHogs = new FilteredList<>(allHogs, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredHogs.setPredicate(hog -> {
                if (newVal == null || newVal.isEmpty())
                    return true;
                String lower = newVal.toLowerCase();
                return (hog.getHogNumber() != null && hog.getHogNumber().toLowerCase().contains(lower)) ||
                        (hog.getProcessor() != null && hog.getProcessor().toLowerCase().contains(lower)) ||
                        (hog.getHogType() != null && hog.getHogType().name().toLowerCase().contains(lower));
            });
        });

        hogTable.setItems(filteredHogs);
    }

    private void loadData() {
        allHogs.setAll(hogService.getAllHogs());
    }

    private void handleEdit(Hog hog) {
        showDialog(hog);
        loadData();
    }

    private void handleDelete(Hog hog) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Hog #" + hog.getHogNumber());
        alert.setContentText("Are you sure? This cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                hogService.deleteHog(hog.getId());
                loadData();
            }
        });
    }

    private void showDialog(Hog hog) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hog_dialog.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            HogDialogController controller = loader.getController();
            controller.setHog(hog);

            Stage stage = new Stage();
            stage.setTitle(hog == null ? "Add Hog" : "Edit Hog");
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
    public void handleAddHogClick() {
        showDialog(null);
        loadData();
    }
}

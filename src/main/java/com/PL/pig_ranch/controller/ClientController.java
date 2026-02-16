package com.PL.pig_ranch.controller;

import com.PL.pig_ranch.model.Client;
import com.PL.pig_ranch.service.ClientService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import java.io.IOException;
import java.util.List;

@Component
public class ClientController {

    private final ClientService clientService;
    private final ApplicationEventPublisher eventPublisher;
    private final ApplicationContext context;
    private ObservableList<Client> allClients;
    private FilteredList<Client> filteredClients;

    @FXML
    private TableView<Client> clientTable;
    @FXML
    private TableColumn<Client, Long> colClientId;
    @FXML
    private TableColumn<Client, String> colClientName;
    @FXML
    private TableColumn<Client, String> colClientEmail;
    @FXML
    private TableColumn<Client, String> colClientPhone;
    @FXML
    private TableColumn<Client, String> colClientType;
    @FXML
    private TableColumn<Client, String> colClientHousehold;
    @FXML
    private TextField searchField;

    @Autowired
    public ClientController(ClientService clientService, ApplicationEventPublisher eventPublisher,
            ApplicationContext context) {
        this.clientService = clientService;
        this.eventPublisher = eventPublisher;
        this.context = context;
    }

    @FXML
    public void initialize() {
        setupClientTable();
        setupSearchFilter();
        loadClientData();
    }

    private void setupSearchFilter() {
        allClients = FXCollections.observableArrayList();
        filteredClients = new FilteredList<>(allClients, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredClients.setPredicate(client -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();

                if (client.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (client.getEmail() != null && client.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (client.getPhoneNumber() != null && client.getPhoneNumber().contains(newValue)) {
                    return true;
                } else if (client.getHousehold() != null && client.getHousehold().getSurname() != null
                        && client.getHousehold().getSurname().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        clientTable.setItems(filteredClients);
    }

    private void setupClientTable() {
        colClientId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClientName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colClientEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colClientPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colClientType.setCellValueFactory(new PropertyValueFactory<>("notes"));

        colClientHousehold.setCellValueFactory(cellData -> {
            if (cellData.getValue().getHousehold() != null) {
                return new SimpleStringProperty(cellData.getValue().getHousehold().getSurname());
            }
            return new SimpleStringProperty("N/A");
        });
    }

    private void loadClientData() {
        List<Client> clients = clientService.getAllClients();
        allClients.setAll(clients);
    }

    @FXML
    public void handleBackClick() {
        eventPublisher.publishEvent(new NavigationEvent(this, "HOME"));
    }

    @FXML
    public void handleNewClientClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/client_form.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Client");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh table after dialog closes
            loadClientData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

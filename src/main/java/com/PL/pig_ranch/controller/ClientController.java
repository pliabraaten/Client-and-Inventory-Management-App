package com.PL.pig_ranch.controller;

import com.PL.pig_ranch.model.Client;
import com.PL.pig_ranch.service.ClientService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientController {

    private final ClientService clientService;
    private final ApplicationEventPublisher eventPublisher;

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
    public ClientController(ClientService clientService, ApplicationEventPublisher eventPublisher) {
        this.clientService = clientService;
        this.eventPublisher = eventPublisher;
    }

    @FXML
    public void initialize() {
        setupClientTable();
        loadClientData();
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
        ObservableList<Client> observableClients = FXCollections.observableArrayList(clients);
        clientTable.setItems(observableClients);
    }

    @FXML
    public void handleBackClick() {
        eventPublisher.publishEvent(new NavigationEvent(this, "HOME"));
    }

    @FXML
    public void handleNewClientClick() {
        System.out.println("New Client clicked");
        // TODO: Implement New Client Dialog
    }
}

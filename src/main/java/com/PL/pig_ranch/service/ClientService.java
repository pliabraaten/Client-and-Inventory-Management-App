package com.PL.pig_ranch.service;

import com.PL.pig_ranch.model.Client;
import java.util.List;
import java.util.Optional;

public interface ClientService {

    Client saveClient(Client client);

    Optional<Client> getClientById(Long id);

    List<Client> getAllClients();

    void deleteClient(Long id);

    boolean isDuplicate(String name, String phoneNumber);
}

package com.PL.pig_ranch.service.impl;

import com.PL.pig_ranch.model.Client;
import com.PL.pig_ranch.repository.ClientRepository;
import com.PL.pig_ranch.service.ClientService;
import com.PL.pig_ranch.service.HouseholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final HouseholdService householdService;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, HouseholdService householdService) {
        this.clientRepository = clientRepository;
        this.householdService = householdService;
    }

    @Override
    @Transactional
    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }

    @Override
    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    @Override
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteClient(Long id) {
        Optional<Client> clientOpt = clientRepository.findById(id);
        if (clientOpt.isPresent()) {
            Long householdId = clientOpt.get().getHousehold() != null ? clientOpt.get().getHousehold().getId() : null;
            clientRepository.deleteById(id);
            if (householdId != null) {
                householdService.deleteHouseholdIfEmpty(householdId);
            }
        }
    }

    @Override
    public boolean isDuplicate(String name, String phoneNumber) {
        return clientRepository.existsByNameIgnoreCaseAndPhoneNumber(name, phoneNumber);
    }
}

package com.PL.pig_ranch.service;

import com.PL.pig_ranch.model.Client;
import com.PL.pig_ranch.model.Household;
import com.PL.pig_ranch.repository.ClientRepository;
import com.PL.pig_ranch.repository.HouseholdRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ClientServiceIntegrationTest {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Test
    void testIsDuplicate() {
        // Arrange
        Client client = new Client();
        client.setName("John Doe");
        client.setPhoneNumber("123-456-7890");
        clientRepository.save(client);

        // Act & Assert
        assertTrue(clientService.isDuplicate("John Doe", "123-456-7890"), "Should recognize exact duplicate");
        assertFalse(clientService.isDuplicate("Jane Doe", "123-456-7890"), "Different name should not be duplicate");
    }

    @Test
    void testSaveClientWithNewHousehold() {
        Household h = new Household();
        h.setSurname("The Skywalker Family");
        householdRepository.save(h);

        Client c = new Client();
        c.setName("Anakin Skywalker");
        c.setHousehold(h);

        Client saved = clientService.saveClient(c);
        assertNotNull(saved.getId());
        assertEquals("The Skywalker Family", saved.getHousehold().getSurname());
    }

    @Test
    void testGetAllClients() {
        clientService.saveClient(new Client(null, "User 1", "u1@test.com", "111", "Notes", null));
        clientService.saveClient(new Client(null, "User 2", "u2@test.com", "222", "Notes", null));

        List<Client> all = clientService.getAllClients();
        assertTrue(all.size() >= 2);
    }
}

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

    @Test
    void testDeleteClientFullFlow() {
        Client c = new Client(null, "Temp User", "temp@test.com", "999", "Notes", null);
        Client saved = clientService.saveClient(c);
        Long id = saved.getId();

        clientService.deleteClient(id);

        assertTrue(clientService.getClientById(id).isEmpty());
    }

    @Test
    void testUpdateClientPersistsChanges() {
        // Arrange
        Client c = new Client(null, "Original Name", "original@test.com", "111", "Notes", null);
        Client saved = clientService.saveClient(c);
        Long id = saved.getId();

        // Act
        saved.setName("Updated Name");
        clientService.saveClient(saved);

        // Assert
        Client found = clientService.getClientById(id).orElseThrow();
        assertEquals("Updated Name", found.getName());
        assertEquals(id, found.getId());

        // Ensure no extra records were created
        assertTrue(clientService.getAllClients().stream().anyMatch(client -> client.getName().equals("Updated Name")));
        assertFalse(
                clientService.getAllClients().stream().anyMatch(client -> client.getName().equals("Original Name")));
    }

    @Test
    void testDuplicateCheckAfterEdit() {
        // Arrange
        Client clientA = clientService
                .saveClient(new Client(null, "User Alpha", "a@test.com", "111-222-3333", "TypeA", null));
        Client clientB = clientService
                .saveClient(new Client(null, "User Beta", "b@test.com", "444-555-6666", "TypeB", null));

        assertNotNull(clientA.getId());
        assertNotNull(clientB.getId());

        // Act & Assert
        // Client B is NOT a duplicate of Client A initially
        assertFalse(clientService.isDuplicate("User Beta", "111-222-3333"), "Phone mismatch");

        // If we try to change Client B's details to match Client A's, it SHOULD be
        // recognized as a duplicate
        assertTrue(clientService.isDuplicate("User Alpha", "111-222-3333"),
                "Should recognize Client A's details as existing");
    }

    @Test
    void testDuplicateCheckCaseInsensitivity() {
        // Arrange
        clientService.saveClient(new Client(null, "John Doe", "john@test.com", "555-0000", "Notes", null));

        // Act & Assert
        assertTrue(clientService.isDuplicate("JOHN DOE", "555-0000"), "Should be case-insensitive on name");
        assertTrue(clientService.isDuplicate("john doe", "555-0000"), "Should be case-insensitive on name");
    }

    @Autowired
    private HouseholdService householdService;

    @Test
    void testDeleteClientCleansUpOrphanHousehold() {
        // Arrange
        Household h = householdService
                .saveHousehold(new Household(null, "Orphan Surname", "Addr", "City", "ST", "123", null, null));
        Client c = clientService.saveClient(new Client(null, "To Be Deleted", "email", "phone", "notes", h));
        Long hhId = h.getId();
        Long cId = c.getId();

        // Act
        clientService.deleteClient(cId);

        // Assert
        assertTrue(clientService.getClientById(cId).isEmpty());
        assertTrue(householdService.getHouseholdById(hhId).isEmpty(), "Household should have been deleted");
    }
}

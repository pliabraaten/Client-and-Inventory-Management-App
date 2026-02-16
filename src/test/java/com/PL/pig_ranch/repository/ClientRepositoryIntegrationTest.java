package com.PL.pig_ranch.repository;

import com.PL.pig_ranch.model.Client;
import com.PL.pig_ranch.model.Household;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ClientRepositoryIntegrationTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Test
    void testExistsByNameAndPhoneNumber() {
        Client c = new Client();
        c.setName("Unique User");
        c.setPhoneNumber("000-000-0000");
        clientRepository.save(c);

        assertTrue(clientRepository.existsByNameAndPhoneNumber("Unique User", "000-000-0000"));
        assertFalse(clientRepository.existsByNameAndPhoneNumber("Someone Else", "000-000-0000"));
    }

    @Test
    void testClientHouseholdRelationship() {
        Household h = new Household();
        h.setSurname("Test Family");
        householdRepository.save(h);

        Client c = new Client();
        c.setName("Family Member");
        c.setHousehold(h);
        clientRepository.save(c);

        Client found = clientRepository.findById(c.getId()).orElse(null);
        assertNotNull(found);
        assertNotNull(found.getHousehold());
        assertEquals("Test Family", found.getHousehold().getSurname());
    }

    @Test
    void testDeleteClient() {
        Client c = new Client();
        c.setName("Delete Me");
        clientRepository.save(c);
        Long id = c.getId();

        clientRepository.deleteById(id);

        assertFalse(clientRepository.existsById(id));
    }
}

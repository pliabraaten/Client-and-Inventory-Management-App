package com.PL.pig_ranch.service;

import com.PL.pig_ranch.model.Client;
import com.PL.pig_ranch.repository.ClientRepository;
import com.PL.pig_ranch.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIsDuplicate() {
        String name = "John Doe";
        String phone = "123-456-7890";

        when(clientRepository.existsByNameIgnoreCaseAndPhoneNumber(name, phone)).thenReturn(true);
        assertTrue(clientService.isDuplicate(name, phone));

        when(clientRepository.existsByNameIgnoreCaseAndPhoneNumber(name, phone)).thenReturn(false);
        assertFalse(clientService.isDuplicate(name, phone));

        verify(clientRepository, times(2)).existsByNameIgnoreCaseAndPhoneNumber(name, phone);
    }

    @Test
    void testGetClientById() {
        Client client = new Client();
        client.setId(1L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        Optional<Client> result = clientService.getClientById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void testGetClientByIdNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<Client> result = clientService.getClientById(99L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testDeleteClientNonExistent() {
        // Many JPA repositories do nothing or throw if you deleteById(null)
        // but often deleteById(nonExistentId) is just a no-op at the DB level.
        // We ensure our service doesn't crash.
        doNothing().when(clientRepository).deleteById(99L);
        assertDoesNotThrow(() -> clientService.deleteClient(99L));
        verify(clientRepository, times(1)).deleteById(99L);
    }
}

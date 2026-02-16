package com.PL.pig_ranch.util;

import com.PL.pig_ranch.model.Client;
import com.PL.pig_ranch.model.Household;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientUtilsTest {

    @Test
    void testFormatName() {
        assertEquals("John Doe", ClientUtils.formatName("  john doe  "));
        assertEquals("John Doe", ClientUtils.formatName("JOHN DOE"));
        assertEquals("Mary-Jane Watson", ClientUtils.formatName("mary-jane watson"));
        assertEquals("John Smith", ClientUtils.formatName("john smith  "));
        assertEquals("", ClientUtils.formatName(""));
        assertNull(ClientUtils.formatName(null));
    }

    @Test
    void testFormatPhoneNumber() {
        assertEquals("123-456-7890", ClientUtils.formatPhoneNumber("1234567890"));
        assertEquals("123-456-7890", ClientUtils.formatPhoneNumber("(123) 456-7890"));
        assertEquals("123-456-7890", ClientUtils.formatPhoneNumber("123.456.7890"));
        assertEquals("12345", ClientUtils.formatPhoneNumber("12345")); // Preservation if not 10
        assertEquals("123456789012", ClientUtils.formatPhoneNumber("123456789012")); // Too many digits - preservation
        assertEquals("", ClientUtils.formatPhoneNumber(null));
    }

    @Test
    void testExtractSurname() {
        assertEquals("Doe", ClientUtils.extractSurname("John Doe"));
        assertEquals("Watson", ClientUtils.extractSurname("Mary Jane Watson")); // Multi-part first name
        assertEquals("Schmidt", ClientUtils.extractSurname("John Jacob Jingleheimer Schmidt"));
        assertEquals("Smith", ClientUtils.extractSurname("  Smith  "));
        assertEquals("", ClientUtils.extractSurname(null));
        assertEquals("", ClientUtils.extractSurname(""));
    }

    @Test
    void testGenerateHouseholdName() {
        assertEquals("The Smith Family", ClientUtils.generateHouseholdName("Smith"));
        assertEquals("The Van Buren Family", ClientUtils.generateHouseholdName("Van Buren"));
        assertEquals("New Family", ClientUtils.generateHouseholdName(null));
        assertEquals("New Family", ClientUtils.generateHouseholdName("  "));
    }

    @Test
    void testIsSearchMatch() {
        Household h = new Household();
        h.setSurname("Skywalker");
        Client c = new Client(1L, "Luke Skywalker", "luke@rebel.org", "555-1234", "Jedi", h);

        assertTrue(ClientUtils.isSearchMatch(c, "luke"));
        assertTrue(ClientUtils.isSearchMatch(c, "SKYWALKER"));
        assertTrue(ClientUtils.isSearchMatch(c, "rebel"));
        assertTrue(ClientUtils.isSearchMatch(c, "555"));
        assertTrue(ClientUtils.isSearchMatch(c, "1234"));

        // Negative test
        assertFalse(ClientUtils.isSearchMatch(c, "vader"));

        // Null/Empty search
        assertTrue(ClientUtils.isSearchMatch(c, null));
        assertTrue(ClientUtils.isSearchMatch(c, ""));
    }
}

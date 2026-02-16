package com.PL.pig_ranch.util;

import com.PL.pig_ranch.model.Client;

public class ClientUtils {

    /**
     * Generates a standard household surname.
     */
    public static String generateHouseholdName(String surname) {
        if (surname == null || surname.trim().isEmpty()) {
            return "New Family";
        }
        return "The " + surname.trim() + " Family";
    }

    /**
     * Centralized search matching logic.
     */
    public static boolean isSearchMatch(Client client, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return true;
        }
        String lowerTerm = searchTerm.toLowerCase().trim();

        if (client.getName() != null && client.getName().toLowerCase().contains(lowerTerm)) {
            return true;
        }
        if (client.getEmail() != null && client.getEmail().toLowerCase().contains(lowerTerm)) {
            return true;
        }
        if (client.getPhoneNumber() != null && client.getPhoneNumber().contains(searchTerm)) {
            return true;
        }
        if (client.getHousehold() != null && client.getHousehold().getSurname() != null
                && client.getHousehold().getSurname().toLowerCase().contains(lowerTerm)) {
            return true;
        }
        return false;
    }

    /**
     * Trims whitespace and title-cases the name.
     * Example: " john doe " -> "John Doe"
     */
    public static String formatName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        String[] spaceParts = name.trim().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String spacePart : spaceParts) {
            if (spacePart.isEmpty())
                continue;

            String[] hyphenParts = spacePart.split("-");
            StringBuilder hyphenated = new StringBuilder();
            for (int i = 0; i < hyphenParts.length; i++) {
                String subPart = hyphenParts[i];
                if (!subPart.isEmpty()) {
                    hyphenated.append(Character.toUpperCase(subPart.charAt(0)))
                            .append(subPart.substring(1).toLowerCase());
                }
                if (i < hyphenParts.length - 1) {
                    hyphenated.append("-");
                }
            }
            result.append(hyphenated).append(" ");
        }
        return result.toString().trim();
    }

    /**
     * Formats a 10-digit phone number as XXX-XXX-XXXX.
     */
    public static String formatPhoneNumber(String rawPhone) {
        if (rawPhone == null) {
            return "";
        }
        // Strip everything except digits
        String digits = rawPhone.replaceAll("\\D", "");

        // If we have exactly 10 digits, format as XXX-XXX-XXXX
        if (digits.length() == 10) {
            return digits.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
        }
        return rawPhone;
    }

    /**
     * Extracts the last name from a full name string.
     */
    public static String extractSurname(String fullName) {
        if (fullName == null)
            return "";
        String trimmed = fullName.trim();
        int lastSpace = trimmed.lastIndexOf(" ");
        if (lastSpace != -1) {
            return trimmed.substring(lastSpace + 1);
        }
        return trimmed;
    }
}

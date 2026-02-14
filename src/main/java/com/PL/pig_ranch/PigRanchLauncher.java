package com.PL.pig_ranch;

import javafx.application.Application;

/**
 * Entry point for the application.
 * <p>
 * JavaFX requires the main() method to reside outside the Application subclass
 * when running without the module system. This class simply delegates to
 * {@link PigRanchApplication}.
 */
public class PigRanchLauncher {

    public static void main(String[] args) {
        Application.launch(PigRanchApplication.class, args);
    }
}

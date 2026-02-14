package com.PL.pig_ranch;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

/**
 * Custom Spring event published when the JavaFX primary Stage is ready.
 * Other Spring beans can listen for this to interact with the Stage.
 */
public class StageReadyEvent extends ApplicationEvent {

    public StageReadyEvent(Stage stage) {
        super(stage);
    }

    public Stage getStage() {
        return (Stage) getSource();
    }
}

package com.PL.pig_ranch;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Main application class — extends JavaFX Application and boots the Spring
 * context.
 * <p>
 * Lifecycle:
 * <ol>
 * <li>{@link #init()} — starts the Spring ApplicationContext</li>
 * <li>{@link #start(Stage)} — loads the FXML UI and shows the window</li>
 * <li>{@link #stop()} — closes the Spring context on exit</li>
 * </ol>
 */

// Extends JavaFX Application and boots the Spring context
@SpringBootApplication
public class PigRanchApplication extends Application {

	private ConfigurableApplicationContext springContext;

	// Starts the Spring ApplicationContext
	@Override
	public void init() {
		springContext = new SpringApplicationBuilder(PigRanchApplication.class)
				.headless(false)
				.run(getParameters().getRaw().toArray(new String[0]));
	}

	// Loads the FXML UI and shows the window
	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));

		// Wire FXML controllers through Spring so they can use @Autowired
		loader.setControllerFactory(springContext::getBean);

		Parent root = loader.load();
		Scene scene = new Scene(root, 800, 600);

		primaryStage.setTitle("Pig Ranch");
		primaryStage.setScene(scene);
		primaryStage.show();

		// Publish event so other Spring beans know the Stage is ready
		springContext.publishEvent(new StageReadyEvent(primaryStage));
	}

	// Closes the Spring context on exit
	@Override
	public void stop() {
		if (springContext != null) {
			springContext.close();
		}
		Platform.exit();
	}
}

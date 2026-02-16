package com.PL.pig_ranch;

import com.PL.pig_ranch.model.Client;
import com.PL.pig_ranch.model.Household;
import com.PL.pig_ranch.repository.ClientRepository;
import com.PL.pig_ranch.repository.HouseholdRepository;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import java.util.Arrays;

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

	// SEED DB
	@Bean
	public CommandLineRunner demoData(HouseholdRepository householdRepository,
			ClientRepository clientRepository) {
		return args -> {
			if (householdRepository.count() == 0) {
				// Seed Households
				Household h1 = new Household(null, "The Smith Family", "123 Maple Dr", "Springfield", "IL", "62704",
						null,
						null);
				Household h2 = new Household(null, "The Doe Family", "456 Oak Ln", "Springfield", "IL", "62704", null,
						null);
				householdRepository.saveAll(Arrays.asList(h1, h2));

				// Seed Clients
				Client c1 = new Client(null, "John Smith", "john@smith.com", "555-0101", "Father", h1);
				Client c2 = new Client(null, "Jane Smith", "jane@smith.com", "555-0102", "Mother", h1);
				Client c3 = new Client(null, "Alice Doe", "alice@doe.com", "555-0201", "Individual", h2);
				clientRepository.saveAll(Arrays.asList(c1, c2, c3));

				System.out.println("--- Data Seeding Completed ---");
			} else {
				System.out.println("--- Database already seeded, skipping ---");
			}
		};
	}
}

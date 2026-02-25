package com.PL.pig_ranch;

import com.PL.pig_ranch.model.Client;
import com.PL.pig_ranch.model.Household;
import com.PL.pig_ranch.model.Hog;
import com.PL.pig_ranch.model.InventoryItem;
import com.PL.pig_ranch.model.Order;
import com.PL.pig_ranch.model.OrderItem;
import com.PL.pig_ranch.repository.ClientRepository;
import com.PL.pig_ranch.repository.HogRepository;
import com.PL.pig_ranch.repository.HouseholdRepository;
import com.PL.pig_ranch.repository.InventoryRepository;
import com.PL.pig_ranch.repository.OrderRepository;
import com.PL.pig_ranch.repository.OrderItemRepository;
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
import java.util.List;

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
	public void start(Stage primaryStage) {
		try {
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
		} catch (Exception e) {
			System.err.println("CRITICAL: Failed to load application UI");
			e.printStackTrace();
			Platform.exit();
		}
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
			ClientRepository clientRepository,
			InventoryRepository inventoryRepository,
			HogRepository hogRepository,
			OrderRepository orderRepository,
			OrderItemRepository orderItemRepository) {
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

				// Seed Inventory (By the Piece)
				List<String> itemNames = Arrays.asList(
						"Bacon", "Bacon Ends", "Chops Bone in", "Chops boneless", "Brats",
						"Breakfast links", "Whole Ham", "Half Ham", "Ham Steaks", "Ham Hocks",
						"Ham Rst Fresh", "Loin Whole", "Pork Burgers", "Ribs", "Baby Back R",
						"Sausage Mild", "Sausage Med", "Sausage Hot", "Ground Pork",
						"Shoulder Roast", "Shoulder Steak", "Boston Butt");

				for (String name : itemNames) {
					inventoryRepository.save(new InventoryItem(null, name, "Meat", "Pork product", 0, 5.0));
				}

				// Seed a sample order
				Order o1 = new Order();
				o1.setClient(c1);
				o1.setNotes("Initial sample order");
				orderRepository.save(o1);

				// Seed Hogs associated with the order
				Hog hog1 = new Hog(null, "H001", Hog.HogType.WHOLE, true, "Valley Meats", 280.0, 196.0, 125.0, o1);
				Hog hog2 = new Hog(null, "H002", Hog.HogType.HALF, false, "Valley Meats", 300.0, null, null, o1);
				hogRepository.saveAll(Arrays.asList(hog1, hog2));

				System.out.println("--- Data Seeding Completed ---");
			} else {
				System.out.println("--- Database already seeded, skipping ---");
			}
		};
	}
}

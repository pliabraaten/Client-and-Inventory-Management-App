package com.PL.pig_ranch;

import com.PL.pig_ranch.model.Client;
import com.PL.pig_ranch.model.Household;
import com.PL.pig_ranch.model.Hog;
import com.PL.pig_ranch.model.InventoryItem;
import com.PL.pig_ranch.model.Order;
import com.PL.pig_ranch.model.OrderItem;
import com.PL.pig_ranch.model.InventoryTransaction;
import com.PL.pig_ranch.repository.ClientRepository;
import com.PL.pig_ranch.repository.HogRepository;
import com.PL.pig_ranch.repository.HouseholdRepository;
import com.PL.pig_ranch.repository.InventoryRepository;
import com.PL.pig_ranch.repository.OrderRepository;
import com.PL.pig_ranch.repository.OrderItemRepository;
import com.PL.pig_ranch.repository.TransactionRepository;
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
import java.math.BigDecimal;
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
			scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());

			primaryStage.setTitle("Pig Ranch Manager");
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

	@Bean
	public CommandLineRunner demoData(HouseholdRepository householdRepository,
			ClientRepository clientRepository,
			InventoryRepository inventoryRepository,
			HogRepository hogRepository,
			OrderRepository orderRepository,
			OrderItemRepository orderItemRepository,
			TransactionRepository transactionRepository) {
		return args -> {
			// 1. Seed Inventory (Independent of households)
			List<String> itemNames = Arrays.asList(
					"Bacon", "Bacon Ends", "Chops Bone in", "Chops boneless", "Brats",
					"Breakfast links", "Whole Ham", "Half Ham", "Ham Steaks", "Ham Hocks",
					"Ham Rst Fresh", "Loin Whole", "Pork Burgers", "Ribs", "Baby Back R",
					"Sausage Mild", "Sausage Med", "Sausage Hot", "Ground Pork",
					"Shoulder Roast", "Shoulder Steak", "Boston Butt");

			for (String name : itemNames) {
				if (!inventoryRepository.existsByName(name)) {
					inventoryRepository
							.save(new InventoryItem(null, name, "Meat", "Pork product", 0, new BigDecimal("5.00")));
					System.out.println("Seeded missing inventory item: " + name);
				}
			}

			// 2. Seed Relationship Data (Only if database is totally empty of households)
			if (householdRepository.count() == 0) {
				// Seed Households
				Household h1 = new Household(null, "The Smith Family", "123 Maple Dr", "Springfield", "IL", "62704",
						null,
						null);
				Household h2 = new Household(null, "The Doe Family", "456 Oak Ln", "Springfield", "IL", "62704", null,
						null);
				householdRepository.saveAll(Arrays.asList(h1, h2));

				// Seed Clients
				Client c1 = new Client(null, "John Smith", "john@smith.com", "555-0101", h1);
				Client c2 = new Client(null, "Jane Smith", "jane@smith.com", "555-0102", h1);
				Client c3 = new Client(null, "Alice Doe", "alice@doe.com", "555-0201", h2);
				clientRepository.saveAll(Arrays.asList(c1, c2, c3));

				// Seed an OPEN order
				Order o1 = new Order();
				o1.setClient(c1);
				o1.setPaid(false);
				o1.setShipped(false);
				o1.evaluateStatus(); // will be OPEN
				o1.setNotes("Initial OPEN sample order");
				orderRepository.save(o1);

				// Seed a PENDING order (Paid but not shipped)
				Order o2 = new Order();
				o2.setClient(c2);
				o2.setPaid(true);
				o2.setShipped(false);
				o2.evaluateStatus(); // will be PENDING
				o2.setNotes("Initial PENDING sample order");
				orderRepository.save(o2);

				// Seed a FULFILLED order (paid and shipped)
				Order o3 = new Order();
				o3.setClient(c3);
				o3.setPaid(true);
				o3.setShipped(true);
				o3.evaluateStatus(); // will be FULFILLED
				o3.setNotes("Initial FULFILLED sample order");
				orderRepository.save(o3);

				// Seed Hogs associated with the OPEN order (o1)
				Hog hog1 = new Hog(null, "H001", Hog.HogType.WHOLE, true, "Valley Meats", new BigDecimal("280.0"),
						new BigDecimal("196.0"), new BigDecimal("125.0"), o1);
				Hog hog2 = new Hog(null, "H002", Hog.HogType.HALF, false, "Valley Meats", new BigDecimal("300.0"), null,
						null, o1);
				hogRepository.saveAll(Arrays.asList(hog1, hog2));

				// Fetch an inventory item to use
				InventoryItem sampleItem = inventoryRepository.findAll().stream().findFirst().orElse(null);
				if (sampleItem != null) {
					// Seed Order Item on PENDING order (o2)
					OrderItem orderItem = new OrderItem(null, o2, sampleItem, 2, sampleItem.getPrice(),
							BigDecimal.ZERO);
					orderItemRepository.save(orderItem);

					// Seed Inventory Transaction
					InventoryTransaction transaction = new InventoryTransaction(null, sampleItem,
							InventoryTransaction.TransactionType.ARRIVAL, 10, null, "Initial stock received");
					transactionRepository.save(transaction);
				}

				System.out.println("--- Core Data Seeding Completed ---");
			} else {
				System.out.println("--- Household data already exists, skipping relationship seeding ---");
			}
		};
	}
}

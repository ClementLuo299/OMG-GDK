import gdk.Logging;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Handles startup of the GDK application
 *
 * @authors Clement Luo
 * @date July 25, 2025
 * @edited July 25, 2025
 * @since 1.0
 */
public class Startup {

    // ==================== DEPENDENCIES ====================
    
    private final Stage primaryStage;
    private final ModuleLoader moduleLoader;
    private GDKGameLobbyController controller; // Store controller reference

    // ==================== CONSTRUCTOR ====================
    
    public Startup(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.moduleLoader = new ModuleLoader();
    }

    // ==================== PUBLIC STARTUP METHODS ====================
    
    /**
     * Starts the GDK application
     */
    public void start() {
        Logging.info("🔄 Starting GDK");
        
        try 
        {
            // Initialize UI components
            Scene lobbyScene = initializeUI();
            
            // Initialize ViewModel
            GDKViewModel viewModel = initializeViewModel();
            
            // Configure stage and set the scene
            configureStage(lobbyScene);
            
            // Wire up controller with ViewModel
            wireUpController(lobbyScene, viewModel);
            
            // Show the stage
            primaryStage.show();
            
            Logging.info("✅ GDK startup completed successfully");
            
        } 
        catch (Exception e) 
        {
            // Log the error and throw a runtime exception
            Logging.error("❌ GDK startup failed: " + e.getMessage(), e);
            throw new RuntimeException("Failed to start GDK", e);
        }
    }

    // ==================== PRIVATE STARTUP METHODS ====================
    
    /**
     * Initializes the UI components
     */
    private Scene initializeUI() {
        Logging.info("🎨 Initializing GDK UI");
        
        try 
        {
            // Load the GDK Game Lobby FXML
            URL fxmlUrl = GDKApplication.class.getResource("/gdk-lobby/GDKGameLobby.fxml");
            if (fxmlUrl == null) 
            {
                throw new RuntimeException("FXML resource not found: /gdk-lobby/GDKGameLobby.fxml");
            }
            
            Logging.info("📂 Loading FXML from: " + fxmlUrl);
            
            // Load the FXML and create the scene
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene lobbyScene = new Scene(loader.load());
            
            // Apply GDK lobby CSS
            URL cssUrl = GDKApplication.class.getResource("/gdk-lobby/gdk-lobby.css");
            if (cssUrl != null) 
            {
                // Add the CSS to the scene
                lobbyScene.getStylesheets().add(cssUrl.toExternalForm());
                Logging.info("📂 CSS loaded from: " + cssUrl);
            }
            
            // Get the controller and set the primary stage
            this.controller = loader.getController();
            if (this.controller != null) 
            {
                // Wire up the controller with references
                this.controller.setPrimaryStage(primaryStage);
                Logging.info("✅ Controller initialized successfully");
            } 
            else 
            {
                // Log the error and throw a runtime exception
                throw new RuntimeException("Controller is null");
            }
            
            Logging.info("✅ GDK Game Lobby loaded successfully");
            return lobbyScene;
            
        } 
        catch (Exception e) 
        {
            // Log the error and throw a runtime exception
            Logging.error("❌ Failed to load GDK Game Lobby: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize GDK UI", e);
        }
    }
    
    /**
     * Initializes the ViewModel
     */
    private GDKViewModel initializeViewModel() {
        Logging.info("🧠 Initializing GDK ViewModel");
        
        try 
        {
            // Create ViewModel
            GDKViewModel viewModel = new GDKViewModel(moduleLoader);
            viewModel.setPrimaryStage(primaryStage);
            
            Logging.info("✅ ViewModel initialized successfully");
            return viewModel;
            
        } 
        catch (Exception e) 
        {
            Logging.error("❌ Failed to initialize ViewModel: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize ViewModel", e);
        }
    }
    
    /**
     * Configures the primary stage
     */
    private void configureStage(Scene lobbyScene) {
        Logging.info("⚙️ Configuring primary stage");
        
        primaryStage.setTitle("OMG Game Development Kit");
        primaryStage.setWidth(1200);
        primaryStage.setHeight(900);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        // Set the scene on the primary stage
        primaryStage.setScene(lobbyScene);
        
        Logging.info("✅ Primary stage configured successfully");
    }
    
    /**
     * Wires up the controller with the ViewModel
     */
    private void wireUpController(Scene lobbyScene, GDKViewModel viewModel) {
        try 
        {
            if (this.controller != null) 
            {
                this.controller.setViewModel(viewModel);
                Logging.info("✅ Controller wired up with ViewModel");
            } 
            else 
            {
                Logging.warning("⚠️ Controller not found for wiring");
            }
        } 
        catch (Exception e) 
        {
            Logging.error("❌ Error wiring up controller: " + e.getMessage(), e);
        }
    }
} 
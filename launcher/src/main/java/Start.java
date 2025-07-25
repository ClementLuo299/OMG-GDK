import gdk.GameModule;
import gdk.Logging;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import java.util.Properties;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Handles startup of the GDK application
 *
 * @authors Clement Luo
 * @date July 24, 2025
 * @since 1.0
 */
public class Start {

    // ==================== CONSTANTS ====================
    
    private static final String CONFIG_FILE = "gdk-config.properties";
    private static final String DEFAULT_MODULES_DIR = "../modules";
    
    // ==================== DEPENDENCIES ====================
    
    private final Stage primaryStage;
    private final ModuleLoader moduleLoader;
    private Properties config;

    // ==================== CONSTRUCTOR ====================
    
    public Start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.moduleLoader = new ModuleLoader();
        this.config = new Properties();
    }

    // ==================== PUBLIC STARTUP METHODS ====================
    
    /**
     * Starts the GDK application
     */
    public GDKStartResult start() {
        Logging.info("🔄 Starting GDK");
        
        try {
            // Load configuration
            loadConfiguration();
            
            // Initialize UI components
            Scene lobbyScene = initializeUI();
            
            // Initialize ViewModel
            GDKViewModel viewModel = initializeViewModel();
            
            // Configure stage and set the scene
            configureStage(lobbyScene);
            
            Logging.info("✅ GDK startup completed successfully");
            
            return new GDKStartResult(lobbyScene, viewModel, true, null);
            
        } catch (Exception e) {
            Logging.error("❌ GDK startup failed: " + e.getMessage(), e);
            return new GDKStartResult(null, null, false, e.getMessage());
        }
    }

    // ==================== PRIVATE STARTUP METHODS ====================
    
    /**
     * Loads configuration from properties file
     */
    private void loadConfiguration() {
        Logging.info("📂 Loading GDK configuration");
        
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                try (FileReader reader = new FileReader(configFile)) {
                    config.load(reader);
                    Logging.info("✅ Configuration loaded from " + CONFIG_FILE);
                }
            } else {
                Logging.info("📝 Creating default configuration");
                createDefaultConfiguration();
            }
        } catch (IOException e) {
            Logging.error("❌ Failed to load configuration: " + e.getMessage(), e);
            createDefaultConfiguration();
        }
    }
    
    /**
     * Creates default configuration
     */
    private void createDefaultConfiguration() {
        Logging.info("📝 Creating default GDK configuration");
        
        config.setProperty("modulesDir", DEFAULT_MODULES_DIR);
        config.setProperty("enableDebugMode", "true");
        config.setProperty("serverPort", "8080");
        config.setProperty("serverUrl", "localhost");
        config.setProperty("username", "GDK Developer");
        
        try {
            saveConfiguration();
            Logging.info("✅ Default configuration created and saved");
        } catch (IOException e) {
            Logging.error("❌ Failed to save default configuration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Saves configuration to file
     */
    private void saveConfiguration() throws IOException {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            config.store(writer, "GDK Configuration");
        }
    }
    
    /**
     * Initializes the UI components
     */
    private Scene initializeUI() {
        Logging.info("🎨 Initializing GDK UI");
        
        try {
            // Load the GDK Game Lobby FXML
            URL fxmlUrl = GDKApplication.class.getResource("/GDKGameLobby.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("FXML resource not found: /GDKGameLobby.fxml");
            }
            
            Logging.info("📂 Loading FXML from: " + fxmlUrl);
            
            // Load the FXML and create the scene
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene lobbyScene = new Scene(loader.load());
            
            // Apply GDK lobby CSS
            URL cssUrl = GDKApplication.class.getResource("/gdk-lobby.css");
            if (cssUrl != null) {
                lobbyScene.getStylesheets().add(cssUrl.toExternalForm());
                Logging.info("📂 CSS loaded from: " + cssUrl);
            }
            
            // Get the controller and set the primary stage
            GDKGameLobbyController controller = loader.getController();
            if (controller != null) {
                // Wire up the controller with references
                controller.setPrimaryStage(primaryStage);
                Logging.info("✅ Controller initialized successfully");
            } else {
                throw new RuntimeException("Controller is null");
            }
            
            Logging.info("✅ GDK Game Lobby loaded successfully");
            return lobbyScene;
            
        } catch (Exception e) {
            Logging.error("❌ Failed to load GDK Game Lobby: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize GDK UI", e);
        }
    }
    
    /**
     * Initializes the ViewModel
     */
    private GDKViewModel initializeViewModel() {
        Logging.info("🧠 Initializing GDK ViewModel");
        
        try {
            // Create ViewModel
            GDKViewModel viewModel = new GDKViewModel(moduleLoader);
            viewModel.setPrimaryStage(primaryStage);
            
            Logging.info("✅ ViewModel initialized successfully");
            return viewModel;
            
        } catch (Exception e) {
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
     * Gets the modules directory from configuration
     */
    public String getModulesDirectory() {
        return config.getProperty("modulesDir", DEFAULT_MODULES_DIR);
    }
    
    /**
     * Gets the configuration properties
     */
    public Properties getConfiguration() {
        return config;
    }
    
    /**
     * Result class for startup
     */
    public static class GDKStartResult {
        private final Scene lobbyScene;
        private final GDKViewModel viewModel;
        private final boolean success;
        private final String errorMessage;
        
        public GDKStartResult(Scene lobbyScene, GDKViewModel viewModel, boolean success, String errorMessage) {
            this.lobbyScene = lobbyScene;
            this.viewModel = viewModel;
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        public Scene getLobbyScene() { return lobbyScene; }
        public GDKViewModel getViewModel() { return viewModel; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }
} 
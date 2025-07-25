import gdk.Logging;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Handles the complete startup process for the GDK application.
 * 
 * This class orchestrates the initialization of all major components
 * required for the GDK to function properly. It follows a step-by-step
 * approach to ensure each component is properly initialized before
 * proceeding to the next.
 * 
 * Key responsibilities:
 * - Load and initialize the main UI (FXML and CSS)
 * - Create and configure the ViewModel
 * - Set up the primary application stage
 * - Wire up all components for proper communication
 * - Handle any startup errors gracefully
 *
 * @authors Clement Luo
 * @date July 25, 2025
 * @edited July 25, 2025
 * @since 1.0
 */
public class Startup {

    // ==================== INSTANCE VARIABLES ====================
    
    /**
     * The primary JavaFX stage that hosts the main application window
     */
    private final Stage primaryApplicationStage;
    
    /**
     * Utility for discovering and loading game modules from the filesystem
     */
    private final ModuleLoader gameModuleLoader;
    
    /**
     * Controller for the main GDK lobby interface
     */
    private GDKGameLobbyController lobbyController;

    // ==================== CONSTRUCTOR ====================
    
    /**
     * Create a new Startup instance with the provided primary stage.
     * 
     * @param primaryApplicationStage The primary stage for the application
     */
    public Startup(Stage primaryApplicationStage) {
        this.primaryApplicationStage = primaryApplicationStage;
        this.gameModuleLoader = new ModuleLoader();
    }

    // ==================== PUBLIC STARTUP ORCHESTRATION ====================
    
    /**
     * Execute the complete GDK startup process.
     * 
     * This method orchestrates the entire startup sequence, ensuring
     * that all components are properly initialized in the correct order.
     * If any step fails, the entire startup process is aborted with
     * appropriate error handling.
     */
    public void start() {
        Logging.info("🔄 Starting GDK application startup process");
        
        try {
            // Step 1: Initialize the main user interface
            Scene mainLobbyScene = initializeMainUserInterface();
            
            // Step 2: Create and configure the ViewModel
            GDKViewModel applicationViewModel = initializeApplicationViewModel();
            
            // Step 3: Configure the primary application stage
            configurePrimaryApplicationStage(mainLobbyScene);
            
            // Step 4: Wire up the controller with the ViewModel
            wireUpControllerWithViewModel(mainLobbyScene, applicationViewModel);
            
            // Step 5: Display the application to the user
            displayApplicationToUser();
            
            Logging.info("✅ GDK application startup completed successfully");
        } catch (Exception startupError) {
            handleStartupProcessError(startupError);
        }
    }

    // ==================== USER INTERFACE INITIALIZATION ====================
    
    /**
     * Initialize the main user interface components.
     * 
     * This method loads the FXML layout, applies CSS styling, and
     * sets up the main controller for the lobby interface.
     * 
     * @return The configured Scene for the main lobby interface
     */
    private Scene initializeMainUserInterface() {
        Logging.info("🎨 Initializing main user interface components");
        
        try {
            Scene mainLobbyScene = loadFXMLSceneFromResources();
            applyCSSStylingToScene(mainLobbyScene);
            setupLobbyController();
            
            Logging.info("✅ Main user interface loaded successfully");
            return mainLobbyScene;
        } catch (Exception uiInitializationError) {
            handleUserInterfaceInitializationError(uiInitializationError);
            return null; // This will never be reached due to exception being thrown
        }
    }
    
    /**
     * Load the FXML scene from application resources.
     * 
     * @return The loaded Scene object
     * @throws Exception if FXML loading fails
     */
    private Scene loadFXMLSceneFromResources() throws Exception {
        URL fxmlResourceUrl = getFXMLResourceUrl();
        Logging.info("📂 Loading FXML from: " + fxmlResourceUrl);
        
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlResourceUrl);
        Scene loadedScene = new Scene(fxmlLoader.load());
        
        // Store the controller reference for later use
        this.lobbyController = fxmlLoader.getController();
        
        return loadedScene;
    }
    
    /**
     * Get the FXML resource URL for the main lobby interface.
     * 
     * @return The URL to the FXML resource
     * @throws RuntimeException if the FXML resource cannot be found
     */
    private URL getFXMLResourceUrl() {
        URL fxmlResourceUrl = GDKApplication.class.getResource("/gdk-lobby/GDKGameLobby.fxml");
        if (fxmlResourceUrl == null) {
            throw new RuntimeException("FXML resource not found: /gdk-lobby/GDKGameLobby.fxml");
        }
        return fxmlResourceUrl;
    }
    
    /**
     * Apply CSS styling to the main lobby scene.
     * 
     * @param mainLobbyScene The scene to apply CSS to
     */
    private void applyCSSStylingToScene(Scene mainLobbyScene) {
        URL cssResourceUrl = GDKApplication.class.getResource("/gdk-lobby/gdk-lobby.css");
        if (cssResourceUrl != null) {
            mainLobbyScene.getStylesheets().add(cssResourceUrl.toExternalForm());
            Logging.info("📂 CSS styling loaded from: " + cssResourceUrl);
        }
    }
    
    /**
     * Set up the lobby controller with necessary references.
     * 
     * This method configures the controller with the primary stage
     * and validates that the controller was properly loaded.
     */
    private void setupLobbyController() {
        if (this.lobbyController != null) {
            this.lobbyController.setPrimaryStage(primaryApplicationStage);
            Logging.info("✅ Lobby controller initialized successfully");
        } else {
            throw new RuntimeException("Lobby controller is null - FXML loading may have failed");
        }
    }
    
    /**
     * Handle errors that occur during user interface initialization.
     * 
     * @param uiInitializationError The exception that occurred
     */
    private void handleUserInterfaceInitializationError(Exception uiInitializationError) {
        Logging.error("❌ Failed to load main user interface: " + uiInitializationError.getMessage(), uiInitializationError);
        throw new RuntimeException("Failed to initialize main user interface", uiInitializationError);
    }
    
    // ==================== VIEWMODEL INITIALIZATION ====================
    
    /**
     * Initialize the application ViewModel.
     * 
     * This method creates the ViewModel instance and configures it
     * with the necessary dependencies for proper operation.
     * 
     * @return The configured ViewModel instance
     */
    private GDKViewModel initializeApplicationViewModel() {
        Logging.info("🧠 Initializing application ViewModel");
        
        try {
            GDKViewModel applicationViewModel = createNewViewModelInstance();
            configureViewModelWithStage(applicationViewModel);
            
            Logging.info("✅ Application ViewModel initialized successfully");
            return applicationViewModel;
        } catch (Exception viewModelInitializationError) {
            handleViewModelInitializationError(viewModelInitializationError);
            return null; // This will never be reached due to exception being thrown
        }
    }
    
    /**
     * Create a new ViewModel instance with the module loader.
     * 
     * @return A new ViewModel instance
     */
    private GDKViewModel createNewViewModelInstance() {
        return new GDKViewModel(gameModuleLoader);
    }
    
    /**
     * Configure the ViewModel with the primary application stage.
     * 
     * @param applicationViewModel The ViewModel to configure
     */
    private void configureViewModelWithStage(GDKViewModel applicationViewModel) {
        applicationViewModel.setPrimaryStage(primaryApplicationStage);
    }
    
    /**
     * Handle errors that occur during ViewModel initialization.
     * 
     * @param viewModelInitializationError The exception that occurred
     */
    private void handleViewModelInitializationError(Exception viewModelInitializationError) {
        Logging.error("❌ Failed to initialize ViewModel: " + viewModelInitializationError.getMessage(), viewModelInitializationError);
        throw new RuntimeException("Failed to initialize ViewModel", viewModelInitializationError);
    }
    
    // ==================== STAGE CONFIGURATION ====================
    
    /**
     * Configure the primary application stage with proper settings.
     * 
     * This method sets up the stage with appropriate title, size,
     * and minimum dimensions for optimal user experience.
     * 
     * @param mainLobbyScene The scene to set on the stage
     */
    private void configurePrimaryApplicationStage(Scene mainLobbyScene) {
        Logging.info("⚙️ Configuring primary application stage");
        
        setStageProperties();
        setSceneOnStage(mainLobbyScene);
        
        Logging.info("✅ Primary application stage configured successfully");
    }
    
    /**
     * Set the basic properties of the primary stage.
     * 
     * This includes the window title, initial size, and minimum
     * dimensions to ensure the application is usable.
     */
    private void setStageProperties() {
        primaryApplicationStage.setTitle("OMG Game Development Kit");
        primaryApplicationStage.setWidth(1200);
        primaryApplicationStage.setHeight(900);
        primaryApplicationStage.setMinWidth(800);
        primaryApplicationStage.setMinHeight(600);
    }
    
    /**
     * Set the main lobby scene on the primary stage.
     * 
     * @param mainLobbyScene The scene to display
     */
    private void setSceneOnStage(Scene mainLobbyScene) {
        primaryApplicationStage.setScene(mainLobbyScene);
    }
    
    // ==================== COMPONENT WIRING ====================
    
    /**
     * Wire up the controller with the ViewModel for proper communication.
     * 
     * This method establishes the connection between the UI controller
     * and the ViewModel, enabling proper data binding and event handling.
     * 
     * @param mainLobbyScene The main lobby scene (unused but kept for consistency)
     * @param applicationViewModel The ViewModel to wire up
     */
    private void wireUpControllerWithViewModel(Scene mainLobbyScene, GDKViewModel applicationViewModel) {
        try {
            if (this.lobbyController != null) {
                this.lobbyController.setViewModel(applicationViewModel);
                Logging.info("✅ Controller wired up with ViewModel successfully");
            } else {
                Logging.warning("⚠️ Lobby controller not found for wiring");
            }
        } catch (Exception wiringError) {
            handleControllerWiringError(wiringError);
        }
    }
    
    /**
     * Handle errors that occur during controller wiring.
     * 
     * @param wiringError The exception that occurred
     */
    private void handleControllerWiringError(Exception wiringError) {
        Logging.error("❌ Error wiring up controller: " + wiringError.getMessage(), wiringError);
    }
    
    // ==================== APPLICATION DISPLAY ====================
    
    /**
     * Display the application to the user.
     * 
     * This method makes the primary stage visible, effectively
     * showing the application window to the user.
     */
    private void displayApplicationToUser() {
        primaryApplicationStage.show();
    }
    
    // ==================== ERROR HANDLING ====================
    
    /**
     * Handle errors that occur during the startup process.
     * 
     * This method logs the error details and re-throws the exception
     * to ensure the application fails fast if startup cannot complete.
     * 
     * @param startupError The exception that occurred during startup
     */
    private void handleStartupProcessError(Exception startupError) {
        Logging.error("❌ GDK application startup failed: " + startupError.getMessage(), startupError);
        throw new RuntimeException("Failed to start GDK application", startupError);
    }
} 
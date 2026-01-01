package launcher.ui_areas.server_simulator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import launcher.core.GDKApplication;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import gdk.internal.Logging;
import java.util.function.Consumer;

/**
 * Controller for the Server Simulator window interface.
 * 
 * This class manages the server simulator interface that allows users to
 * send and receive JSON messages to simulate server communication with
 * running games. It provides a simple messaging interface for testing
 * game-server communication protocols.
 * 
 * Key responsibilities:
 * - Manage the server simulator user interface
 * - Handle message sending and receiving
 * - Provide real-time message display with timestamps
 * - Coordinate with the main GDK application
 * - Handle window lifecycle and cleanup
 *
 * @author Clement Luo
 * @date July 25, 2025
 * @edited August 10, 2025   
 * @since 1.0
 */
public class ServerSimulatorController {
    
    // ==================== FXML INJECTIONS ====================
    
    /**
     * Root container for the server simulator interface
     */
    @FXML private VBox rootContainer;
    
    /**
     * Text area for displaying received and sent messages
     */
    @FXML private TextArea receivedMessagesDisplayArea;
    
    /**
     * Text field for entering messages to send
     */
    @FXML private TextField messageInputField;
    
    /**
     * Button for sending messages
     */
         @FXML private Button sendMessageButton;
     
     /**
      * Button for clearing all messages from the display
      */
     @FXML private Button clearMessagesButton;
     
     /** Save/Load buttons */
     @FXML private Button saveMessagesButton;
     @FXML private Button loadMessagesButton;
     
     /**
      * Button for closing the server simulator window
      */
     @FXML private Button closeWindowButton;
    
    // ==================== DEPENDENCIES ====================
    
    /**
     * The stage hosting the server simulator window
     */
    private Stage serverSimulatorStage;
    
    /**
     * Handler for processing messages sent to games
     */
    private Consumer<String> gameMessageHandler;
    
    /**
     * Reference to the main GDK application for coordination
     */
    private GDKApplication gdkApplication;

    // ==================== CONSTANTS ====================
    
    /**
     * File path for persisting input content
     */
    private static final String INPUT_PERSISTENCE_FILE = "saved/server-simulator-input.txt";

    // ==================== INITIALIZATION ====================
    
    /**
     * Initialize the server simulator controller when FXML is loaded.
     * 
     * This method is called automatically by JavaFX when the FXML
     * file is loaded. It sets up the user interface components and
     * configures event handlers for user interactions.
     */
    @FXML
    public void initialize() {
        Logging.info("🔧 Server Simulator Controller initialized");
        
        setupMessageInputHandling();
        setupSendButtonStateManagement();
        setupSaveLoadHandlers();
        setupClearHandler();
        loadInputContent(); // Load input content on initialization
    }
    
    /**
     * Set up message input handling for the text field.
     * 
     * This method configures the message input field to respond
     * to Enter key presses for quick message sending.
     */
    private void setupMessageInputHandling() {
        messageInputField.setOnAction(event -> handleSendMessageAction());
        if (sendMessageButton != null) {
            sendMessageButton.setOnAction(event -> handleSendMessageAction());
        }
    }
    
    /**
     * Set up send button state management based on input content.
     * 
     * This method configures the send button to be enabled only
     * when there is valid text in the input field.
     */
    private void setupSendButtonStateManagement() {
        sendMessageButton.setDisable(true);
        messageInputField.textProperty().addListener((observable, oldValue, newValue) -> {
            sendMessageButton.setDisable(newValue == null || newValue.trim().isEmpty());
        });
    }

    /**
     * Sets up save and load message handlers.
     * 
     * <p>This method configures the save and load buttons to allow users
     * to persist and restore server simulator input content.
     */
    private void setupSaveLoadHandlers() {
        if (saveMessagesButton != null) {
            saveMessagesButton.setOnAction(e -> handleSaveMessages());
        }
        if (loadMessagesButton != null) {
            loadMessagesButton.setOnAction(e -> handleLoadMessages());
        }
    }
    
    // ==================== FILE OPERATIONS ====================
    
    /**
     * Handles saving the current input content to a file.
     * 
     * <p>This method opens a file chooser dialog allowing the user to save
     * the current input field content to a text file. The file chooser
     * defaults to the "saved" directory if it exists.
     */
    private void handleSaveMessages() {
        try {
            // Create file chooser for save
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save Server Simulator Input");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("All Files", "*.*")
            );
            
            // Set initial directory to saved folder if it exists
            java.io.File savedDir = new java.io.File("saved");
            if (savedDir.exists() && savedDir.isDirectory()) {
                fileChooser.setInitialDirectory(savedDir);
            }
            
            // Get the window from the scene instead of stored stage reference
            javafx.stage.Window window = null;
            if (messageInputField != null && messageInputField.getScene() != null) {
                window = messageInputField.getScene().getWindow();
            } else if (serverSimulatorStage != null) {
                window = serverSimulatorStage;
            }
            
            if (window == null) {
                addReceivedMessageToDisplay("❌ ERROR: Cannot determine window for file chooser");
                Logging.error("❌ Cannot determine window for file chooser in server simulator");
                return;
            }
            
            // Show save dialog
            java.io.File file = fileChooser.showSaveDialog(window);
            if (file != null) {
                // Save only the input field content
                String inputContent = messageInputField.getText();
                if (inputContent == null) inputContent = "";
                java.nio.file.Files.writeString(file.toPath(), inputContent);
                addReceivedMessageToDisplay("✅ Input content saved to: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            addReceivedMessageToDisplay("❌ ERROR saving: " + e.getMessage());
            Logging.error("❌ Failed to save server simulator input: " + e.getMessage(), e);
        }
    }

    /**
     * Handles ui_loading input content from a file.
     * 
     * <p>This method opens a file chooser dialog allowing the user to load
     * input content from a text file into the input field. The file chooser
     * defaults to the "saved" directory if it exists.
     */
    private void handleLoadMessages() {
        try {
            // Create file chooser for load
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Load Server Simulator Input");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("All Files", "*.*")
            );
            
            // Set initial directory to saved folder if it exists
            java.io.File savedDir = new java.io.File("saved");
            if (savedDir.exists() && savedDir.isDirectory()) {
                fileChooser.setInitialDirectory(savedDir);
            }
            
            // Get the window from the scene instead of stored stage reference
            javafx.stage.Window window = null;
            if (messageInputField != null && messageInputField.getScene() != null) {
                window = messageInputField.getScene().getWindow();
            } else if (serverSimulatorStage != null) {
                window = serverSimulatorStage;
            }
            
            if (window == null) {
                addReceivedMessageToDisplay("❌ ERROR: Cannot determine window for file chooser");
                Logging.error("❌ Cannot determine window for file chooser in server simulator");
                return;
            }
            
            // Show open dialog
            java.io.File file = fileChooser.showOpenDialog(window);
            if (file != null) {
                if (!file.exists()) {
                    addReceivedMessageToDisplay("❌ File not found: " + file.getAbsolutePath());
                    return;
                }
                
                String content = java.nio.file.Files.readString(file.toPath());
                messageInputField.setText(content);
                addReceivedMessageToDisplay("✅ Input content loaded from: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            addReceivedMessageToDisplay("❌ ERROR ui_loading: " + e.getMessage());
            Logging.error("❌ Failed to load server simulator input: " + e.getMessage(), e);
        }
    }

    // ==================== INPUT PERSISTENCE METHODS ====================
    
    /**
     * Saves the current input content to the persistence file.
     * 
     * <p>This method automatically saves the input field content to a
     * persistence file in the "saved" directory. This allows the input
     * to be restored when the server simulator is reopened.
     */
    private void saveInputContent() {
        try {
            // Ensure saved directory exists
            java.nio.file.Path savedDir = java.nio.file.Paths.get("saved");
            if (!java.nio.file.Files.exists(savedDir)) {
                java.nio.file.Files.createDirectories(savedDir);
            }
            
            // Save current input content
            String inputContent = messageInputField.getText();
            if (inputContent != null && !inputContent.trim().isEmpty()) {
                java.nio.file.Files.writeString(java.nio.file.Paths.get(INPUT_PERSISTENCE_FILE), inputContent);
                Logging.info("💾 Server simulator input content saved");
            }
        } catch (Exception e) {
            Logging.error("❌ Failed to save input content: " + e.getMessage(), e);
        }
    }
    
    /**
     * Loads the previously saved input content from the persistence file.
     * 
     * <p>This method automatically loads the input field content from the
     * persistence file when the server simulator is initialized. If no
     * saved content exists, this method does nothing.
     */
    private void loadInputContent() {
        try {
            java.nio.file.Path inputFile = java.nio.file.Paths.get(INPUT_PERSISTENCE_FILE);
            if (java.nio.file.Files.exists(inputFile)) {
                String savedContent = java.nio.file.Files.readString(inputFile);
                if (savedContent != null && !savedContent.trim().isEmpty()) {
                    messageInputField.setText(savedContent);
                    Logging.info("📂 Server simulator input content restored");
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Failed to load input content: " + e.getMessage(), e);
        }
    }
    
    /**
     * Clears the saved input content from the persistence file.
     * 
     * <p>This method deletes the persistence file if it exists. This is
     * typically called when the window is closed or when the user explicitly
     * clears the input.
     */
    private void clearSavedInputContent() {
        try {
            java.nio.file.Path inputFile = java.nio.file.Paths.get(INPUT_PERSISTENCE_FILE);
            if (java.nio.file.Files.exists(inputFile)) {
                java.nio.file.Files.deleteIfExists(inputFile);
                Logging.info("🗑️ Server simulator saved input content cleared");
            }
        } catch (Exception e) {
            Logging.error("❌ Failed to clear saved input content: " + e.getMessage(), e);
        }
    }

    /**
     * Sets up the clear messages button handler.
     * 
     * <p>This method configures the clear button to clear both the message
     * display area and the input field when clicked.
     */
    private void setupClearHandler() {
        if (clearMessagesButton != null) {
            clearMessagesButton.setOnAction(e -> {
                if (receivedMessagesDisplayArea != null) {
                    receivedMessagesDisplayArea.clear();
                }
                if (messageInputField != null) {
                    messageInputField.clear();
                }
                Logging.info("🧹 Server Simulator messages cleared");
            });
        }
    }

    // ==================== DEPENDENCY INJECTION ====================
    
    /**
     * Sets the stage reference for window management.
     * 
     * <p>This method is called by the parent component to provide the stage
     * hosting the server simulator window. This allows the controller to
     * manage window lifecycle operations.
     * 
     * @param serverSimulatorStage The stage hosting the server simulator window
     */
    public void setStage(Stage serverSimulatorStage) {
        this.serverSimulatorStage = serverSimulatorStage;
    }
    
    /**
     * Sets the GDK application reference for coordination.
     * 
     * <p>This method is called by the parent component to provide a reference
     * to the main GDK application. This allows the controller to coordinate
     * with the main application if needed.
     * 
     * @param gdkApplication The main GDK application instance
     */
    public void setGDKApplication(GDKApplication gdkApplication) {
        this.gdkApplication = gdkApplication;
    }
    
    /**
     * Sets the message handler for sending messages to games.
     * 
     * <p>This method is called by the parent component to provide a handler
     * function that processes messages sent to games. The handler receives
     * the message text and forwards it to the currently running game.
     * 
     * @param gameMessageHandler The handler for processing messages sent to games
     */
    public void setMessageHandler(Consumer<String> gameMessageHandler) {
        this.gameMessageHandler = gameMessageHandler;
        Logging.info("🔧 Server Simulator message handler set");
    }

    // ==================== MESSAGE HANDLING ====================
    
    // ==================== MESSAGE HANDLING ====================
    
    /**
     * Handles the send message action initiated by the user.
     * 
     * <p>This method processes the message from the input field, sends it
     * to the game via the message handler, updates the display with the
     * sent message, clears the input field, and saves the input content
     * for persistence.
     */
    @FXML
    private void handleSendMessageAction() {
        String messageText = messageInputField.getText().trim();
        if (!messageText.isEmpty() && gameMessageHandler != null) {
            try {
                sendMessageToGame(messageText);
                addSentMessageToDisplay(messageText);
                clearMessageInputField();
                saveInputContent(); // Save input content after sending
                
            } catch (Exception messageError) {
                Logging.error("❌ Error sending message: " + messageError.getMessage());
                addReceivedMessageToDisplay("ERROR: " + messageError.getMessage());
            }
        }
    }
    
    /**
     * Sends a message to the game via the message handler.
     * 
     * <p>This method invokes the game message handler with the provided
     * message text. The handler is responsible for forwarding the message
     * to the currently running game module.
     * 
     * @param messageText The message text to send to the game
     */
    private void sendMessageToGame(String messageText) {
        Logging.info("📤 Server Simulator sending message: " + messageText);
        gameMessageHandler.accept(messageText);
    }
    
    /**
     * Adds a sent message to the display area with timestamp.
     * 
     * <p>This method formats the message with a timestamp and appends it
     * to the display area. The display automatically scrolls to show the
     * latest message.
     * 
     * @param messageText The message text that was sent
     */
    private void addSentMessageToDisplay(String messageText) {
        String currentTimestamp = java.time.LocalTime.now().toString();
        String formattedMessage = "[" + currentTimestamp + "] -> " + messageText + "\n";
        receivedMessagesDisplayArea.appendText(formattedMessage);
        receivedMessagesDisplayArea.setScrollTop(Double.MAX_VALUE);
    }
    
    /**
     * Clears the message input field after sending.
     * 
     * <p>This method clears the input field to prepare for the next message.
     * It is called automatically after a message is successfully sent.
     */
    private void clearMessageInputField() {
        messageInputField.clear();
    }
    
    /**
     * Adds a received message to the display area with timestamp.
     * 
     * <p>This method adds a message to the display area with proper
     * timestamp formatting and ensures the display scrolls to show
     * the latest message. This method is thread-safe and can be called
     * from any thread, as it uses Platform.runLater to update the UI.
     * 
     * @param messageText The message text to display
     */
    public void addReceivedMessageToDisplay(String messageText) {
        if (receivedMessagesDisplayArea != null) {
            String currentTimestamp = java.time.LocalTime.now().toString();
            String formattedMessage = "[" + currentTimestamp + "] <- " + messageText + "\n";
            
            javafx.application.Platform.runLater(() -> {
                receivedMessagesDisplayArea.appendText(formattedMessage);
                receivedMessagesDisplayArea.setScrollTop(Double.MAX_VALUE);
            });
        }
    }

    // ==================== USER INTERFACE ACTIONS ====================
    
    // ==================== USER INTERFACE ACTIONS ====================
    
    /**
     * Handles the clear messages action initiated by the user.
     * 
     * <p>This method clears the message input field and clears the saved
     * input content from the persistence file. The message history in the
     * display area is preserved.
     */
    @FXML
    private void handleClearMessagesAction() {
        clearMessageInputField();
        clearSavedInputContent(); // Also clear saved input content
        Logging.info("🧹 Server Simulator input field and saved content cleared");
    }
    
    /**
     * Handles the close window action initiated by the user.
     * 
     * <p>This method closes the server simulator window and clears the
     * saved input content from the persistence file. This ensures a clean
     * state when the window is reopened.
     */
    @FXML
    private void handleCloseWindowAction() {
        Logging.info("🔒 Server Simulator window closing");
        if (serverSimulatorStage != null) {
            serverSimulatorStage.close();
        }
        clearSavedInputContent(); // Clear saved input on window close
    }

    // ==================== UTILITY METHODS ====================
    
    /**
     * Gets the root VBox container for scene creation.
     * 
     * <p>This method provides access to the root container for
     * creating the server simulator scene. This is typically used
     * by the parent component to create the scene.
     * 
     * @return The root VBox container
     */
    public VBox getRootContainer() {
        return rootContainer;
    }
    
    /**
     * Called when the server simulator window is about to close.
     * 
     * <p>This method performs cleanup operations when the server
     * simulator window is closing, including clearing saved input
     * content and notifying the main GDK application if necessary.
     */
    public void onClose() {
        Logging.info("🔒 Server Simulator cleanup");
        // Notify GDK application that server simulator is closing
        if (gdkApplication != null) {
            // This could trigger game cleanup if needed
            // Currently not implemented but available for future use
        }
        clearSavedInputContent(); // Clear saved input on application exit
    }
} 
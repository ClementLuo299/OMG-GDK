package launcher.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import launcher.GDKApplication;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import gdk.Logging;
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
 * @authors Clement Luo
 * @date July 25, 2025
 * @edited August 9, 2025   
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

    private void setupSaveLoadHandlers() {
        if (saveMessagesButton != null) {
            saveMessagesButton.setOnAction(e -> handleSaveMessages());
        }
        if (loadMessagesButton != null) {
            loadMessagesButton.setOnAction(e -> handleLoadMessages());
        }
    }

    private void handleSaveMessages() {
        try {
            // Ensure saved directory exists
            java.nio.file.Path savedDir = java.nio.file.Paths.get("saved");
            if (!java.nio.file.Files.exists(savedDir)) {
                java.nio.file.Files.createDirectories(savedDir);
            }
            
            // Save raw message area
            String content = receivedMessagesDisplayArea.getText();
            if (content == null) content = "";
            java.nio.file.Path out = java.nio.file.Paths.get("saved/server-simulator-messages.txt");
            java.nio.file.Files.writeString(out, content);
            addReceivedMessageToDisplay("Saved to " + out.toAbsolutePath());
            // Also save structured transcript JSONL
            java.nio.file.Path transcript = launcher.utils.TranscriptRecorder.saveTranscript(java.nio.file.Paths.get("saved/transcript.jsonl"));
            if (transcript != null) {
                addReceivedMessageToDisplay("Transcript saved: " + transcript.toAbsolutePath());
            }
        } catch (Exception e) {
            addReceivedMessageToDisplay("ERROR saving: " + e.getMessage());
        }
    }

    private void handleLoadMessages() {
        try {
            java.nio.file.Path in = java.nio.file.Paths.get("server-simulator-messages.txt");
            if (!java.nio.file.Files.exists(in)) {
                addReceivedMessageToDisplay("No saved file found: " + in.toAbsolutePath());
                return;
            }
            String content = java.nio.file.Files.readString(in);
            receivedMessagesDisplayArea.setText(content);
            addReceivedMessageToDisplay("Loaded from " + in.toAbsolutePath());
        } catch (Exception e) {
            addReceivedMessageToDisplay("ERROR loading: " + e.getMessage());
        }
    }

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

    // ==================== SETUP METHODS ====================
    
    /**
     * Set the stage reference for window management.
     * 
     * @param serverSimulatorStage The stage hosting the server simulator window
     */
    public void setStage(Stage serverSimulatorStage) {
        this.serverSimulatorStage = serverSimulatorStage;
    }
    
    /**
     * Set the GDK application reference for coordination.
     * 
     * @param gdkApplication The main GDK application instance
     */
    public void setGDKApplication(GDKApplication gdkApplication) {
        this.gdkApplication = gdkApplication;
    }
    
    /**
     * Set the message handler for sending messages to games.
     * 
     * @param gameMessageHandler The handler for processing messages sent to games
     */
    public void setMessageHandler(Consumer<String> gameMessageHandler) {
        this.gameMessageHandler = gameMessageHandler;
        Logging.info("🔧 Server Simulator message handler set");
    }

    // ==================== MESSAGE HANDLING ====================
    
    /**
     * Handle the send message action initiated by the user.
     * 
     * This method processes the message from the input field, sends it
     * to the game via the message handler, and updates the display.
     */
    @FXML
    private void handleSendMessageAction() {
        String messageText = messageInputField.getText().trim();
        if (!messageText.isEmpty() && gameMessageHandler != null) {
            try {
                sendMessageToGame(messageText);
                addSentMessageToDisplay(messageText);
                clearMessageInputField();
                
            } catch (Exception messageError) {
                Logging.error("❌ Error sending message: " + messageError.getMessage());
                addReceivedMessageToDisplay("ERROR: " + messageError.getMessage());
            }
        }
    }
    
    /**
     * Send a message to the game via the message handler.
     * 
     * @param messageText The message text to send to the game
     */
    private void sendMessageToGame(String messageText) {
        Logging.info("📤 Server Simulator sending message: " + messageText);
        gameMessageHandler.accept(messageText);
    }
    
    /**
     * Add a sent message to the display area with timestamp.
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
     * Clear the message input field after sending.
     * 
     * This method clears the input field to prepare for the next message.
     */
    private void clearMessageInputField() {
        messageInputField.clear();
    }
    
    /**
     * Add a received message to the display area with timestamp.
     * 
     * This method adds a message to the display area with proper
     * timestamp formatting and ensures the display scrolls to show
     * the latest message.
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
    
    /**
     * Handle the clear messages action initiated by the user.
     * 
     * This method clears all messages from the display area
     * and provides user feedback about the action.
     */
    @FXML
    private void handleClearMessagesAction() {
        if (receivedMessagesDisplayArea != null) {
            receivedMessagesDisplayArea.clear();
            Logging.info("🧹 Server Simulator messages cleared");
        }
    }
    
    /**
     * Handle the close window action initiated by the user.
     * 
     * This method closes the server simulator window and
     * logs the action for debugging purposes.
     */
    @FXML
    private void handleCloseWindowAction() {
        Logging.info("🔒 Server Simulator window closing");
        if (serverSimulatorStage != null) {
            serverSimulatorStage.close();
        }
    }

    // ==================== UTILITY METHODS ====================
    
    /**
     * Get the root VBox container for scene creation.
     * 
     * This method provides access to the root container for
     * creating the server simulator scene.
     * 
     * @return The root VBox container
     */
    public VBox getRootContainer() {
        return rootContainer;
    }
    
    /**
     * Called when the server simulator window is about to close.
     * 
     * This method performs cleanup operations when the server
     * simulator window is closing, including notifying the
     * main GDK application if necessary.
     */
    public void onClose() {
        Logging.info("🔒 Server Simulator cleanup");
        // Notify GDK application that server simulator is closing
        if (gdkApplication != null) {
            // This could trigger game cleanup if needed
            // Currently not implemented but available for future use
        }
    }
} 
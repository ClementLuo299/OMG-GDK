import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.gdk.shared.utils.error_handling.Logging;

import java.util.function.Consumer;

/**
 * Controller for the Server Simulator window.
 * Allows sending and receiving JSON messages to simulate server communication.
 */
public class ServerSimulatorController {
    
    @FXML private VBox root;
    @FXML private TextArea receivedMessagesArea;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private Button clearButton;
    @FXML private Button closeButton;
    
    private Stage stage;
    private Consumer<String> messageHandler;
    private GDKApplication gdkApplication;
    
    @FXML
    public void initialize() {
        Logging.info("🔧 Server Simulator Controller initialized");
        
        // Set up message input handling
        messageInput.setOnAction(event -> handleSendMessage());
        
        // Disable send button initially
        sendButton.setDisable(true);
        messageInput.textProperty().addListener((observable, oldValue, newValue) -> {
            sendButton.setDisable(newValue == null || newValue.trim().isEmpty());
        });
    }
    
    /**
     * Sets the stage reference for window management.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    /**
     * Sets the GDK application reference for coordination.
     */
    public void setGDKApplication(GDKApplication gdkApplication) {
        this.gdkApplication = gdkApplication;
    }
    
    /**
     * Sets the message handler for sending messages to the game.
     */
    public void setMessageHandler(Consumer<String> messageHandler) {
        this.messageHandler = messageHandler;
        Logging.info("🔧 Server Simulator message handler set");
    }
    
    /**
     * Adds a received message to the display area.
     */
    public void addReceivedMessage(String message) {
        if (receivedMessagesArea != null) {
            String timestamp = java.time.LocalTime.now().toString();
            String formattedMessage = "[" + timestamp + "] " + message + "\n";
            
            javafx.application.Platform.runLater(() -> {
                receivedMessagesArea.appendText(formattedMessage);
                // Auto-scroll to bottom
                receivedMessagesArea.setScrollTop(Double.MAX_VALUE);
            });
        }
    }
    
    /**
     * Handles sending a message to the game.
     */
    @FXML
    private void handleSendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty() && messageHandler != null) {
            try {
                Logging.info("📤 Server Simulator sending message: " + message);
                messageHandler.accept(message);
                
                // Add to received messages area as "sent"
                String timestamp = java.time.LocalTime.now().toString();
                String formattedMessage = "[" + timestamp + "] SENT: " + message + "\n";
                receivedMessagesArea.appendText(formattedMessage);
                receivedMessagesArea.setScrollTop(Double.MAX_VALUE);
                
                // Clear input
                messageInput.clear();
                
            } catch (Exception e) {
                Logging.error("❌ Error sending message: " + e.getMessage());
                addReceivedMessage("ERROR: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handles clearing all messages.
     */
    @FXML
    private void handleClearMessages() {
        if (receivedMessagesArea != null) {
            receivedMessagesArea.clear();
            Logging.info("🧹 Server Simulator messages cleared");
        }
    }
    
    /**
     * Handles closing the server simulator window.
     */
    @FXML
    private void handleClose() {
        Logging.info("🔒 Server Simulator window closing");
        if (stage != null) {
            stage.close();
        }
    }
    
    /**
     * Gets the root VBox for scene creation.
     */
    public VBox getRoot() {
        return root;
    }
    
    /**
     * Called when the window is about to close.
     */
    public void onClose() {
        Logging.info("🔒 Server Simulator cleanup");
        // Notify GDK application that server simulator is closing
        if (gdkApplication != null) {
            // This could trigger game cleanup if needed
        }
    }
} 
package launcher.ui.lobby.managers.messaging;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Manages message display queue system to prevent message spam.
 * 
 * Messages are queued and displayed one by one every 250ms to ensure
 * all messages are shown while preventing UI spam.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public class MessageManager {
    
    private static final long MESSAGE_INTERVAL_MS = 250;
    
    private final VBox messageContainer;
    private final ScrollPane messageScrollPane;
    
    private final Queue<String> messageQueue = new LinkedList<>();
    private boolean messageTimerRunning = false;
    private boolean isRefreshing = false;
    
    /**
     * Create a new MessageManager.
     * 
     * @param messageContainer The VBox container for messages
     * @param messageScrollPane The ScrollPane containing the message container
     */
    public MessageManager(VBox messageContainer, ScrollPane messageScrollPane) {
        this.messageContainer = messageContainer;
        this.messageScrollPane = messageScrollPane;
    }
    
    /**
     * Set whether the application is currently refreshing (affects message deduplication).
     * 
     * @param isRefreshing true if refreshing, false otherwise
     */
    public void setRefreshing(boolean isRefreshing) {
        this.isRefreshing = isRefreshing;
    }
    
    /**
     * Add a message to the queue for display.
     * 
     * @param userMessage The message to display
     */
    public void addMessage(String userMessage) {
        // If this is a loading animation message and we're already refreshing,
        // replace the last message instead of adding a new one
        if (isRefreshing && userMessage.startsWith("Reload in progress")) {
            // Remove the last message if it's also a loading message
            if (!messageQueue.isEmpty()) {
                String lastMessage = ((LinkedList<String>) messageQueue).peekLast();
                if (lastMessage != null && lastMessage.startsWith("Reload in progress")) {
                    ((LinkedList<String>) messageQueue).removeLast();
                }
            }
        }
        
        // Add message to queue
        messageQueue.offer(userMessage);
        
        // Start the message timer if it's not already running
        if (!messageTimerRunning) {
            startMessageTimer();
        }
    }
    
    /**
     * Clear all messages from the container.
     */
    public void clearMessages() {
        Platform.runLater(() -> messageContainer.getChildren().clear());
    }
    
    /**
     * Start the message timer to process queued messages.
     */
    private void startMessageTimer() {
        messageTimerRunning = true;
        
        // Create a timer that runs every 250ms
        new Thread(() -> {
            while (!messageQueue.isEmpty()) {
                try {
                    Thread.sleep(MESSAGE_INTERVAL_MS);
                    
                    // Process one message from the queue
                    String message = messageQueue.poll();
                    if (message != null) {
                        Platform.runLater(() -> displayMessage(message));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            messageTimerRunning = false;
        }).start();
    }
    
    /**
     * Display a single message to the user interface.
     * 
     * @param userMessage The message to display
     */
    private void displayMessage(String userMessage) {
        // Create a timestamp in HH:mm:ss format for the message
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        // Create a simple text label for regular messages
        Label messageLabel = new Label("[" + timestamp + "] " + userMessage);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("simple-message");
        
        // Add the message to the container
        messageContainer.getChildren().add(messageLabel);
        
        // Auto-scroll to the bottom to show the latest message
        messageScrollPane.setVvalue(1.0);
    }
}


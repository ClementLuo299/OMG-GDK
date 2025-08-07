import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import gdk.Logging;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Startup Progress Window
 * 
 * Shows a progress window during application startup with:
 * - Progress bar showing initialization progress
 * - Real-time messages about what's happening
 * - Clean, modern UI design
 * 
 * @author Clement Luo
 * @since 1.0
 */
public class StartupProgressWindow {
    
    private Stage progressStage;
    private ProgressBar progressBar;
    private Label statusLabel;
    private TextArea messageArea;
    private VBox root;
    
    // Progress tracking
    private AtomicInteger currentStep = new AtomicInteger(0);
    private int totalSteps = 8; // Total initialization steps
    
    // Message queue for thread-safe updates
    private java.util.Queue<String> messageQueue = new java.util.LinkedList<>();
    private boolean messageTimerRunning = false;
    
    /**
     * Initialize the progress window
     */
    public StartupProgressWindow() {
        Logging.info("🚀 Initializing startup progress window");
        createProgressWindow();
    }
    
    /**
     * Create and configure the progress window UI
     */
    private void createProgressWindow() {
        // Create the main stage
        progressStage = new Stage();
        progressStage.setTitle("GDK Game Development Kit - Starting Up");
        progressStage.initStyle(StageStyle.UNDECORATED); // No window decorations
        progressStage.initModality(Modality.APPLICATION_MODAL); // Block other windows
        progressStage.setResizable(false);
        
        // Create UI components
        createUIComponents();
        
        // Set up the scene
        Scene scene = new Scene(root, 600, 400);
        progressStage.setScene(scene);
        
        // Center the window
        progressStage.centerOnScreen();
        
        Logging.info("✅ Startup progress window created");
    }
    
    /**
     * Create and configure UI components
     */
    private void createUIComponents() {
        // Main container
        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e);");
        
        // Title
        Label titleLabel = new Label("GDK Game Development Kit");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: white;");
        
        // Subtitle
        Label subtitleLabel = new Label("Initializing...");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setStyle("-fx-text-fill: #bdc3c7;");
        
        // Progress bar
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(500);
        progressBar.setPrefHeight(20);
        progressBar.setStyle(
            "-fx-accent: #3498db;" +
            "-fx-background-color: #34495e;" +
            "-fx-border-color: #2c3e50;" +
            "-fx-border-width: 1px;"
        );
        
        // Status label
        statusLabel = new Label("Starting up...");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        statusLabel.setStyle("-fx-text-fill: white;");
        statusLabel.setAlignment(Pos.CENTER);
        
        // Message area
        messageArea = new TextArea();
        messageArea.setPrefRowCount(8);
        messageArea.setPrefColumnCount(60);
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setStyle(
            "-fx-control-inner-background: #2c3e50;" +
            "-fx-text-fill: #ecf0f1;" +
            "-fx-font-family: 'Monaco', 'Consolas', monospace;" +
            "-fx-font-size: 11px;"
        );
        
        // Add components to root
        root.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            progressBar,
            statusLabel,
            messageArea
        );
    }
    
    /**
     * Show the progress window
     */
    public void show() {
        Logging.info("🎬 Showing startup progress window");
        Platform.runLater(() -> {
            progressStage.show();
            startMessageTimer();
        });
    }
    
    /**
     * Hide the progress window
     */
    public void hide() {
        Logging.info("🏁 Hiding startup progress window");
        Platform.runLater(() -> {
            progressStage.hide();
            stopMessageTimer();
        });
    }
    
    /**
     * Update progress and status
     * @param step The current step (0 to totalSteps)
     * @param status The status message
     */
    public void updateProgress(int step, String status) {
        Platform.runLater(() -> {
            currentStep.set(step);
            double progress = (double) step / totalSteps;
            progressBar.setProgress(progress);
            statusLabel.setText(status);
            
            // Add message to queue
            addMessage("Step " + step + "/" + totalSteps + ": " + status);
            
            Logging.info("📊 Progress: " + step + "/" + totalSteps + " - " + status);
        });
    }
    
    /**
     * Add a message to the message area
     * @param message The message to add
     */
    public void addMessage(String message) {
        messageQueue.offer(message);
        if (!messageTimerRunning) {
            startMessageTimer();
        }
    }
    
    /**
     * Start the message timer for processing queued messages
     */
    private void startMessageTimer() {
        if (messageTimerRunning) return;
        
        messageTimerRunning = true;
        new Thread(() -> {
            while (messageTimerRunning && !messageQueue.isEmpty()) {
                String message = messageQueue.poll();
                if (message != null) {
                    Platform.runLater(() -> {
                        messageArea.appendText(message + "\n");
                        // Auto-scroll to bottom
                        messageArea.setScrollTop(Double.MAX_VALUE);
                    });
                }
                
                try {
                    Thread.sleep(50); // Small delay between messages
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            messageTimerRunning = false;
        }).start();
    }
    
    /**
     * Stop the message timer
     */
    private void stopMessageTimer() {
        messageTimerRunning = false;
    }
    
    /**
     * Set the total number of steps
     * @param totalSteps The total number of initialization steps
     */
    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }
    
    /**
     * Get the current progress stage
     * @return The progress stage
     */
    public Stage getProgressStage() {
        return progressStage;
    }
} 
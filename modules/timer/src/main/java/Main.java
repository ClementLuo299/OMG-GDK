import gdk.GameModule;
import gdk.Logging;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Map;

public class Main implements GameModule {
    
    private Metadata metadata;
    private Label timeDisplay;
    private Button startButton, pauseButton, resetButton;
    private Spinner<Integer> minutesSpinner, secondsSpinner;
    private Timeline timer;
    private int totalSeconds = 0;
    private int remainingSeconds = 0;
    private boolean isRunning = false;
    
    public Main() {
        this.metadata = new Metadata();
    }
    
    @Override
    public javafx.scene.Scene launchGame(Stage primaryStage) {
        Logging.info("⏰ Timer module launching...");
        
        // Configure the stage
        primaryStage.setTitle("Timer - GDK Test Module");
        primaryStage.setMinWidth(350);
        primaryStage.setMinHeight(400);
        
        // Create the main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);
        
        // Create time display
        timeDisplay = new Label("00:00");
        timeDisplay.setFont(Font.font("Arial", 48));
        timeDisplay.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3;");
        
        // Create time input spinners
        HBox timeInputLayout = new HBox(10);
        timeInputLayout.setAlignment(Pos.CENTER);
        
        minutesSpinner = new Spinner<>(0, 59, 0);
        minutesSpinner.setEditable(true);
        minutesSpinner.setPrefWidth(80);
        minutesSpinner.setStyle("-fx-font-size: 18px;");
        
        secondsSpinner = new Spinner<>(0, 59, 0);
        secondsSpinner.setEditable(true);
        secondsSpinner.setPrefWidth(80);
        secondsSpinner.setStyle("-fx-font-size: 18px;");
        
        Label minutesLabel = new Label("min");
        Label secondsLabel = new Label("sec");
        minutesLabel.setStyle("-fx-font-size: 14px;");
        secondsLabel.setStyle("-fx-font-size: 14px;");
        
        timeInputLayout.getChildren().addAll(
            minutesSpinner, minutesLabel,
            secondsSpinner, secondsLabel
        );
        
        // Create control buttons
        HBox buttonLayout = new HBox(10);
        buttonLayout.setAlignment(Pos.CENTER);
        
        startButton = new Button("▶️ Start");
        startButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        startButton.setOnAction(e -> startTimer());
        
        pauseButton = new Button("⏸️ Pause");
        pauseButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px; -fx-background-color: #FF9800; -fx-text-fill: white;");
        pauseButton.setDisable(true);
        pauseButton.setOnAction(e -> pauseTimer());
        
        resetButton = new Button("🔄 Reset");
        resetButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px; -fx-background-color: #F44336; -fx-text-fill: white;");
        resetButton.setOnAction(e -> resetTimer());
        
        buttonLayout.getChildren().addAll(startButton, pauseButton, resetButton);
        
        // Create quick preset buttons
        HBox presetLayout = new HBox(10);
        presetLayout.setAlignment(Pos.CENTER);
        
        Button preset1 = new Button("1 min");
        Button preset2 = new Button("5 min");
        Button preset3 = new Button("10 min");
        
        preset1.setOnAction(e -> setPresetTime(1, 0));
        preset2.setOnAction(e -> setPresetTime(5, 0));
        preset3.setOnAction(e -> setPresetTime(10, 0));
        
        presetLayout.getChildren().addAll(preset1, preset2, preset3);
        
        // Add components to main layout
        mainLayout.getChildren().addAll(
            timeDisplay,
            timeInputLayout,
            buttonLayout,
            presetLayout
        );
        
        // Initialize timer
        initializeTimer();
        
        // Create and return the scene
        Scene scene = new Scene(mainLayout);
        Logging.info("✅ Timer interface created successfully");
        return scene;
    }
    
    private void initializeTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (remainingSeconds > 0) {
                remainingSeconds--;
                updateTimeDisplay();
                
                if (remainingSeconds == 0) {
                    timerComplete();
                }
            }
        }));
        timer.setCycleCount(Animation.INDEFINITE);
    }
    
    private void startTimer() {
        if (!isRunning) {
            if (remainingSeconds == 0) {
                // Set time from spinners
                int minutes = minutesSpinner.getValue();
                int seconds = secondsSpinner.getValue();
                totalSeconds = minutes * 60 + seconds;
                remainingSeconds = totalSeconds;
                
                if (totalSeconds == 0) {
                    Logging.info("⚠️ Timer: Cannot start with 0 seconds");
                    return;
                }
            }
            
            timer.play();
            isRunning = true;
            startButton.setDisable(true);
            pauseButton.setDisable(false);
            minutesSpinner.setDisable(true);
            secondsSpinner.setDisable(true);
            
            Logging.info("▶️ Timer started: " + totalSeconds + " seconds");
        }
    }
    
    private void pauseTimer() {
        if (isRunning) {
            timer.pause();
            isRunning = false;
            startButton.setDisable(false);
            pauseButton.setDisable(true);
            
            Logging.info("⏸️ Timer paused at: " + remainingSeconds + " seconds remaining");
        }
    }
    
    private void resetTimer() {
        timer.stop();
        isRunning = false;
        remainingSeconds = 0;
        totalSeconds = 0;
        
        startButton.setDisable(false);
        pauseButton.setDisable(true);
        minutesSpinner.setDisable(false);
        secondsSpinner.setDisable(false);
        
        updateTimeDisplay();
        
        Logging.info("🔄 Timer reset");
    }
    
    private void setPresetTime(int minutes, int seconds) {
        minutesSpinner.getValueFactory().setValue(minutes);
        secondsSpinner.getValueFactory().setValue(seconds);
        Logging.info("⏰ Timer preset set: " + minutes + "m " + seconds + "s");
    }
    
    private void updateTimeDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timeDisplay.setText(String.format("%02d:%02d", minutes, seconds));
        
        // Change color based on time remaining
        if (remainingSeconds <= 10) {
            timeDisplay.setStyle("-fx-font-weight: bold; -fx-text-fill: #F44336;"); // Red
        } else if (remainingSeconds <= 30) {
            timeDisplay.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF9800;"); // Orange
        } else {
            timeDisplay.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3;"); // Blue
        }
    }
    
    private void timerComplete() {
        timer.stop();
        isRunning = false;
        startButton.setDisable(false);
        pauseButton.setDisable(true);
        minutesSpinner.setDisable(false);
        secondsSpinner.setDisable(false);
        
        timeDisplay.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;"); // Green
        timeDisplay.setText("DONE!");
        
        Logging.info("⏰ Timer completed!");
        
        // Show completion alert
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Timer Complete!");
            alert.setHeaderText("Time's up!");
            alert.setContentText("Your timer of " + formatTime(totalSeconds) + " has completed.");
            alert.showAndWait();
        });
    }
    
    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + 
                   (seconds > 0 ? " and " + seconds + " second" + (seconds > 1 ? "s" : "") : "");
        } else {
            return seconds + " second" + (seconds > 1 ? "s" : "");
        }
    }
    
    @Override
    public Map<String, Object> handleMessage(Map<String, Object> message) {
        if (message == null) return null;
        
        String function = String.valueOf(message.get("function"));
        Logging.info("📨 Timer received message: " + function);
        
        if ("start".equals(function)) {
            Logging.info("✅ Timer start message acknowledged");
            return Map.of("status", "ok", "message", "Timer started successfully");
        } else if ("end".equals(function)) {
            Logging.info("🏁 Timer end message received");
            if (isRunning) {
                pauseTimer();
            }
            return Map.of("status", "ok", "message", "Timer ending");
        }
        
        return Map.of("status", "ok", "message", "Timer message processed");
    }
    
    @Override
    public void stopGame() {
        Logging.info("🔄 Timer closing - cleaning up resources");
        if (timer != null) {
            timer.stop();
        }
    }
    
    @Override
    public Metadata getMetadata() {
        return metadata;
    }
} 
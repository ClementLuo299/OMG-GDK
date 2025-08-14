import gdk.GameModule;
import gdk.Logging;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.util.Map;

public class Main implements GameModule {
    
    private Metadata metadata;
    private Rectangle colorDisplay;
    private Label colorInfoLabel;
    private Slider redSlider, greenSlider, blueSlider;
    private Label redLabel, greenLabel, blueLabel;
    
    public Main() {
        this.metadata = new Metadata();
    }
    
    @Override
    public javafx.scene.Scene launchGame(Stage primaryStage) {
        Logging.info("🎨 Color Picker module launching...");
        
        // Configure the stage
        primaryStage.setTitle("Color Picker - GDK Test Module");
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(500);
        
        // Create the main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);
        
        // Create color display
        colorDisplay = new Rectangle(200, 100);
        colorDisplay.setFill(Color.WHITE);
        colorDisplay.setStroke(Color.BLACK);
        colorDisplay.setStrokeWidth(2);
        
        // Create color info label
        colorInfoLabel = new Label("RGB: 255, 255, 255");
        colorInfoLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Create sliders
        redSlider = createColorSlider("Red", 255);
        greenSlider = createColorSlider("Green", 255);
        blueSlider = createColorSlider("Blue", 255);
        
        // Create labels for sliders
        redLabel = new Label("Red: 255");
        greenLabel = new Label("Green: 255");
        blueLabel = new Label("Blue: 255");
        
        // Create random color button
        Button randomButton = new Button("🎲 Random Color");
        randomButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");
        randomButton.setOnAction(e -> generateRandomColor());
        
        // Create copy button
        Button copyButton = new Button("📋 Copy RGB Values");
        copyButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");
        copyButton.setOnAction(e -> copyRGBValues());
        
        // Add components to main layout
        mainLayout.getChildren().addAll(
            colorDisplay,
            colorInfoLabel,
            redSlider, redLabel,
            greenSlider, greenLabel,
            blueSlider, blueLabel,
            randomButton,
            copyButton
        );
        
        // Set up slider change listeners
        setupSliderListeners();
        
        // Create and return the scene
        Scene scene = new Scene(mainLayout);
        Logging.info("✅ Color Picker interface created successfully");
        return scene;
    }
    
    private Slider createColorSlider(String colorName, int defaultValue) {
        Slider slider = new Slider(0, 255, defaultValue);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(64);
        slider.setMinorTickCount(15);
        slider.setPrefWidth(300);
        
        // Style the slider based on color
        String style = "-fx-control-inner-background: ";
        switch (colorName.toLowerCase()) {
            case "red":
                style += "linear-gradient(to right, #000000, #ff0000);";
                break;
            case "green":
                style += "linear-gradient(to right, #000000, #00ff00);";
                break;
            case "blue":
                style += "linear-gradient(to right, #000000, #0000ff);";
                break;
        }
        slider.setStyle(style);
        
        return slider;
    }
    
    private void setupSliderListeners() {
        redSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            redLabel.setText("Red: " + value);
            updateColor();
        });
        
        greenSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            greenLabel.setText("Green: " + value);
            updateColor();
        });
        
        blueSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            blueLabel.setText("Blue: " + value);
            updateColor();
        });
    }
    
    private void updateColor() {
        int red = (int) redSlider.getValue();
        int green = (int) greenSlider.getValue();
        int blue = (int) blueSlider.getValue();
        
        Color color = Color.rgb(red, green, blue);
        colorDisplay.setFill(color);
        colorInfoLabel.setText(String.format("RGB: %d, %d, %d", red, green, blue));
        
        Logging.info("🎨 Color updated: RGB(" + red + ", " + green + ", " + blue + ")");
    }
    
    private void generateRandomColor() {
        int red = (int) (Math.random() * 256);
        int green = (int) (Math.random() * 256);
        int blue = (int) (Math.random() * 256);
        
        redSlider.setValue(red);
        greenSlider.setValue(green);
        blueSlider.setValue(blue);
        
        Logging.info("🎲 Random color generated: RGB(" + red + ", " + green + ", " + blue + ")");
    }
    
    private void copyRGBValues() {
        int red = (int) redSlider.getValue();
        int green = (int) greenSlider.getValue();
        int blue = (int) blueSlider.getValue();
        
        String rgbText = String.format("%d, %d, %d", red, green, blue);
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(rgbText);
        clipboard.setContent(content);
        
        Logging.info("📋 RGB values copied to clipboard: " + rgbText);
    }
    
    @Override
    public Map<String, Object> handleMessage(Map<String, Object> message) {
        if (message == null) return null;
        
        String function = String.valueOf(message.get("function"));
        Logging.info("📨 Color Picker received message: " + function);
        
        if ("start".equals(function)) {
            Logging.info("✅ Color Picker start message acknowledged");
            return Map.of("status", "ok", "message", "Color Picker started successfully");
        } else if ("end".equals(function)) {
            Logging.info("🏁 Color Picker end message received");
            return Map.of("status", "ok", "message", "Color Picker ending");
        }
        
        return Map.of("status", "ok", "message", "Color Picker message processed");
    }
    
    @Override
    public void stopGame() {
        Logging.info("🔄 Color Picker closing - cleaning up resources");
    }
    
    @Override
    public Metadata getMetadata() {
        return metadata;
    }
} 
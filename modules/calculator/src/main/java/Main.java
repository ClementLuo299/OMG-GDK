import gdk.GameModule;
import gdk.Logging;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.util.Map;

public class Main implements GameModule {
    
    private Metadata metadata;
    private TextField displayField;
    private double currentValue = 0;
    private String currentOperation = "";
    private boolean newNumber = true;
    
    public Main() {
        this.metadata = new Metadata();
    }
    
    @Override
    public javafx.scene.Scene launchGame(Stage primaryStage) {
        Logging.info("🧮 Calculator module launching...");
        
        // Configure the stage
        primaryStage.setTitle("Calculator - GDK Test Module");
        primaryStage.setMinWidth(300);
        primaryStage.setMinHeight(400);
        
        // Create the main layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);
        
        // Create display field
        displayField = new TextField("0");
        displayField.setEditable(false);
        displayField.setAlignment(Pos.CENTER_RIGHT);
        displayField.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        displayField.setPrefHeight(50);
        
        // Create button grid
        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(5);
        buttonGrid.setVgap(5);
        
        // Calculator buttons
        String[][] buttonLabels = {
            {"C", "±", "%", "÷"},
            {"7", "8", "9", "×"},
            {"4", "5", "6", "-"},
            {"1", "2", "3", "+"},
            {"0", ".", "="}
        };
        
        for (int row = 0; row < buttonLabels.length; row++) {
            for (int col = 0; col < buttonLabels[row].length; col++) {
                String label = buttonLabels[row][col];
                Button button = createButton(label);
                
                if (label.equals("=")) {
                    buttonGrid.add(button, col, row, 2, 1); // Span 2 columns
                } else {
                    buttonGrid.add(button, col, row);
                }
            }
        }
        
        // Add components to main layout
        mainLayout.getChildren().addAll(displayField, buttonGrid);
        
        // Create and return the scene
        Scene scene = new Scene(mainLayout);
        Logging.info("✅ Calculator interface created successfully");
        return scene;
    }
    
    private Button createButton(String text) {
        Button button = new Button(text);
        button.setPrefSize(60, 50);
        button.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Style based on button type
        if (text.matches("[0-9.]")) {
            button.setStyle(button.getStyle() + "; -fx-background-color: #f0f0f0;");
        } else if (text.equals("=")) {
            button.setStyle(button.getStyle() + "; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        } else {
            button.setStyle(button.getStyle() + "; -fx-background-color: #FF9800; -fx-text-fill: white;");
        }
        
        button.setOnAction(e -> handleButtonClick(text));
        return button;
    }
    
    private void handleButtonClick(String buttonText) {
        Logging.info("🔘 Calculator button clicked: " + buttonText);
        
        if (buttonText.matches("[0-9]")) {
            if (newNumber) {
                displayField.setText(buttonText);
                newNumber = false;
            } else {
                displayField.setText(displayField.getText() + buttonText);
            }
        } else if (buttonText.equals(".")) {
            if (!displayField.getText().contains(".")) {
                displayField.setText(displayField.getText() + ".");
                newNumber = false;
            }
        } else if (buttonText.equals("C")) {
            displayField.setText("0");
            currentValue = 0;
            currentOperation = "";
            newNumber = true;
        } else if (buttonText.equals("±")) {
            double value = Double.parseDouble(displayField.getText());
            displayField.setText(String.valueOf(-value));
        } else if (buttonText.equals("%")) {
            double value = Double.parseDouble(displayField.getText());
            displayField.setText(String.valueOf(value / 100));
        } else if (buttonText.matches("[+\\-×÷]")) {
            currentValue = Double.parseDouble(displayField.getText());
            currentOperation = buttonText;
            newNumber = true;
        } else if (buttonText.equals("=")) {
            if (!currentOperation.isEmpty()) {
                double secondValue = Double.parseDouble(displayField.getText());
                double result = calculate(currentValue, secondValue, currentOperation);
                displayField.setText(String.valueOf(result));
                currentOperation = "";
                newNumber = true;
            }
        }
    }
    
    private double calculate(double a, double b, String operation) {
        switch (operation) {
            case "+": return a + b;
            case "-": return a - b;
            case "×": return a * b;
            case "÷": return b != 0 ? a / b : 0;
            default: return b;
        }
    }
    
    @Override
    public Map<String, Object> handleMessage(Map<String, Object> message) {
        if (message == null) return null;
        
        String function = String.valueOf(message.get("function"));
        Logging.info("📨 Calculator received message: " + function);
        
        if ("start".equals(function)) {
            Logging.info("✅ Calculator start message acknowledged");
            return Map.of("status", "ok", "message", "Calculator started successfully");
        } else if ("end".equals(function)) {
            Logging.info("🏁 Calculator end message received");
            return Map.of("status", "ok", "message", "Calculator ending");
        }
        
        return Map.of("status", "ok", "message", "Calculator message processed");
    }
    
    @Override
    public void stopGame() {
        Logging.info("🔄 Calculator closing - cleaning up resources");
    }
    
    @Override
    public Metadata getMetadata() {
        return metadata;
    }
} 
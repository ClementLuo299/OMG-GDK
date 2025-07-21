package com;

import com.gdk.shared.settings.GameSetting;
import com.gdk.shared.settings.GameSettings;
import com.gdk.shared.utils.error_handling.Logging;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

/**
 * Dialog for configuring custom game settings.
 * Allows users to configure game-specific settings before launching a game.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @since 1.0
 */
public class GameSettingsDialog {
    
    private final Stage dialog;
    private final GameSettings gameSettings;
    private final List<GameSetting<?>> settings;
    private boolean settingsValid = true;
    
    /**
     * Creates a new game settings dialog.
     * 
     * @param parentStage The parent stage
     * @param gameSettings The game settings to configure
     */
    public GameSettingsDialog(Stage parentStage, GameSettings gameSettings) {
        this.gameSettings = gameSettings;
        this.settings = gameSettings.getCustomSettings();
        
        // Create the dialog
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle(gameSettings.getSettingsDisplayName());
        dialog.setResizable(false);
        
        // Create the UI
        VBox root = createUI();
        Scene scene = new Scene(root);
        dialog.setScene(scene);
        
        // Set dialog size
        dialog.setMinWidth(500);
        dialog.setMinHeight(400);
    }
    
    /**
     * Creates the UI for the settings dialog.
     * @return The root VBox
     */
    private VBox createUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        
        // Title and description
        Label titleLabel = new Label(gameSettings.getSettingsDisplayName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label descriptionLabel = new Label(gameSettings.getSettingsDescription());
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        descriptionLabel.setWrapText(true);
        
        // Settings grid
        GridPane settingsGrid = createSettingsGrid();
        
        // Buttons
        HBox buttonBox = createButtonBox();
        
        root.getChildren().addAll(titleLabel, descriptionLabel, settingsGrid, buttonBox);
        
        return root;
    }
    
    /**
     * Creates the settings grid.
     * @return The settings grid
     */
    private GridPane createSettingsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.TOP_LEFT);
        
        int row = 0;
        for (GameSetting<?> setting : settings) {
            // Setting label
            Label label = new Label(setting.getDisplayName() + ":");
            if (setting.isRequired()) {
                label.setText(setting.getDisplayName() + " *:");
                label.setStyle("-fx-font-weight: bold;");
            }
            grid.add(label, 0, row);
            
            // Setting control
            Control control = setting.createControl();
            grid.add(control, 1, row);
            
            // Validation label
            Label validationLabel = new Label();
            validationLabel.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
            grid.add(validationLabel, 2, row);
            
            // Update validation when control changes
            control.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) { // Lost focus
                    updateValidation(setting, control, validationLabel);
                }
            });
            
            row++;
        }
        
        return grid;
    }
    
    /**
     * Creates the button box.
     * @return The button box
     */
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button resetButton = new Button("Reset to Defaults");
        resetButton.setOnAction(e -> resetToDefaults());
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> dialog.close());
        
        Button okButton = new Button("OK");
        okButton.setDefaultButton(true);
        okButton.setOnAction(e -> validateAndClose());
        
        buttonBox.getChildren().addAll(resetButton, cancelButton, okButton);
        
        return buttonBox;
    }
    
    /**
     * Updates validation for a setting.
     * @param setting The setting to validate
     * @param control The control to read from
     * @param validationLabel The label to show validation errors
     */
    private void updateValidation(GameSetting<?> setting, Control control, Label validationLabel) {
        // Update the setting value from the control
        setting.updateFromControl(control);
        
        // Check validation
        if (!setting.isValid()) {
            validationLabel.setText(setting.getValidationError());
            settingsValid = false;
        } else {
            validationLabel.setText("");
        }
    }
    
    /**
     * Resets all settings to their default values.
     */
    private void resetToDefaults() {
        gameSettings.resetToDefaults();
        
        // Refresh the UI
        dialog.getScene().setRoot(createUI());
        settingsValid = true;
        
        Logging.info("🔄 Reset game settings to defaults");
    }
    
    /**
     * Validates all settings and closes the dialog if valid.
     */
    private void validateAndClose() {
        // Validate all settings
        if (gameSettings.validateSettings()) {
            Logging.info("✅ Game settings validated successfully");
            dialog.close();
        } else {
            // Show validation errors
            List<String> errors = gameSettings.getValidationErrors();
            StringBuilder errorMessage = new StringBuilder("Please fix the following errors:\n\n");
            for (String error : errors) {
                errorMessage.append("• ").append(error).append("\n");
            }
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Invalid Settings");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            
            Logging.warning("❌ Game settings validation failed: " + errors.size() + " errors");
        }
    }
    
    /**
     * Shows the dialog and waits for user input.
     * @return true if the user clicked OK and settings are valid
     */
    public boolean showAndWait() {
        dialog.showAndWait();
        return settingsValid && gameSettings.validateSettings();
    }
    
    /**
     * Gets the configured game settings.
     * @return The game settings
     */
    public GameSettings getGameSettings() {
        return gameSettings;
    }
} 
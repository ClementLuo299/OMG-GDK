package launcher.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.application.Platform;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Utility class for displaying various types of dialogs throughout the application.
 * 
 * This class provides a centralized way to show error, warning, information,
 * confirmation, and exception dialogs with consistent styling and behavior.
 * 
 * @author Clement Luo
 * @date August 6, 2025
 * @edited August 6, 2025
 * @since 1.0
 */
public class DialogUtil {
    
    // ==================== ERROR DIALOGS ====================
    
    /**
     * Display an error dialog with title and message.
     * 
     * @param title The dialog title
     * @param message The error message
     */
    public static void showError(String title, String message) {
        showError(title, null, message);
    }
    
    /**
     * Display an error dialog with title, header, and message.
     * 
     * @param title The dialog title
     * @param header The header text (can be null)
     * @param message The error message
     */
    public static void showError(String title, String header, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * Display an error dialog for exceptions with stack trace.
     * 
     * @param title The dialog title
     * @param header The header text (can be null)
     * @param message The error message
     * @param exception The exception to display
     */
    public static void showError(String title, String header, String message, Exception exception) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(message);
            
            // Create expandable exception details
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            String exceptionText = sw.toString();
            
            Label label = new Label("The exception stacktrace was:");
            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            
            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);
            
            alert.getDialogPane().setExpandableContent(expContent);
            alert.showAndWait();
        });
    }
    
    // ==================== WARNING DIALOGS ====================
    
    /**
     * Display a warning dialog with title and message.
     * 
     * @param title The dialog title
     * @param message The warning message
     */
    public static void showWarning(String title, String message) {
        showWarning(title, null, message);
    }
    
    /**
     * Display a warning dialog with title, header, and message.
     * 
     * @param title The dialog title
     * @param header The header text (can be null)
     * @param message The warning message
     */
    public static void showWarning(String title, String header, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    // ==================== INFORMATION DIALOGS ====================
    
    /**
     * Display an information dialog with title and message.
     * 
     * @param title The dialog title
     * @param message The information message
     */
    public static void showInfo(String title, String message) {
        showInfo(title, null, message);
    }
    
    /**
     * Display an information dialog with title, header, and message.
     * 
     * @param title The dialog title
     * @param header The header text (can be null)
     * @param message The information message
     */
    public static void showInfo(String title, String header, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    // ==================== CONFIRMATION DIALOGS ====================
    
    /**
     * Display a confirmation dialog with title and message.
     * 
     * @param title The dialog title
     * @param message The confirmation message
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        return showConfirmation(title, null, message);
    }
    
    /**
     * Display a confirmation dialog with title, header, and message.
     * 
     * @param title The dialog title
     * @param header The header text (can be null)
     * @param message The confirmation message
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String header, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Display a confirmation dialog with custom button text.
     * 
     * @param title The dialog title
     * @param header The header text (can be null)
     * @param message The confirmation message
     * @param confirmButtonText Text for the confirm button
     * @param cancelButtonText Text for the cancel button
     * @return true if user clicked confirm, false otherwise
     */
    public static boolean showConfirmation(String title, String header, String message, 
                                         String confirmButtonText, String cancelButtonText) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        
        ButtonType confirmButton = new ButtonType(confirmButtonText);
        ButtonType cancelButton = new ButtonType(cancelButtonText);
        alert.getButtonTypes().setAll(confirmButton, cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmButton;
    }
    
    // ==================== CUSTOM DIALOGS ====================
    
    /**
     * Display a custom dialog with the specified content.
     * 
     * @param title The dialog title
     * @param content The dialog content
     * @return The dialog instance for further customization
     */
    public static Dialog<Void> showCustomDialog(String title, javafx.scene.Node content) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setContentText(null);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
        return dialog;
    }
    
    /**
     * Display a dialog with expandable content.
     * 
     * @param title The dialog title
     * @param header The header text (can be null)
     * @param content The main content
     * @param expandableContent The expandable content
     * @param alertType The type of alert
     */
    public static void showExpandableDialog(String title, String header, String content, 
                                          javafx.scene.Node expandableContent, AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.getDialogPane().setExpandableContent(expandableContent);
            alert.showAndWait();
        });
    }
    
    // ==================== APPLICATION-SPECIFIC DIALOGS ====================
    
    /**
     * Display a startup error dialog (used by GDKApplication).
     * 
     * @param dialogTitle The dialog title
     * @param headerText The header text (can be null)
     * @param contentText The error message
     */
    public static void showStartupError(String dialogTitle, String headerText, String contentText) {
        showError(dialogTitle, headerText, contentText);
    }
    
    /**
     * Display a fatal application error dialog.
     * 
     * @param title The dialog title
     * @param header The header text
     * @param message The error message
     */
    public static void showFatalError(String title, String header, String message) {
        showError(title, header, message);
    }
    
    /**
     * Display a game-related error dialog.
     * 
     * @param title The dialog title
     * @param message The error message
     */
    public static void showGameError(String title, String message) {
        showError(title, message);
    }
    
    /**
     * Display a JSON validation error dialog.
     * 
     * @param title The dialog title
     * @param message The error message
     */
    public static void showJsonError(String title, String message) {
        showError(title, message);
    }
    
    /**
     * Display a module compilation error dialog.
     * 
     * @param title The dialog title
     * @param message The error message
     */
    public static void showCompilationError(String title, String message) {
        showError(title, message);
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Ensure dialog is shown on the JavaFX Application Thread.
     * 
     * @param runnable The dialog display code
     */
    public static void runOnFXThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }
    
    /**
     * Create a styled dialog pane with consistent appearance.
     * 
     * @param alert The alert to style
     */
    public static void styleDialogPane(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            DialogUtil.class.getResource("/gdk-lobby/gdk-lobby.css").toExternalForm()
        );
    }
} 
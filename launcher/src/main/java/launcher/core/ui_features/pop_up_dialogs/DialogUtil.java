package launcher.core.ui_features.pop_up_dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.application.Platform;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Utility class for displaying various types of pop_up_dialogs throughout the application.
 * 
 * <p>This class has a single responsibility: providing a centralized way to show
 * error, warning, information, confirmation, and exception pop_up_dialogs with consistent
 * styling_theme and behavior.
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Displaying error pop_up_dialogs (with optional exception details)</li>
 *   <li>Displaying warning pop_up_dialogs</li>
 *   <li>Displaying information pop_up_dialogs</li>
 *   <li>Displaying confirmation pop_up_dialogs (with custom button text support)</li>
 *   <li>Displaying custom and expandable pop_up_dialogs</li>
 *   <li>Application-specific dialog helpers</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date August 6, 2025
 * @edited August 6, 2025
 * @since 1.0
 */
public class DialogUtil {
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private DialogUtil() {
        throw new AssertionError("DialogUtil should not be instantiated");
    }
    
    // ==================== ERROR DIALOGS ====================
    
    /**
     * Displays an error dialog with title and message.
     * 
     * <p>This method shows a simple error dialog with just a title and message.
     * The dialog is displayed on the JavaFX Application Thread.
     * 
     * @param title The dialog title
     * @param message The error message
     */
    public static void showError(String title, String message) {
        showError(title, null, message);
    }
    
    /**
     * Displays an error dialog with title, header, and message.
     * 
     * <p>This method shows an error dialog with optional header text.
     * The dialog is displayed on the JavaFX Application Thread.
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
     * Displays an error dialog for exceptions with stack trace.
     * 
     * <p>This method shows an error dialog with expandable exception details.
     * The stack trace is displayed in an expandable content area for debugging.
     * The dialog is displayed on the JavaFX Application Thread.
     * 
     * @param title The dialog title
     * @param header The header text (can be null)
     * @param message The error message
     * @param exception The exception to display (stack trace will be shown)
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
     * Displays a warning dialog with title and message.
     * 
     * <p>This method shows a simple warning dialog with just a title and message.
     * The dialog is displayed on the JavaFX Application Thread.
     * 
     * @param title The dialog title
     * @param message The warning message
     */
    public static void showWarning(String title, String message) {
        showWarning(title, null, message);
    }
    
    /**
     * Displays a warning dialog with title, header, and message.
     * 
     * <p>This method shows a warning dialog with optional header text.
     * The dialog is displayed on the JavaFX Application Thread.
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
     * Displays an information dialog with title and message.
     * 
     * <p>This method shows a simple information dialog with just a title and message.
     * The dialog is displayed on the JavaFX Application Thread.
     * 
     * @param title The dialog title
     * @param message The information message
     */
    public static void showInfo(String title, String message) {
        showInfo(title, null, message);
    }
    
    /**
     * Displays an information dialog with title, header, and message.
     * 
     * <p>This method shows an information dialog with optional header text.
     * The dialog is displayed on the JavaFX Application Thread.
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
     * Displays a confirmation dialog with title and message.
     * 
     * <p>This method shows a simple confirmation dialog with standard OK/Cancel buttons.
     * 
     * @param title The dialog title
     * @param message The confirmation message
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        return showConfirmation(title, null, message);
    }
    
    /**
     * Displays a confirmation dialog with title, header, and message.
     * 
     * <p>This method shows a confirmation dialog with optional header text
     * and standard OK/Cancel buttons.
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
     * Displays a confirmation dialog with custom button text.
     * 
     * <p>This method shows a confirmation dialog with custom button labels
     * instead of the standard OK/Cancel buttons.
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
     * Displays a custom dialog with the specified content.
     * 
     * <p>This method creates and shows a custom dialog with user-provided content.
     * The dialog includes a standard OK button and can be further customized
     * using the returned Dialog instance.
     * 
     * @param title The dialog title
     * @param content The dialog content (JavaFX Node)
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
     * Displays a dialog with expandable content.
     * 
     * <p>This method creates a dialog with both main content and expandable
     * content that can be shown/hidden by the user. The dialog is displayed
     * on the JavaFX Application Thread.
     * 
     * @param title The dialog title
     * @param header The header text (can be null)
     * @param content The main content text
     * @param expandableContent The expandable content (JavaFX Node)
     * @param alertType The type of alert (ERROR, WARNING, INFORMATION, etc.)
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
     * Displays a startup error dialog (used by GDKApplication).
     * 
     * <p>This is a convenience method for showing errors during application startup.
     * 
     * @param dialogTitle The dialog title
     * @param headerText The header text (can be null)
     * @param contentText The error message
     */
    public static void showStartupError(String dialogTitle, String headerText, String contentText) {
        showError(dialogTitle, headerText, contentText);
    }
    
    /**
     * Displays a fatal application error dialog.
     * 
     * <p>This is a convenience method for showing fatal errors that require
     * application termination.
     * 
     * @param title The dialog title
     * @param header The header text
     * @param message The error message
     */
    public static void showFatalError(String title, String header, String message) {
        showError(title, header, message);
    }
    
    /**
     * Displays a game-related error dialog.
     * 
     * <p>This is a convenience method for showing errors related to game operations.
     * 
     * @param title The dialog title
     * @param message The error message
     */
    public static void showGameError(String title, String message) {
        showError(title, message);
    }
    
    /**
     * Displays a JSON module_code_validation error dialog.
     * 
     * <p>This is a convenience method for showing errors related to JSON module_code_validation.
     * 
     * @param title The dialog title
     * @param message The error message
     */
    public static void showJsonError(String title, String message) {
        showError(title, message);
    }
    
    /**
     * Displays a module compilation error dialog.
     * 
     * <p>This is a convenience method for showing errors related to module compilation.
     * 
     * @param title The dialog title
     * @param message The error message
     */
    public static void showCompilationError(String title, String message) {
        showError(title, message);
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Ensures dialog is shown on the JavaFX Application Thread.
     * 
     * <p>This method checks if the current helpers is the JavaFX Application Thread.
     * If not, it schedules the runnable to run on that helpers using Platform.runLater().
     * 
     * @param runnable The dialog display code to execute
     */
    public static void runOnFXThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }
    
    /**
     * Creates a styled dialog pane with consistent appearance.
     * 
     * <p>This method applies the application's CSS stylesheet to the dialog pane
     * to ensure consistent appearance across all pop_up_dialogs.
     * 
     * @param alert The alert to style
     */
    public static void styleDialogPane(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            DialogUtil.class.getResource("/lobby/gdk-lobby.css").toExternalForm()
        );
    }
}


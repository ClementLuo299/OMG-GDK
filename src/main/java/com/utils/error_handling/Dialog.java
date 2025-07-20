package com.utils.error_handling;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonBar;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dialog utility for the application.
 * Provides methods for displaying dialogs to the user.
 * 
 * @authors Clement Luo
 * @date June 22, 2025
 * @edited June 24, 2025
 * @since 1.0
 * @version 1.0
 */
public final class Dialog {

    /**
     * Tells the dialog utility that the application is shutting down.
     * Stops the application from showing dialogs during shutdown.
     */
    private static final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Dialog() { throw new UnsupportedOperationException("Utility class cannot be instantiated"); }

    //region ==================== ALERT DIALOGS ====================

    /**
     * Shows a warning dialog with the specified parameters.
     * Handles thread safety by ensuring the dialog is shown on the JavaFX Application Thread.
     *
     * @param title the dialog title
     * @param message the warning message
     * @param throwable the throwable to display (can be null)
     */
    public static void showWarning(String title, String message, Throwable throwable) {
        if (isShuttingDown.get()) {
            Logging.warning("Ignoring warning dialog during shutdown: " + message);
            return;
        }
        showAlert(title, message, throwable, Alert.AlertType.WARNING, false);
    }

    /**
     * Shows a compact warning dialog with the specified parameters.
     * Handles thread safety by ensuring the dialog is shown on the JavaFX Application Thread.
     *
     * @param title the dialog title
     * @param message the warning message
     * @param throwable the throwable to display (can be null)
     */
    public static void showWarningCompact(String title, String message, Throwable throwable) {
        if (isShuttingDown.get()) {
            Logging.warning("Ignoring warning dialog during shutdown: " + message);
            return;
        }
        showAlert(title, message, throwable, Alert.AlertType.WARNING, true);
    }

    /**
     * Shows an error dialog with the specified parameters.
     * Handles thread safety by ensuring the dialog is shown on the JavaFX Application Thread.
     *
     * @param title the dialog title
     * @param message the error message
     * @param throwable the throwable to display (can be null)
     */
    public static void showError(String title, String message, Throwable throwable) {
        if (isShuttingDown.get()) {
            Logging.warning("Ignoring error dialog during shutdown: " + message);
            return;
        }
        showAlert(title, message, throwable, Alert.AlertType.ERROR, false);
    }

    /**
     * Shows a compact error dialog with the specified parameters.
     * Handles thread safety by ensuring the dialog is shown on the JavaFX Application Thread.
     *
     * @param title the dialog title
     * @param message the error message
     * @param throwable the throwable to display (can be null)
     */
    public static void showErrorCompact(String title, String message, Throwable throwable) {
        if (isShuttingDown.get()) {
            Logging.warning("Ignoring error dialog during shutdown: " + message);
            return;
        }
        showAlert(title, message, throwable, Alert.AlertType.ERROR, true);
    }

    /**
     * Shows an error dialog and exits the application after the user acknowledges it.
     * Handles thread safety by ensuring the dialog is shown on the JavaFX Application Thread.
     *
     * @param title the dialog title
     * @param message the error message
     * @param throwable the throwable to display (can be null)
     */
    public static void showErrorAndExit(String title, String message, Throwable throwable) {
        showAlertAndExit(title, message, throwable);
    }

    /**
     * Shows an information dialog with the specified parameters.
     * Handles thread safety by ensuring the dialog is shown on the JavaFX Application Thread.
     *
     * @param title the dialog title
     * @param message the information message
     */
    public static void showInfo(String title, String message) {
        if (isShuttingDown.get()) {
            Logging.warning("Ignoring info dialog during shutdown: " + message);
            return;
        }
        showAlert(title, message, null, Alert.AlertType.INFORMATION, false);
    }

    /**
     * Shows a compact error dialog with just a message (no throwable).
     * Handles thread safety by ensuring the dialog is shown on the JavaFX Application Thread.
     *
     * @param title the dialog title
     * @param message the error message
     */
    public static void showErrorCompact(String title, String message) {
        if (isShuttingDown.get()) {
            Logging.warning("Ignoring error dialog during shutdown: " + message);
            return;
        }
        
        // Check if already on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            showCompactAlertOnFxThread(title, message, Alert.AlertType.ERROR);
        } else {
            // Schedule dialog to run on JavaFX thread
            Platform.runLater(() -> showCompactAlertOnFxThread(title, message, Alert.AlertType.ERROR));
        }
    }

    /**
     * Shows a compact warning dialog with just a message (no throwable).
     * Handles thread safety by ensuring the dialog is shown on the JavaFX Application Thread.
     *
     * @param title the dialog title
     * @param message the warning message
     */
    public static void showWarningCompact(String title, String message) {
        if (isShuttingDown.get()) {
            Logging.warning("Ignoring warning dialog during shutdown: " + message);
            return;
        }
        
        // Check if already on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            showCompactAlertOnFxThread(title, message, Alert.AlertType.WARNING);
        } else {
            // Schedule dialog to run on JavaFX thread
            Platform.runLater(() -> showCompactAlertOnFxThread(title, message, Alert.AlertType.WARNING));
        }
    }

    //endregion

    //region ==================== CONFIRMATION DIALOGS ====================

    /**
     * Shows a confirmation dialog and returns the user's choice.
     * Must be called on the JavaFX Application Thread.
     *
     * @param title the dialog title
     * @param message the confirmation message
     * @return true if the user confirmed, false otherwise
     * @throws IllegalStateException if not called on the JavaFX Application Thread
     */
    public static boolean showConfirmation(String title, String message) {
        // Ensure this method is called on the JavaFX Application Thread
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Must be called on the JavaFX Application Thread");
        }

        // Create confirmation dialog with standard OK/Cancel buttons
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Show dialog and wait for user response
        Optional<ButtonType> result = alert.showAndWait();
        // Return true if user clicked OK, false otherwise
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Shows a confirmation dialog with custom buttons and returns the user's choice.
     * Must be called on the JavaFX Application Thread.
     *
     * @param title the dialog title
     * @param message the confirmation message
     * @param confirmText the text for the confirm button
     * @param cancelText the text for the cancel button
     * @return true if the user confirmed, false otherwise
     * @throws IllegalStateException if not called on the JavaFX Application Thread
     */
    public static boolean showConfirmation(String title, String message, String confirmText, String cancelText) {
        // Ensure this method is called on the JavaFX Application Thread
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Must be called on the JavaFX Application Thread");
        }

        // Create confirmation dialog with custom button text
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Create custom buttons with specified text
        ButtonType confirmButton = new ButtonType(confirmText);
        ButtonType cancelButton = new ButtonType(cancelText);
        // Replace default buttons with custom ones
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        // Show dialog and wait for user response
        Optional<ButtonType> result = alert.showAndWait();
        // Return true if user clicked the confirm button, false otherwise
        return result.isPresent() && result.get() == confirmButton;
    }

    //endregion

    //region ==================== INPUT DIALOGS ====================

    /**
     * Shows a text input dialog and returns the user's input.
     * Must be called on the JavaFX Application Thread.
     *
     * @param title the dialog title
     * @param message the input prompt message
     * @param defaultValue the default value for the input field
     * @return the user's input, or null if cancelled
     * @throws IllegalStateException if not called on the JavaFX Application Thread
     */
    public static String showTextInput(String title, String message, String defaultValue) {
        // Ensure this method is called on the JavaFX Application Thread
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Must be called on the JavaFX Application Thread");
        }

        // Create text input dialog with default value
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);

        // Show dialog and wait for user input
        Optional<String> result = dialog.showAndWait();
        // Return user input or null if cancelled
        return result.orElse(null);
    }

    /**
     * Shows a text input dialog and returns the user's input.
     * Must be called on the JavaFX Application Thread.
     *
     * @param title the dialog title
     * @param message the input prompt message
     * @return the user's input, or null if cancelled
     * @throws IllegalStateException if not called on the JavaFX Application Thread
     */
    public static String showTextInput(String title, String message) {
        // Delegate to the full version with empty default value
        return showTextInput(title, message, "");
    }

    /**
     * Shows a password input dialog and returns the user's input.
     * Must be called on the JavaFX Application Thread.
     *
     * @param title the dialog title
     * @param message the input prompt message
     * @return the user's password input, or null if cancelled
     * @throws IllegalStateException if not called on the JavaFX Application Thread
     */
    public static String showPasswordInput(String title, String message) {
        // Ensure this method is called on the JavaFX Application Thread
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Must be called on the JavaFX Application Thread");
        }

        // Create custom dialog for password input
        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        // Add OK and Cancel buttons
        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create password field with masked input
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        // Create content layout with message and password field
        VBox content = new VBox();
        content.getChildren().addAll(new Label(message), passwordField);
        dialog.getDialogPane().setContent(content);

        // Set result converter to return password text when OK is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return passwordField.getText();
            }
            return null;
        });

        // Show dialog and wait for user input
        Optional<String> result = dialog.showAndWait();
        // Return password or null if cancelled
        return result.orElse(null);
    }

    /**
     * Shows a choice dialog and returns the user's selection.
     * Must be called on the JavaFX Application Thread.
     *
     * @param <T> the type of the choices
     * @param title the dialog title
     * @param message the selection prompt message
     * @param choices the list of available choices
     * @param defaultValue the default selected choice
     * @return the user's selection, or null if cancelled
     * @throws IllegalStateException if not called on the JavaFX Application Thread
     */
    public static <T> T showChoice(String title, String message, List<T> choices, T defaultValue) {
        // Ensure this method is called on the JavaFX Application Thread
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Must be called on the JavaFX Application Thread");
        }

        // Create choice dialog with default selection and available choices
        ChoiceDialog<T> dialog = new ChoiceDialog<>(defaultValue, choices);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);

        // Show dialog and wait for user selection
        Optional<T> result = dialog.showAndWait();
        // Return selected choice or null if cancelled
        return result.orElse(null);
    }

    /**
     * Shows a choice dialog and returns the user's selection.
     * Must be called on the JavaFX Application Thread.
     *
     * @param <T> the type of the choices
     * @param title the dialog title
     * @param message the selection prompt message
     * @param choices the list of available choices
     * @return the user's selection, or null if cancelled
     * @throws IllegalStateException if not called on the JavaFX Application Thread
     */
    public static <T> T showChoice(String title, String message, List<T> choices) {
        // Return null if no choices are available
        if (choices.isEmpty()) {
            return null;
        }
        // Delegate to the full version with first choice as default
        return showChoice(title, message, choices, choices.get(0));
    }

    //endregion

    //region ==================== PRIVATE UTILITY METHODS ====================

    /**
     * Shows an alert dialog with the specified parameters.
     * Handles thread safety by ensuring the dialog is shown on the JavaFX Application Thread.
     * 
     * @param title the dialog title
     * @param header the dialog header
     * @param e the throwable to display (can be null)
     * @param type the alert type
     */
    private static void showAlert(String title, String header, Throwable e, Alert.AlertType type, boolean compact) {
        // Check if already on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            // Show dialog directly on current thread
            showAlertOnFxThread(title, header, e, type, false, compact);
        } else {
            // Schedule dialog to run on JavaFX thread
            Platform.runLater(() -> showAlertOnFxThread(title, header, e, type, false, compact));
        }
    }

    /**
     * Shows an alert dialog and exits the application after the user acknowledges it.
     * Handles thread safety by ensuring the dialog is shown on the JavaFX Application Thread.
     * 
     * @param title the dialog title
     * @param header the dialog header
     * @param e the throwable to display (can be null)
     */
    private static void showAlertAndExit(String title, String header, Throwable e) {
        // Check if already on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            // Show dialog directly on current thread with exit flag
            showAlertOnFxThread(title, header, e, Alert.AlertType.ERROR, true, false);
        } else {
            // Schedule dialog to run on JavaFX thread with exit flag
            Platform.runLater(() -> showAlertOnFxThread(title, header, e, Alert.AlertType.ERROR, true, false));
        }
    }

    /**
     * Shows an alert dialog on the JavaFX Application Thread.
     * This method must be called on the JavaFX Application Thread.
     * 
     * @param title the dialog title
     * @param header the dialog header
     * @param e the throwable to display (can be null)
     * @param type the alert type
     * @param exitAfter true if the application should exit after the dialog is closed, false otherwise
     * @param compact true if the dialog should be compact (no expandable content), false otherwise
     */
    private static void showAlertOnFxThread(String title, String header, Throwable e, Alert.AlertType type, boolean exitAfter, boolean compact) {
        // Create alert dialog with specified type
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        
        // Handle content text based on whether throwable is provided
        if (e != null && !compact) {
            // Full dialog with expandable stack trace
            alert.setContentText(e.getMessage());

            // Convert exception stack trace to string
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String exceptionText = sw.toString();

            // Create text area for exception details
            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            // Create expandable content container
            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(textArea, 0, 0);

            // Add expandable content to dialog
            alert.getDialogPane().setExpandableContent(expContent);
        } else if (e != null && compact) {
            // Compact dialog with just exception message
            alert.setContentText(e.getMessage());
        } else {
            // No throwable - use header as content text to avoid empty expandable area
            if (header != null && !header.isEmpty()) {
                alert.setContentText(header);
                alert.setHeaderText(null); // Remove header to avoid duplication
            }
        }
        
        // Set up exit behavior if requested
        if (exitAfter) {
            alert.setOnCloseRequest(event -> safelyExit());
        }
        // Show dialog and wait for user response
        alert.showAndWait();
        // Exit application if requested and dialog was closed
        if (exitAfter) {
            safelyExit();
        }
    }

    /**
     * Safely exits the application, ensuring proper cleanup.
     * 
     * @param title the dialog title
     * @param header the dialog header
     * @param e the throwable to display (can be null)
     * @param type the alert type
     * @param exitAfter true if the application should exit after the dialog is closed, false otherwise
     */
    private static void safelyExit() {
        // Use atomic operation to prevent multiple exit attempts
        if (isShuttingDown.compareAndSet(false, true)) {
            try {
                // Log shutdown initiation
                Logging.info("Initiating application shutdown...");
                // Exit JavaFX application
                Platform.exit();
            } finally {
                // Force system exit with error code
                System.exit(1);
            }
        }
    }

    /**
     * Shows a compact alert dialog on the JavaFX Application Thread.
     * This method ensures only one section is shown with no expandable content.
     * 
     * @param title the dialog title
     * @param message the dialog message
     * @param type the alert type
     */
    private static void showCompactAlertOnFxThread(String title, String message, Alert.AlertType type) {
        // Create alert dialog with specified type
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header to avoid extra section
        alert.setContentText(message); // Set message as content text
        
        // Ensure no expandable content is created
        alert.getDialogPane().setExpandableContent(null);
        
        // Show dialog and wait for user response
        alert.showAndWait();
    }

    //endregion

    //region ==================== SHUTDOWN MANAGEMENT ====================

    /**
     * Marks the application as shutting down to prevent dialogs during shutdown.
     */
    public static void markShuttingDown() {
        // Set shutdown flag to prevent new dialogs
        isShuttingDown.set(true);
    }

    /**
     * Checks if the application is currently shutting down.
     *
     * @return true if the application is shutting down, false otherwise
     */
    public static boolean isShuttingDown() {
        // Return current shutdown state
        return isShuttingDown.get();
    }

    //endregion

    //region ==================== UTILITY METHODS ====================

    /**
     * Shows a dialog with custom actions that can be performed after user interaction.
     * Must be called on the JavaFX Application Thread.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @param type the alert type
     * @param onConfirm the action to perform if user confirms (can be null)
     * @param onCancel the action to perform if user cancels (can be null)
     * @return true if the user confirmed, false otherwise
     * @throws IllegalStateException if not called on the JavaFX Application Thread
     */
    public static boolean showDialogWithActions(String title, String message, Alert.AlertType type, 
                                              Runnable onConfirm, Runnable onCancel) {
        // Ensure this method is called on the JavaFX Application Thread
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Must be called on the JavaFX Application Thread");
        }

        // Create alert dialog with specified type
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Show dialog and wait for user response
        Optional<ButtonType> result = alert.showAndWait();
        boolean confirmed = result.isPresent() && result.get() == ButtonType.OK;

        // Execute appropriate action based on user choice
        if (confirmed && onConfirm != null) {
            onConfirm.run();
        } else if (!confirmed && onCancel != null) {
            onCancel.run();
        }

        return confirmed;
    }

    /**
     * Shows a dialog with custom actions that can be performed after user interaction.
     * Thread-safe version that can be called from any thread.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @param type the alert type
     * @param onConfirm the action to perform if user confirms (can be null)
     * @param onCancel the action to perform if user cancels (can be null)
     */
    public static void showDialogWithActionsAsync(String title, String message, Alert.AlertType type, 
                                                Runnable onConfirm, Runnable onCancel) {
        // Check if already on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            // Execute directly on current thread
            showDialogWithActions(title, message, type, onConfirm, onCancel);
        } else {
            // Schedule execution on JavaFX thread
            Platform.runLater(() -> showDialogWithActions(title, message, type, onConfirm, onCancel));
        }
    }

    /**
     * Shows a confirmation dialog with custom actions.
     * Thread-safe version that can be called from any thread.
     *
     * @param title the dialog title
     * @param message the confirmation message
     * @param onConfirm the action to perform if user confirms
     * @param onCancel the action to perform if user cancels (can be null)
     */
    public static void showConfirmationAsync(String title, String message, Runnable onConfirm, Runnable onCancel) {
        // Check if already on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            // Execute confirmation dialog directly
            boolean confirmed = showConfirmation(title, message);
            if (confirmed) {
                onConfirm.run();
            } else if (onCancel != null) {
                onCancel.run();
            }
        } else {
            // Schedule confirmation dialog on JavaFX thread
            Platform.runLater(() -> {
                boolean confirmed = showConfirmation(title, message);
                if (confirmed) {
                    onConfirm.run();
                } else if (onCancel != null) {
                    onCancel.run();
                }
            });
        }
    }

    /**
     * Shows a text input dialog with custom actions.
     * Thread-safe version that can be called from any thread.
     *
     * @param title the dialog title
     * @param message the input prompt message
     * @param defaultValue the default value for the input field
     * @param onInput the action to perform with the user's input
     * @param onCancel the action to perform if user cancels (can be null)
     */
    public static void showTextInputAsync(String title, String message, String defaultValue, 
                                        java.util.function.Consumer<String> onInput, Runnable onCancel) {
        // Check if already on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            // Execute text input dialog directly
            String input = showTextInput(title, message, defaultValue);
            if (input != null) {
                onInput.accept(input);
            } else if (onCancel != null) {
                onCancel.run();
            }
        } else {
            // Schedule text input dialog on JavaFX thread
            Platform.runLater(() -> {
                String input = showTextInput(title, message, defaultValue);
                if (input != null) {
                    onInput.accept(input);
                } else if (onCancel != null) {
                    onCancel.run();
                }
            });
        }
    }

    /**
     * Shows a password input dialog with custom actions.
     * Thread-safe version that can be called from any thread.
     *
     * @param title the dialog title
     * @param message the input prompt message
     * @param onInput the action to perform with the user's password input
     * @param onCancel the action to perform if user cancels (can be null)
     */
    public static void showPasswordInputAsync(String title, String message, 
                                            java.util.function.Consumer<String> onInput, Runnable onCancel) {
        // Check if already on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            // Execute password input dialog directly
            String input = showPasswordInput(title, message);
            if (input != null) {
                onInput.accept(input);
            } else if (onCancel != null) {
                onCancel.run();
            }
        } else {
            // Schedule password input dialog on JavaFX thread
            Platform.runLater(() -> {
                String input = showPasswordInput(title, message);
                if (input != null) {
                    onInput.accept(input);
                } else if (onCancel != null) {
                    onCancel.run();
                }
            });
        }
    }

    /**
     * Shows a choice dialog with custom actions.
     * Thread-safe version that can be called from any thread.
     *
     * @param <T> the type of the choices
     * @param title the dialog title
     * @param message the selection prompt message
     * @param choices the list of available choices
     * @param defaultValue the default selected choice
     * @param onChoice the action to perform with the user's choice
     * @param onCancel the action to perform if user cancels (can be null)
     */
    public static <T> void showChoiceAsync(String title, String message, List<T> choices, T defaultValue, 
                                         java.util.function.Consumer<T> onChoice, Runnable onCancel) {
        // Check if already on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            // Execute choice dialog directly
            T choice = showChoice(title, message, choices, defaultValue);
            if (choice != null) {
                onChoice.accept(choice);
            } else if (onCancel != null) {
                onCancel.run();
            }
        } else {
            // Schedule choice dialog on JavaFX thread
            Platform.runLater(() -> {
                T choice = showChoice(title, message, choices, defaultValue);
                if (choice != null) {
                    onChoice.accept(choice);
                } else if (onCancel != null) {
                    onCancel.run();
                }
            });
        }
    }

    //endregion
}

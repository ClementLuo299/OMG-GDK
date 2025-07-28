

package launcher;

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.TwoDimensional;
import org.fxmisc.richtext.model.TwoDimensional.Bias;
import org.fxmisc.richtext.model.TwoDimensional.Position;
import org.fxmisc.richtext.model.StyledDocument;
import org.fxmisc.richtext.model.StyledSegment;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.Duration;
import javafx.scene.layout.Priority;

/**
 * A professional JSON editor using RichTextFX with syntax highlighting,
 * line numbers, and dual text areas for input and output.
 */
public class ProfessionalJsonEditor extends VBox {
    
    private final CodeArea inputCodeArea;
    private final CodeArea outputCodeArea;
    private final StringProperty inputTextProperty;
    private final StringProperty outputTextProperty;
    
    // JSON syntax highlighting patterns
    private static final String[] KEYWORDS = new String[] {
        "true", "false", "null"
    };
    
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String NUMBER_PATTERN = "\\b\\d+(\\.\\d+)?\\b";
    private static final String BRACE_PATTERN = "[{}]";
    private static final String BRACKET_PATTERN = "[\\[\\]]";
    private static final String COLON_PATTERN = ":";
    private static final String COMMA_PATTERN = ",";
    
    private static final Pattern PATTERN = Pattern.compile(
        "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
        + "|(?<STRING>" + STRING_PATTERN + ")"
        + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
        + "|(?<BRACE>" + BRACE_PATTERN + ")"
        + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
        + "|(?<COLON>" + COLON_PATTERN + ")"
        + "|(?<COMMA>" + COMMA_PATTERN + ")"
    );
    
    /**
     * Creates a new professional JSON editor with dual text areas.
     */
    public ProfessionalJsonEditor() {
        this.setSpacing(0);
        this.setPadding(new Insets(5));
        this.getStyleClass().add("professional-json-editor");
        
        // Initialize text properties
        this.inputTextProperty = new SimpleStringProperty("");
        this.outputTextProperty = new SimpleStringProperty("");
        
        // Create the input code area
        this.inputCodeArea = new CodeArea();
        this.inputCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(inputCodeArea));
        this.inputCodeArea.setMinHeight(150);
        this.inputCodeArea.getStyleClass().add("json-code-area");
        
        // Create the output code area
        this.outputCodeArea = new CodeArea();
        this.outputCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(outputCodeArea));
        this.outputCodeArea.setMinHeight(150);
        this.outputCodeArea.getStyleClass().add("json-code-area");
        this.outputCodeArea.setEditable(false); // Read-only for output
        
        // Set up syntax highlighting for both areas
        setupSyntaxHighlighting(inputCodeArea);
        setupSyntaxHighlighting(outputCodeArea);
        
        // Set up context menus
        setupContextMenu(inputCodeArea, true);
        setupContextMenu(outputCodeArea, false);
        
        // Set up keyboard shortcuts
        setupKeyboardShortcuts();
        
        // Create toolbar with save/load buttons
        createToolbar();
        
        // Set up bidirectional binding for input area
        this.inputTextProperty.addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(inputCodeArea.getText())) {
                inputCodeArea.replaceText(newValue);
            }
        });
        
        this.inputCodeArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(inputTextProperty.get())) {
                inputTextProperty.set(newValue);
            }
        });
        
        // Set up binding for output area
        this.outputTextProperty.addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(outputCodeArea.getText())) {
                outputCodeArea.replaceText(newValue);
            }
        });
        
        // Create split pane for the two areas
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.5);
        
        // Create labeled containers for each area
        VBox inputContainer = createLabeledContainer("JSON Input", inputCodeArea);
        VBox outputContainer = createLabeledContainer("JSON Output", outputCodeArea);
        
        splitPane.getItems().addAll(inputContainer, outputContainer);
        
        // Add the split pane to the layout
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);
    }
    
    /**
     * Creates a labeled container for a code area.
     */
    private VBox createLabeledContainer(String title, CodeArea codeArea) {
        VBox container = new VBox(5);
        container.setPadding(new Insets(5));
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("json-area-title");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        VBox.setVgrow(codeArea, Priority.ALWAYS);
        container.getChildren().addAll(titleLabel, codeArea);
        
        return container;
    }
    
    /**
     * Sets up syntax highlighting for a code area.
     */
    private void setupSyntaxHighlighting(CodeArea codeArea) {
        // Compute syntax highlighting
        codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));
        
        // Set initial highlighting
        codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
    }
    
    /**
     * Computes syntax highlighting for the given text.
     */
    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        
        while (matcher.find()) {
            String styleClass = 
                matcher.group("KEYWORD") != null ? "keyword" :
                matcher.group("STRING") != null ? "string" :
                matcher.group("NUMBER") != null ? "number" :
                matcher.group("BRACE") != null ? "brace" :
                matcher.group("BRACKET") != null ? "bracket" :
                matcher.group("COLON") != null ? "colon" :
                matcher.group("COMMA") != null ? "comma" :
                null;
            
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
    
    /**
     * Sets up the context menu for editor options.
     */
    private void setupContextMenu(CodeArea codeArea, boolean isInput) {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem formatJsonItem = new MenuItem("Format JSON");
        formatJsonItem.setOnAction(e -> formatJson(codeArea));
        
        if (isInput) {
            MenuItem saveItem = new MenuItem("Save Input to File");
            saveItem.setOnAction(e -> saveToFile(codeArea, "Save Input JSON"));
            
            MenuItem loadItem = new MenuItem("Load Input from File");
            loadItem.setOnAction(e -> loadFromFile(codeArea, "Load Input JSON"));
        } else {
            MenuItem saveItem = new MenuItem("Save Output to File");
            saveItem.setOnAction(e -> saveToFile(codeArea, "Save Output JSON"));
            
            MenuItem clearItem = new MenuItem("Clear Output");
            clearItem.setOnAction(e -> codeArea.clear());
        }
        
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> codeArea.copy());
        
        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setOnAction(e -> codeArea.paste());
        
        MenuItem selectAllItem = new MenuItem("Select All");
        selectAllItem.setOnAction(e -> codeArea.selectAll());
        
        if (isInput) {
            MenuItem saveItem = new MenuItem("Save Input to File");
            saveItem.setOnAction(e -> saveToFile(codeArea, "Save Input JSON"));
            
            MenuItem loadItem = new MenuItem("Load Input from File");
            loadItem.setOnAction(e -> loadFromFile(codeArea, "Load Input JSON"));
            
            contextMenu.getItems().addAll(
                formatJsonItem,
                saveItem,
                loadItem,
                copyItem,
                pasteItem,
                selectAllItem
            );
        } else {
            MenuItem saveItem = new MenuItem("Save Output to File");
            saveItem.setOnAction(e -> saveToFile(codeArea, "Save Output JSON"));
            
            MenuItem clearItem = new MenuItem("Clear Output");
            clearItem.setOnAction(e -> codeArea.clear());
            
            contextMenu.getItems().addAll(
                formatJsonItem,
                saveItem,
                clearItem,
                copyItem,
                selectAllItem
            );
        }
        
        codeArea.setContextMenu(contextMenu);
    }
    
    /**
     * Creates a toolbar with save and load buttons.
     */
    private void createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(5));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("json-editor-toolbar");
        
        // Save input button
        Button saveInputButton = new Button("💾 Save Input");
        saveInputButton.setTooltip(new Tooltip("Save input JSON to file"));
        saveInputButton.setOnAction(e -> saveToFile(inputCodeArea, "Save Input JSON"));
        
        // Load input button
        Button loadInputButton = new Button("📁 Load Input");
        loadInputButton.setTooltip(new Tooltip("Load input JSON from file"));
        loadInputButton.setOnAction(e -> loadFromFile(inputCodeArea, "Load Input JSON"));
        
        // Save output button
        Button saveOutputButton = new Button("💾 Save Output");
        saveOutputButton.setTooltip(new Tooltip("Save output JSON to file"));
        saveOutputButton.setOnAction(e -> saveToFile(outputCodeArea, "Save Output JSON"));
        
        // Clear output button
        Button clearOutputButton = new Button("🗑️ Clear Output");
        clearOutputButton.setTooltip(new Tooltip("Clear output area"));
        clearOutputButton.setOnAction(e -> outputCodeArea.clear());
        
        // Format button
        Button formatButton = new Button("✨ Format");
        formatButton.setTooltip(new Tooltip("Format JSON (Ctrl+Shift+F)"));
        formatButton.setOnAction(e -> formatJson(inputCodeArea));
        
        toolbar.getChildren().addAll(
            saveInputButton, loadInputButton, 
            saveOutputButton, clearOutputButton, 
            formatButton
        );
        
        // Add toolbar to the top of the editor
        this.getChildren().add(0, toolbar);
    }
    
    /**
     * Saves the content of a code area to a file.
     */
    private void saveToFile(CodeArea codeArea, String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        // Try to get the current stage
        Stage stage = (Stage) this.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                String content = codeArea.getText();
                Files.write(Paths.get(file.getAbsolutePath()), content.getBytes());
                
                showAlert(AlertType.INFORMATION, "Success", "JSON saved successfully!", 
                         "File saved to: " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(AlertType.ERROR, "Error", "Failed to save file", 
                         "Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Loads JSON content from a file into a code area.
     */
    private void loadFromFile(CodeArea codeArea, String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        // Try to get the current stage
        Stage stage = (Stage) this.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
                codeArea.replaceText(content);
                
                showAlert(AlertType.INFORMATION, "Success", "JSON loaded successfully!", 
                         "File loaded from: " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(AlertType.ERROR, "Error", "Failed to load file", 
                         "Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Shows an alert dialog.
     */
    private void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Sets up keyboard shortcuts.
     */
    private void setupKeyboardShortcuts() {
        // Ctrl+Shift+F for format (applies to input area)
        inputCodeArea.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.isShiftDown() && event.getCode() == KeyCode.F) {
                formatJson(inputCodeArea);
                event.consume();
            }
        });
    }
    
    /**
     * Formats the JSON content in a code area.
     */
    private void formatJson(CodeArea codeArea) {
        String currentText = codeArea.getText().trim();
        if (currentText.isEmpty()) {
            return;
        }
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Object jsonObject = mapper.readValue(currentText, Object.class);
            String formattedJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            codeArea.replaceText(formattedJson);
        } catch (Exception e) {
            // If formatting fails, just return without changing the text
        }
    }
    
    /**
     * Gets the input text property for binding.
     */
    public StringProperty inputTextProperty() {
        return inputTextProperty;
    }
    
    /**
     * Gets the output text property for binding.
     */
    public StringProperty outputTextProperty() {
        return outputTextProperty;
    }
    
    /**
     * Gets the current input text content.
     */
    public String getInputText() {
        return inputTextProperty.get();
    }
    
    /**
     * Gets the current output text content.
     */
    public String getOutputText() {
        return outputTextProperty.get();
    }
    
    /**
     * Sets the input text content.
     */
    public void setInputText(String text) {
        inputTextProperty.set(text);
    }
    
    /**
     * Sets the output text content.
     */
    public void setOutputText(String text) {
        outputTextProperty.set(text);
    }
    
    /**
     * Clears the input text content.
     */
    public void clearInput() {
        inputTextProperty.set("");
    }
    
    /**
     * Clears the output text content.
     */
    public void clearOutput() {
        outputTextProperty.set("");
    }
    
    /**
     * Gets the input code area.
     */
    public CodeArea getInputCodeArea() {
        return inputCodeArea;
    }
    
    /**
     * Gets the output code area.
     */
    public CodeArea getOutputCodeArea() {
        return outputCodeArea;
    }
    
    /**
     * Gets the main code area (for backward compatibility).
     */
    public CodeArea getCodeArea() {
        return inputCodeArea;
    }
    
    /**
     * Gets the text property (for backward compatibility).
     */
    public StringProperty textProperty() {
        return inputTextProperty;
    }
    
    /**
     * Gets the current text content (for backward compatibility).
     */
    public String getText() {
        return inputTextProperty.get();
    }
    
    /**
     * Sets the text content (for backward compatibility).
     */
    public void setText(String text) {
        inputTextProperty.set(text);
    }
    
    /**
     * Clears the text content (for backward compatibility).
     */
    public void clear() {
        inputTextProperty.set("");
    }
} 
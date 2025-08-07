package launcher.gui;

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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

import launcher.utils.DialogUtil;

/**
 * A professional single-area JSON editor using RichTextFX with syntax highlighting,
 * line numbers, and comprehensive editing features.
 * 
 * @author: Clement Luo
 * @date: August 5, 2025
 * @edited: August 6, 2025
 * @since: 1.0
 */
public class SingleJsonEditor extends VBox {
    
    private final CodeArea codeArea;
    private final StringProperty textProperty;
    private final String title;
    
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
     * Creates a new single-area JSON editor with the specified title.
     * 
     * @param title The title for this JSON editor
     */
    public SingleJsonEditor(String title) {
        this.title = title;
        this.textProperty = new SimpleStringProperty("");
        
        // Create the code area
        this.codeArea = new CodeArea();
        
        // Set up the editor
        setupCodeArea();
        setupSyntaxHighlighting();
        setupContextMenu();
        setupKeyboardShortcuts();
        
        // Create the main layout first
        createLayout();
        
        // Create toolbar after layout is set up
        createToolbar();
    }

    /**
     * Creates a new single-area JSON editor with default title.
     */
    public SingleJsonEditor() {
        this("JSON Editor");
    }

    /**
     * Set up the code area with basic configuration.
     */
    private void setupCodeArea() {
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setWrapText(true);
        
        // Set preferred size
        codeArea.setPrefHeight(200);
        codeArea.setPrefWidth(600);
        
        // Apply CSS class for styling
        codeArea.getStyleClass().add("code-area");
        
        // Bind text property
        textProperty.bind(codeArea.textProperty());
        
        // Set up auto-indent
        codeArea.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String currentLine = getCurrentLine();
                String indent = getIndentation(currentLine);
                if (currentLine.trim().endsWith("{")) {
                    indent += "    ";
                }
                codeArea.insertText(codeArea.getCaretPosition(), "\n" + indent);
                event.consume();
            }
        });
    }

    /**
     * Get the current line text.
     */
    private String getCurrentLine() {
        int caretPosition = codeArea.getCaretPosition();
        int paragraph = codeArea.offsetToPosition(caretPosition, Bias.Backward).getMajor();
        return codeArea.getText(paragraph, paragraph + 1);
    }

    /**
     * Get the indentation for the current line.
     */
    private String getIndentation(String line) {
        StringBuilder indent = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == ' ' || c == '\t') {
                indent.append(c);
            } else {
                break;
            }
        }
        return indent.toString();
    }

    /**
     * Create the main layout with title and toolbar.
     */
    private void createLayout() {
        setSpacing(0);
        setPadding(new Insets(0));
        
        // Apply CSS class to the main container
        getStyleClass().add("single-json-editor");
        
        // Title label
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title-label");
        
        // Add components
        getChildren().addAll(titleLabel, codeArea);
        
        // Make code area grow
        VBox.setVgrow(codeArea, Priority.ALWAYS);
    }

    /**
     * Create the toolbar with action buttons.
     */
    private void createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar");
        
        // Format button
        Button formatButton = new Button("Format JSON");
        formatButton.setTooltip(new Tooltip("Format and indent JSON"));
        formatButton.setOnAction(e -> formatJson());
        
        // Save button
        Button saveButton = new Button("Save");
        saveButton.setTooltip(new Tooltip("Save JSON to file"));
        saveButton.setOnAction(e -> saveToFile());
        
        // Load button
        Button loadButton = new Button("Load");
        loadButton.setTooltip(new Tooltip("Load JSON from file"));
        loadButton.setOnAction(e -> loadFromFile());
        
        // Clear button
        Button clearButton = new Button("Clear");
        clearButton.setTooltip(new Tooltip("Clear all content"));
        clearButton.setOnAction(e -> clear());
        
        toolbar.getChildren().addAll(formatButton, saveButton, loadButton, clearButton);
        
        // Insert toolbar after title
        getChildren().add(1, toolbar);
    }

    /**
     * Set up syntax highlighting for JSON.
     */
    private void setupSyntaxHighlighting() {
        codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));
    }

    /**
     * Compute syntax highlighting for the given text.
     */
    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        
        while (matcher.find()) {
            String styleClass = null;
            
            if (matcher.group("KEYWORD") != null) {
                styleClass = "keyword";
            } else if (matcher.group("STRING") != null) {
                styleClass = "string";
            } else if (matcher.group("NUMBER") != null) {
                styleClass = "number";
            } else if (matcher.group("BRACE") != null) {
                styleClass = "brace";
            } else if (matcher.group("BRACKET") != null) {
                styleClass = "bracket";
            } else if (matcher.group("COLON") != null) {
                styleClass = "colon";
            } else if (matcher.group("COMMA") != null) {
                styleClass = "comma";
            }
            
            if (styleClass != null) {
                spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
                lastKwEnd = matcher.end();
            }
        }
        
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    /**
     * Set up context menu for the code area.
     */
    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem formatItem = new MenuItem("Format JSON");
        formatItem.setOnAction(e -> formatJson());
        
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> codeArea.copy());
        
        MenuItem cutItem = new MenuItem("Cut");
        cutItem.setOnAction(e -> codeArea.cut());
        
        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setOnAction(e -> codeArea.paste());
        
        MenuItem selectAllItem = new MenuItem("Select All");
        selectAllItem.setOnAction(e -> codeArea.selectAll());
        
        contextMenu.getItems().addAll(formatItem, copyItem, cutItem, pasteItem, selectAllItem);
        codeArea.setContextMenu(contextMenu);
    }

    /**
     * Set up keyboard shortcuts.
     */
    private void setupKeyboardShortcuts() {
        // Ctrl+F for format
        codeArea.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.F) {
                formatJson();
                event.consume();
            }
        });
    }

    /**
     * Format the JSON content.
     */
    private void formatJson() {
        try {
            String text = codeArea.getText().trim();
            if (text.isEmpty()) {
                return;
            }
            
            // Parse and format JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(text);
            String formattedJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
            
            codeArea.replaceText(formattedJson);
            showAlert(AlertType.INFORMATION, "Success", "JSON Formatted", "JSON has been formatted successfully.");
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "Invalid JSON", "The content is not valid JSON: " + e.getMessage());
        }
    }

    /**
     * Save the content to a file.
     */
    private void saveToFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save JSON File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                Files.write(file.toPath(), codeArea.getText().getBytes());
                showAlert(AlertType.INFORMATION, "Success", "File Saved", "JSON has been saved to: " + file.getName());
            } catch (IOException e) {
                showAlert(AlertType.ERROR, "Error", "Save Failed", "Failed to save file: " + e.getMessage());
            }
        }
    }

    /**
     * Load content from a file.
     */
    private void loadFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load JSON File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                codeArea.replaceText(content);
                showAlert(AlertType.INFORMATION, "Success", "File Loaded", "JSON has been loaded from: " + file.getName());
            } catch (IOException e) {
                showAlert(AlertType.ERROR, "Error", "Load Failed", "Failed to load file: " + e.getMessage());
            }
        }
    }

    /**
     * Show an alert dialog.
     */
    private void showAlert(AlertType type, String title, String header, String content) {
        switch (type) {
            case ERROR:
                DialogUtil.showError(title, header, content);
                break;
            case WARNING:
                DialogUtil.showWarning(title, header, content);
                break;
            case INFORMATION:
                DialogUtil.showInfo(title, header, content);
                break;
            default:
                DialogUtil.showInfo(title, header, content);
                break;
        }
    }

    // ==================== PUBLIC API ====================

    /**
     * Get the text property for binding.
     */
    public StringProperty textProperty() {
        return textProperty;
    }

    /**
     * Get the current text content.
     */
    public String getText() {
        return codeArea.getText();
    }

    /**
     * Set the text content.
     */
    public void setText(String text) {
        codeArea.replaceText(text);
    }

    /**
     * Clear all content.
     */
    public void clear() {
        codeArea.clear();
    }

    /**
     * Get the underlying code area.
     */
    public CodeArea getCodeArea() {
        return codeArea;
    }

    /**
     * Get the title of this editor.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Set the title of this editor.
     * This method is used by FXML to set the title property.
     */
    public void setTitle(String title) {
        // Note: This method is called by FXML but the title is already set in constructor
        // We could update the title label here if needed
        if (getChildren().size() > 0) {
            Label titleLabel = (Label) getChildren().get(0);
            titleLabel.setText(title);
        }
    }
} 
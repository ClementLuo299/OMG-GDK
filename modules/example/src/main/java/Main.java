

import gdk.GameModule;
import gdk.Logging;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import launcher.utils.DialogUtil;

/**
 * Example Game Module - A simple game module for testing the GDK.
 * Demonstrates basic game module implementation and communication.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited July 24, 2025
 * @since 1.0
 */
public class Main implements GameModule {
    
    // ==================== GAME CONSTANTS ====================
    
    private static final String GAME_ID = "example";
    private final Metadata metadata;
    
    // Transient state from start message
    private String lastGameMode = "unknown";
    private List<Map<String, Object>> lastPlayers = new ArrayList<>();
    private String localPlayerId = null;
    
    // UI references
    private javafx.scene.control.TextArea chatAreaRef;
    private javafx.scene.control.TextArea playersTextRef;
    private javafx.scene.control.TextArea movesAreaRef;
    private javafx.scene.control.Label infoLabelRef;
    private javafx.scene.control.Label youLabelRef;
    private javafx.scene.control.ComboBox<String> simSenderRef;
    private javafx.scene.control.TextField simTextRef;
    private javafx.scene.control.TextField localMoveInputRef;
    
    /**
     * Constructor for Main.
     */
    public Main() {
        this.metadata = new Metadata();
    }
    
    @Override
    public Scene launchGame(Stage primaryStage) {
        Logging.info("🎮 Launching Example Game");
        try {
            return createTestInterface(primaryStage);
        } catch (Exception e) {
            Logging.error("❌ Failed to launch Example Game: " + e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public void stopGame() {
        Logging.info("🔄 " + metadata.getGameName() + " closing - cleaning up resources");
    }
    
    @Override
    public Map<String, Object> handleMessage(Map<String, Object> message) {
        if (message == null) {
            return null;
        }
        
        String function = (String) message.get("function");
        if ("metadata".equals(function)) {
            Logging.info("📋 Returning metadata for Example Game");
            return metadata.toMap();
        }
        if ("start".equals(function)) {
            // Extract simple fields for display later
            Object modeObj = message.get("gameMode");
            String mode = modeObj instanceof String ? (String) modeObj : "unknown";
            Object playersObj = message.get("players");
            Object localIdObj = message.get("localPlayerId");
            List<Map<String, Object>> players = new ArrayList<>();
            if (playersObj instanceof List) {
                for (Object p : (List<?>) playersObj) {
                    if (p instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> pm = (Map<String, Object>) p;
                        players.add(pm);
                    }
                }
            }
            Logging.info("✅ Start received: mode=" + mode + ", players=" + players.size());
            // Store state
            this.lastGameMode = mode;
            this.lastPlayers = players;
            this.localPlayerId = (localIdObj instanceof String) ? (String) localIdObj : null;
            // Update UI if available
            javafx.application.Platform.runLater(() -> {
                if (infoLabelRef != null) {
                    int playerCount = lastPlayers != null ? lastPlayers.size() : 0;
                    infoLabelRef.setText("Mode: " + lastGameMode + " | Players: " + playerCount);
                }
                if (youLabelRef != null) {
                    youLabelRef.setText("You are: " + (localPlayerId == null ? "(unknown)" : localPlayerId));
                }
                if (playersTextRef != null) {
                    playersTextRef.setText(buildPlayersText());
                }
                if (simSenderRef != null) {
                    populateSimSenderOptions();
                }
            });
            return java.util.Map.of("status", "ok");
        }
        if ("chat".equals(function) || "message".equals(function)) {
            String from = String.valueOf(message.getOrDefault("from", "server"));
            String text = String.valueOf(message.getOrDefault("text", ""));
            Logging.info("💬 Message from " + from + ": " + text);
            appendChat(from, text);
            return java.util.Map.of("status", "ok");
        }
        if ("broadcast".equals(function)) {
            String text = String.valueOf(message.getOrDefault("text", ""));
            Logging.info("📢 Broadcast: " + text);
            appendChat("broadcast", text);
            return java.util.Map.of("status", "ok");
        }
        if ("move".equals(function)) {
            String playerId = String.valueOf(message.getOrDefault("playerId", "?"));
            Object moveObj = message.get("move");
            String moveStr;
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                moveStr = mapper.writeValueAsString(moveObj);
            } catch (Exception e) {
                moveStr = String.valueOf(moveObj);
            }
            Logging.info("🎯 Move from " + playerId + ": " + moveStr);
            appendMove(playerId, moveStr);
            return java.util.Map.of("status", "ok");
        }
        
        return null;
    }
    
    @Override
    public Metadata getMetadata() {
        return metadata;
    }
    
    // ==================== PRIVATE METHODS ====================
    
    private String buildPlayersText() {
        if (lastPlayers == null || lastPlayers.isEmpty()) {
            return "No players provided in start message.";
        }
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> p : lastPlayers) {
            String id = String.valueOf(p.getOrDefault("id", "?"));
            String name = String.valueOf(p.getOrDefault("name", id));
            String role = String.valueOf(p.getOrDefault("role", "player"));
            sb.append("- ").append(name).append(" (id=").append(id).append(", role=").append(role).append(")\n");
        }
        return sb.toString();
    }
    
    private void appendChat(String from, String text) {
        if (chatAreaRef == null) return;
        javafx.application.Platform.runLater(() -> {
            chatAreaRef.appendText("[" + from + "] " + text + "\n");
        });
    }
    
    private void populateSimSenderOptions() {
        java.util.List<String> senders = new java.util.ArrayList<>();
        senders.add("server");
        if (lastPlayers != null) {
            for (java.util.Map<String, Object> p : lastPlayers) {
                Object id = p.get("id");
                if (id != null) {
                    senders.add(String.valueOf(id));
                }
            }
        }
        simSenderRef.getItems().setAll(senders);
        if (!senders.isEmpty()) {
            simSenderRef.getSelectionModel().selectFirst();
        }
    }
    
    private void appendMove(String playerId, String moveText) {
        if (movesAreaRef == null) return;
        javafx.application.Platform.runLater(() -> {
            movesAreaRef.appendText("[" + playerId + "] " + moveText + "\n");
        });
    }

    /**
     * Creates a simple test interface to demonstrate game communication.
     */
    private Scene createTestInterface(Stage primaryStage) {
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        
        // Title
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("🎮 Game Communication Test (Incremental!)");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #007bff;");
        
        // Game info (reflect start message if available)
        int playerCount = lastPlayers != null ? lastPlayers.size() : 0;
        String mode = lastGameMode != null ? lastGameMode : "unknown";
        infoLabelRef = new javafx.scene.control.Label(
            "Mode: " + mode + " | Players: " + playerCount
        );
        infoLabelRef.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        
        // Status
        javafx.scene.control.Label statusLabel = new javafx.scene.control.Label("🎮 Game is running!");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #28a745; -fx-font-weight: bold;");
        
        // Local player label
        youLabelRef = new javafx.scene.control.Label("You are: " + (localPlayerId == null ? "(unknown)" : localPlayerId));
        youLabelRef.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        
        // Players breakdown
        playersTextRef = new javafx.scene.control.TextArea();
        playersTextRef.setEditable(false);
        playersTextRef.setPrefRowCount(4);
        playersTextRef.setWrapText(true);
        playersTextRef.setText(buildPlayersText());
        
        // Moves area
        movesAreaRef = new javafx.scene.control.TextArea();
        movesAreaRef.setEditable(false);
        movesAreaRef.setPrefRowCount(6);
        movesAreaRef.setWrapText(true);
        
        // Local move input (simulating local player's move)
        localMoveInputRef = new javafx.scene.control.TextField();
        localMoveInputRef.setPromptText("Type local move JSON (e.g., {\"function\":\"move\",\"playerId\":\"p1\",\"move\":{...}}) and press Enter...");
        localMoveInputRef.setOnAction(e -> {
            String text = localMoveInputRef.getText().trim();
            if (!text.isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> msg = mapper.readValue(text, java.util.Map.class);
                    handleMessage(msg);
                } catch (Exception ex) {
                    appendMove("invalid", text);
                }
                localMoveInputRef.clear();
            }
        });
        
        // Chatroom UI
        chatAreaRef = new javafx.scene.control.TextArea();
        chatAreaRef.setEditable(false);
        chatAreaRef.setPrefRowCount(6);
        chatAreaRef.setWrapText(true);
        javafx.scene.control.TextField chatInput = new javafx.scene.control.TextField();
        chatInput.setPromptText("Type a message and press Enter...");
        chatInput.setOnAction(e -> {
            String text = chatInput.getText().trim();
            if (!text.isEmpty()) {
                appendChat("me", text);
                chatInput.clear();
            }
        });
        
        // In-game Simulator (send function=message)
        javafx.scene.control.Label simLabel = new javafx.scene.control.Label("Simulated Server:");
        simSenderRef = new javafx.scene.control.ComboBox<>();
        populateSimSenderOptions();
        simTextRef = new javafx.scene.control.TextField();
        simTextRef.setPromptText("Enter message text to send as function=message");
        javafx.scene.control.Button simSendBtn = new javafx.scene.control.Button("Send");
        simSendBtn.setOnAction(e -> {
            String from = simSenderRef.getValue() == null ? "server" : simSenderRef.getValue();
            String text = simTextRef.getText() == null ? "" : simTextRef.getText().trim();
            if (!text.isEmpty()) {
                java.util.Map<String, Object> msg = new java.util.HashMap<>();
                msg.put("function", "message");
                msg.put("from", from);
                msg.put("text", text);
                handleMessage(msg);
                simTextRef.clear();
            }
        });
        
        javafx.scene.control.Button jsonDataButton = new javafx.scene.control.Button("📦 Check JSON Data");
        jsonDataButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        
        javafx.scene.control.Button backButton = new javafx.scene.control.Button("🔙 Back to Lobby");
        backButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        
        // Event handlers
        jsonDataButton.setOnAction(e -> {
            Logging.info("📦 Checking for custom JSON data...");
            Logging.info("📦 No custom JSON data found");
            DialogUtil.showInfo("JSON Data Information", "No Custom Data", "No custom JSON data was provided when launching this game.");
        });
        
        javafx.scene.control.Button closeButton = new javafx.scene.control.Button("❌ Close Game");
        closeButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        
        closeButton.setOnAction(e -> {
            Logging.info("🔒 Closing Example Game");
            stopGame();
            Platform.exit();
        });
        
        backButton.setOnAction(e -> {
            Logging.info("🔙 Returning to lobby from Example Game");
            stopGame();
        });
        
        // Add components to root
        root.getChildren().addAll(
            titleLabel,
            infoLabelRef,
            youLabelRef,
            statusLabel,
            new javafx.scene.control.Label("Players:"),
            playersTextRef,
            new javafx.scene.control.Label("Moves:"),
            movesAreaRef,
            new javafx.scene.control.Label("Local Move (JSON):"),
            localMoveInputRef,
            new javafx.scene.control.Label("Chatroom:"),
            chatAreaRef,
            chatInput,
            simLabel,
            new javafx.scene.layout.HBox(10, new javafx.scene.control.Label("From:"), simSenderRef),
            simTextRef,
            simSendBtn,
            jsonDataButton,
            backButton,
            closeButton
        );
        
        // Create scene
        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/games/example/css/example.css").toExternalForm());
        
        // Configure stage
        primaryStage.setTitle("Example Game - GDK Test");
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(300);
        
        Logging.info("✅ Example Game interface created successfully");
        return scene;
    }
} 
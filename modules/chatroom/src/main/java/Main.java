

import gdk.GameModule;
import gdk.Logging;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;

import launcher.utils.DialogUtil;

/**
 * Chatroom Module - A multiplayer chatroom application for testing the GDK.
 * Demonstrates basic module implementation and communication.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited August 9, 2025
 * @since 1.0
 */
public class Main implements GameModule {
    
    // ==================== GAME CONSTANTS ====================
    
    private static final String GAME_ID = "chatroom";
    private final Metadata metadata;
    
    // Transient state from start message
    private String lastGameMode = "unknown";
    private List<Map<String, Object>> lastPlayers = new ArrayList<>();
    private String localPlayerId = null;
    
    // UI references
    private javafx.scene.control.TextArea chatAreaRef;
    private javafx.scene.control.TextArea playersTextRef;
    private javafx.scene.control.TextArea allMessagesAreaRef;
    private javafx.scene.control.Label infoLabelRef;
    private javafx.scene.control.Label youLabelRef;
    
    /**
     * Constructor for Main.
     */
    public Main() {
        this.metadata = new Metadata();
    }
    
    @Override
    public Scene launchGame(Stage primaryStage) {
        Logging.info("🎮 Launching Chatroom");
        try {
            return createTestInterface(primaryStage);
        } catch (Exception e) {
            Logging.error("❌ Failed to launch Chatroom: " + e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public void stopGame() {
        Logging.info("🔄 " + metadata.getGameName() + " closing - cleaning up resources");
        // Publish end-of-game message to server simulator
        try {
            java.util.Map<String, Object> endMsg = new java.util.HashMap<>();
            endMsg.put("function", "end");
            if (localPlayerId != null) endMsg.put("playerId", localPlayerId);
            endMsg.put("timestamp", java.time.Instant.now().toString());
            gdk.MessagingBridge.publish(endMsg);
        } catch (Exception ignored) {
        }
    }
    
    @Override
    public Map<String, Object> handleMessage(Map<String, Object> message) {
        if (message == null) {
            return null;
        }
        
        String function = (String) message.get("function");
        if ("metadata".equals(function)) {
            Logging.info("📋 Returning metadata for Chatroom");
            return metadata.toMap();
        }
        if ("start".equals(function)) {
            appendAllMessages(message);
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

            });
            return java.util.Map.of("status", "ok");
        }
        if ("chat".equals(function) || "message".equals(function)) {
            appendAllMessages(message);
            String from = String.valueOf(message.getOrDefault("from", "server"));
            String text = String.valueOf(message.getOrDefault("text", ""));
            Logging.info("💬 Message from " + from + ": " + text);
            appendChat(from, text);
            return java.util.Map.of("status", "ok");
        }
        if ("ack".equals(function)) {
            String of = String.valueOf(message.getOrDefault("of", "message"));
            String status = String.valueOf(message.getOrDefault("status", "ok"));
            appendChat("server-ack", of + " => " + status);
            return java.util.Map.of("status", "ok");
        }
        if ("end".equals(function)) {
            appendAllMessages(message);
            Logging.info("🏁 End message received");
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
    
    private void appendAllMessages(java.util.Map<String, Object> message) {
        if (allMessagesAreaRef == null || message == null) return;
        javafx.application.Platform.runLater(() -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
                allMessagesAreaRef.appendText(pretty + "\n\n");
            } catch (Exception e) {
                allMessagesAreaRef.appendText(String.valueOf(message) + "\n\n");
            }
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
        
        // All messages area
        allMessagesAreaRef = new javafx.scene.control.TextArea();
        allMessagesAreaRef.setEditable(false);
        allMessagesAreaRef.setPrefRowCount(8);
        allMessagesAreaRef.setWrapText(true);
        
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
                // Publish to simulator for visibility
                java.util.Map<String, Object> out = new java.util.HashMap<>();
                out.put("function", "message");
                out.put("from", localPlayerId == null ? "me" : localPlayerId);
                out.put("text", text);
                gdk.MessagingBridge.publish(out);
                chatInput.clear();
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
            Logging.info("🔒 Closing Chatroom");
            stopGame();
            Platform.exit();
        });
        
        backButton.setOnAction(e -> {
            Logging.info("🔙 Returning to lobby from Chatroom");
            // Send end message before stopping
            java.util.Map<String, Object> endMessage = new java.util.HashMap<>();
            endMessage.put("function", "end");
            endMessage.put("reason", "user_returned_to_lobby");
            endMessage.put("timestamp", java.time.Instant.now().toString());
            gdk.MessagingBridge.publish(endMessage);
            stopGame();
            // Use the messaging bridge to return to lobby instead of firing close event
            gdk.MessagingBridge.returnToLobby();
        });
        
        // Add components to root
        root.getChildren().addAll(
            titleLabel,
            infoLabelRef,
            youLabelRef,
            statusLabel,
            new javafx.scene.control.Label("Players:"),
            playersTextRef,
            new javafx.scene.control.Label("All Messages:"),
            allMessagesAreaRef,
            new javafx.scene.control.Label("Chatroom:"),
            chatAreaRef,
            chatInput,
            jsonDataButton,
            backButton,
            closeButton
        );
        
        // Create scene
        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/games/chatroom/css/chatroom.css").toExternalForm());
        
        // Configure stage
        primaryStage.setTitle("Chatroom - GDK Test");
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(300);
        
        Logging.info("✅ Chatroom interface created successfully");
        return scene;
    }
} 
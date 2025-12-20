import gdk.api.GameModule;
import gdk.internal.Logging;
import gdk.internal.MessagingBridge;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;

import launcher.utils.gui.DialogUtil;

/**
 * Chatroom Module - A multiplayer chatroom application for testing the GDK.
 * Demonstrates basic module implementation and communication.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited August 12, 2025
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
    private String currentPlayerSelector = null; // For local multiplayer mode
    private boolean isSinglePlayerMode = false; // For single player mode
    
    // UI references
    private javafx.scene.control.TextArea chatAreaRef;
    private javafx.scene.control.TextArea playersTextRef;
    private javafx.scene.control.TextArea allMessagesAreaRef;
    private javafx.scene.control.Label infoLabelRef;
    private javafx.scene.control.Label youLabelRef;
    private javafx.scene.control.Label localMultiplayerLabel;
    private javafx.scene.control.ComboBox<String> playerSelector;
    private javafx.scene.control.Label titleLabel;
    
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
        // Always publish end-of-game message to server simulator for transcript generation
        try {
            java.util.Map<String, Object> endMsg = new java.util.HashMap<>();
            endMsg.put("function", "end");
            if (localPlayerId != null) endMsg.put("playerId", localPlayerId);
            endMsg.put("timestamp", java.time.Instant.now().toString());
            MessagingBridge.publish(endMsg);
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
            // Only process start message if we haven't already received one
            if (!"unknown".equals(lastGameMode)) {
                Logging.info("⚠️ Ignoring subsequent start message - game mode already set to: " + lastGameMode);
                return java.util.Map.of("status", "ignored", "reason", "game_mode_already_set");
            }
            
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
            Logging.info("🔍 DEBUG: modeObj type=" + (modeObj != null ? modeObj.getClass().getSimpleName() : "null") + ", value=" + modeObj);
            Logging.info("🔍 DEBUG: lastGameMode before=" + lastGameMode + ", setting to=" + mode);
            
            // Store state
            this.lastGameMode = mode;
            this.lastPlayers = players;
            this.localPlayerId = (localIdObj instanceof String) ? (String) localIdObj : null;
            
            // Set default player selector for local multiplayer mode
            if ("local_multiplayer".equals(mode) && !players.isEmpty()) {
                this.currentPlayerSelector = String.valueOf(players.get(0).getOrDefault("id", "player1"));
            }
            
            // Set single player mode flag
            this.isSinglePlayerMode = "single_player".equals(mode);
            Logging.info("🔍 DEBUG: isSinglePlayerMode set to=" + isSinglePlayerMode + " for mode=" + mode);
            Logging.info("🔍 DEBUG: lastGameMode after=" + lastGameMode);
            
            // Close server simulator if in single player mode (not needed for local AI)
            if (isSinglePlayerMode) {
                Logging.info("🤖 Single player mode detected - closing unnecessary server simulator");
                // Send a message to close the server simulator
                java.util.Map<String, Object> closeSimulatorMsg = new java.util.HashMap<>();
                closeSimulatorMsg.put("function", "close_server_simulator");
                closeSimulatorMsg.put("reason", "single_player_mode_no_server_needed");
                closeSimulatorMsg.put("timestamp", java.time.Instant.now().toString());
                Logging.info("🔍 DEBUG: Sending close_server_simulator message: " + closeSimulatorMsg);
                MessagingBridge.publish(closeSimulatorMsg);
                Logging.info("🔍 DEBUG: close_server_simulator message sent via MessagingBridge");
            }
            
            // Update UI if available
            javafx.application.Platform.runLater(() -> {
                Logging.info("🔍 DEBUG: Starting UI update for game mode: " + lastGameMode);
                Logging.info("🔍 DEBUG: titleLabel=" + (titleLabel != null ? "exists" : "null"));
                Logging.info("🔍 DEBUG: infoLabelRef=" + (infoLabelRef != null ? "exists" : "null"));
                Logging.info("🔍 DEBUG: localMultiplayerLabel=" + (localMultiplayerLabel != null ? "exists" : "null"));
                Logging.info("🔍 DEBUG: playerSelector=" + (playerSelector != null ? "exists" : "null"));
                
                if (titleLabel != null) {
                    String newTitle = "🎮 Chatroom - " + getGameModeDisplayName();
                    Logging.info("🔍 DEBUG: Setting title to: " + newTitle);
                    titleLabel.setText(newTitle);
                }
                if (infoLabelRef != null) {
                    int playerCount = lastPlayers != null ? lastPlayers.size() : 0;
                    String newInfo = "Mode: " + lastGameMode + " | Players: " + playerCount;
                    Logging.info("🔍 DEBUG: Setting info to: " + newInfo);
                    infoLabelRef.setText(newInfo);
                }
                if (youLabelRef != null) {
                    String newYou = "You are: " + (localPlayerId == null ? "(unknown)" : localPlayerId);
                    Logging.info("🔍 DEBUG: Setting you label to: " + newYou);
                    youLabelRef.setText(newYou);
                }
                if (playersTextRef != null) {
                    String newPlayers = buildPlayersText();
                    Logging.info("🔍 DEBUG: Setting players text to: " + newPlayers);
                    playersTextRef.setText(newPlayers);
                }
                
                // Update the local multiplayer label
                if (localMultiplayerLabel != null) {
                    String newLabelText;
                    if ("local_multiplayer".equals(lastGameMode)) {
                        newLabelText = "🎭 Local Multiplayer Mode: Select a player above to send messages as that player (Server simulator active for transcripts)";
                    } else if ("single_player".equals(lastGameMode)) {
                        newLabelText = "🤖 Single Player Mode: Chat with AI - it will respond with random 6-letter messages (Server simulator active for transcripts)";
                    } else {
                        newLabelText = "🎮 Multiplayer Mode: Standard chat functionality";
                    }
                    Logging.info("🔍 DEBUG: Setting localMultiplayerLabel to: " + newLabelText);
                    localMultiplayerLabel.setText(newLabelText);
                }
                
                // Update player selector visibility and content
                if (playerSelector != null) {
                    if ("local_multiplayer".equals(lastGameMode) && !lastPlayers.isEmpty()) {
                        Logging.info("🔍 DEBUG: Showing player selector for local multiplayer");
                        playerSelector.getItems().clear();
                        for (Map<String, Object> player : lastPlayers) {
                            String playerId = String.valueOf(player.getOrDefault("id", "player"));
                            playerSelector.getItems().add(playerId);
                        }
                        if (currentPlayerSelector != null) {
                            playerSelector.setValue(currentPlayerSelector);
                        } else if (!lastPlayers.isEmpty()) {
                            playerSelector.setValue(String.valueOf(lastPlayers.get(0).getOrDefault("id", "player1")));
                            currentPlayerSelector = playerSelector.getValue();
                        }
                        playerSelector.setVisible(true);
                    } else if ("single_player".equals(lastGameMode)) {
                        Logging.info("🔍 DEBUG: Hiding player selector for single player");
                        playerSelector.setVisible(false);
                    } else {
                        Logging.info("🔍 DEBUG: Hiding player selector for other modes");
                        playerSelector.setVisible(false);
                    }
                }
                
                Logging.info("🎮 UI updated for game mode: " + lastGameMode);
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
    
    /**
     * Generates a random 6-letter message for AI responses in single player mode
     */
    private String generateRandomMessage() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder message = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < 6; i++) {
            message.append(letters.charAt(random.nextInt(letters.length())));
        }
        
        return message.toString();
    }
    
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
    
    private String getGameModeDisplayName() {
        if ("local_multiplayer".equals(lastGameMode)) {
            return "Local Multiplayer Chat";
        } else if ("single_player".equals(lastGameMode)) {
            return "Single Player Chat vs AI";
        } else {
            return "Multiplayer Chat";
        }
    }

    


    /**
     * Creates a simple test interface to demonstrate game communication.
     */
    private Scene createTestInterface(Stage primaryStage) {
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        
        // Title
        titleLabel = new javafx.scene.control.Label("🎮 Chatroom - " + getGameModeDisplayName());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #007bff;");
        
        // Game info (reflect start message if available)
        int playerCount = lastPlayers != null ? lastPlayers.size() : 0;
        String mode = lastGameMode != null ? lastGameMode : "unknown";
        infoLabelRef = new javafx.scene.control.Label(
            "Mode: " + mode + " | Players: " + playerCount + 
            ("local_multiplayer".equals(mode) ? " (Local Multiplayer)" : "") +
            ("single_player".equals(mode) ? " (Single Player vs AI)" : "")
        );
        infoLabelRef.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        
        // Status
        javafx.scene.control.Label statusLabel = new javafx.scene.control.Label("🎮 Game is running!");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #28a745; -fx-font-weight: bold;");
        
        // Local player label
        youLabelRef = new javafx.scene.control.Label("You are: " + (localPlayerId == null ? "(unknown)" : localPlayerId));
        youLabelRef.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        
        // Local multiplayer info label
        localMultiplayerLabel = new javafx.scene.control.Label();
        localMultiplayerLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #28a745; -fx-font-weight: bold;");
        if ("local_multiplayer".equals(lastGameMode)) {
            localMultiplayerLabel.setText("🎭 Local Multiplayer Mode: Select a player above to send messages as that player (Server simulator active for transcripts)");
        } else if ("single_player".equals(lastGameMode)) {
            localMultiplayerLabel.setText("🤖 Single Player Mode: Chat with AI - it will respond with random 6-letter messages (Server simulator active for transcripts)");
        } else {
            localMultiplayerLabel.setText("🎮 Multiplayer Mode: Standard chat functionality");
        }
        
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
        
        // Player selector for local multiplayer mode
        playerSelector = new javafx.scene.control.ComboBox<>();
        playerSelector.setPromptText("Select player...");
        playerSelector.setVisible(false); // Hidden by default
        
        // Update player selector when game mode changes
        if ("local_multiplayer".equals(lastGameMode) && !lastPlayers.isEmpty()) {
            playerSelector.getItems().clear();
            for (Map<String, Object> player : lastPlayers) {
                String playerId = String.valueOf(player.getOrDefault("id", "player"));
                playerSelector.getItems().add(playerId);
            }
            playerSelector.setValue(currentPlayerSelector);
            playerSelector.setVisible(true);
            
            // Update current player selector when selection changes
            playerSelector.setOnAction(e -> {
                currentPlayerSelector = playerSelector.getValue();
                Logging.info("🎭 Player selector changed to: " + currentPlayerSelector);
            });
        } else if ("single_player".equals(lastGameMode)) {
            playerSelector.setVisible(false);
        } else {
            playerSelector.setVisible(false);
        }
        
        javafx.scene.control.TextField chatInput = new javafx.scene.control.TextField();
        chatInput.setPromptText("Type a message and press Enter...");
        chatInput.setOnAction(e -> {
            String text = chatInput.getText().trim();
            if (!text.isEmpty()) {
                String sender = "me";
                if ("local_multiplayer".equals(lastGameMode) && currentPlayerSelector != null) {
                    sender = currentPlayerSelector;
                } else if (localPlayerId != null) {
                    sender = localPlayerId;
                }
                
                Logging.info("🔍 DEBUG: Chat input - lastGameMode=" + lastGameMode + ", isSinglePlayerMode=" + isSinglePlayerMode);
                Logging.info("🔍 DEBUG: Chat input - sender=" + sender + ", text=" + text);
                
                appendChat(sender, text);
                
                // Generate AI response in single player mode
                if (isSinglePlayerMode) {
                    Logging.info("🔍 DEBUG: Single player mode detected, generating AI response");
                    // Add a small delay to simulate thinking
                    javafx.application.Platform.runLater(() -> {
                        try {
                            Thread.sleep(500); // 500ms delay
                        } catch (InterruptedException ignored) {}
                        
                        String aiResponse = generateRandomMessage();
                        Logging.info("🔍 DEBUG: AI response generated: " + aiResponse);
                        appendChat("AI", aiResponse);
                        
                        // Log the AI response
                        Logging.info("🤖 AI generated response: " + aiResponse);
                        
                        // Send AI response to server simulator for transcript generation
                        java.util.Map<String, Object> aiMessage = new java.util.HashMap<>();
                        aiMessage.put("function", "message");
                        aiMessage.put("from", "AI");
                        aiMessage.put("text", aiResponse);
                        aiMessage.put("timestamp", java.time.Instant.now().toString());
                        MessagingBridge.publish(aiMessage);
                    });
                } else {
                    Logging.info("🔍 DEBUG: Not single player mode, skipping AI response");
                }
                
                // Always publish messages to server simulator for transcript generation and consistency
                java.util.Map<String, Object> out = new java.util.HashMap<>();
                out.put("function", "message");
                out.put("from", sender);
                out.put("text", text);
                out.put("timestamp", java.time.Instant.now().toString());
                MessagingBridge.publish(out);
                
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
            // Always send end message before stopping for transcript generation
            java.util.Map<String, Object> endMessage = new java.util.HashMap<>();
            endMessage.put("function", "end");
            endMessage.put("reason", "user_returned_to_lobby");
            endMessage.put("timestamp", java.time.Instant.now().toString());
            MessagingBridge.publish(endMessage);
            stopGame();
            // Use the messaging bridge to return to lobby instead of firing close event
            MessagingBridge.returnToLobby();
        });
        
        // Add components to root
        root.getChildren().addAll(
            titleLabel,
            infoLabelRef,
            youLabelRef,
            statusLabel,
            localMultiplayerLabel,
            new javafx.scene.control.Label("Players:"),
            playersTextRef,
            new javafx.scene.control.Label("All Messages:"),
            allMessagesAreaRef,
            new javafx.scene.control.Label("Chatroom:"),
            chatAreaRef,
            playerSelector,
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

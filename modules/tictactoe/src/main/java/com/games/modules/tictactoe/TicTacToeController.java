package com.games.modules.tictactoe;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.gdk.shared.game.GameOptions;
import com.gdk.shared.game.GameState;
import com.gdk.shared.game.GameEventHandler;
import com.gdk.shared.game.GameEvent;
import com.gdk.shared.game.GameMode;
import com.gdk.shared.utils.error_handling.Logging;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Controller for the TicTacToe game screen.
 * Handles game logic, UI updates, and user interactions.
 * This implements a local couch co-op mode where two players take turns using the same device.
 */
public class TicTacToeController implements Initializable {

    // Main container
    @FXML private BorderPane mainContainer;
    
    // Header components
    @FXML private Button backButton;
    @FXML private Text gameTitle;
    @FXML private Label statusLabel;
    @FXML private Label currentPlayerLabel;
    @FXML private Label timerLabel;
    @FXML private Label chatStatusLabel;
    @FXML private Label moveCountLabel;
    
    // Player information
    @FXML private ImageView player1Avatar;
    @FXML private Label player1Name;
    @FXML private Label player1Score;
    @FXML private ImageView player2Avatar;
    @FXML private Label player2Name;
    @FXML private Label player2Score;
    @FXML private Label matchIdLabel;
    
    // Game controls
    @FXML private Button restartGameButton;
    @FXML private Button forfeitGameButton;
    @FXML private Button exitGameButton;
    
    // Game board
    @FXML private GridPane gameBoard;
    
    // Game board buttons
    @FXML private Button btn00;
    @FXML private Button btn01;
    @FXML private Button btn02;
    @FXML private Button btn10;
    @FXML private Button btn11;
    @FXML private Button btn12;
    @FXML private Button btn20;
    @FXML private Button btn21;
    @FXML private Button btn22;
    
    // Chat components
    @FXML private TextArea chatMessagesArea;
    @FXML private TextField chatInputField;
    @FXML private Button sendMessageButton;
    
    // Move history
    @FXML private ListView<String> moveHistoryList;

    // Game state variables
    private List<Button> boardButtons;
    private TicTacToeGame game;
    private TicTacToePlayer player1;
    private TicTacToePlayer player2;
    private TicTacToePlayer currentPlayer;
    private int player1ScoreCount = 0;
    private int player2ScoreCount = 0;
    private boolean gameInProgress = false;
    private ObservableList<String> moveHistory = FXCollections.observableArrayList();
    private String matchId;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // Timer variables
    private javafx.animation.Timeline moveTimer;
    private int timeRemaining = 30;
    
    // Game module integration
    private GameMode gameMode;
    private int playerCount;
    private GameOptions gameOptions;
    private Stage primaryStage;
    private GameEventHandler eventHandler;

    /**
     * Initialize the controller.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Store all the board buttons in a list for easier access
        boardButtons = new ArrayList<>();
        boardButtons.add(btn00);
        boardButtons.add(btn01);
        boardButtons.add(btn02);
        boardButtons.add(btn10);
        boardButtons.add(btn11);
        boardButtons.add(btn12);
        boardButtons.add(btn20);
        boardButtons.add(btn21);
        boardButtons.add(btn22);
        
        // Initialize players with proper error handling
        try {
            // Create Player objects with simple usernames
            player1 = new TicTacToePlayer("Player 1", "X");
            player2 = new TicTacToePlayer("Player 2", "O");
            
            // Apply color effects to avatar images
            if (player1Avatar != null) {
                player1Avatar.setEffect(new javafx.scene.effect.ColorAdjust(0, 0.5, 0, -0.2)); // Blue tint
            }
            
            if (player2Avatar != null) {
                player2Avatar.setEffect(new javafx.scene.effect.ColorAdjust(0.7, 0.5, 0, 0)); // Red tint
            }
        } catch (Exception e) {
            Logging.error("Error initializing game: " + e.getMessage(), e);
        }

        // Initialize player information
        player1Name.setText(player1.getUsername());
        player2Name.setText(player2.getUsername());
        player1Score.setText("Score: " + player1ScoreCount);
        player2Score.setText("Score: " + player2ScoreCount);
        
        // Generate a random match ID
        matchId = "M" + (10000 + (int)(Math.random() * 90000));
        matchIdLabel.setText(matchId);
        
        // Set up chat functionality
        setupChat();
        
        // Set up move history
        moveHistoryList.setItems(moveHistory);
        
        // Initialize move count
        updateMoveCount();
        
        // Set up chat status
        if (chatStatusLabel != null) {
            chatStatusLabel.setText("Online");
        }
        
        // Initialize timer
        setupMoveTimer();
        
        // Start a new game
        startNewGame();
    }
    
    /**
     * Set the primary stage for navigation
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    public void setEventHandler(GameEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }
    
    /**
     * Initialize the game with parameters from the game module framework
     */
    public void initializeGame(GameMode gameMode, int playerCount, GameOptions gameOptions) {
        this.gameMode = gameMode;
        this.playerCount = playerCount;
        this.gameOptions = gameOptions;
        
        // Initialize players
        player1 = new TicTacToePlayer("Player 1", "X");
        player2 = new TicTacToePlayer("Player 2", "O");
        
        Logging.info("🎯 Initializing TicTacToe game with " + playerCount + " players, mode: " + gameMode.getDisplayName());
        
        // Apply any game-specific options
        if (gameOptions != null) {
            int timeLimit = gameOptions.getIntOption("timeLimit", 30);
            timeRemaining = timeLimit;
            updateTimerDisplay();
        }
        
        // Start the game
        startNewGame();
    }
    
    /**
     * Set up chat functionality
     */
    private void setupChat() {
        // Disable text area editing
        chatMessagesArea.setEditable(false);
        
        // Add initial message
        addSystemMessage("Game started. Have fun!");
        
        // Set up enter key press handling for chat input
        chatInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendChatMessage();
            }
        });
    }
    
    /**
     * Add a system message to the chat
     */
    private void addSystemMessage(String message) {
        String timeStamp = LocalTime.now().format(timeFormatter);
        chatMessagesArea.appendText("[" + timeStamp + "] System: " + message + "\n");
    }
    
    /**
     * Add a player message to the chat
     */
    private void addPlayerMessage(TicTacToePlayer player, String message) {
        String timeStamp = LocalTime.now().format(timeFormatter);
        String playerName = player.getUsername();
        chatMessagesArea.appendText("[" + timeStamp + "] " + playerName + ": " + message + "\n");
    }
    
    /**
     * Send a chat message
     */
    @FXML
    private void onSendMessageClicked() {
        sendChatMessage();
    }
    
    /**
     * Process and send the chat message
     */
    private void sendChatMessage() {
        String message = chatInputField.getText().trim();
        if (!message.isEmpty()) {
            addPlayerMessage(currentPlayer, message);
            chatInputField.clear();
        }
    }
    
    /**
     * Add a move to the move history
     */
    private void addMoveToHistory(TicTacToePlayer player, int row, int col) {
        String playerName = player.getUsername();
        String symbol = player.getSymbol();
        String move = playerName + " (" + symbol + ") → Row " + (row + 1) + ", Col " + (col + 1);
        moveHistory.add(move);
        
        // Scroll to the latest move
        moveHistoryList.scrollTo(moveHistory.size() - 1);
        
        // Update move count
        updateMoveCount();
    }

    /**
     * Handle board button clicks.
     */
    @FXML
    private void onBoardButtonClicked(javafx.event.ActionEvent event) {
        if (!gameInProgress) {
            showAlert("Game not started", "Please start a new game first.");
            return;
        }

        Button clickedButton = (Button) event.getSource();
        int position = getBoardPosition(clickedButton);

        // Ensure the button hasn't been played yet
        if (clickedButton.getText().isEmpty()) {
            makeMove(clickedButton, position);
        }
    }

    /**
     * Handle the forfeit game button click.
     */
    @FXML
    private void onForfeitGameClicked() {
        if (!gameInProgress) {
            showAlert("Game not started", "No active game to forfeit.");
            return;
        }
        
        // Current player forfeits
        TicTacToePlayer winner = (currentPlayer == player1) ? player2 : player1;
        
        // Update score
        if (winner == player1) {
            player1ScoreCount++;
            statusLabel.setText("Player 2 forfeited. Player 1 wins!");
            addSystemMessage(currentPlayer.getUsername() + " forfeited the game.");
        } else {
            player2ScoreCount++;
            statusLabel.setText("Player 1 forfeited. Player 2 wins!");
            addSystemMessage(currentPlayer.getUsername() + " forfeited the game.");
        }
        
        updateScoreLabels();
        gameInProgress = false;
        
        // Return to GDK after forfeit
        Logging.info("🏳️ Game forfeited - requesting return to GDK");
        onBackButtonClicked();
    }

    /**
     * Handle the back button click to return to the GDK.
     */
    @FXML
    private void onBackButtonClicked() {
        Logging.info("🔙 Back button clicked - requesting return to GDK");
        
        // Stop any active timers
        if (moveTimer != null) {
            moveTimer.stop();
            Logging.info("⏸️ Timer stopped");
        }
        
        // Send event to GDK to handle the return to lobby
        if (eventHandler != null) {
            eventHandler.handleGameEvent(new GameEvent(
                GameEvent.EventType.BACK_TO_LOBBY_REQUESTED,
                "tictactoe",
                "User requested return to GDK lobby"
            ));
            } else {
            Logging.warning("⚠️ No event handler available, cannot return to lobby");
        }
    }

    /**
     * Handle the new game button click.
     */
    @FXML
    private void onNewGameClicked() {
        startNewGame();
    }

    /**
     * Start a new game by resetting the board and initializing game state.
     */
    private void startNewGame() {
        // Reset UI
        for (Button button : boardButtons) {
            button.setText("");
            button.getStyleClass().remove("x");
            button.getStyleClass().remove("o");
            button.getStyleClass().remove("winning");
        }

        // Initialize game with players
        game = new TicTacToeGame(Arrays.asList(player1, player2));
        
        // Set player1 as the starting player
        currentPlayer = player1;
        
        // Update UI with current game state
        statusLabel.setText("Game started!");
        if (currentPlayerLabel != null) {
            currentPlayerLabel.setText("Player 1 (X)");
        }
        gameInProgress = true;
        
        // Clear move history for new game
        moveHistory.clear();
        updateMoveCount();
        addSystemMessage("New game started.");
        
        // Restart timer
        restartTimer();
        
        // Highlight current player's info
        highlightCurrentPlayer();
    }

    /**
     * Make a move on the board.
     * 
     * @param button The button that was clicked
     * @param position The board position (0-8)
     */
    private void makeMove(Button button, int position) {
        // Update game state
        game.place(currentPlayer, position);
        
        // Add move to history
        int row = position / 3;
        int col = position % 3;
        addMoveToHistory(currentPlayer, row, col);
        
        // Update UI
        if (currentPlayer == player1) {
            button.setText("X");
            button.getStyleClass().add("x");
        } else {
            button.setText("O");
            button.getStyleClass().add("o");
        }
        
        // Get the winner from the game
        TicTacToePlayer winner = game.getWinner();
        
        // Check for game end
        if (winner != null) {
            handleGameWon();
        } else if (game.isDrawn()) {
            handleGameDraw();
        } else {
            // Switch players
            currentPlayer = (currentPlayer == player1) ? player2 : player1;
            
            // Update turn indicators
            statusLabel.setText((currentPlayer == player1) ? "Player 1's turn" : "Player 2's turn");
            if (currentPlayerLabel != null) {
                currentPlayerLabel.setText((currentPlayer == player1) ? "Player 1 (X)" : "Player 2 (O)");
            }
            
            // Reset the timer for the new player's turn
            restartTimer();
            
            // Highlight current player's info
            highlightCurrentPlayer();
        }
    }

    /**
     * Highlight the current player's info panel
     */
    private void highlightCurrentPlayer() {
        if (currentPlayer == player1) {
            player1Name.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-player-x-color;");
            player2Name.setStyle("-fx-font-weight: normal; -fx-text-fill: -fx-text-color;");
        } else {
            player1Name.setStyle("-fx-font-weight: normal; -fx-text-fill: -fx-text-color;");
            player2Name.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-player-o-color;");
        }
    }

    /**
     * Handle game won event.
     */
    private void handleGameWon() {
        TicTacToePlayer winner = game.getWinner();
        
        // Pause the timer
        pauseTimer();
        
        // Update score
        if (winner == player1) {
            player1ScoreCount++;
            statusLabel.setText("Player 1 wins!");
            if (currentPlayerLabel != null) {
                currentPlayerLabel.setText("Player 1 (X)");
            }
            addSystemMessage("Player 1 wins the game!");
        } else {
            player2ScoreCount++;
            statusLabel.setText("Player 2 wins!");
            if (currentPlayerLabel != null) {
                currentPlayerLabel.setText("Player 2 (O)");
            }
            addSystemMessage("Player 2 wins the game!");
        }
        
        updateScoreLabels();
        highlightWinningCombination();
        gameInProgress = false;
    }

    /**
     * Handle game draw event.
     */
    private void handleGameDraw() {
        // Pause the timer
        pauseTimer();
        
        statusLabel.setText("Game ended in a draw!");
        if (currentPlayerLabel != null) {
            currentPlayerLabel.setText("Draw!");
        }
        gameInProgress = false;
        addSystemMessage("Game ended in a draw!");
    }

    /**
     * Update score labels with current scores.
     */
    private void updateScoreLabels() {
        player1Score.setText("Score: " + player1ScoreCount);
        player2Score.setText("Score: " + player2ScoreCount);
    }

    /**
     * Highlight the winning combination on the board.
     */
    private void highlightWinningCombination() {
        // Get the winning positions based on the current board state
        List<Integer> winningPositions = getWinningPositions();
        
        if (winningPositions != null && !winningPositions.isEmpty()) {
            for (Integer position : winningPositions) {
                boardButtons.get(position).getStyleClass().add("winning");
            }
        }
    }
    
    /**
     * Calculate the winning positions based on the current game state.
     * 
     * @return List of positions (0-8) that form the winning line, or null if no winning line
     */
    private List<Integer> getWinningPositions() {
        TicTacToePlayer winner = game.getWinner();
        
        if (winner == null) {
            return null;
        }
        
        String board = game.getBoard();
        List<Integer> positions = new ArrayList<>();
        
        // Check rows
        if (checkLine(board, 0, 1, 2, positions)) return positions;
        if (checkLine(board, 3, 4, 5, positions)) return positions;
        if (checkLine(board, 6, 7, 8, positions)) return positions;
        
        // Check columns
        if (checkLine(board, 0, 3, 6, positions)) return positions;
        if (checkLine(board, 1, 4, 7, positions)) return positions;
        if (checkLine(board, 2, 5, 8, positions)) return positions;
        
        // Check diagonals
        if (checkLine(board, 0, 4, 8, positions)) return positions;
        if (checkLine(board, 2, 4, 6, positions)) return positions;
        
        return null;
    }
    
    /**
     * Check if a line forms a winning combination and add positions to the list if it does.
     * 
     * @param board The game board
     * @param pos1 First position
     * @param pos2 Second position
     * @param pos3 Third position
     * @param positions List to add winning positions to
     * @return true if this line is a winning line
     */
    private boolean checkLine(String board, int pos1, int pos2, int pos3, List<Integer> positions) {
        char c1 = board.charAt(pos1);
        char c2 = board.charAt(pos2);
        char c3 = board.charAt(pos3);
        
        if ((c1 == 'X' || c1 == 'O') && c1 == c2 && c2 == c3) {
            positions.add(pos1);
            positions.add(pos2);
            positions.add(pos3);
            return true;
        }
        
        return false;
    }

    /**
     * Get the board position (0-8) from a button.
     */
    private int getBoardPosition(Button button) {
        return boardButtons.indexOf(button);
    }

    /**
     * Show an alert dialog.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Sets the match ID for this game session
     * @param matchId The match ID to set
     */
    public void setMatchId(String matchId) {
        this.matchId = matchId;
        if (matchIdLabel != null) {
            matchIdLabel.setText(matchId);
        }
    }

    /**
     * Set up the move timer
     */
    private void setupMoveTimer() {
        if (moveTimer != null) {
            moveTimer.stop();
        }
        
        timeRemaining = 30;
        updateTimerDisplay();
        
        moveTimer = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), event -> {
                timeRemaining--;
                updateTimerDisplay();
                
                if (timeRemaining <= 0) {
                    // Time's up for current player - optional: auto forfeit or skip turn
                    addSystemMessage("Time's up for " + currentPlayer.getUsername() + "!");
                    restartTimer();
                }
            })
        );
        
        moveTimer.setCycleCount(javafx.animation.Animation.INDEFINITE);
    }
    
    /**
     * Update the timer display
     */
    private void updateTimerDisplay() {
        if (timerLabel != null) {
            String timeText = String.format("%02d:%02d", timeRemaining / 60, timeRemaining % 60);
            timerLabel.setText(timeText);
            
            // Change color when time is running low
            if (timeRemaining <= 10) {
                timerLabel.setStyle("-fx-text-fill: -fx-danger-color; -fx-font-weight: bold;");
            } else {
                timerLabel.setStyle("-fx-text-fill: -fx-accent-color; -fx-font-weight: bold;");
            }
        }
    }
    
    /**
     * Start the move timer
     */
    private void startTimer() {
        if (moveTimer != null) {
            moveTimer.play();
        }
    }
    
    /**
     * Pause the move timer
     */
    private void pauseTimer() {
        if (moveTimer != null) {
            moveTimer.pause();
        }
    }
    
    /**
     * Restart the timer with full time
     */
    private void restartTimer() {
        if (moveTimer != null) {
            moveTimer.stop();
            timeRemaining = 30;
            updateTimerDisplay();
            moveTimer.play();
        }
    }
    
    /**
     * Update the move count display
     */
    private void updateMoveCount() {
        if (moveCountLabel != null) {
            moveCountLabel.setText("Moves: " + moveHistory.size());
        }
    }
} 
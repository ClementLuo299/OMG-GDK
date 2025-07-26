package tictactoe;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import gdk.Logging;
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
    private javafx.animation.Timeline moveTimer;
    private int timeRemaining = 30;
    private int playerCount = 2; // Default to 2 players
    private Stage primaryStage;
    private Main gameModule;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logging.info("🎮 Initializing TicTacToe Controller");
        
        try {
            // Initialize board buttons list
            boardButtons = Arrays.asList(btn00, btn01, btn02, btn10, btn11, btn12, btn20, btn21, btn22);
            
            // Set up move history
            moveHistoryList.setItems(moveHistory);
            
            // Set up chat
            setupChat();
            
            // Initialize game with default settings
            initializeGame();
            
            // Set up event handlers
            setupEventHandlers();
            
            Logging.info("✅ TicTacToe Controller initialized successfully");
            
        } catch (Exception e) {
            Logging.error("❌ Error initializing TicTacToe Controller: " + e.getMessage(), e);
        }
    }
    
    /**
     * Sets the primary stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Sets the game module reference
     */
    public void setGameModule(Main gameModule) {
        this.gameModule = gameModule;
    }
    
    /**
     * Initializes the game with default settings
     */
    public void initializeGame() {
        try {
            Logging.info("🎮 Initializing TicTacToe game");
            
            // Create players
            player1 = new TicTacToePlayer("Player 1", "X");
            player2 = new TicTacToePlayer("Player 2", "O");
            currentPlayer = player1;
            
            // Create game instance
            game = new TicTacToeGame();
            
            // Set up UI
            updatePlayerLabels();
            highlightCurrentPlayer();
            
            // Start new game
            startNewGame();
            
            Logging.info("✅ TicTacToe game initialized successfully");
            
        } catch (Exception e) {
            Logging.error("❌ Error initializing TicTacToe game: " + e.getMessage(), e);
        }
    }
    
    /**
     * Sets up chat functionality
     */
    private void setupChat() {
        chatInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendChatMessage();
            }
        });
        
        addSystemMessage("Welcome to Tic Tac Toe! Players take turns placing X and O on the board.");
    }
    
    /**
     * Adds a system message to the chat
     */
    private void addSystemMessage(String message) {
        String timestamp = LocalTime.now().format(timeFormatter);
        chatMessagesArea.appendText("[" + timestamp + "] System: " + message + "\n");
    }
    
    /**
     * Adds a player message to the chat
     */
    private void addPlayerMessage(TicTacToePlayer player, String message) {
        String timestamp = LocalTime.now().format(timeFormatter);
        chatMessagesArea.appendText("[" + timestamp + "] " + player.getName() + ": " + message + "\n");
    }
    
    /**
     * Handles send message button click
     */
    @FXML
    private void onSendMessageClicked() {
        sendChatMessage();
    }
    
    /**
     * Sends a chat message
     */
    private void sendChatMessage() {
        String message = chatInputField.getText().trim();
        if (!message.isEmpty()) {
            addPlayerMessage(currentPlayer, message);
            chatInputField.clear();
        }
    }
    
    /**
     * Adds a move to the history
     */
    private void addMoveToHistory(TicTacToePlayer player, int row, int col) {
        String timestamp = LocalTime.now().format(timeFormatter);
        String move = "[" + timestamp + "] " + player.getName() + " placed " + player.getSymbol() + " at (" + row + "," + col + ")";
        moveHistory.add(move);
        moveHistoryList.scrollTo(moveHistory.size() - 1);
    }
    
    /**
     * Handles board button clicks
     */
    @FXML
    private void onBoardButtonClicked(javafx.event.ActionEvent event) {
        if (!gameInProgress) return;
        
        Button clickedButton = (Button) event.getSource();
        int position = getBoardPosition(clickedButton);
        
        if (game.isValidMove(position)) {
            makeMove(clickedButton, position);
        } else {
            showAlert("Invalid Move", "That position is already taken!");
        }
    }
    
    /**
     * Handles forfeit game button click
     */
    @FXML
    private void onForfeitGameClicked() {
        if (gameInProgress) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Forfeit Game");
            alert.setHeaderText("Are you sure you want to forfeit?");
            alert.setContentText("This will end the current game.");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    Logging.info("🏳️ Game forfeited by " + currentPlayer.getName());
                    addSystemMessage(currentPlayer.getName() + " forfeited the game!");
                    handleGameWon();
                }
            });
        }
    }
    
    /**
     * Handles back button click
     */
    @FXML
    private void onBackButtonClicked() {
        Logging.info("🔙 Returning to lobby from TicTacToe");
        if (gameModule != null) {
            gameModule.stopGame();
        }
    }
    
    /**
     * Handles new game button click
     */
    @FXML
    private void onNewGameClicked() {
        startNewGame();
    }
    
    /**
     * Starts a new game
     */
    private void startNewGame() {
        Logging.info("🔄 Starting new TicTacToe game");
        
        // Reset game state
        game = new TicTacToeGame();
        gameInProgress = true;
        currentPlayer = player1;
        
        // Clear board
        for (Button button : boardButtons) {
            button.setText("");
            button.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        }
        
        // Update UI
        statusLabel.setText("Game in progress");
        currentPlayerLabel.setText("Current Player: " + currentPlayer.getName());
        highlightCurrentPlayer();
        
        // Clear move history
        moveHistory.clear();
        addSystemMessage("New game started! " + currentPlayer.getName() + " goes first.");
        
        // Start timer
        startTimer();
        
        Logging.info("✅ New TicTacToe game started");
    }
    
    /**
     * Makes a move on the board
     */
    private void makeMove(Button button, int position) {
        // Make the move
        game.makeMove(position, currentPlayer.getSymbol());
        button.setText(currentPlayer.getSymbol());
        
        // Style the button based on player
        if (currentPlayer == player1) {
            button.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #007bff;");
        } else {
            button.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
        }
        
        // Add to move history
        int row = position / 3;
        int col = position % 3;
        addMoveToHistory(currentPlayer, row, col);
        
        // Check for win or draw
        if (game.checkWin(currentPlayer.getSymbol())) {
            handleGameWon();
        } else if (game.isBoardFull()) {
            handleGameDraw();
        } else {
            // Switch players
            currentPlayer = (currentPlayer == player1) ? player2 : player1;
            currentPlayerLabel.setText("Current Player: " + currentPlayer.getName());
            highlightCurrentPlayer();
            
            // Restart timer
            restartTimer();
        }
    }
    
    /**
     * Highlights the current player
     */
    private void highlightCurrentPlayer() {
        if (currentPlayer == player1) {
            player1Name.setStyle("-fx-font-weight: bold; -fx-text-fill: #007bff;");
            player2Name.setStyle("-fx-font-weight: normal; -fx-text-fill: black;");
        } else {
            player1Name.setStyle("-fx-font-weight: normal; -fx-text-fill: black;");
            player2Name.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc3545;");
        }
    }
    
    /**
     * Handles game won
     */
    private void handleGameWon() {
        gameInProgress = false;
        
        // Update score
        if (currentPlayer == player1) {
            player1ScoreCount++;
        } else {
            player2ScoreCount++;
        }
        
        updateScoreLabels();
        
        // Highlight winning combination
        highlightWinningCombination();
        
        // Show win message
        String message = currentPlayer.getName() + " wins!";
        statusLabel.setText(message);
        addSystemMessage(message);
        
        showAlert("Game Over", message);
        
        // Stop timer
        pauseTimer();
        
        Logging.info("🏆 " + message);
    }
    
    /**
     * Handles game draw
     */
    private void handleGameDraw() {
        gameInProgress = false;
        
        String message = "It's a draw!";
        statusLabel.setText(message);
        addSystemMessage(message);
        
        showAlert("Game Over", message);
        
        // Stop timer
        pauseTimer();
        
        Logging.info("🤝 " + message);
    }
    
    /**
     * Updates score labels
     */
    private void updateScoreLabels() {
        player1Score.setText(String.valueOf(player1ScoreCount));
        player2Score.setText(String.valueOf(player2ScoreCount));
    }
    
    /**
     * Highlights the winning combination
     */
    private void highlightWinningCombination() {
        List<Integer> winningPositions = getWinningPositions();
        for (int position : winningPositions) {
            boardButtons.get(position).setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #28a745; -fx-background-color: #d4edda;");
        }
    }
    
    /**
     * Gets the winning positions
     */
    private List<Integer> getWinningPositions() {
        List<Integer> positions = new ArrayList<>();
        String board = game.getBoard();
        
        // Check rows
        for (int i = 0; i < 9; i += 3) {
            if (checkLine(board, i, i + 1, i + 2, positions)) {
                return positions;
            }
        }
        
        // Check columns
        for (int i = 0; i < 3; i++) {
            if (checkLine(board, i, i + 3, i + 6, positions)) {
                return positions;
            }
        }
        
        // Check diagonals
        if (checkLine(board, 0, 4, 8, positions)) {
            return positions;
        }
        if (checkLine(board, 2, 4, 6, positions)) {
            return positions;
        }
        
        return positions;
    }
    
    /**
     * Checks if a line has the same symbol
     */
    private boolean checkLine(String board, int pos1, int pos2, int pos3, List<Integer> positions) {
        char symbol = board.charAt(pos1);
        if (symbol != ' ' && symbol == board.charAt(pos2) && symbol == board.charAt(pos3)) {
            positions.clear();
            positions.add(pos1);
            positions.add(pos2);
            positions.add(pos3);
            return true;
        }
        return false;
    }
    
    /**
     * Gets the board position from a button
     */
    private int getBoardPosition(Button button) {
        return boardButtons.indexOf(button);
    }
    
    /**
     * Shows an alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Sets the match ID
     */
    public void setMatchId(String matchId) {
        this.matchId = matchId;
        if (matchIdLabel != null) {
            matchIdLabel.setText("Match ID: " + matchId);
        }
    }
    
    /**
     * Sets up the move timer
     */
    private void setupMoveTimer() {
        moveTimer = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), event -> {
                timeRemaining--;
                updateTimerDisplay();
                if (timeRemaining <= 0) {
                    pauseTimer();
                    addSystemMessage("Time's up! " + currentPlayer.getName() + " loses by timeout.");
                    handleGameWon();
                }
            })
        );
        moveTimer.setCycleCount(javafx.animation.Animation.INDEFINITE);
    }
    
    /**
     * Updates the timer display
     */
    private void updateTimerDisplay() {
        if (timerLabel != null) {
            timerLabel.setText("Time: " + timeRemaining + "s");
        }
    }
    
    /**
     * Starts the timer
     */
    private void startTimer() {
        if (moveTimer == null) {
            setupMoveTimer();
        }
        timeRemaining = 30;
        updateTimerDisplay();
        moveTimer.play();
    }
    
    /**
     * Pauses the timer
     */
    private void pauseTimer() {
        if (moveTimer != null) {
            moveTimer.pause();
        }
    }
    
    /**
     * Restarts the timer
     */
    private void restartTimer() {
        timeRemaining = 30;
        updateTimerDisplay();
        if (moveTimer != null) {
            moveTimer.play();
        }
    }
    
    /**
     * Updates the move count display
     */
    private void updateMoveCount() {
        if (moveCountLabel != null) {
            moveCountLabel.setText("Moves: " + moveHistory.size());
        }
    }
    
    /**
     * Updates player labels
     */
    private void updatePlayerLabels() {
        if (player1Name != null) {
            player1Name.setText(player1.getName());
        }
        if (player2Name != null) {
            player2Name.setText(player2.getName());
        }
        if (player1Score != null) {
            player1Score.setText(String.valueOf(player1ScoreCount));
        }
        if (player2Score != null) {
            player2Score.setText(String.valueOf(player2ScoreCount));
        }
    }
    
    /**
     * Sets up event handlers
     */
    private void setupEventHandlers() {
        // Add any additional event handlers here
    }
} 
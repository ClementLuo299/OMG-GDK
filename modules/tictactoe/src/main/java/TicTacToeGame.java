

import java.util.List;

/**
 * TicTacToe game implementation.
 * Manages the game board, validates moves, and detects wins/draws.
 */
public class TicTacToeGame {
    
    private static final char PLAYER_ONE_CHAR = 'X';
    private static final char PLAYER_TWO_CHAR = 'O';
    
    // Board state - stored as a string where each character represents a position
    // "123456789" where 1-9 will be X for player 1, O for player 2, or
    // the initial number for an unclaimed box.
    // _____________
    // | 1 | 2 | 3 |
    // | 4 | 5 | 6 |
    // | 7 | 8 | 9 |
    // '---'---'---'
    private String board;
    
    private final List<TicTacToePlayer> players;
    private TicTacToePlayer currentPlayer;
    private TicTacToePlayer winner;

    /**
     * Creates a new TicTacToe game with default players.
     */
    public TicTacToeGame() {
        this.players = List.of(
            new TicTacToePlayer("Player 1", "X"),
            new TicTacToePlayer("Player 2", "O")
        );
        this.board = "123456789";
        this.currentPlayer = players.get(0); // Player 1 starts
        this.winner = null;
    }
    
    /**
     * Creates a new TicTacToe game.
     * 
     * @param players List of players, should contain exactly 2 players
     */
    public TicTacToeGame(List<TicTacToePlayer> players) {
        if (players.size() != 2) {
            throw new IllegalArgumentException("TicTacToe requires exactly 2 players");
        }
        
        this.players = players;
        this.board = "123456789";
        this.currentPlayer = players.get(0); // Player 1 starts
        this.winner = null;
    }

    /**
     * Gets the current board state.
     * @return The board state as a string
     */
    public String getBoard() {
        return this.board;
    }

    /**
     * Places a mark on the board.
     * 
     * @param player The player making the move
     * @param position The position to place the mark (0-8)
     * @return true if the move was successful, false otherwise
     */
    public boolean place(TicTacToePlayer player, int position) {
        // Validate position
        if (position < 0 || position > 8) {
            return false; // Invalid position
        }
        
        // Check if position is already taken
        if (this.board.charAt(position) == PLAYER_ONE_CHAR ||
            this.board.charAt(position) == PLAYER_TWO_CHAR) {
            return false; // Position already taken
        }
        
        // Check if it's the player's turn
        if (player != currentPlayer) {
            return false; // Not this player's turn
        }
        
        // Place the mark
        char mark = player.getSymbol().equals("X") ? PLAYER_ONE_CHAR : PLAYER_TWO_CHAR;
        this.board = this.board.substring(0, position) + mark + this.board.substring(position + 1);
        
        // Check for win
        TicTacToePlayer winCheck = checkWinner();
        if (winCheck != null) {
            this.winner = winCheck;
        }
        
        // Switch turns if no winner
        if (this.winner == null) {
            this.currentPlayer = (this.currentPlayer == players.get(0)) ? players.get(1) : players.get(0);
        }
        
        return true;
    }

    /**
     * Checks if a player has won.
     * @return The winning player, or null if no winner
     */
    private TicTacToePlayer checkWinner() {
        // Check rows
        if (checkLine(0, 1, 2)) return getPlayerAtPosition(0);
        if (checkLine(3, 4, 5)) return getPlayerAtPosition(3);
        if (checkLine(6, 7, 8)) return getPlayerAtPosition(6);
        
        // Check columns
        if (checkLine(0, 3, 6)) return getPlayerAtPosition(0);
        if (checkLine(1, 4, 7)) return getPlayerAtPosition(1);
        if (checkLine(2, 5, 8)) return getPlayerAtPosition(2);
        
        // Check diagonals
        if (checkLine(0, 4, 8)) return getPlayerAtPosition(0);
        if (checkLine(2, 4, 6)) return getPlayerAtPosition(2);
        
        return null;
    }
    
    /**
     * Checks if a line (row, column, or diagonal) has the same mark.
     * 
     * @param pos1 First position
     * @param pos2 Second position
     * @param pos3 Third position
     * @return true if all positions have the same mark (X or O)
     */
    private boolean checkLine(int pos1, int pos2, int pos3) {
        char c1 = this.board.charAt(pos1);
        char c2 = this.board.charAt(pos2);
        char c3 = this.board.charAt(pos3);
        
        return (c1 == PLAYER_ONE_CHAR || c1 == PLAYER_TWO_CHAR) && 
               c1 == c2 && c2 == c3;
    }
    
    /**
     * Gets the player at a given position.
     * 
     * @param position The board position
     * @return The player at that position, or null if no player
     */
    private TicTacToePlayer getPlayerAtPosition(int position) {
        char mark = this.board.charAt(position);
        if (mark == PLAYER_ONE_CHAR) {
            return players.get(0);
        } else if (mark == PLAYER_TWO_CHAR) {
            return players.get(1);
        }
        return null;
    }

    /**
     * Checks if the game is a draw.
     * @return true if the game is a draw, false otherwise
     */
    public boolean isDrawn() {
        // If there's a winner, it's not a draw
        if (this.winner != null) {
            return false;
        }
        
        // Check if the board is full
        for (int i = 0; i < 9; i++) {
            char c = this.board.charAt(i);
            if (c != PLAYER_ONE_CHAR && c != PLAYER_TWO_CHAR) {
                return false; // Board still has empty spaces
            }
        }
        
        // If no winner and the board is full, it's a draw
        return true;
    }
    
    /**
     * Gets the current player.
     * @return The current player
     */
    public TicTacToePlayer getCurrentPlayer() {
        return currentPlayer;
    }
    
    /**
     * Gets the winner of the game.
     * @return The winning player, or null if no winner
     */
    public TicTacToePlayer getWinner() {
        return winner;
    }
    
    /**
     * Gets the list of players.
     * @return The list of players
     */
    public List<TicTacToePlayer> getPlayers() {
        return players;
    }
    
    /**
     * Checks if the game is over (either won or drawn).
     * @return true if the game is over
     */
    public boolean isGameOver() {
        return winner != null || isDrawn();
    }
    
    /**
     * Resets the game to its initial state.
     */
    public void reset() {
        this.board = "123456789";
        this.currentPlayer = players.get(0);
        this.winner = null;
    }
    
    /**
     * Checks if a move is valid at the given position.
     * @param position The position to check (0-8)
     * @return true if the move is valid, false otherwise
     */
    public boolean isValidMove(int position) {
        if (position < 0 || position > 8) {
            return false; // Invalid position
        }
        
        // Check if position is already taken
        return this.board.charAt(position) != PLAYER_ONE_CHAR && 
               this.board.charAt(position) != PLAYER_TWO_CHAR;
    }
    
    /**
     * Makes a move at the given position with the given symbol.
     * @param position The position to place the mark (0-8)
     * @param symbol The symbol to place (X or O)
     * @return true if the move was successful, false otherwise
     */
    public boolean makeMove(int position, String symbol) {
        if (!isValidMove(position)) {
            return false;
        }
        
        char mark = symbol.equals("X") ? PLAYER_ONE_CHAR : PLAYER_TWO_CHAR;
        this.board = this.board.substring(0, position) + mark + this.board.substring(position + 1);
        
        // Check for win
        TicTacToePlayer winCheck = checkWinner();
        if (winCheck != null) {
            this.winner = winCheck;
        }
        
        return true;
    }
    
    /**
     * Checks if the given symbol has won.
     * @param symbol The symbol to check (X or O)
     * @return true if the symbol has won, false otherwise
     */
    public boolean checkWin(String symbol) {
        char mark = symbol.equals("X") ? PLAYER_ONE_CHAR : PLAYER_TWO_CHAR;
        
        // Check rows
        if (checkLine(0, 1, 2) && getMarkAtPosition(0) == mark) return true;
        if (checkLine(3, 4, 5) && getMarkAtPosition(3) == mark) return true;
        if (checkLine(6, 7, 8) && getMarkAtPosition(6) == mark) return true;
        
        // Check columns
        if (checkLine(0, 3, 6) && getMarkAtPosition(0) == mark) return true;
        if (checkLine(1, 4, 7) && getMarkAtPosition(1) == mark) return true;
        if (checkLine(2, 5, 8) && getMarkAtPosition(2) == mark) return true;
        
        // Check diagonals
        if (checkLine(0, 4, 8) && getMarkAtPosition(0) == mark) return true;
        if (checkLine(2, 4, 6) && getMarkAtPosition(2) == mark) return true;
        
        return false;
    }
    
    /**
     * Checks if the board is full (draw).
     * @return true if the board is full, false otherwise
     */
    public boolean isBoardFull() {
        for (int i = 0; i < 9; i++) {
            if (this.board.charAt(i) != PLAYER_ONE_CHAR && this.board.charAt(i) != PLAYER_TWO_CHAR) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Gets the mark at the given position.
     * @param position The position to check (0-8)
     * @return The mark at the position
     */
    private char getMarkAtPosition(int position) {
        return this.board.charAt(position);
    }
}
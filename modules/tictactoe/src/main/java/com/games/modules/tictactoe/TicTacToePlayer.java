package com.games.modules.tictactoe;

/**
 * Represents a player in the TicTacToe game.
 * Each player has a symbol (X or O) and a username.
 */
public class TicTacToePlayer {
    
    private final String username;
    private final String symbol;
    
    /**
     * Creates a new TicTacToe player.
     * 
     * @param username The username for this player
     * @param symbol The player's symbol (X or O)
     */
    public TicTacToePlayer(String username, String symbol) {
        this.username = username;
        this.symbol = symbol;
    }
    
    /**
     * Gets the player's username.
     * @return The username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Gets the player's symbol (X or O).
     * @return The player's symbol
     */
    public String getSymbol() {
        return symbol;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TicTacToePlayer that = (TicTacToePlayer) obj;
        return username.equals(that.username) && symbol.equals(that.symbol);
    }
    
    @Override
    public int hashCode() {
        return username.hashCode() * 31 + symbol.hashCode();
    }
    
    @Override
    public String toString() {
        return "TicTacToePlayer{" +
                "username='" + username + '\'' +
                ", symbol='" + symbol + '\'' +
                '}';
    }
} 
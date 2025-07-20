package com.games.modules.tictactoe;

import com.entities.UserAccount;

/**
 * Represents a player in the TicTacToe game.
 * Each player has a symbol (X or O) and a reference to their UserAccount.
 */
public class TicTacToePlayer {
    
    private final UserAccount account;
    private final String symbol;
    
    /**
     * Creates a new TicTacToe player.
     * 
     * @param account The user account for this player
     * @param symbol The player's symbol (X or O)
     */
    public TicTacToePlayer(UserAccount account, String symbol) {
        this.account = account;
        this.symbol = symbol;
    }
    
    /**
     * Gets the user account for this player.
     * @return The user account
     */
    public UserAccount getAccount() {
        return account;
    }
    
    /**
     * Gets the player's symbol (X or O).
     * @return The player's symbol
     */
    public String getSymbol() {
        return symbol;
    }
    
    /**
     * Gets the player's username.
     * @return The username
     */
    public String getUsername() {
        return account.getUsername();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TicTacToePlayer that = (TicTacToePlayer) obj;
        return account.equals(that.account) && symbol.equals(that.symbol);
    }
    
    @Override
    public int hashCode() {
        return account.hashCode() * 31 + symbol.hashCode();
    }
    
    @Override
    public String toString() {
        return "TicTacToePlayer{" +
                "username='" + getUsername() + '\'' +
                ", symbol='" + symbol + '\'' +
                '}';
    }
} 
package com.headtrixz.game;

import com.headtrixz.game.helpers.GameModelHelper;
import com.headtrixz.game.players.HumanPlayer;
import com.headtrixz.game.players.Player;
import java.util.List;
import javafx.scene.paint.Color;

/**
 * The base for all games.
 */
public abstract class GameModel {
    /**
     * The state of the game.
     */
    public enum GameState {
        PLAYING,
        PLAYER_ONE_WON,
        PLAYER_TWO_WON,
        DRAW
    }

    protected Color backgroundColor;
    protected GameBoard board;
    protected Player currentPlayer;
    protected GameModelHelper helper;
    protected String name;
    protected Player[] players;
    protected String[] images;

    protected volatile int guiMove = -1;

    /**
     * The base for all games.
     *
     * @param name      The name of the game.
     * @param boardSize The size of the board.
     */
    public GameModel(String name, int boardSize, Color backgroundColor, String... images) {
        this.name = name;
        this.backgroundColor = backgroundColor;
        this.board = new GameBoard(boardSize);
        this.images = images;
    }

    /**
     * clone the GameModel.
     *
     * @return a clone of the game model.
     */
    public GameModel clone() {
        try {
            GameModel gameClone = getClass().getDeclaredConstructor().newInstance();

            // assign current state to new game
            gameClone.board = board.clone();
            gameClone.currentPlayer = currentPlayer;
            gameClone.helper = helper.clone(gameClone);
            gameClone.players = players.clone();

            return gameClone;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the background color for the GUI to use for this game.
     *
     * @return The color.
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Returns the original board. WARNING: Do NOT modify the board.
     *
     * @return The original board.
     */
    public GameBoard getBoard() {
        return board;
    }

    /**
     * Returns the player whose turn it currently is.
     *
     * @return The current player.
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Returns the move a human player has set. Will return -1 if none is set.
     *
     * @return The move a human player has set.
     */
    public int getGuiMove() {
        return guiMove;
    }

    /**
     * Get the helper.
     *
     * @return the helper.
     */
    public GameModelHelper getHelper() {
        return helper;
    }

    /**
     * Returns the image to use for the player on the game grid.
     *
     * @param player The player to get the image for.
     * @return An image.
     */
    public String getImage(int player) {
        return images[player];
    }

    /**
     * Returns the player at the given index.
     *
     * @param i The index of the player you want to get.
     * @return The player at the index i.
     */
    public Player getPlayer(int i) {
        return players[i];
    }

    /**
     * Returns the player with the given username.
     *
     * @param username The username of the player to get.
     * @return A player object.
     */
    public Player getPlayer(String username) {
        for (Player player : players) {
            if (player.getUsername().equals(username)) {
                return player;
            }
        }

        throw new RuntimeException("Unknown player: " + username);
    }

    /**
     * Return the next player.
     *
     * @return The opponent of the current player.
     */
    public Player getOpponent() {
        return getOpponent(currentPlayer);
    }

    /**
     * Returns the opponent of the passed in player.
     *
     * @param player The player to get the opponent of.
     * @return The opponent of the passed player.
     */
    public Player getOpponent(Player player) {
        return getPlayer(player.getId() % players.length);
    }

    /**
     * Initializes the game.
     *
     * @param helper A helper class for either an offline or online game.
     * @param players The players that participate in the game.
     */
    public void initialize(GameModelHelper helper, Player... players) {
        this.currentPlayer = players[0];
        this.helper = helper;
        this.players = players;
        for (int i = 0; i < players.length; i++) {
            players[i].setId(i + 1);
        }

        helper.initialize();
    }

    /**
     * Sets the current player to the given player.
     *
     * @param player The player whose turn it is.
     */
    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }

    /**
     * If the move is valid, set the guiMove variable to the move.
     *
     * @param move The move that the player wants to make.
     */
    public void setGuiMove(int move) {
        if (!(currentPlayer instanceof HumanPlayer)) {
            return;
        }

        if (move == -1 || isValidMove(move)) {
            guiMove = move;
        }
    }

    /**
     * Returns the score of the current player at the current depth.
     *
     * @param currentPlayer The player whose turn it is to move.
     * @param depth         The depth of the current node in the tree.
     * @return The score of the current player.
     */
    public abstract float getScore(Player currentPlayer, int depth);

    /**
     * Returns the current state of the game.
     *
     * @return The current state of the game.
     */
    public abstract GameState getState();

    /**
     * Returns a list of all available cells on the board.
     *
     * @param player The player to get the valid moves for.
     * @return A list of all available cells on the board.
     */
    public abstract List<Integer> getValidMoves(int player);

    /**
     * Returns whether the player has any available cells.
     *
     * @param player The player to check.
     * @return Whether the player has any available cells.
     */
    public abstract boolean hasValidMoves(int player);

    /**
     * Returns whether the move is allowed to be set.
     *
     * @param move The move to be checked.
     * @return Whether the move is valid.
     */
    public abstract boolean isValidMove(int move);

    /**
     * Sets the move for a specific player.
     *
     * @param move   The move that the player wants to make.
     * @param player The player who is making the move.
     */
    public abstract void setMove(int move, int player);
}

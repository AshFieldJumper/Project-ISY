package com.headtrixz.game;

import static com.headtrixz.game.GameBoard.EMPTY_CELL;
import static com.headtrixz.game.GameBoard.PLAYER_ONE;
import static com.headtrixz.game.GameBoard.PLAYER_TWO;

import com.headtrixz.game.players.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game of Tic Tac Toe.
 */
public class TicTacToe extends GameModel {
    private static final int BOARD_SIZE = 3;
    private static final String NAME = "Tic-Tac-Toe";

    /**
     * Represents a game of Tic Tac Toe.
     */
    public TicTacToe() {
        super(NAME, BOARD_SIZE);
    }

    /**
     * Returns the current state of the game.
     *
     * @return The current state of the game.
     */
    @Override
    public GameState getState() {
        GameState state = helper.getState();
        if (state != null) {
            return state;
        }

        if (hasWon(PLAYER_ONE)) {
            return GameState.PLAYER_ONE_WON;
        }

        if (hasWon(PLAYER_TWO)) {
            return GameState.PLAYER_TWO_WON;
        }

        if (isFull()) {
            return GameState.DRAW;
        }

        return GameState.PLAYING;
    }

    /**
     * get the score of the game in its current state. the scoring is
     * 80 if current player has won, -80 if current player has lost,
     * 0 if game is still going or ended in draw. and 40 if currentPlayer
     * has the center cell and game is still playing
     *
     * @return the score of the board
     */
    public int getScore(Player currentPlayer, int depth, int maxDepth) {
        double depthPenalty = depth / (double) maxDepth;

        switch (getState()) {
            case DRAW, PLAYING -> {
                if (getBoard().getMove(1, 1) == currentPlayer.getId()) {
                    return (int) (40 * depthPenalty);
                }
                return 0;
            }
            case PLAYER_ONE_WON, PLAYER_TWO_WON -> {
                return (int) ((hasPlayerWon(currentPlayer) ? 80 : -80) * depthPenalty);
            }
            default -> {
                System.out.println("there was a error. unknown state");
                return 0;
            }
        }
    }

    /**
     * Returns a list of all available cells on the board.
     *
     * @return A list of all available cells on the board.
     */
    public List<Integer> getValidMoves() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < board.getCellCount(); i++) {
            if (board.getMove(i) == EMPTY_CELL) {
                list.add(i);
            }
        }

        return list;
    }

    /**
     * If the player has three of their moves in a row, column, or diagonal, then they have won.
     *
     * @param player The player who is being checked for a win.
     * @return Whether the player has won.
     */
    private boolean hasWon(int player) {
        return player == board.getMove(0, 0) && player == board.getMove(1, 0) && player == board.getMove(2, 0)
                || player == board.getMove(0, 1) && player == board.getMove(1, 1) && player == board.getMove(2, 1)
                || player == board.getMove(0, 2) && player == board.getMove(1, 2) && player == board.getMove(2, 2)

                || player == board.getMove(0, 0) && player == board.getMove(0, 1) && player == board.getMove(0, 2)
                || player == board.getMove(1, 0) && player == board.getMove(1, 1) && player == board.getMove(1, 2)
                || player == board.getMove(2, 0) && player == board.getMove(2, 1) && player == board.getMove(2, 2)

                || player == board.getMove(0, 0) && player == board.getMove(1, 1) && player == board.getMove(2, 2)
                || player == board.getMove(0, 2) && player == board.getMove(1, 1) && player == board.getMove(2, 0);
    }

    /**
     * Returns whether the board has no empty cells left.
     *
     * @return Whether the board has no empty cells left.
     */
    public boolean isFull() {
        for (int cell : board.getCells()) {
            if (cell == EMPTY_CELL) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns whether the move is within the bounds of the board and the cell is empty.
     *
     * @param move The move to be checked
     * @return Whether the move is valid.
     */
    public boolean isValidMove(int move) {
        return move >= 0
                && move < board.getCellCount()
                && board.getMove(move) == EMPTY_CELL;
    }

    /**
     * Sets the move for a specific player.
     *
     * @param move The move that the player wants to make.
     * @param player The player who is making the move.
     */
    public void setMove(int move, int player) {
        board.setMove(move, player);
    }
}

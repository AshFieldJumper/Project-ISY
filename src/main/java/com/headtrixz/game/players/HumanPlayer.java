package com.headtrixz.game.players;

import com.headtrixz.game.GameModel;
import com.headtrixz.game.players.Player;

public class HumanPlayer extends Player {
    private final GameModel game;

    public HumanPlayer(GameModel game, String username) {
        super(username);
        this.game = game;
    }

    @Override
    public int getMove() {
        while (game.getState() == GameModel.GameState.PLAYING) {
            int move = game.getGuiMove();
            if (move != -1) {
                game.setGuiMove(-1);
                return move;
            }
        }

        return -1;
    }
}
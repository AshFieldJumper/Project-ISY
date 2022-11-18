package com.headtrixz.game.helpers;

import com.headtrixz.game.GameModel;
import com.headtrixz.game.players.Player;
import com.headtrixz.game.players.RemotePlayer;
import com.headtrixz.networking.Connection;
import com.headtrixz.networking.InputHandler;
import com.headtrixz.networking.ServerMessage;
import com.headtrixz.networking.ServerMessageType;
import javafx.application.Platform;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Represents a helper class that handles the game logic for an online game.
 */
public class OnlineHelper implements GameModelHelper {
    private Connection connection;
    private GameModel game;
    private Player localPlayer;
    private GameModel.GameState state;

    /**
     * Represents a helper class that handles the game logic for an online game.
     *
     * @param game The game the helper is for.
     */
    public OnlineHelper(GameModel game) {
        this.game = game;
    }

    /**
     * Ends the game and heads to the finish screen.
     */
    private void endGame() {
        unsubscribeAll();
        Platform.runLater(() -> {
            game.getController().endGame();
        });
    }

    /**
     * Ends the game when the player forfeits.
     */
    @Override
    public void forfeit() {
        unsubscribeAll();
        connection.getOutputHandler().forfeit();
    }

    /**
     * Returns the current state of the game.
     *
     * @return The current state of the game.
     */
    @Override
    public GameModel.GameState getState() {
        return state;
    }

    /**
     * Initializes the helper.
     */
    @Override
    public void initialize() {
        this.connection = Connection.getInstance();
        this.localPlayer = game.getPlayer(0) instanceof RemotePlayer
                ? game.getPlayer(1)
                : game.getPlayer(0);

        InputHandler input = connection.getInputHandler();
        input.on(ServerMessageType.MOVE, onMove);
        input.on(ServerMessageType.DRAW, onDraw);
        input.on(ServerMessageType.LOSS, onLoss);
        input.on(ServerMessageType.WIN, onWin);
        input.on(ServerMessageType.YOURTURN, onYourTurn);
    }

    /**
     * Tells the player it's their turn and handles their move.
     *
     * @param player The player whose turn it is.
     */
    @Override
    public void nextTurn(Player player) {
        player.onTurn(m -> {
            connection.getOutputHandler().move(m);
        });
    }

    /**
     * A listener for the "SVR GAME MOVE" event.
     */
    private final Consumer<ServerMessage> onMove = message -> {
        HashMap<String, String> obj = message.getObject();
        Player player = game.getPlayer(obj.get("PLAYER"));
        int move = Integer.parseInt(obj.get("MOVE"));

        game.getBoard().setMove(move, player.getId());
        Platform.runLater(() -> {
            game.getController().update(move, player);
        });
    };

    /**
     * A listener for the "SVR GAME DRAW" event.
     */
    private final Consumer<ServerMessage> onDraw = message -> {
        state = GameModel.GameState.DRAW;
        endGame();
    };

    /**
     * A listener for the "SVR GAME LOSS" event.
     */
    private final Consumer<ServerMessage> onLoss = message -> {
        state = localPlayer.getId() == 1
                ? GameModel.GameState.PLAYER_TWO_WON
                : GameModel.GameState.PLAYER_ONE_WON;

        endGame();
    };

    /**
     * A listener for the "SVR GAME WIN" event.
     */
    private final Consumer<ServerMessage> onWin = message -> {
        state = localPlayer.getId() == 1
                ? GameModel.GameState.PLAYER_ONE_WON
                : GameModel.GameState.PLAYER_TWO_WON;

        endGame();
    };

    /**
     * A listener for the "SVR GAME YOURTURN" event.
     */
    private final Consumer<ServerMessage> onYourTurn = message -> {
        nextTurn(localPlayer);
    };

    /**
     * Unsubscribes all listeners.
     */
    private void unsubscribeAll() {
        InputHandler input = connection.getInputHandler();
        input.off(ServerMessageType.MOVE, onMove);
        input.off(ServerMessageType.DRAW, onDraw);
        input.off(ServerMessageType.LOSS, onLoss);
        input.off(ServerMessageType.WIN, onWin);
        input.off(ServerMessageType.YOURTURN, onYourTurn);
    }
}

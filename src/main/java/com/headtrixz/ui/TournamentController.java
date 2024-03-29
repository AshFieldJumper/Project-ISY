package com.headtrixz.ui;

import com.headtrixz.game.GameBoard;
import com.headtrixz.game.GameMethods;
import com.headtrixz.game.GameModel;
import com.headtrixz.game.Othello;
import com.headtrixz.game.TicTacToe;
import com.headtrixz.game.helpers.GameModelHelper;
import com.headtrixz.game.helpers.OnlineHelper;
import com.headtrixz.game.players.AIPlayer;
import com.headtrixz.game.players.Player;
import com.headtrixz.game.players.RemotePlayer;
import com.headtrixz.networking.Connection;
import com.headtrixz.networking.InputListener;
import com.headtrixz.networking.ServerMessageType;
import com.headtrixz.ui.elements.GameGrid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Controller for the tournament screen.
 */
public class TournamentController implements GameMethods {
    private static final int INTERVAL = 5000;

    @FXML
    private StackPane containerStackPane;
    @FXML
    private Label drawsLabel;
    @FXML
    private Label loggedInAsLabel;
    @FXML
    private Label losesLabel;
    @FXML
    private Label onlineLabel;
    @FXML
    private ImageView playerOneIcon;
    @FXML
    private Label playerOneLabel;
    @FXML
    private ListView<String> playersListView;
    @FXML
    private ImageView playerTwoIcon;
    @FXML
    private Label playerTwoLabel;
    @FXML
    private Label winsLabel;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private GameModel game;
    private GameGrid gameGrid;
    private int drawCount;
    private int loseCount;
    private int winCount;
    private String username;

    /**
     * FXML init method. Logs into the server when the screen has loaded.
     */
    public void initialize() {
        username = UIManager.getSetting("username");
        loggedInAsLabel.setText(String.format("Ingelogd als: %s", username));

        Connection connection = Connection.getInstance();
        connection.getOutputHandler().login(username);
        connection.getInputHandler().subscribe(ServerMessageType.MATCH, onMatch);
        connection.getInputHandler().subscribe(ServerMessageType.PLAYERLIST, onPlayerList);

        executor.scheduleAtFixedRate(() ->
            connection.getOutputHandler().getPlayerList(), 0, INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * Appends text to the log text field and scrolls down the latest message.
     *
     * @param message the message to append.
     */
    public void addToLogs(String message) {
        System.out.println(message);
    }

    /**
     * On click event for the disconnect button.
     * Forfeits the current match and logs out of the server.
     */
    public void disconnect() {
        if (game != null) {
            game.getHelper().forfeit();
        }

        executor.shutdown();

        Connection connection = Connection.getInstance();
        connection.getInputHandler().unsubscribe(ServerMessageType.MATCH, onMatch);
        connection.getInputHandler().unsubscribe(ServerMessageType.PLAYERLIST, onPlayerList);
        connection.getOutputHandler().logout();
        connection.close();

        UIManager.switchScreen("home");
    }

    /**
     * Gets called when a game has ended. Shows a message in the log for what
     * the result was and increments the appropriate counter.
     */
    @Override
    public void endGame() {
        Player localPlayer = game.getHelper().getLocalPlayer();
        String text = switch (game.getState()) {
            case PLAYING -> throw new RuntimeException("Tried ending game while still playing.");
            case PLAYER_ONE_WON -> localPlayer.getId() == GameBoard.PLAYER_ONE ? onWin() : onLoss();
            case PLAYER_TWO_WON -> localPlayer.getId() == GameBoard.PLAYER_TWO ? onWin() : onLoss();
            case DRAW -> onDraw();
        };

        String opponent = game.getOpponent(localPlayer).getUsername();
        addToLogs(String.format("%s: %s\n", text, opponent));

        game = null;
    }

    /**
     * Method to execute when the game ends in a draw.
     *
     * @return String to log.
     */
    private String onDraw() {
        drawCount++;
        drawsLabel.setText(String.format("Gelijkspel: %d", drawCount));
        return "Match gelijkgespeeld tegen";
    }

    /**
     * A listener that listens to the network connecting and starts a local game
     * to mirror the online game.
     */
    private final InputListener onMatch = message -> {
        Map<String, String> obj = message.getObject();
        String opponent = obj.get("OPPONENT");
        addToLogs("Start een match met: " + opponent);

        // TODO: Replace with factory.
        game = switch (obj.get("GAMETYPE")) {
            case "Tic-tac-toe" -> new TicTacToe();
            case "Reversi" -> new Othello();
            default -> throw new RuntimeException("Unknown type of game: " + obj.get("GAMETYPE"));
        };

        Player playerOne = obj.get("PLAYERTOMOVE").equals(opponent)
            ? new RemotePlayer(opponent)
            : new AIPlayer(game, username);
        Player playerTwo = obj.get("PLAYERTOMOVE").equals(opponent)
            ? new AIPlayer(game, username)
            : new RemotePlayer(opponent);

        GameModelHelper helper = new OnlineHelper(this, game);
        game.initialize(helper, playerOne, playerTwo);

        Platform.runLater(() -> {
            containerStackPane.getChildren().remove(gameGrid);
            gameGrid = new GameGrid(
                game.getBoard().getSize(),
                containerStackPane.getHeight(),
                game.getBackgroundColor()
            );

            containerStackPane.getChildren().add(gameGrid);

            Image black = new Image(game.getImage(GameBoard.PLAYER_ONE), 25, 25, false, true);
            playerOneLabel.setText(playerOne.getUsername());
            playerOneIcon.setImage(black);

            playerOneLabel.getParent().getStyleClass().add("player");

            Image white = new Image(game.getImage(GameBoard.PLAYER_TWO), 25, 25, false, true);
            playerTwoLabel.setText(playerTwo.getUsername());
            playerTwoIcon.setImage(white);

            playerTwoLabel.getParent().getStyleClass().add("player");

            update(-1, playerOne);
        });
    };

    /**
     * Method to execute when you lose the game.
     *
     * @return String to log.
     */
    private String onLoss() {
        loseCount++;
        losesLabel.setText(String.format("Verloren: %d", loseCount));
        return "Match verloren van";
    }

    /**
     * A listener that listens to the user playlist and sets that visible in the GUI.
     */
    private final InputListener onPlayerList = message -> {
        List<String> playersList = new ArrayList<>(Arrays.asList(message.getArray()));

        Platform.runLater(() -> {
            onlineLabel.setText(String.format("Online: %d", playersList.size()));
            playersList.remove(username);

            playersListView.setItems(FXCollections.observableArrayList(playersList));
            playersListView.refresh();
        });
    };

    /**
     * Method to execute when you win the game.
     *
     * @return String to log.
     */
    private String onWin() {
        winCount++;
        winsLabel.setText(String.format("Gewonnen: %d", winCount));
        return "Match gewonnen van";
    }

    /**
     * Gets called when a set is done on the board by either players.
     * Puts a log message of the move.
     *
     * @param move   the index of the move the player has done.
     * @param player the player who has set the move.
     */
    @Override
    public void update(int move, Player player) {
        gameGrid.update(game);
        addToLogs(String.format("%s was gezet door speler %s", move, player.getUsername()));
    }
}

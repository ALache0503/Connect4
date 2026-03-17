package de.thbingen.connect4.gaming.model.entity;

import de.thbingen.connect4.common.model.enums.GameState;
import lombok.Data;

import java.util.Objects;
import java.util.Random;

@Data
public class Game {
    private Lobby lobby;
    private GameState state;
    private LobbyActor currentTurn;
    private LobbyActor player1;
    private LobbyActor player2;
    private GameBoard board;

    public Game(Lobby lobby, LobbyActor player1, LobbyActor player2) {
        Random random = new Random();

        this.currentTurn = random.nextBoolean() ? player1 : player2;

        this.state = GameState.RUNNING;
        this.board = new GameBoard();

        this.lobby = lobby;
        this.player1 = player1;
        this.player2 = player2;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(game.lobby, lobby);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lobby);
    }
}

package com.suitandtiefinancial.baseball.testbench;

import java.util.ArrayList;
import java.util.List;

import com.suitandtiefinancial.baseball.game.Game;
import com.suitandtiefinancial.baseball.game.GameRecord;
import com.suitandtiefinancial.baseball.game.Rules;
import com.suitandtiefinancial.baseball.player.Player;

public class TestBench {
    private final int numberOfSims;
    private final int numberOfPlayers;
    private final List<Player> players;
    private final List<GameRecord> history;
    private final boolean debugPrint;
    private final int numberOfDecks;

    private int round = 0;

    public TestBench(int numberOfSims, boolean debugPrint, Player focusPlayer, Player... players) {
        this.numberOfSims = numberOfSims;
        this.debugPrint = debugPrint;
        this.numberOfPlayers = 1 + players.length;
        this.players = new ArrayList<Player>();
        this.players.add(focusPlayer);
        for (Player p : players) {
            this.players.add(p);
        }
        history = new ArrayList<GameRecord>(numberOfSims);
        numberOfDecks = 1 + numberOfPlayers / 4;
    }

    public void simulate() {
        if (round != 0) {
            throw new IllegalStateException();
        }
        for (round = 0; round < numberOfSims; round++) {
            simulateSingleGame();
        }
    }

    private void simulateSingleGame() {
        Game g = new Game(numberOfPlayers, numberOfDecks, Rules.HAWKEN_STANDARD());

        int firstPlayer = round % numberOfPlayers;
        int focusPlayerIndexInGame = -1;

        for (int playerIndexInGame = 0; playerIndexInGame < numberOfPlayers; playerIndexInGame++) {
            int playerIndexInTestBench = (playerIndexInGame + firstPlayer) % numberOfPlayers;
            g.addPlayer(players.get(playerIndexInTestBench));
            players.get(playerIndexInTestBench).initialize(g.getGameView(), playerIndexInGame);
            if (playerIndexInTestBench == 0) {
                focusPlayerIndexInGame = playerIndexInGame;
            }
        }

        if (debugPrint) {
            g.DEBUG_PRINT = true;
            System.out.println("Starting Simulation " + round + " focus player is player " + focusPlayerIndexInGame);
            System.out.println(g.toString());
        }
        while (!g.isGameOver()) {
            g.tick();
            if (debugPrint) {
                System.out.println(g.toString());
            }
        }

        if (debugPrint) {
            g.printGameOver();
        }
        history.add(g.generateGameRecord(focusPlayerIndexInGame));
    }

    public void printHistory() {
        int round = 0;
        for (GameRecord gr : history) {
            System.out.println("Round " + round);
            System.out.println(gr);
        }
    }

    public void printSummary() {
        int gamesPlayed = 0;
        int gamesWon = 0;
        float averageScoreEveryone = 0;
        float averageScoreUs = 0;

        for (GameRecord gr : history) {
            gamesPlayed++;
            if (gr.winnerIndex == gr.playerIndex) {
                gamesWon++;
            }
            averageScoreEveryone += gr.averageScore;
            averageScoreUs += gr.playerScore;
        }
        averageScoreEveryone /= gamesPlayed;
        averageScoreUs /= gamesPlayed;

        System.out.println("Played " + gamesPlayed + " games, and won " + gamesWon);
        System.out.println("We had an average score of " + averageScoreUs + " compared to the global average score at "
                + averageScoreEveryone);
    }
}

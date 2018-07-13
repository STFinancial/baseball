package com.suitandtiefinancial.baseball.game;

import com.suitandtiefinancial.baseball.player.Player;

import java.util.List;

/**
 * Game information available for players.
 */
public final class GameView {
    public static final int ROWS = 3;
    public static final int COLUMNS = 3;

    private final Game game;
    private final Rules rules;
    private final int numDecks;
    private final int numPlayers;
    private final List<Player> players;
    private final List<Hand> hands;
    private final Shoe shoe;

    GameView(Game game, Rules rules, int numDecks, int numPlayers, List<Player> players, List<Hand> hands, Shoe shoe) {
        this.game = game;
        this.rules = rules;
        this.numDecks = numDecks;
        this.numPlayers = numPlayers;
        this.players = players;
        this.hands = hands;
        this.shoe = shoe;
    }

    public int getNumberOfPlayers() {
        return numPlayers;
    }

    public int getNumDecks() { return numDecks; }

    public Card viewCard(int player, int row, int column) {
        return hands.get(player).viewCard(row, column);
    }

    public Card getDiscardUpCard() {
        return shoe.peekDiscard();
    }

    public int getRound() { return game.getRound(); }

    public boolean isColumnCollapsed(int player, int column) {
        return hands.get(player).peekCard(0, column) == null;
    }

    public int getRevealedTotal(int player) {
        return hands.get(player).getRevealedTotal();
    }
}

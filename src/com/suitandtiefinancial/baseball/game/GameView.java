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
    private final List<HandView> handViews;
    private final Shoe shoe;

    GameView(Game game, Rules rules, int numDecks, int numPlayers, List<Player> players, List<HandView> handViews, Shoe shoe) {
        this.game = game;
        this.rules = rules;
        this.numDecks = numDecks;
        this.numPlayers = numPlayers;
        this.players = players;
        this.handViews = handViews;
        this.shoe = shoe;
    }

    public int getNumberOfPlayers() {
        return numPlayers;
    }
    public int getNumDecks() { return numDecks; }

    public Card viewCard(int player, int row, int column) {
        return handViews.get(player).viewCard(row, column);
    }
    public boolean isCardRevealed(int player, int row, int column) {
        return handViews.get(player).getSpotState(row, column) == SpotState.FACE_UP;
    }
    public boolean isColumnCollapsed(int player, int column) {
        return handViews.get(player).getSpotState(0, column) == SpotState.COLLAPSED;
    }
    public int getRevealedTotal(int player) {
        return handViews.get(player).getRevealedTotal();
    }

    public Card getDiscardUpCard() {
        return shoe.peekDiscard();
    }
    public List<Card> getDiscard() { return shoe.peekFullDiscard(); }
}

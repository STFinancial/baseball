package com.suitandtiefinancial.baseball.player.trophycase;

import com.suitandtiefinancial.baseball.game.*;
import com.suitandtiefinancial.baseball.player.Player;

/**
 * Created by Timothy on 7/14/18.
 */
public class ContinuousEVPlayer implements Player {
    GameView g;
    Hand hand;
    boolean firstOpener;
    int playerIndex;

    ContinuousEVPlayer() {
        hand = new Hand(Game.ROWS, Game.COLUMNS);
    }

    @Override
    public void initialize(GameView g, int index) {
        this.g = g;
        this.playerIndex = index;
        hand.clear();
        firstOpener = true;
    }

    @Override
    public Move getOpener() {
        if (firstOpener) {
            firstOpener = false;
            return new Move(MoveType.FLIP, 0, 0);
        } else {
            return new Move(MoveType.FLIP, 0, 1);
        }
    }

    @Override
    public Move getMove() {
        // Check if the discard card is "better" than a draw card.
        return null;
    }

    @Override
    public Move getMoveWithDraw(Card c) {
        return null;
    }

    @Override
    public void showPeekedCard(int row, int column, Card c) {
        /* We never use (for now) this but will still implement for now */
        hand.setPeekedCard(c, row, column);
    }

    private double computeUnknownEV() {
        // TODO

        int numDecks = g.getNumDecks();

        // Calculate total before revealed cards
        double total = 0;
        int numCards = 0;
        for (Card card : Card.values()) {
            total += card.getValue() * card.getQuantity();
            numCards += card.getQuantity();
        }

        // Subtract revealed cards
        for (Card discard : g.getDiscard()) {

        }
        return 0.0;
    }
}

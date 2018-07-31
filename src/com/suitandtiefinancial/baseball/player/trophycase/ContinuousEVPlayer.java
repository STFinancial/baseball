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

        double downCardEv = computeUnknownEV();
        if (g.getDiscardUpCard().getValue() < downCardEv) {
            return discardMove();
        } else if (findWorstSpot().getState() == SpotState.FACE_UP) {
            return new Move(MoveType.DRAW);
        } else {
            return flipMove();
        }
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

    @Override
    public void processEvent(Event event) {
        switch (event.getType()) {
            case SHUFFLE:

                break;
        }

    }

    private double computeUnknownEV() {
        // TODO(stfinancial): IMPORTANT! DOWN CARD EV DIVERGES FROM DECK EV ONCE DISCARD IS RESHUFFLED. Need to implement!
        // TODO(stfinancial): How do we handle a discard pile that is reshuffled as the deck
        int numDecks = g.getNumDecks();

        // Calculate total before revealed cards
        double total = 0;
        int numCards = 0;
        for (Card card : Card.values()) {
            total += card.getValue() * card.getQuantity();
            numCards += card.getQuantity();
        }

        // Subtract discarded cards
        for (Card discard : g.getDiscard()) {
            --numCards;
            total -= discard.getValue();
        }

        // Subtract visible cards
        for (int p = 0; p < g.getNumberOfPlayers(); ++p) {
            for (int row = 0; row < Game.ROWS; ++row) {
                for (int column = 0; column < Game.COLUMNS; ++column) {
                    if (g.isCardRevealed(p, row, column)) {
                        --numCards;
                        total -= g.viewCard(p, row, column).getValue();
                    }
                }
            }
        }

        // TODO(stfinancial): Subtract our peeked cards.

        return total / numCards;
    }

    private Move discardMove() {
        // Find the worst card and remove it.
        Hand.Spot s = findWorstSpot();
        hand.setCard(g.getDiscardUpCard(), s.getRow(), s.getColumn());
        return new Move(MoveType.REPLACE_WITH_DISCARD, s.getRow(), s.getColumn());
    }

    private Hand.Spot findWorstSpot() {
        double unknownEV = computeUnknownEV();
        double worstValue = Double.MIN_VALUE;
        double currentValue;
        Hand.Spot worstSpot = null;

        for (Hand.Spot s: hand) {
            if (s.getState() == SpotState.FACE_UP || s.getState() == SpotState.FACE_DOWN_PEEKED) {
                currentValue = s.getCard().getValue();
            } else if (s.getState() == SpotState.COLLAPSED) {
                currentValue = Double.MIN_VALUE;
            } else if (s.getState() == SpotState.FACE_DOWN) {
                currentValue = unknownEV;
            } else {
                throw new IllegalStateException("Spot in unknown state: " + s.getState());
            }
            if (currentValue > worstValue) {
                worstValue = currentValue;
                worstSpot = s;
            }
        }
        return worstSpot;
    }

    private Move flipMove() {
        // Find the first unflipped card
        for (Hand.Spot s: hand) {
            if (s.getState() == SpotState.FACE_DOWN) {

                return new Move(MoveType.FLIP, s.getRow(), s.getColumn());
            }
        }
        throw new IllegalStateException("No cards left to flip.");
    }
}

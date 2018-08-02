package com.suitandtiefinancial.baseball.player.trophycase;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Event;
import com.suitandtiefinancial.baseball.game.Game;
import com.suitandtiefinancial.baseball.game.GameView;
import com.suitandtiefinancial.baseball.game.Move;
import com.suitandtiefinancial.baseball.game.MoveType;
import com.suitandtiefinancial.baseball.game.SpotState;
import com.suitandtiefinancial.baseball.player.Player;

/**
 * Created by Timothy on 7/14/18.
 */
public class ContinuousEVPlayer implements Player {
    private GameView g;
    private Hand hand;
    private boolean firstOpener;
    private int playerIndex;

    private ShoeInfo shoeInfo;

    public ContinuousEVPlayer() {
        hand = new Hand(Game.ROWS, Game.COLUMNS);
    }

    @Override
    public void initialize(GameView g, int index) {
        this.g = g;
        this.playerIndex = index;
        hand.clear();
        firstOpener = true;
        shoeInfo = new ShoeInfo(g.getNumDecks(), g);
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
        if (g.getDiscardUpCard().getValue() < shoeInfo.getDrawEv()) {
            return discardMove();
        } else if (findWorstSpot().getState() == SpotState.FACE_UP || findWorstSpot().getState() == SpotState.FACE_DOWN_PEEKED) {
            return new Move(MoveType.DRAW);
        } else {
            return flipMove();
        }
    }

    @Override
    public Move getMoveWithDraw(Card c) {
        Hand.Spot s = findWorstSpot();
        double worstValue;
        if (s.getState() == SpotState.FACE_UP || s.getState() == SpotState.FACE_DOWN_PEEKED) {
            worstValue = s.getCard().getValue();
        } else if (s.getState() == SpotState.FACE_DOWN) {
            worstValue = shoeInfo.getDownCardEv();
        } else {
            throw new IllegalStateException("Worst spot should not be collapsed.");
        }
        if (c.getValue() < worstValue) {
            return new Move(MoveType.REPLACE_WITH_DRAWN_CARD, s.getRow(), s.getColumn());
        } else {
            return new Move(MoveType.DECLINE_DRAWN_CARD);
        }
    }

    @Override
    public void showPeekedCard(int row, int column, Card c) {
        /* We never use (for now) this but will still implement for now */
        hand.setPeekedCard(c, row, column);
    }

    @Override
    public void processEvent(Event event) {
        shoeInfo.processEvent(event);
        if (event.hasPlayerIndex() && event.getPlayerIndex() == playerIndex) {
            hand.processEvent(event);
        }
//        switch (event.getType()) {
//            case SHUFFLE:
//
//                break;
//        }

    }

    private Move discardMove() {
        // Find the worst card and remove it.
        Hand.Spot s = findWorstSpot();
//        if (s == null) {
//            System.out.println("Discard Move is null.");
//            System.out.println(hand.toString());
//        }
//        hand.setCard(g.getDiscardUpCard(), s.getRow(), s.getColumn());
        return new Move(MoveType.REPLACE_WITH_DISCARD, s.getRow(), s.getColumn());
    }

    private Hand.Spot findWorstSpot() {
        double unknownEV = shoeInfo.getDownCardEv();
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
//        if (worstSpot == null) {
//            System.out.println("Worst Spot is null.");
//            System.out.println(g.toString());
//            System.out.println(hand.toString());
//            System.out.println(shoeInfo.toString());
//        }
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

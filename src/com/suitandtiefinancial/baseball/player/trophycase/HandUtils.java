package com.suitandtiefinancial.baseball.player.trophycase;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Game;
import com.suitandtiefinancial.baseball.game.Move;
import com.suitandtiefinancial.baseball.game.MoveType;
import com.suitandtiefinancial.baseball.game.SpotState;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by Timothy on 8/1/18.
 */
class HandUtils {

    static boolean canCollapseWithCard(Hand hand, Card card, int column) {
        int columnCardCount = 0;
        for (int row = 0; row < Game.ROWS; ++row) {
            switch (hand.getSpotState(row, column)) {
                case COLLAPSED:
                    return false;
                case FACE_DOWN:
                    break;
                case FACE_DOWN_PEEKED:
                    // TODO(stfinancial): Figure out how to handle this!!!! IMPORTANT
//                    if (hand.viewCard(row, column) == card) ++columnCardCount;
                    break;
                case FACE_UP:
                    if (hand.viewCard(row, column) == card) ++columnCardCount;
                    break;
                default:
                    throw new IllegalStateException("Spot in invalid state: " + hand.getSpotState(row, column));
            }
        }
        return columnCardCount == 2;
    }

    static int getFaceUpCardCountForColumn(Hand hand, Card card, int column) {
        int columnCardCount = 0;
        for (int row = 0; row < Game.ROWS; ++row) {
            if (hand.getSpotState(row, column) == SpotState.FACE_UP && hand.viewCard(row, column) == card) {
                ++columnCardCount;
            }
        }
        return columnCardCount;
    }

    static double scoreHand(Hand hand, double downCardEv) {
        double ev = 0;
        // TODO(stfinancial): Handle face-down peeked properly!!!, we can factor eminent collapses into calculation
        // TODO(stfinancial): We also need to factor peeks into downcardev
        for (Hand.Spot s: hand) {
            if (s.getState() == SpotState.FACE_UP || s.getState() == SpotState.FACE_DOWN_PEEKED) {
                ev += s.getCard().getValue();
            } else if (s.getState() == SpotState.FACE_DOWN) {
                ev += downCardEv;
            }
        }
        return ev;
    }

    static int calculateAffinity(Hand hand) {
        // TODO(stfinancial): We also need to take into account probability of obtaining collapse card
        int affinity = 0;
        int[] counts;
        for (int col = 0; col < Game.COLUMNS; ++col) {
            counts = new int[Card.values().length];
            for (int row = 0; row < Game.ROWS; ++row) {
                if (hand.getSpotState(row, col) == SpotState.FACE_UP) {
                    counts[hand.viewCard(row, col).ordinal()]++;
                }
            }
            for (Card c: Card.values()) {
                if (counts[c.ordinal()] > 1) {
                    affinity += c.getValue() * counts[c.ordinal()];
                }
            }
        }
        return affinity;
    }

    static Hand simulateMove(Hand hand, Move move, Card c) {
        // TODO(stfinancial): Combine this?
        Hand clonedHand = new Hand(hand);
        if (move.getMoveType() == MoveType.REPLACE_WITH_DRAWN_CARD) {
            clonedHand.setCard(c, move.getRow(), move.getColumn());
            if (checkCollapseForColumn(clonedHand, move.getColumn())) {
                clonedHand.collapseColumn(move.getColumn());
            }
        } else if (move.getMoveType() == MoveType.REPLACE_WITH_DISCARD) {
            clonedHand.setCard(c, move.getRow(), move.getColumn());
            if (checkCollapseForColumn(clonedHand, move.getColumn())) {
                clonedHand.collapseColumn(move.getColumn());
            }
        } else {
            throw new IllegalStateException("Cannot simulate Move of type: " + move.getMoveType());
        }
        return clonedHand;
    }

    private static boolean checkCollapseForColumn(Hand h, int col) {
        Card collapseCard = null;
        // TODO(stfinancial): Add some logic to deal with face-down peeked.
        for (int row = 0; row < Game.ROWS; ++row) {
            if (h.getSpotState(row, col) != SpotState.FACE_UP) {
                return false;
            }
            if (collapseCard == null) {
                collapseCard = h.viewCard(row, col);
            } else if (h.viewCard(row, col) != collapseCard) {
                return false;
            }
        }
        return true;
    }
}

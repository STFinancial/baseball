package com.suitandtiefinancial.baseball.player.trophycase;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Game;
import com.suitandtiefinancial.baseball.game.SpotState;

/**
 * Created by Timothy on 8/1/18.
 */
class HandUtils {

    boolean canCollapseWithCard(Hand hand, Card card, int column) {
        int columnCardCount = 0;
        for (int row = 0; row < Game.ROWS; ++row) {
            switch (hand.getSpotState(row, column)) {
                case COLLAPSED:
                    return false;
                case FACE_DOWN:
                    break;
                case FACE_DOWN_PEEKED:
                    if (hand.viewCard(row, column) == card) ++columnCardCount;
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
}

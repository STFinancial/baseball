package com.suitandtiefinancial.baseball.player.trophycase;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Game;

/**
 * Player internal representation of the player's knowledge about a hand.
 */
class Hand {
    private Spot[][] spots;
    private int rows;
    private int columns;

    Hand(int rows, int columns) {
        spots = new Spot[rows][columns];
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                spots[row][column] = new Spot();
            }
        }
    }

    void clear() {
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                spots[row][column].clear();
            }
        }
    }

    /** Mark a face-down card as peeked, set a card value if we peeked at our own card */
    void setPeekedCard(Card c, int row, int column) {
        // TODO(stfinancial): Allow setting null values for cards to represent other players' peeks
        spots[row][column].card = c;
        spots[row][column].peeked = true;
    }

    /** Set the card value of a spot, and set to face-up if not already */
    void setCard(Card c, int row, int column) {
        spots[row][column].card = c;
        spots[row][column].isFaceUp = true;
    }





    private class Spot {
        Card card = null;
        boolean isFaceUp = false;
        // TODO(stfinancial): Maybe need a better way to do this, not face up but we peeked at it before.
        boolean peeked = false;

        void clear() {
            card = null;
            isFaceUp = false;
            peeked = false;
        }
    }
}

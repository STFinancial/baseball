package com.suitandtiefinancial.baseball.player.trophycase;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Event;
import com.suitandtiefinancial.baseball.game.Game;
import com.suitandtiefinancial.baseball.game.SpotState;

import java.util.Iterator;

// TODO(stfinancial): I think this is only our representation about our own hand.
// TODO(stfinancial): If we want to construct representations of public hands, then this class needs to change a bit.

/**
 * Player internal representation of the player's knowledge about a hand.
 */
class Hand implements Iterable<Hand.Spot> {
    private Spot[][] spots;
    private int rows;
    private int columns;

    // TODO(stfinancial): Associative array of playerIndex and Hand in the player class. Need to adjust FACE_DOWN_PEEKED logic then.
    Hand(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        spots = new Spot[rows][columns];
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                spots[row][column] = new Spot(row, column);
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

    void processEvent(Event event) {
        switch (event.getType()) {
            case SET:
                setCard(event.getCard(), event.getRow(), event.getColumn());
                break;
            case FLIP:
                setCard(event.getCard(), event.getRow(), event.getColumn());
                break;
            case COLLAPSE:
                collapseColumn(event.getColumn());
                break;
            case PEEK:
                // TODO(stfinancial): Implement this.
                break;
            default:
                // Ignore.
        }
    }

    /** Mark a face-down card as peeked, set a card value if we peeked at our own card */
    void setPeekedCard(Card c, int row, int column) {
        // TODO(stfinancial): Allow setting null values for cards to represent other players' peeks
        // TODO(stfinancial): We assume that we know the value of FACE_DOWN_PEEKED, so we would need something else.
        spots[row][column].card = c;
        spots[row][column].state = SpotState.FACE_DOWN_PEEKED;
    }

//    void flipCard(int row, int column) {
//        spots[row][column].
//    }

    /** Set the card value of a spot, and set to face-up if not already */
    void setCard(Card c, int row, int column) {
        spots[row][column].card = c;
        spots[row][column].state = SpotState.FACE_UP;
    }

    SpotState getSpotState(int row, int column) {
        return spots[row][column].state;
    }

    /** If the card is face-down and not been peeked, return null. Otherwise, return the Card */
    Card viewCard(int row, int column) {
        if (spots[row][column].state == SpotState.FACE_DOWN) {
            return null;
        }
        return spots[row][column].card;
    }

    void collapseColumn(int column) {
        for (int row = 0; row < Game.ROWS; ++row) {
            spots[row][column].state = SpotState.COLLAPSED;
            spots[row][column].card = null;
        }
    }

    /** Iterates over the hand spots by column then by row. That is, column is the inner loop and row is the outer loop */
    @Override
    public Iterator<Spot> iterator() {
        return new Iterator<Spot>() {
            private int row = 0;
            private int column = 0;

            @Override
            public boolean hasNext() {
                return row < Game.ROWS;
            }

            @Override
            public Spot next() {
                Spot s = spots[row][column];
                column++;
                if (column == Game.COLUMNS) {
                    column = 0;
                    ++row;
                }
                return s;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < Game.ROWS; ++row) {
            for (int col = 0; col < Game.COLUMNS; ++col) {
                sb.append(row + ", " + col + ": ");
                switch (spots[row][col].getState()) {
                    case FACE_UP:
                        sb.append(spots[row][col].card.getValue() + " - FACE_UP\n");
                        break;
                    case COLLAPSED:
                        sb.append("X - COLLAPSED\n");
                        break;
                    case FACE_DOWN:
                        sb.append("? - FACE_DOWN\n");
                        break;
                    case FACE_DOWN_PEEKED:
                        sb.append(spots[row][col].card.getValue() + " - FACE_DOWN_PEEKED\n");
                        break;
                    default:
                        throw new IllegalStateException("Spot in Illegal State: " + spots[row][col].getState());
                }
            }
        }
        return sb.toString();
    }

    class Spot {
        private Card card = null;
        private SpotState state = SpotState.FACE_DOWN;
        private int row;
        private int column;

        private Spot(int row, int column) {
            this.row = row;
            this.column = column;
        }

        private void clear() {
            card = null;
            state = SpotState.FACE_DOWN;
        }

        int getRow() { return row; }
        int getColumn() { return column; }
        Card getCard() { return card; }
        SpotState getState() { return state; }
    }
}

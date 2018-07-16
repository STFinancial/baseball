package com.suitandtiefinancial.baseball.game;

/**
 * Class telling us things about hands.
 */
class HandUtils {
    // Package private for now.

    static int getRevealedTotal(Hand hand) {
        int total = 0;
        for (int row = 0; row < Game.ROWS; ++row) {
            for (int column = 0; column < Game.COLUMNS; ++column) {
                if (hand.getSpotState(row, column) == SpotState.FACE_UP) {
                    total += hand.peekCard(row, column).getValue();
                }
            }
        }
        return total;
    }

    static boolean isOut(Hand hand) {
        for (int row = 0; row < Game.ROWS; ++row) {
            for (int column = 0; column < Game.COLUMNS; ++column) {
                // If we find a spot that isn't either face up or collapsed, we're not out.
                if (hand.getSpotState(row, column) == SpotState.FACE_DOWN || hand.getSpotState(row, column) == SpotState.FACE_DOWN_PEEKED) {
                    return false;
                }
            }
        }
        return true;
    }

    static String displayRow(Hand hand, int row) {
        String s = "";
        for (int column = 0; column < Game.COLUMNS; column++) {
            if (hand.getSpotState(row, column) == SpotState.FACE_DOWN || hand.getSpotState(row, column) == SpotState.FACE_DOWN_PEEKED) {
                s += "X";
            } else if (hand.getSpotState(row, column) == SpotState.COLLAPSED || hand.peekCard(row, column) == null) {
                s += " ";
            } else {
                switch (hand.peekCard(row, column)) {
                    case ACE: s += "A"; break;
                    case EIGHT: s += "8"; break;
                    case FIVE: s += "5"; break;
                    case FOUR: s += "4"; break;
                    case JACK: s += "J"; break;
                    case JOKER: s += "E"; break;
                    case KING: s += "K"; break;
                    case NINE: s += "9"; break;
                    case QUEEN: s += "Q"; break;
                    case SEVEN: s += "7"; break;
                    case SIX: s += "6"; break;
                    case TEN: s += "T"; break;
                    case THREE: s += "3"; break;
                    case TWO: s += "2"; break;
                }
            }
        }
        return s;
    }
}

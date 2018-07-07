/**
 * Created by Timothy on 7/6/18.
 */
class Hand {
    private Spot[][] spots;
    private int total;

    Hand() {
        spots = new Spot[3][3];
    }

    Card replace(Card replacement, int row, int column) {
        Spot s = spots[row][column];
        if (s.isRevealed) {
            total -= s.card.getValue();
        }
        Card returnCard = s.card;

        s.card = replacement;
        total += replacement.getValue();
        s.isRevealed = true;

        return returnCard;
    }

    /** Flips a face-down card face-up */
    void flip(int row, int column) {
        if (!spots[row][column].isRevealed) {
            spots[row][column].isRevealed = true;
            total += spots[row][column].card.getValue();
        }
    }

    int getRevealedTotal() {
        return total;
    }

    // TODO(stfinancial): Still gotta figure out how to make the cards collapse

    boolean checkCollapse(int column) {
        return  spots[0][column].isRevealed && spots[1][column].isRevealed && spots[2][column].isRevealed &&
                spots[0][column].card.getValue() == spots[1][column].card.getValue() &&
                spots[1][column].card.getValue() == spots[2][column].card.getValue();

    }

    private class Spot {
        boolean isRevealed = false;
        Card card;
    }
}

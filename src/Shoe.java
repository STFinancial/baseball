import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by Timothy on 7/6/18.
 */
public class Shoe {
    private LinkedList<Card> deck;
    private LinkedList<Card> discard;

    Shoe(int numDecks) {
        deck = new LinkedList<>();
        discard = new LinkedList<>();

        for (int deckNum = 0; deckNum < numDecks; ++deckNum) {
            for (int card = 0; card < 54; ++card) {
                deck.add(Card.values()[card / 4]);
            }
        }
        reset();
    }

    /** Deal a card from the top of the deck */
    Card deal() { return deck.remove(); }

    /** Pick up top card on the discard pile */
    Card takeDiscard() { return discard.removeLast(); }

    /** Show top card on the discard pile */
    Card showDiscard() { return discard.get(discard.size() - 1); }

    /** Discard provided card */
    void discard(Card card) { discard.add(card); }


    void reset() {
        deck.addAll(discard);
        discard.clear();
        Collections.shuffle(deck);
    }
}

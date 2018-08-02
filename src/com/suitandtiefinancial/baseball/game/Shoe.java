package com.suitandtiefinancial.baseball.game;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The shoe of {@link Card Cards} including the deck and the discard pile.
 */
class Shoe {
    private LinkedList<Card> deck;
    private LinkedList<Card> discard;

    Shoe(int numDecks) {
        deck = new LinkedList<>();
        discard = new LinkedList<>();

        for (int deckNum = 0; deckNum < numDecks; ++deckNum) {
            for (Card c : Card.values()) {
            	for (int count = 0; count < c.getQuantity(); count++) {
            		deck.add(c);
            	}
            }
        }
        reset();
    }

    /** Deal a card from the top of the deck */
    Card draw() { return deck.remove(); }

    boolean isDeckEmpty() {
        return deck.size() == 0;
    }

    /** Pick up top card on the discard pile */
    Card popDiscard() { return discard.removeLast(); }

    /** Show top card on the discard pile */
    Card peekDiscard() { return discard.get(discard.size() - 1); }
    /** @return An immutable list containing the full discard pile, with the last card in the list being on top */
    List<Card> peekFullDiscard() { return Collections.unmodifiableList(discard); }

    /** Discard provided card */
    void pushDiscard(Card card) { discard.add(card); }

    /** Add the discard to the deck, and reshuffle */
    void reset() {
        deck.addAll(discard);
        discard.clear();
        Collections.shuffle(deck);
    }
}

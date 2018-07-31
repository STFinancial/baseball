package com.suitandtiefinancial.baseball.player.trophycase;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Event;
import com.suitandtiefinancial.baseball.game.GameView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Timothy on 7/24/18.
 */
class ShoeInfo {
    private final int numDecks;
    private final int totalCards;
    private final int totalValue;


    List<Card> discard;

    /** The total value of all cards that have not been publicly revealed during the course of the game. Discarded cards shuffled back into the deck have been revealed. */
    double unrevealedTotal;
    int unrevealedCards;

    double drawableTotal;
    int drawableCards;

    /** Number of shuffles that have happened in the game. */
    int numShuffles;


    ShoeInfo(int numDecks) {
        this.numDecks = numDecks;
        int tempCards = 0;
        int tempValue = 0;
        for (Card c: Card.values()) {
            tempCards += c.getQuantity();
            tempValue += c.getValue() * c.getQuantity();
        }
        this.totalCards = numDecks * tempCards;
        this.totalValue = numDecks * tempValue;
        discard = new LinkedList<>();
        numShuffles = 0;
        reset();
    }

    void reset() {
        discard.clear();
        numShuffles = 0;
        unrevealedCards = totalCards;
        unrevealedTotal = totalValue;
    }

    double getDownCardEv() {
        return unrevealedTotal / unrevealedCards;
    }

    double getDrawEv() {
        // If we haven't reshuffled the discard, then the draw EV is equal to the down card EV.
        // Otherwise, the draw EV is the reshuffled discard minus whatever has been discarded.
        if (numShuffles > 1) {
            return drawableTotal / drawableCards;
        } else {
            return unrevealedTotal / unrevealedCards;
        }
    }

    void processEvent(Event event) {
        switch (event.getType()) {
            case SHUFFLE:
                if (numShuffles > 0) {
                    // Shuffle the discard into the deck
                    int discardTotal = 0;
                    int discardCards = 0;
                    for (Card c: discard) {
                        discardTotal -= c.getValue();
                        --discardCards;
                    }
                    drawableTotal = discardTotal;
                    drawableCards = discardCards;
                    discard.clear();
                }
                ++numShuffles;
                break;
            case INITIAL_DISCARD:
                discard.add(event.getCard());
                unrevealedTotal -= event.getCard().getValue();
                unrevealedCards--;
                break;
            case FLIP:
                unrevealedCards--;
                unrevealedTotal -= event.getCard().getValue();
                break;
            case DRAW_DISCARD:
                // TODO
                discard.remove(0);
                break;
            case DISCARD:
                discard.add(event.getCard());
                break;
            case SET:
                // TODO(stfinancial): How do we know what we replaced was face up or face down? We can't check GameView because it's already been updated.

        }
    }
}

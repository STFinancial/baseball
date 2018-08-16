package com.suitandtiefinancial.baseball.player.trophycase;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Event;
import com.suitandtiefinancial.baseball.game.EventType;
import com.suitandtiefinancial.baseball.game.Game;
import com.suitandtiefinancial.baseball.game.GameView;
import com.suitandtiefinancial.baseball.game.SpotState;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Timothy on 7/24/18.
 */
class ShoeInfo {
    // <Card, <Card State, #Cards in state>>
//    private EnumMap<Card, EnumMap<CardState, Integer>> f;
    // <Card State, <Card, #Cards in state>>
    private EnumMap<CardState, EnumMap<Card, Integer>> shoeState;
    private LinkedList<Card> discard;
    private GameView gameView;

    private final int numDecks;
    private final int totalCards;
    private final int totalValue;

    // TODO(stfinancial): Come up with a better name for this.
    /** The total value of all cards that have not been publicly revealed during the course of the game.
     * Discarded cards shuffled back into the deck have been revealed.
     * This is the sum of all UNREVEALED cards before the first shuffle, and all FACE_DOWN cards after the first shuffle.
     */
    private double unrevealedTotal;
    private int unrevealedCards;

    private double drawableTotal;
    private int drawableCards;

    /** Number of shuffles that have happened in the game. Excluding initial shuffle. */
    private int numShuffles;


    ShoeInfo(int numDecks, GameView gameView) {
        this.numDecks = numDecks;
        this.gameView = gameView;
//        this.shoeState = new EnumMap<>(Card.class);
        this.shoeState = new EnumMap<>(CardState.class);
        int tempCards = 0;
        int tempValue = 0;
//        for (Card c: Card.values()) {
//            shoeState.put(c, new EnumMap<>(CardState.class));
//            tempCards += c.getQuantity();
//            tempValue += c.getValue() * c.getQuantity();
//        }
        for (CardState s: CardState.values()) {
            shoeState.put(s, new EnumMap<>(Card.class));
        }
        for (Card c: Card.values()) {
            tempCards += c.getQuantity();
            tempValue += c.getValue() * c.getQuantity();
        }
        this.totalCards = numDecks * tempCards;
        this.totalValue = numDecks * tempValue;
        discard = new LinkedList<>();
        reset();
    }

    void reset() {
        discard.clear();
        numShuffles = 0;
        drawableCards = totalCards - (Game.ROWS * Game.COLUMNS * gameView.getNumberOfPlayers());
        unrevealedCards = totalCards;
        unrevealedTotal = totalValue;
        // Set all states to unrevealed.
//        shoeState.forEach((card, states) -> {
//            for (CardState state: CardState.values()) {
//                if (state == CardState.UNREVEALED) {
//                    states.put(state, card.getQuantity() * numDecks);
//                } else {
//                    states.put(state, 0);
//                }
//            }
//        });
        shoeState.forEach((state, cards) -> {
            for (Card c: Card.values()) {
                if (state == CardState.UNREVEALED) {
                    cards.put(c, c.getQuantity() * numDecks);
                } else {
                    cards.put(c, 0);
                }
            }
        });
    }

    double getDownCardEv() {
//        System.out.println("Down Card EV: " + unrevealedTotal / unrevealedCards);
        return unrevealedTotal / unrevealedCards;
    }

    /**
     * Returns the expected value of a down card, taking into account the card we just drew.
     * @param draw - The card we just drew.
     * @return The expected value of a down card.
     */
    double getDownCardEvWithDraw(Card draw) {
        if (numShuffles > 0) {
            return unrevealedTotal / unrevealedCards;
        } else {
            return (unrevealedTotal - draw.getValue()) / (unrevealedCards - 1);
        }
    }

    double getDrawEv() {
        // If we haven't reshuffled the discard, then the draw EV is equal to the down card EV.
        // Otherwise, the draw EV is the reshuffled discard minus whatever has been discarded.
        if (drawableCards == 0) {
            double discardTotal = 0;
            int discardCards = 0;
            for (Card c: discard) {
                discardCards++;
                discardTotal += c.getValue();
            }
//            System.out.println("Draw EV: " + discardTotal / discardCards);
            return discardTotal / discardCards;
        } else if (numShuffles > 0) {
//            System.out.println("Draw EV: " + drawableTotal / drawableCards);
            return drawableTotal / drawableCards;
        } else {
//            System.out.println("Draw EV: " + unrevealedTotal / unrevealedCards);
            return unrevealedTotal / unrevealedCards;
        }
    }

    Map<Card, Integer> getCardCountsForState(CardState state) {
        return Collections.unmodifiableMap(shoeState.get(state));
    }

    void processEvent(Event event) {
        // TODO(stfinancial): Want to update deck on our own personal draw since we get new information.
        switch (event.getType()) {
            case INITIAL_DISCARD:
                discard.add(event.getCard());
                unrevealedTotal -= event.getCard().getValue();
                unrevealedCards--;
                decrementCount(CardState.UNREVEALED, event.getCard());
                incrementCount(CardState.DISCARD, event.getCard());
                break;
            case SHUFFLE:
                if (numShuffles == 0) {
                    // TODO(stfinancial): THIS DOES NOT MESH WITH OUR CURRENT INCARNATION OF UNREVEALED TOTAL
                    // Mark remaining UNREVEALED cards as FACE_DOWN
                    for (Card c: Card.values()) {
                        shoeState.get(CardState.FACE_DOWN).put(c, shoeState.get(CardState.UNREVEALED).get(c));
                        shoeState.get(CardState.UNREVEALED).put(c, 0);
//                        shoeState.get(c).put(CardState.FACE_DOWN, shoeState.get(c).get(CardState.UNREVEALED));
//                        shoeState.get(c).put(CardState.UNREVEALED, 0);
                    }
                }
                // Shuffle the discard into the deck
                int discardTotal = 0;
                int discardCards = 0;
                for (Card c: discard) {
                    discardTotal += c.getValue();
                    ++discardCards;
                    decrementCount(CardState.DISCARD, c);
                    incrementCount(CardState.DECK, c);
                }
                drawableTotal = discardTotal;
                drawableCards = discardCards;
                discard.clear();
                ++numShuffles;
                break;
            case FLIP:
                unrevealedCards--;
                unrevealedTotal -= event.getCard().getValue();
                // TODO(stfinancial): Deal with face-down peeked
                if (numShuffles > 0) {
                    decrementCount(CardState.FACE_DOWN, event.getCard());
                } else {
                    decrementCount(CardState.UNREVEALED, event.getCard());
                }
                incrementCount(CardState.FACE_UP, event.getCard());
                break;
            case SET:
                if (event.getTriggeringEvent().getType() == EventType.DRAW) {
                    incrementCount(CardState.FACE_UP, event.getCard());
                    if (numShuffles > 0) {
                        decrementCount(CardState.DECK, event.getCard());
                        drawableTotal -= event.getCard().getValue();
                    } else {
                        decrementCount(CardState.UNREVEALED, event.getCard());
                        unrevealedCards--;
                        unrevealedTotal -= event.getCard().getValue();
                    }
                    drawableCards--; // We need to keep track of cards in deck no matter what.
                } else if (event.getTriggeringEvent().getType() == EventType.DRAW_DISCARD) {
                    // This is the only time DRAW_DISCARD should be thrown.
                    discard.removeLast();
                    decrementCount(CardState.DISCARD, event.getCard());
                    incrementCount(CardState.FACE_UP, event.getCard());
                } else {
                    throw new IllegalStateException("SET event triggering event should not be of type: " + event.getTriggeringEvent().getType());
                }
                break;
            case DISCARD:
                switch (event.getTriggeringEvent().getType()) {
                    case DRAW:
                        if (numShuffles > 0) {
                            decrementCount(CardState.DECK, event.getCard());
                            drawableTotal -= event.getCard().getValue();
                        } else {
                            decrementCount(CardState.UNREVEALED, event.getCard());
                            unrevealedCards--;
                            unrevealedTotal -= event.getCard().getValue();
                        }
                        drawableCards--; // We need to keep track of cards in deck no matter what.
                        break;
                    case SET:
                        if (event.getTriggeringEvent().getPriorState() == SpotState.FACE_UP) {
                            decrementCount(CardState.FACE_UP, event.getCard());
                        } else if (event.getTriggeringEvent().getPriorState() == SpotState.FACE_DOWN_PEEKED || event.getTriggeringEvent().getPriorState() == SpotState.FACE_DOWN) {
                            if (numShuffles > 0) {
                                decrementCount(CardState.FACE_DOWN, event.getCard());
                            } else {
                                decrementCount(CardState.UNREVEALED, event.getCard());
                            }
                            unrevealedCards--;
                            unrevealedTotal -= event.getCard().getValue();
                        } else {
                            throw new IllegalStateException("Illegal SET on already collapsed spot state.");
                        }
                        break;
                    case COLLAPSE:
                        decrementCount(CardState.FACE_UP, event.getCard());
                        incrementCount(CardState.DISCARD, event.getCard());
                        break;
                    default:
                        throw new IllegalStateException("DISCARD event triggering event should not be of type: " + event.getTriggeringEvent().getType());
                }
                discard.add(event.getCard());
                incrementCount(CardState.DISCARD, event.getCard());
                break;
            case PEEK:
                //TODO(stfinancial): Face-down peeked
                break;
            default:
                // Do nothing.
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Shoe Info:\n");
        sb.append("Num Decks: " + numDecks);
        sb.append("\nNum Shuffles: " + numShuffles);
        sb.append("\nDiscard Size: " + discard.size());
        sb.append("\nUnrevealed Total: " + unrevealedTotal);
        sb.append("\nUnrevealed Cards: " + unrevealedCards);
        sb.append("\nDrawable Total: " + drawableTotal);
        sb.append("\nDrawable Cards: " + drawableCards);
        sb.append("\n");
        return sb.toString();
    }

    private void incrementCount(CardState state, Card card) {
        shoeState.get(state).put(card, shoeState.get(state).get(card) + 1);
    }

    private void decrementCount(CardState state, Card card) {
        shoeState.get(state).put(card, shoeState.get(state).get(card) - 1);
    }

//    private void incrementCount(CardState state, Card card) {
//        shoeState.get(card).put(state, shoeState.get(card).get(state) + 1);
//    }

//    private void decrementCount(CardState state, Card card) {
//        shoeState.get(card).put(state, shoeState.get(card).get(state) - 1);
//    }
}

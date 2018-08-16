package com.suitandtiefinancial.baseball.player.trophycase;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Event;
import com.suitandtiefinancial.baseball.game.EventType;
import com.suitandtiefinancial.baseball.game.Game;
import com.suitandtiefinancial.baseball.game.GameView;
import com.suitandtiefinancial.baseball.game.Move;
import com.suitandtiefinancial.baseball.game.MoveType;
import com.suitandtiefinancial.baseball.game.SpotState;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Timothy on 8/15/18.
 */
public class STFPlayer1 extends AbstractPlayer {
    private GameView g;
    private ShoeInfo shoeInfo;
    private int playerIndex;
    private boolean firstOpener;
    private int numShuffles;

    @Override
    public void initialize(GameView g, int index) {
        this.g = g;
        this.playerIndex = index;
        this.numShuffles = 0;
        hand.clear();
        firstOpener = true;
        shoeInfo = new ShoeInfo(g.getNumDecks(), g);
    }

    @Override
    public Move getOpener() {
        if (firstOpener) {
            firstOpener = false;
            return new Move(MoveType.FLIP, 0, 0);
        } else {
            return new Move(MoveType.FLIP, 0, 1);
        }
    }

    @Override
    public Move getMove() {
        /*
         * The fact we can choose to discard the draw card changes the EV quite a bit
         * I think what we need to do is compute the EV delta of each move.
         */
        List<ScoredMove> scoredMoves = new LinkedList<>();
        // TODO(stfinancial): Take into account info gained in delta-EV of flip move.
        scoredMoves.add(new ScoredMove(getFlipMove(), 0));
        scoredMoves.add(getScoredDrawMove());
        scoredMoves.add(getScoredDrawDiscardMove());
        Collections.sort(scoredMoves);
//        System.out.println("Draw EV: " + shoeInfo.getDrawEv());
//        System.out.println("Down Card EV: " + shoeInfo.getDownCardEv());
//        scoredMoves.forEach(s -> System.out.println(s));
        return scoredMoves.get(0).getMove();
    }

    @Override
    public Move getMoveWithDraw(Card c) {
        // TODO(stfinancial): We have drawn so we need to incorporate that into EVs (need to call the other method in shoeinfo)
        ScoredSpot ss = findWorstSpot();
        Hand.Spot worstSpot = ss.getSpot();
        if (worstSpot.getState() == SpotState.FACE_DOWN) {
            worstSpot = findBestFaceDownSpotForCard(c);
        }
        if (c.getValue() < ss.getScore()) {
            return new Move(MoveType.REPLACE_WITH_DRAWN_CARD, worstSpot.getRow(), worstSpot.getColumn());
        } else {
            return new Move(MoveType.DECLINE_DRAWN_CARD);
        }
    }

    @Override
    public void showPeekedCard(int row, int column, Card c) {
        /* We never use (for now) this but will still implement for now */
        hand.setPeekedCard(c, row, column);
        // TODO(stfinancial): A *lot* of logic needs to be fixed when this is implemented. Especially with collapse checks.
    }

    @Override
    public void processEvent(Event event) {
        shoeInfo.processEvent(event);
        if (event.hasPlayerIndex() && event.getPlayerIndex() == playerIndex) {
            hand.processEvent(event);
        }
        if (event.getType() == EventType.SHUFFLE) {
            ++numShuffles;
        }
    }

    private ScoredMove getScoredDrawMove() {
        ScoredSpot worstSpot = findWorstSpot();

        Map<Card, Integer> drawableCounts;
        if (numShuffles > 0) {
            drawableCounts = shoeInfo.getCardCountsForState(CardState.DECK);
        } else {
            drawableCounts = shoeInfo.getCardCountsForState(CardState.UNREVEALED);
        }

        // Compute the delta-EV by setting the delta-EV of discarded draws to 0, and draw - worst for everything else
        double deltaTotal = 0;
        int deltaCount = 0;
        for (Map.Entry<Card, Integer> count : drawableCounts.entrySet()) {
            if (count.getKey().getValue() > worstSpot.getScore()) {
                // If the draw is worse than our worst card, we would discard and delta EV is 0.
                deltaCount += count.getValue();
            } else {
                // Otherwise, the delta EV for a draw is draw minus worst card.
                deltaTotal += (count.getKey().getValue() - worstSpot.getScore()) * count.getValue();
                deltaCount += count.getValue();
            }
        }
        return new ScoredMove(new Move(MoveType.DRAW), deltaTotal / deltaCount);
    }

    private ScoredMove getScoredDrawDiscardMove() {
        for (int col = 0; col < Game.COLUMNS; ++col) {
            // TODO(stfinancial): We're going to need to construct other players hands, so we can check remaining turns.
            // TODO(stfinancial): Add logic to replace queens/jacks etc. if delta-EV is better and game is about to end.
            // TODO(stfinancail): Also, do we want to always collapse if available, or just give the EV delta? Because collapsing aces might not be better than replacing a queen to the scorer, but if we have additional turns to replace the queen, it is worth the collapse now.
            if (HandUtils.canCollapseWithCard(hand, g.getDiscardUpCard(), col)) {
                double collapseDelta = 0;
                int collapseRow = -1;
                for (int row = 0; row < Game.ROWS; ++row) {
                    if (hand.viewCard(row, col) == g.getDiscardUpCard()) {
                        collapseDelta -= hand.viewCard(row, col).getValue();
                    } else if (hand.viewCard(row, col) == null) {
                        collapseDelta -= shoeInfo.getDownCardEv();
                        collapseRow = row;
                    } else {
                        collapseDelta -= hand.viewCard(row, col).getValue();
                        collapseRow = row;
                    }
                }
                return new ScoredMove(new Move(MoveType.REPLACE_WITH_DISCARD, collapseRow, col), collapseDelta);
            }
        }
        ScoredSpot spot = findWorstSpot();
        // TODO(stfinancial): We are assuming all face down cards have the same EV. We should not do this.
        if (spot.getSpot().getState() == SpotState.FACE_DOWN) {
            spot = new ScoredSpot(findBestFaceDownSpotForCard(g.getDiscardUpCard()), spot.getScore());
        }

        return new ScoredMove(new Move(MoveType.REPLACE_WITH_DISCARD, spot.getSpot().getRow(), spot.getSpot().getColumn()), g.getDiscardUpCard().getValue() - spot.getScore());
    }

    private Move getFlipMove() {
        // Find the first unflipped card
        for (Hand.Spot s: hand) {
            if (s.getState() == SpotState.FACE_DOWN) {
                return new Move(MoveType.FLIP, s.getRow(), s.getColumn());
            }
        }
        throw new IllegalStateException("No cards left to flip.");
    }

    private ScoredSpot findWorstSpot() {
        // TODO(stfinancial): Move to HandUtils?
        double unknownEV = shoeInfo.getDownCardEv();
        double worstValue = Double.MIN_VALUE;
        double currentValue;
        Hand.Spot worstSpot = null;

        for (Hand.Spot s: hand) {
            if (s.getState() == SpotState.FACE_UP || s.getState() == SpotState.FACE_DOWN_PEEKED) {
                currentValue = s.getCard().getValue();
            } else if (s.getState() == SpotState.COLLAPSED) {
                currentValue = Double.MIN_VALUE;
            } else if (s.getState() == SpotState.FACE_DOWN) {
                currentValue = unknownEV;
            } else {
                throw new IllegalStateException("Spot in unknown state: " + s.getState());
            }
            if (currentValue > worstValue) {
                worstValue = currentValue;
                worstSpot = s;
            }
        }
        if (worstSpot == null) {
            // TODO(stfinancial): Encountered this bug again, need to find out why.
            System.out.println("Worst Spot is null.");
            System.out.println(g.toString());
            System.out.println(hand.toString());
            System.out.println(shoeInfo.toString());
        }
        if (worstSpot.getState() == SpotState.COLLAPSED) {
            System.out.println(g.toString());
            throw new IllegalStateException("Worst spot should not be in collapsed state: " + worstSpot.getState());
        }
//        if (worstSpot == null) {
//            System.out.println("Worst Spot is null.");
//            System.out.println(g.toString());
//            System.out.println(hand.toString());
//            System.out.println(shoeInfo.toString());
//        }
        return new ScoredSpot(worstSpot, worstValue);
    }

    private Hand.Spot findBestFaceDownSpotForCard(Card card) {
        // TODO(stfinancial): Move to HandUtils? Handle joker case.
        // Find row with highest affinity, if not joker.
        int highestAffinity = Integer.MIN_VALUE;
        Hand.Spot bestSpot = null;
        int currentAffinity;
        for (Hand.Spot s: hand) {
            if (s.getState() == SpotState.FACE_DOWN) {
                currentAffinity = HandUtils.getFaceUpCardCountForColumn(hand, card, s.getColumn());
                if (currentAffinity > highestAffinity) {
                    bestSpot = s;
                    highestAffinity = currentAffinity;
                }
            }
        }
        return bestSpot;
    }
}

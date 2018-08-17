package com.suitandtiefinancial.baseball.player.trophycase;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Event;
import com.suitandtiefinancial.baseball.game.EventType;
import com.suitandtiefinancial.baseball.game.Game;
import com.suitandtiefinancial.baseball.game.GameView;
import com.suitandtiefinancial.baseball.game.Move;
import com.suitandtiefinancial.baseball.game.MoveType;
import com.suitandtiefinancial.baseball.game.SpotState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Timothy on 8/16/18.
 */
public class STFPlayer2 extends AbstractPlayer {
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
        List<Move> moves = generatePreDrawMoves();
        List<ScoredMove> scoredMoves = new ArrayList<>(moves.size());
        moves.forEach(m -> scoredMoves.add(scoreMove(m, g.getDiscardUpCard())));
        Collections.sort(scoredMoves);
        return scoredMoves.get(0).getMove();
    }

    @Override
    public Move getMoveWithDraw(Card c) {
        List<Move> moves = generatePostDrawMoves();
        List<ScoredMove> scoredMoves = new ArrayList<>(moves.size());
        moves.forEach(m -> scoredMoves.add(scoreMove(m, c)));
        Collections.sort(scoredMoves);
        return scoredMoves.get(0).getMove();
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

    private ScoredMove scoreMove(Move m, Card c) {
        ScoredMove scoredMove;
        switch(m.getMoveType()) {
            case FLIP:
                scoredMove = scoreFlipMove(m);
                break;
            case REPLACE_WITH_DISCARD:
                scoredMove = scoreReplaceDiscardMove(m, c);
                break;
            case DRAW:
                scoredMove = scoreDrawMove(m);
                break;
            case REPLACE_WITH_DRAWN_CARD:
                scoredMove = scoreReplaceDrawMove(m, c);
                break;
            case DECLINE_DRAWN_CARD:
                scoredMove = new ScoredMove(m, 0, 0);
                break;
            default:
                throw new IllegalStateException("Invalid MoveType for payer: " + m.getMoveType());
        }
        return scoredMove;
    }

    private ScoredMove scoreFlipMove(Move m) {
        // No flipperino.
        return new ScoredMove(m, 0, 0);
    }

    private ScoredMove scoreDrawMove(Move m) {
        Map<Card, Integer> drawableCounts;
        if (numShuffles > 0) {
            drawableCounts = shoeInfo.getCardCountsForState(CardState.DECK);
        } else {
            drawableCounts = shoeInfo.getCardCountsForState(CardState.UNREVEALED);
        }

        List<ScoredMove> possibleDrawMoves = new ArrayList<>(Game.COLUMNS * Game.ROWS);
        double deltaEvTotal = 0;
        double deltaAffinityTotal = 0;
        double deltaCount = 0;
        ScoredMove bestMove;
//        List<ScoredMove> possibleDrawMoves = new ArrayList<>(Card.values().length * Game.COLUMNS * Game.ROWS);
//        ScoredMove scoredMove;
        for (Map.Entry<Card, Integer> count: drawableCounts.entrySet()) {
            // If there is a drawable card left of this type. Generate all the possible moves for that draw.
            if (count.getValue() > 0) {
                possibleDrawMoves.clear();
                for (int row = 0; row < Game.ROWS; ++row) {
                    for (int col = 0; col < Game.COLUMNS; ++col) {
//                        scoredMove = ;
                        possibleDrawMoves.add(scoreReplaceDrawMove(new Move(MoveType.REPLACE_WITH_DRAWN_CARD, row, col), count.getKey()));
                    }
                }
                Collections.sort(possibleDrawMoves);
                bestMove = possibleDrawMoves.get(0);
                // Take the best move as the move we would do for that draw. If the deltaEv is non-positive, we will use the card.
                if (bestMove.getDeltaEV() <= 0) {
                    deltaEvTotal += bestMove.getDeltaEV() * count.getValue();
                    deltaAffinityTotal += bestMove.getDeltaAffinity() * count.getValue();
                }
                deltaCount += count.getValue();
            }
        }
        // TODO(stfinancial): Treat affinity better, it should probably be worth some EV
        return new ScoredMove(m, deltaEvTotal / deltaCount, deltaAffinityTotal / deltaCount);
    }

    private ScoredMove scoreReplaceDrawMove(Move m, Card c) {
        Hand clonedHand = HandUtils.simulateMove(hand, m, c);
        double ev = HandUtils.scoreHand(clonedHand, shoeInfo.getDownCardEvWithDraw(c));
        int affinity = HandUtils.calculateAffinity(clonedHand);

        double deltaEv = ev - HandUtils.scoreHand(hand, shoeInfo.getDownCardEvWithDraw(c));
        int deltaAffinity = affinity - HandUtils.calculateAffinity(hand);

        return new ScoredMove(m, deltaEv, deltaAffinity);

    }

    private ScoredMove scoreReplaceDiscardMove(Move m, Card c) {
        Hand clonedHand = HandUtils.simulateMove(hand, m, c);
        double ev = HandUtils.scoreHand(clonedHand, shoeInfo.getDownCardEv());
        int affinity = HandUtils.calculateAffinity(clonedHand);

        double deltaEv = ev - HandUtils.scoreHand(hand, shoeInfo.getDownCardEv());
        int deltaAffinity = affinity - HandUtils.calculateAffinity(hand);

        return new ScoredMove(m, deltaEv, deltaAffinity);
    }

    private List<Move> generatePreDrawMoves() {
        List<Move> moves = new LinkedList<>();
        // Generate flip moves and draw-discard moves.
        for (Hand.Spot s: hand) {
            if (s.getState() == SpotState.FACE_DOWN || s.getState() == SpotState.FACE_DOWN_PEEKED) {
                moves.add(new Move(MoveType.FLIP, s.getRow(), s.getColumn()));
            }
            moves.add(new Move(MoveType.REPLACE_WITH_DISCARD, s.getRow(), s.getColumn()));
        }
        moves.add(new Move(MoveType.DRAW));
        return moves;
    }

    private List<Move> generatePostDrawMoves() {
        List<Move> moves = new LinkedList<>();
        // Generate replace with drawn card moves.
        for (Hand.Spot s: hand) {
            moves.add(new Move(MoveType.REPLACE_WITH_DRAWN_CARD, s.getRow(), s.getColumn()));
        }
        moves.add(new Move(MoveType.DECLINE_DRAWN_CARD));
        return moves;
    }
}

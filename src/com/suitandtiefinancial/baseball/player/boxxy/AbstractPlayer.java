package com.suitandtiefinancial.baseball.player.boxxy;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Event;
import com.suitandtiefinancial.baseball.game.Game;
import com.suitandtiefinancial.baseball.game.GameView;
import com.suitandtiefinancial.baseball.game.Move;
import com.suitandtiefinancial.baseball.game.MoveType;
import com.suitandtiefinancial.baseball.player.Player;

abstract class AbstractPlayer implements Player {
	protected final float cardEv;
	protected Hand myHand;
	protected Hand fastestHand;
	protected Hand bestHandBesidesMe;
	protected List<Hand> hands;
	private int index;
	protected GameView gv;
	protected Set<PossibleMove> possibleMoves;
	Card drawnCard;

	public AbstractPlayer() {
		cardEv = calculateCardEv();
		possibleMoves = new HashSet<PossibleMove>();
	}

	public void processEvent(Event event) {
		switch (event.getType()) {
		case COLLAPSE:
			hands.get(event.getPlayerIndex()).collapseColumn(event.getColumn());
			break;
		case FLIP:
		case SET:
			hands.get(event.getPlayerIndex()).setCard(event.getCard(), event.getRow(), event.getColumn());
			break;
		default:
			break;

		}
	}

	public void initialize(GameView gv, int index) {
		this.gv = gv;
		this.index = index;
		hands = new ArrayList<Hand>();
		for (int i = 0; i < gv.getNumberOfPlayers(); i++) {
			hands.add(new Hand(Game.ROWS, Game.COLUMNS));
		}
		myHand = hands.get(index);
		fastestHand = myHand;
	}

	public Move getOpener() {
		if (myHand.isCardRevealed(0, 0)) {
			return new Move(MoveType.FLIP, 0, 1);
		} else {
			return new Move(MoveType.FLIP, 0, 0);
		}
	}

	public Move getMove() {
		return getMoveInternal(null);
	}

	public Move getMoveWithDraw(Card c) {
		return getMoveInternal(c);
	}

	private Move getMoveInternal(Card c) {
		drawnCard = c;
		generateMoves(c);
		evaluateMoves();
		drawnCard = null;
		return highestScoringMove();
	}

	private void generateMoves(Card c) {
		int row, column;
		possibleMoves.clear();
		if (c == null) {
			possibleMoves.add(new PossibleMove(MoveType.DRAW));
			for (column = 0; column < myHand.getColumns(); column++) {
				if (myHand.getHiddenCardsInColumn(column) > 0) {
					row = myHand.getRowOfFirstHiddenCardInColumn(column);
					possibleMoves.add(new PossibleMove(MoveType.FLIP, row, column));
				}
			}
		} else {
			possibleMoves.add(new PossibleMove(MoveType.DECLINE_DRAWN_CARD));
		}

		MoveType mt = (c == null ? MoveType.REPLACE_WITH_DISCARD : MoveType.REPLACE_WITH_DRAWN_CARD);
		for (column = 0; column < myHand.getColumns(); column++) {
			if (myHand.getHiddenCardsInColumn(column) > 0) {
				row = myHand.getRowOfFirstHiddenCardInColumn(column);
				possibleMoves.add(new PossibleMove(mt, row, column));
			}
		}

		for (column = 0; column < myHand.getColumns(); column++) {
			if (myHand.isColumnCollapsed(column)) {
				continue;
			}
			for (row = 0; row < myHand.getRows(); row++) {
				if (myHand.isCardRevealed(row, column)) {
					possibleMoves.add(new PossibleMove(mt, row, column));
				}
			}
		}

	}

	private void evaluateMoves() {
		evaluateMovesPreCalculations();
		for (PossibleMove pm : possibleMoves) {
			pm.score = evaluateMove(pm.move);
		}
	}

	float evaluateMove(Move move) {
		switch (move.getMoveType()) {
		case DECLINE_DRAWN_CARD:
			return 0;
		case DRAW:
			return evaluateDraw();
		case FLIP:
			return evaluateFlip(move.getColumn());
		case REPLACE_WITH_DISCARD:
			return evaluateReplace(getDiscard(), move.getRow(), move.getColumn());
		case REPLACE_WITH_DRAWN_CARD:
			return evaluateReplace(drawnCard, move.getRow(), move.getColumn());
		default:
			throw new IllegalStateException();

		}
	}

	protected float evaluateReplace(Card newCard, int row, int column) {
		if (!myHand.isCardRevealed(row, column)) {
			return evaluateReplaceHidden(newCard, column);
		} else {
			return evaluateReplaceCard(newCard, myHand.getCard(row, column), row, column);
		}
	}

	abstract float evaluateReplaceHidden(Card newCard, int column);

	abstract float evaluateReplaceCard(Card newCard, Card oldCard, int row, int column);

	abstract float evaluateDraw();

	abstract float evaluateFlip(int column);

	private Move highestScoringMove() {
		float max = -100000000000f;
		Move best = null;
		for (PossibleMove pm : possibleMoves) {
			if (pm.score > max) {
				max = pm.score;
				best = pm.move;
			}
		}
		return best;
	}

	private float calculateCardEv() {
		int number = 0;
		float total = 0;
		for (Card c : Card.values()) {
			number += c.getQuantity();
			total += c.getQuantity() * c.getValue();
		}
		return total / number;
	}

	@Override
	public void showPeekedCard(int row, int column, Card c) {
		// We don't peek
	}

	protected Card getDiscard() {
		return gv.getDiscardUpCard();
	}

	protected void evaluateMovesPreCalculations() {
		float bestOtherScoreSimulated = 100000;
		for (int player = 0; player < gv.getNumberOfPlayers(); player++) {
			Hand h = hands.get(player);

			if (h.getNumberOfHiddenCards() < fastestHand.getNumberOfHiddenCards()) {
				fastestHand = h;
			}

			if (player == index) {
				continue;
			}

			float simulatedScore = evaluateHand(h);
			if (simulatedScore < bestOtherScoreSimulated) {
				bestHandBesidesMe = h;
				bestOtherScoreSimulated = simulatedScore;
			}
		}
	}

	protected float evaluateHand(Hand h) {
		float simulatedScore = h.getTotal();
		if (h.getNumberOfHiddenCards() > 0) {
			simulatedScore += 2 + (h.getNumberOfHiddenCards() - 1) * 6;
		}
		//TODO this doesnt consider collapse shits
		return simulatedScore;
	}

}

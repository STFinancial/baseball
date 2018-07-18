package com.suitandtiefinancial.baseball.player.boxxy;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Game;
import com.suitandtiefinancial.baseball.game.GameView;
import com.suitandtiefinancial.baseball.game.Move;
import com.suitandtiefinancial.baseball.game.MoveType;
import com.suitandtiefinancial.baseball.player.Player;

abstract class AbstractPlayer implements Player {
	protected final float cardEv;
	protected Hand hand;
	private int index;
	private GameView gv;
	protected Move lastMove;
	protected Set<PossibleMove> possibleMoves;
	Card drawnCard;

	public AbstractPlayer() {
		cardEv = calculateCardEv();
		possibleMoves = new HashSet<PossibleMove>();
	}

	public void initialize(GameView gv, int index) {
		this.gv = gv;
		this.index = index;
		hand = new Hand(Game.ROWS, Game.COLUMNS);
		lastMove = null;
	}

	public Move getOpener() {
		updateHandFromLastMove();
		if (hand.isCardRevealed(0, 0)) {
			lastMove = new Move(MoveType.FLIP, 0, 1);
			return lastMove;
		} else {
			lastMove = new Move(MoveType.FLIP, 0, 0);
			return lastMove;
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
		updateHandFromLastMove();
		generateMoves(c);
		evaluateMoves();
		drawnCard = null;
		lastMove = highestScoringMove();
		return lastMove;
	}

	private void generateMoves(Card c) {
		int row, column;
		possibleMoves.clear();
		if (c == null) {
			possibleMoves.add(new PossibleMove(MoveType.DRAW));
			for (column = 0; column < hand.getColumns(); column++) {
				if (hand.getHiddenCardsInColumn(column) > 0) {
					row = hand.getRowOfFirstHiddenCardInColumn(column);
					possibleMoves.add(new PossibleMove(MoveType.FLIP, row, column));
				}
			}
		} else {
			possibleMoves.add(new PossibleMove(MoveType.DECLINE_DRAWN_CARD));
		}

		MoveType mt = (c == null ? MoveType.REPLACE_WITH_DISCARD : MoveType.REPLACE_WITH_DRAWN_CARD);
		for (column = 0; column < hand.getColumns(); column++) {
			if (hand.getHiddenCardsInColumn(column) > 0) {
				row = hand.getRowOfFirstHiddenCardInColumn(column);
				possibleMoves.add(new PossibleMove(mt, row, column));
			}
		}

		for (column = 0; column < hand.getColumns(); column++) {
			if (hand.isColumnCollapsed(column)) {
				continue;
			}
			for (row = 0; row < hand.getRows(); row++) {
				if (hand.isCardRevealed(row, column)) {
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

	abstract void evaluateMovesPreCalculations();

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

	private float evaluateReplace(Card newCard, int row, int column) {
		if (!hand.isCardRevealed(row, column)) {
			return evaluateReplaceHidden(newCard, column);
		} else {
			return evaluateReplaceCard(newCard, hand.getCard(row, column), row, column);
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

	protected void updateHandFromLastMove() {
		if (lastMove == null) {
			return;
		}
		if (!lastMove.getMoveType().hasRowColumn()) {
			return;
		}
		if (gv.isColumnCollapsed(index, lastMove.getColumn())) {
			hand.collapseColumn(lastMove.getColumn());
		} else {
			hand.setCard(gv.viewCard(index, lastMove.getRow(), lastMove.getColumn()), lastMove.getRow(),
					lastMove.getColumn());
		}
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

}

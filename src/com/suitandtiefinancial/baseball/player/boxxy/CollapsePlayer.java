package com.suitandtiefinancial.baseball.player.boxxy;

import java.awt.Point;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Move;
import com.suitandtiefinancial.baseball.game.MoveType;

public class CollapsePlayer extends AbstractPlayer {

	@Override
	Move myGetMove() {

		if (canCollapse(getDiscard())) {
			return collapse(MoveType.REPLACE_WITH_DISCARD, getDiscard());
		} else if (cardIsGood(getDiscard(), cardEv)) {
			if (weHaveABadCard(cardEv)) {
				return replaceWorstCard(MoveType.REPLACE_WITH_DISCARD);
			} else {
				return replaceHiddenCard(MoveType.REPLACE_WITH_DISCARD, getDiscard());
			}
		} else if (weHaveABadCard(cardEv)) {
			return move(MoveType.DRAW);
		} else {
			return flipNext();
		}

	}

	@Override
	public Move getMoveWithDraw(Card c) {
		if (canCollapse(c)) {
			return collapse(MoveType.REPLACE_WITH_DRAWN_CARD, c);
		} else if (cardIsGood(c, cardEv)) {
			if (weHaveABadCard(cardEv)) {
				return replaceWorstCard(MoveType.REPLACE_WITH_DRAWN_CARD);
			} else {
				return replaceHiddenCard(MoveType.REPLACE_WITH_DRAWN_CARD, c);
			}
		} else if (weHaveABadCard(c.getValue())) {
			return replaceWorstCard(MoveType.REPLACE_WITH_DRAWN_CARD);
		} else {

			return move(MoveType.DECLINE_DRAWN_CARD);
		}
	}

	private Move flipNext() {
		int column = hand.getColumnWithMostHiddenCards();
		return move(MoveType.FLIP, hand.getRowOfFirstHiddenCardInColumn(column), column);
	}

	private boolean cardIsGood(Card c, float threshold) {
		return c.getValue() < threshold;
	}

	private boolean weHaveABadCard(float threshold) {
		for (int column = 0; column < hand.getColumns(); column++) {
			if (hand.isColumnCollapsed(column)) {
				continue;
			}
			for (int row = 0; row < hand.getRows(); row++) {
				if (hand.isCardRevealed(row, column)) {
					if (!cardIsGood(hand.getCard(row, column), threshold)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean canCollapse(Card card) {
		if (card.getValue() < 1) {
			return false;
		}
		return hand.getColumnWithCard(card, 2) > -1;
	}

	private Move collapse(MoveType mt, Card c) {
		int column = hand.getColumnWithCard(c, 2);
		if (column < 0) {
			throw new IllegalStateException();
		}
		int row = -1;
		for (row = 0; row < hand.getRows(); row++) {
			if (!hand.isCardRevealed(row, column)) {
				break;
			} else if (hand.getCard(row, column) != c) {
				break;
			}
		}
		return move(mt, row, column);
	}

	private Move replaceHiddenCard(MoveType mt, Card c) {
		int columnWithPotential = hand.getColumnWithCard(c, 1);
		int row = -1;
		if (columnWithPotential > -1) {
			if (hand.getHiddenCardsInColumn(columnWithPotential) > 0) {
				// We have a column that matches our card, and has space to try for a collapse
				row = hand.getRowOfFirstHiddenCardInColumn(columnWithPotential);
				return move(mt, row, columnWithPotential);
			}
		}

		int column = hand.getColumnWithMostHiddenCards();
		row = hand.getRowOfFirstHiddenCardInColumn(column);
		return move(MoveType.FLIP, row, column);
	}

	private Move replaceWorstCard(MoveType mt) {
		return move(mt, getWorstCardLocation());
	}

	private Point getWorstCardLocation() {
		Point p = new Point(-1, -1);
		int max = -3;
		for (int column = 0; column < hand.getColumns(); column++) {
			if (hand.isColumnCollapsed(column)) {
				continue;
			}
			for (int row = 0; row < hand.getRows(); row++) {
				if (!hand.isCardRevealed(row, column)) {
					continue;
				}
				if (hand.getCard(row, column).getValue() > max) {
					max = hand.getCard(row, column).getValue();
					p.setLocation(row, column);
				}
			}
		}

		return p;
	}

}

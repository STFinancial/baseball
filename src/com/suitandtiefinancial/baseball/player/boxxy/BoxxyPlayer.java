package com.suitandtiefinancial.baseball.player.boxxy;

import java.util.HashSet;
import java.util.Set;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.MoveType;

public class BoxxyPlayer extends AbstractPlayer {

	private float probabilityToFinishColumn = 0;
	private float evBonusForCollapse;
	private float evBonusForReveal;
	private final boolean basicGoOutCheck;

	public BoxxyPlayer(float collapseBonus, float revealBonus, boolean basicGoOutCheck) {
		evBonusForCollapse = collapseBonus;
		this.evBonusForReveal = revealBonus;
		this.basicGoOutCheck = basicGoOutCheck;
	}

	@Override
	protected void evaluateMovesPreCalculations() {
		super.evaluateMovesPreCalculations();

		if (cardsLeft == 0) {
			probabilityToFinishColumn = .03f;
		} else if (cardsLeft == 1) {
			probabilityToFinishColumn = .1f;
		} else if (cardsLeft == 2) {
			probabilityToFinishColumn = .15f;
		} else if (cardsLeft == 3) {
			probabilityToFinishColumn = .3f;
		} else if (cardsLeft == 4) {
			probabilityToFinishColumn = .4f;
		} else if (cardsLeft == 5) {
			probabilityToFinishColumn = .5f;
		} else {
			probabilityToFinishColumn = .6f;
		}
	}

	@Override
	float evaluateDraw() {
		float base = 0, sum = 0, divisor = 0;
		for (Card c : Card.values()) {
			sum += evaluateDraw(c) * c.getQuantity();
			divisor += c.getQuantity();
		}
		return base + (sum / divisor);
	}

	private float evaluateDraw(Card c) {
		MoveType mt = MoveType.REPLACE_WITH_DRAWN_CARD;
		Set<PossibleMove> possible = new HashSet<PossibleMove>();
		int column, row;
		for (column = 0; column < hand.getColumns(); column++) {
			if (hand.getHiddenCardsInColumn(column) > 0) {
				row = hand.getRowOfFirstHiddenCardInColumn(column);
				possible.add(new PossibleMove(mt, row, column));
			}
		}

		for (column = 0; column < hand.getColumns(); column++) {
			if (hand.isColumnCollapsed(column)) {
				continue;
			}
			for (row = 0; row < hand.getRows(); row++) {
				if (hand.isCardRevealed(row, column)) {
					possible.add(new PossibleMove(mt, row, column));
				}
			}
		}

		float max = 0;
		for (PossibleMove pm : possible) {
			pm.score = evaluateReplace(c, pm.move.getRow(), pm.move.getColumn());
			if (pm.score > max) {
				max = pm.score;
			}
		}
		return max;
	}

	@Override
	float evaluateFlip(int column) {
		return -99999f; // Fuck flipping
	}

	@Override
	float evaluateReplaceHidden(Card newCard, int column) {
		float additional = 0f;

		if (hand.getHiddenCardsInColumn(column) == 3) {
			additional += evBonusForReveal * 2;
		} else if (hand.getHiddenCardsInColumn(column) == 2) {
			additional += evBonusForReveal;
		}

		if (cardsWeHaveLeft == 1 && basicGoOutCheck && super.cardsLeft != 0) {
			int scoreAfterFinal = getScoreAfterFinalMove(newCard);
			if (scoreAfterFinal >= super.bestOtherScoreSimulated + 3) {
				additional += -10000f;
			}else if (scoreAfterFinal <= super.bestOtherScoreSimulated - 3){
				additional += 10000f;
			}
		}

		return evaluateReplaceInternal(newCard, cardEv,hand.getRowOfFirstHiddenCardInColumn(column), column) + additional;
	}

	private int getScoreAfterFinalMove(Card finalCard) {
		if (cardsWeHaveLeft != 1) {
			throw new IllegalStateException();
		}
		int column;
		for (column = 0; column < hand.getColumns(); column++) {
			if (hand.getHiddenCardsInColumn(column) > 0) {
				break;
			}
		}
		if (hand.getCountOfCardInColumn(column, finalCard) == 2) {
			return super.ourActualTotal - finalCard.getValue() * 2;
		} else {
			return super.ourActualTotal + finalCard.getValue();
		}
	}

	@Override
	float evaluateReplaceCard(Card newCard, Card oldCard, int row, int column) {
		if (newCard == oldCard) {
			return 0f;
		}
		float additional = 0f;

		if (hand.getCountOfCardInColumn(column, oldCard) == 2) {
			Card otherCard = getOtherCardInColumn(column, oldCard, oldCard);
			float otherCardValue = (otherCard == null ? cardEv : otherCard.getValue());
			additional -= probabilityToFinishColumn * (oldCard.getValue() * 2 + otherCardValue) * evBonusForCollapse;
		}

		return evaluateReplaceInternal(newCard, oldCard.getValue(),row, column) + additional;
	}

	private float evaluateReplaceInternal(Card newCard, float oldCardEv, int row, int column) {
		if (hand.getCountOfCardInColumn(column, newCard) == 2) {
			return newCard.getValue() * 2 + oldCardEv;
		}
		float additional = 0;

		if (hand.getCountOfCardInColumn(column, newCard) == 1 && newCard != Card.JOKER) {
			Card otherCard = getOtherCardInColumn(column, newCard, null);
			float otherCardValue = (otherCard == null ? cardEv : otherCard.getValue());
			additional += probabilityToFinishColumn * (newCard.getValue() * 2 + otherCardValue) * evBonusForCollapse;
		}
		
		if(otherTwoCardsPair(row, column)) {
			additional -= hand.getCard((row + 1) % hand.getRows(), column).getValue();
		}

		return oldCardEv - newCard.getValue() + additional;
	}

	private boolean otherTwoCardsPair(int row, int column) {
		Card c = null;
		for(int r = 0; r < hand.getRows(); r++) {
			if(r == row) {
				continue;
			}
			if(!hand.isCardRevealed(r, column)) {
				return false;
			}
			
			if(c == null) {
				c = hand.getCard(r, column);
			}else {
				if(c != hand.getCard(r, column)) {
					return false;
				}
			}
		}
		return true;
	}

	private Card getOtherCardInColumn(int column, Card c1, Card c2) {
		boolean c1Used = false, c2Used = false;
		for (int row = 0; row < 3; row++) {
			Card c;
			if (!hand.isCardRevealed(row, column)) {
				c = null;
			} else {
				c = hand.getCard(row, column);
			}
			if (!c1Used && c == c1) {
				c1Used = true;
			} else if (!c2Used && c == c2) {
				c2Used = true;
			} else {
				return c;
			}
		}
		throw new IllegalStateException();
	}

}

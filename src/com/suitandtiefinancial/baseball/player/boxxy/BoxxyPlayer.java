package com.suitandtiefinancial.baseball.player.boxxy;

import java.util.HashSet;
import java.util.Set;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Event;
import com.suitandtiefinancial.baseball.game.MoveType;

public class BoxxyPlayer extends AbstractPlayer {

	private float evBonusForCollapse;
	private float evBonusForReveal;
	private float goOutBalls;

	public BoxxyPlayer(float collapseBonus, float revealBonus, float goOutBalls) {
		evBonusForCollapse = collapseBonus;
		this.evBonusForReveal = revealBonus;
		this.goOutBalls = goOutBalls;
	}

	@Override
	protected void evaluateMovesPreCalculations() {
		super.evaluateMovesPreCalculations();
	}

	@Override
	float evaluateDraw() {
		float sum = 0, divisor = 0;
		for (Card c : Card.values()) {
			if (getCardCountDraw(c) == 0) {
				continue;
			}
			sum += evaluateDraw(c) * getCardCountDraw(c);
			divisor += getCardCountDraw(c);
		}
		return (sum / divisor);
	}

	private float evaluateDraw(Card c) {
		MoveType mt = MoveType.REPLACE_WITH_DRAWN_CARD;
		Set<PossibleMove> possible = new HashSet<PossibleMove>();
		int column, row;
		for (column = 0; column < myHand.getColumns(); column++) {
			if (myHand.getHiddenCardsInColumn(column) > 0) {
				row = myHand.getRowOfFirstHiddenCardInColumn(column);
				possible.add(new PossibleMove(mt, row, column));
			}
		}

		for (column = 0; column < myHand.getColumns(); column++) {
			if (myHand.isColumnCollapsed(column)) {
				continue;
			}
			for (row = 0; row < myHand.getRows(); row++) {
				if (myHand.isCardRevealed(row, column)) {
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
		float additional = 0, base = 0;
		if (myHand.getHiddenCardsInColumn(column) == 3) {
			additional += evBonusForReveal;
		} else if (myHand.getHiddenCardsInColumn(column) == 2) {
			additional += evBonusForReveal / 2;
		}
		base = getEvFaceDown() - 7;
		if (myHand.getNumberOfHiddenCards() == 1 && super.fastestHand.getNumberOfHiddenCards() != 0) {
			int scoreAfterFinal = getScoreAfterFinalMove(null);
			if (scoreAfterFinal >= super.evaluateHand(bestHandBesidesMe)) {
				additional += -10000f;
			} else if (scoreAfterFinal <= super.evaluateHand(bestHandBesidesMe) - goOutBalls) {
				additional += 10000f;
			}
		}
		return additional + base;
	}

	@Override
	float evaluateReplaceHidden(Card newCard, int column) {
		float additional = 0f;

		if (myHand.getHiddenCardsInColumn(column) == 3) {
			additional += evBonusForReveal * 2;
		} else if (myHand.getHiddenCardsInColumn(column) == 2) {
			additional += evBonusForReveal;
		}

		if (myHand.getNumberOfHiddenCards() == 1 && super.fastestHand.getNumberOfHiddenCards() != 0) {
			int scoreAfterFinal = getScoreAfterFinalMove(newCard);
			if (scoreAfterFinal >= super.evaluateHand(bestHandBesidesMe)) {
				additional += -10000f;
			} else if (scoreAfterFinal <= super.evaluateHand(bestHandBesidesMe) - goOutBalls) {
				additional += 10000f;
			}
		}

		return evaluateReplaceInternal(newCard, cardEv, myHand.getRowOfFirstHiddenCardInColumn(column), column)
				+ additional;
	}

	private int getScoreAfterFinalMove(Card finalCard) {
		if (myHand.getNumberOfHiddenCards() != 1) {
			throw new IllegalStateException();
		}
		if (finalCard == null) {
			return (int) (myHand.getTotal() + super.getEvFaceDown()) + 1;
		}
		int column;
		for (column = 0; column < myHand.getColumns(); column++) {
			if (myHand.getHiddenCardsInColumn(column) > 0) {
				break;
			}
		}
		if (myHand.getCountOfCardInColumn(column, finalCard) == 2) {
			return super.myHand.getTotal() - finalCard.getValue() * 2;
		} else {
			return super.myHand.getTotal() + finalCard.getValue();
		}
	}

	@Override
	float evaluateReplaceCard(Card newCard, Card oldCard, int row, int column) {
		if (newCard == oldCard) {
			return 0f;
		}
		float additional = 0f;

		if (myHand.getCountOfCardInColumn(column, oldCard) == 2) {
			Card otherCard = getOtherCardInColumn(column, oldCard, oldCard);
			float otherCardValue = (otherCard == null ? cardEv : otherCard.getValue());
			additional -= probabilityToGetOneMoreCard(oldCard, false, otherCard == null)
					* (oldCard.getValue() * 2 + otherCardValue) * evBonusForCollapse;
		}

		return evaluateReplaceInternal(newCard, oldCard.getValue(), row, column) + additional;
	}

	private float evaluateReplaceInternal(Card newCard, float oldCardEv, int row, int column) {
		if (myHand.getCountOfCardInColumn(column, newCard) == 2) {
			return newCard.getValue() * 2 + oldCardEv;
		}
		float additional = 0;

		if (myHand.getCountOfCardInColumn(column, newCard) == 1 && newCard != Card.JOKER) {
			// We want to encourage placing a pair when getting a collapse is likely
			Card otherCard = getOtherCardInColumn(column, newCard, null);
			float otherCardValue = (otherCard == null ? cardEv : otherCard.getValue());
			additional += probabilityToGetOneMoreCard(newCard, true, otherCard == null)
					* (newCard.getValue() * 2 + otherCardValue) * evBonusForCollapse;
		}

		if (otherTwoCardsPair(row, column)) {
			// We want to discourage placing a card in a probable collapse
			Card otherCard = myHand.getCard((row + 1) % myHand.getRows(), column);
			additional -= otherCard.getValue() * probabilityToGetOneMoreCard(otherCard, false, false);
		}

		return oldCardEv - newCard.getValue() + additional;
	}

	private boolean otherTwoCardsPair(int row, int column) {
		Card c = null;
		for (int r = 0; r < myHand.getRows(); r++) {
			if (r == row) {
				continue;
			}
			if (!myHand.isCardRevealed(r, column)) {
				return false;
			}

			if (c == null) {
				c = myHand.getCard(r, column);
			} else {
				if (c != myHand.getCard(r, column)) {
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
			if (!myHand.isCardRevealed(row, column)) {
				c = null;
			} else {
				c = myHand.getCard(row, column);
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

	private float probabilityToGetOneMoreCard(Card c, boolean subtractOne, boolean hasTheFlip) {
		int minimumTriesLeft = super.fastestHand.getNumberOfHiddenCards(); // TODO predict whether or not we will get
																			// extra turns or this player will rush out,
																			// for now we assume worst case
		if (hasTheFlip) {
			minimumTriesLeft++;
		}
		int downCardsLeft = super.getCardCountFaceDown(c);
		int totalDownCards = super.getTotalCardCountFaceDown();
		// TODO we don't properly check post shuffle, or predict if we will get a
		// shuffle
		if (subtractOne) {
			downCardsLeft--;
		}
		float probabilityOfNotDrawing = 1f;
		for (int attempt = 0; attempt < minimumTriesLeft; attempt++) {
			probabilityOfNotDrawing *= (totalDownCards - attempt - downCardsLeft) / (totalDownCards - attempt);
		}
		// TODO cache some of this shit
		return (1 - probabilityOfNotDrawing);
	}
}

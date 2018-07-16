package com.suitandtiefinancial.baseball.player;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.GameView;
import com.suitandtiefinancial.baseball.game.Move;
import com.suitandtiefinancial.baseball.game.MoveType;

public class EVPlayer implements Player {
	private GameView g;
	private int index;
	private float downCardEv;
	boolean doneFirstOpener = false;
	int worstCardRow, worstCardColumn;
	float worstCardValue;

	public void initialize(GameView g, int index) {
		this.g = g;
		this.index = index;
		if (downCardEv == 0) {
			downCardEv = calculateDownCardEv();
		}
		doneFirstOpener = false;
	}

	private float calculateDownCardEv() {
		int number = 0;
		float total = 0;
		for (Card c : Card.values()) {
			number += c.getQuantity();
			total += c.getQuantity() * c.getValue();
		}
		return total / number;
	}

	@Override
	public Move getOpener() {
		if (doneFirstOpener) {
			return new Move(MoveType.FLIP, 0, 0);
		} else {
			doneFirstOpener = true;
			return new Move(MoveType.FLIP, 0, 1);
		}
	}

	@Override
	public Move getMove() {

		if (g.getDiscardUpCard().getValue() < downCardEv) {
			return discardMove();
		} else if (findWorstCardValue() > downCardEv) {
			return new Move(MoveType.DRAW);
		} else {
			return flipMove();
		}
	}

	private float findWorstCardValue() {

		worstCardRow = -1;
		worstCardColumn = -1;
		worstCardValue = -9999;
		float currentCardValue = -1;

		for (int column = 0; column < GameView.COLUMNS; column++) {
			if (g.isColumnCollapsed(index, column)) {
				continue;
			}
			for (int row = 0; row < GameView.ROWS; row++) {

				if (!g.isCardRevealed(index, row, column)) {
					currentCardValue = downCardEv;
				} else {
					currentCardValue = g.viewCard(index, row, column).getValue();
				}
				if (currentCardValue > worstCardValue) {
					worstCardRow = row;
					worstCardColumn = column;
					worstCardValue = currentCardValue;
				}
			}
		}
		return worstCardValue;
	}

	private Move flipMove() {
		for (int column = 0; column < GameView.COLUMNS; column++) {
			if (g.isColumnCollapsed(index, column)) {
				continue;
			}
			for (int row = 0; row < GameView.ROWS; row++) {
				if (!g.isCardRevealed(index, row, column)) {
					return new Move(MoveType.FLIP, row, column);
				}
			}
		}
		throw new IllegalStateException("No cards left to flip");
	}

	private Move discardMove() {
		findWorstCardValue();
		return new Move(MoveType.REPLACE_WITH_DISCARD, worstCardRow, worstCardColumn);
	}

	@Override
	public Move getMoveWithDraw(Card c) {
		if (c.getValue() < worstCardValue) {
			return new Move(MoveType.REPLACE_WITH_DRAWN_CARD, worstCardRow, worstCardColumn);
		} else {
			return new Move(MoveType.DECLINE_DRAWN_CARD);
		}
	}

	@Override
	public void showPeekedCard(int row, int column, Card c) {
		// not used
	}
}

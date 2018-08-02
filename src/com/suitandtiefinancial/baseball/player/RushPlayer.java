package com.suitandtiefinancial.baseball.player;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Event;
import com.suitandtiefinancial.baseball.game.GameView;
import com.suitandtiefinancial.baseball.game.Move;
import com.suitandtiefinancial.baseball.game.MoveType;

public class RushPlayer implements Player {
	private GameView g;
	private int index;
	private int row, column;
	private float downCardEv;

	public void initialize(GameView g, int index) {
		this.g = g;
		this.index = index;
		row = 0;
		column = 0;
		if (downCardEv == 0) {
			downCardEv = calculateDownCardEv();
		}
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
		return move(MoveType.FLIP);
	}

	@Override
	public Move getMove() {

		MoveType mt;
		if (g.getDiscardUpCard().getValue() < downCardEv) {
			mt = MoveType.REPLACE_WITH_DISCARD;
		} else {
			mt = MoveType.FLIP;
		}

		return move(mt);
	}

	private Move move(MoveType mt) {

		Move m = new Move(mt, row, column);
		column++;
		if (column == GameView.COLUMNS) {
			column = 0;
			row++;
		}
		return m;
	}

	@Override
	public Move getMoveWithDraw(Card c) {
		// not used
		return null;
	}

	@Override
	public void showPeekedCard(int row, int column, Card c) {
		// not used
	}

	@Override
	public void processEvent(Event event) {
		// not used
	}
}

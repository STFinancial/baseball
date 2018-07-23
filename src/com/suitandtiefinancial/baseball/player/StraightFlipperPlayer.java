package com.suitandtiefinancial.baseball.player;

import java.util.Random;

import com.suitandtiefinancial.baseball.game.*;

public class StraightFlipperPlayer implements Player {
	private GameView g;
	private int index;
	private int row, column;

	public void initialize(GameView g, int index) {
		this.g = g;
		this.index = index;
		row = 0;
		column = 0;
	}

	@Override
	public Move getOpener() {
		return flipNext();
	}

	@Override
	public Move getMove() {
		return flipNext();
	}

	private Move flipNext() {
		Move m = new Move(MoveType.FLIP, row, column);
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

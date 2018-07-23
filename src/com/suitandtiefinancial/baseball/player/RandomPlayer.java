package com.suitandtiefinancial.baseball.player;

import java.util.Random;

import com.suitandtiefinancial.baseball.game.*;

public class RandomPlayer implements Player {
	private final Random r;
	private GameView g;
	private int index;

	public RandomPlayer(Random r) {
		this.r = r;
	}

	public void initialize(GameView g, int index) {
		this.g = g;
		this.index = index;
	}

	@Override
	public Move getOpener() {
		int row = r.nextInt(GameView.ROWS);
		int column = r.nextInt(GameView.COLUMNS);
		if (r.nextBoolean()) {
			if (g.viewCard(index, row, column) != null) {
				// on the off chance we already flipped this card, we need a different legal
				// move
				return getOpener();
			}
			return new Move(MoveType.FLIP, row, column);
		} else {
			return new Move(MoveType.PEEK, row, column);
		}
	}

	@Override
	public Move getMove() {
		int nextInt = r.nextInt(3);
		if(nextInt==0) {
			return new Move(MoveType.DRAW);
		}else if(nextInt==1){
			return new Move(MoveType.REPLACE_WITH_DISCARD, r.nextInt(GameView.ROWS), r.nextInt(GameView.COLUMNS));
		}else {
			int row = r.nextInt(GameView.ROWS);
			int column = r.nextInt(GameView.COLUMNS);
			if (g.viewCard(index, row, column) != null) {
				return getMove();
			}else {
				return new Move(MoveType.FLIP, row, column);
			}
		}
	}

	@Override
	public Move getMoveWithDraw(Card c) {
		if(r.nextBoolean()) {
			return new Move(MoveType.DECLINE_DRAWN_CARD);
		}else {
			return new Move(MoveType.REPLACE_WITH_DRAWN_CARD, r.nextInt(GameView.ROWS), r.nextInt(GameView.COLUMNS));
		}
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

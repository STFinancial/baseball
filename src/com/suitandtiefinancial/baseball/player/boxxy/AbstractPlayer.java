package com.suitandtiefinancial.baseball.player.boxxy;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Game;
import com.suitandtiefinancial.baseball.game.GameView;
import com.suitandtiefinancial.baseball.game.Move;
import com.suitandtiefinancial.baseball.game.MoveType;
import com.suitandtiefinancial.baseball.player.Player;

public abstract class AbstractPlayer implements Player {
	protected final float cardEv;
	protected Hand hand;
	private int index;
	private GameView gv;
	protected Move lastMove;

	public AbstractPlayer() {
		cardEv = calculateCardEv();
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
			return move(MoveType.FLIP, 0, 1);
		} else {
			return move(MoveType.FLIP, 0, 0);
		}
	}

	public Move getMove() {
		updateHandFromLastMove();
		return myGetMove();
	}

	abstract Move myGetMove();

	protected Move move(MoveType mt, int row, int column) {
		Move m = new Move(mt, row, column);
		lastMove = m;
		return m;
	}
	protected Move move(MoveType mt, Point p) {
		Move m = new Move(mt, p.x, p.y);
		lastMove = m;
		return m;
	}

	protected Move move(MoveType mt) {
		// TODO Auto-generated method stub
		return new Move(mt);
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

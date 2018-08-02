package com.suitandtiefinancial.baseball.player.boxxy;

import com.suitandtiefinancial.baseball.game.Move;
import com.suitandtiefinancial.baseball.game.MoveType;

class PossibleMove {
	public final Move move;
	public float score = 0;

	public PossibleMove(MoveType moveType) {
		move = new Move(moveType);
	}

	public PossibleMove(MoveType moveType, int row, int column) {
		move = new Move(moveType, row, column);
	}

}

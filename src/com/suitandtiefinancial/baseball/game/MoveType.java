package com.suitandtiefinancial.baseball.game;

public enum MoveType {
	PEEK(true), FLIP(true), REPLACE_WITH_DISCARD(true),
	DRAW(false), REPLACE_WITH_DRAWN_CARD(true), DECLINE_DRAWN_CARD(false);
	
	private final boolean hasRowColumn;

	private MoveType(boolean hasRowColumn) {
		this.hasRowColumn = hasRowColumn;
	}

	public boolean hasRowColumn() {
		return hasRowColumn;
	}
}

package com.suitandtiefinancial.baseball.game;

public class Move {
	private int row = -1;
	private int column = -1;
	private final MoveType moveType;

	public Move(MoveType moveType) {
		this.moveType = moveType;
		if (moveType.hasRowColumn()) {
			throw new IllegalStateException("Created " + moveType + " move that required a row/column but didn't have them provided");
		}
	}

	public Move(MoveType moveType, int row, int column) {
		if (!moveType.hasRowColumn()) {
			throw new IllegalStateException("Created " + moveType + " move that did not required a row/column but had them provided");
		}
		this.moveType = moveType;
		this.row = (row);
		this.column = (column);
	}

	public MoveType getMoveType() {
		return moveType;
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}
	
	@Override
	public String toString() {
		if(moveType.hasRowColumn()) {
			return moveType + " at (" + row + ", " + column + ")";
		}else {
			return moveType.toString();
		}
	}

}

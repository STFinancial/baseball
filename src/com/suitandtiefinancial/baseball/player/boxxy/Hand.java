package com.suitandtiefinancial.baseball.player.boxxy;

import java.util.ArrayList;
import java.util.List;

import com.suitandtiefinancial.baseball.game.Card;

class Hand {
	private final List<Column> columns;
	private final int rows, cols;

	public Hand(int rows, int cols) {
		columns = new ArrayList<Column>(cols);
		this.cols = cols;
		this.rows = rows;
		for (int column = 0; column < cols; column++) {
			columns.add(new Column(rows));
		}
	}

	public boolean isColumnCollapsed(int column) {
		return columns.get(column).isCollapsed();
	}

	public boolean isCardRevealed(int row, int column) {
		return columns.get(column).getTile(row).isRevealed();
	}

	public Card getCard(int row, int column) {
		return columns.get(column).getTile(row).getCard();
	}

	public void setCard(Card c, int row, int column) {
		columns.get(column).getTile(row).set(c);
	}

	public void collapseColumn(int column) {
		columns.get(column).collapse();
	}

	public int getCountOfCardInColumn(int column, Card c) {
		return columns.get(column).getCountOfCard(c);
	}

	public int getColumns() {
		return cols;
	}

	public int getRows() {
		return rows;
	}

	public int getColumnWithMostHiddenCards() {
		int max = 0;
		int maxColumn = 0;
		for (int column = 0; column < cols; column++) {
			int count = getHiddenCardsInColumn(column);
			if (count > max) {
				max = count;
				maxColumn = column;
			}
		}
		return maxColumn;
	}

	public int getHiddenCardsInColumn(int column) {
		int count = 0;
		for (Tile t : columns.get(column).tiles) {
			if (!t.isRevealed()) {
				count++;
			}
		}
		return count;
	}

	
	public int getColumnWithCard(Card c, int numberOfCards) {
		for (int column = 0; column < getColumns(); column++) {
			if (getCountOfCardInColumn(column, c) == numberOfCards) {
				return column;
			}
		}
		return -1;
	}
	
	private class Column {
		private final List<Tile> tiles;

		public Column(int rows) {
			tiles = new ArrayList<Tile>(rows);
			for (int row = 0; row < rows; row++) {
				tiles.add(new Tile());
			}
		}

		public int getCountOfCard(Card c) {
			int count = 0;
			for (Tile t : tiles) {
				if (!t.isRevealed) {
					continue;
				}
				if (t.getCard() == c) {
					count++;
				}
			}
			return count;
		}

		public void collapse() {
			tiles.clear();
		}

		public boolean isCollapsed() {
			return tiles.size() == 0;
		}

		public Tile getTile(int index) {
			return tiles.get(index);
		}
	}

	private class Tile {
		private Card c = null;
		private boolean isRevealed = false;

		public Card getCard() {
			if (!isRevealed) {
				throw new IllegalStateException("Tried to look at a revealed card");
			}
			return c;
		}

		public void set(Card c) {
			this.c = c;
			isRevealed = true;
		}

		public boolean isRevealed() {
			return isRevealed;
		}

	}

	public int getRowOfFirstHiddenCardInColumn(int column) {
		for (int row = 0; row < rows; row++) {
			if (!isCardRevealed(row, column)) {
				return row;
			}
		}
		throw new IllegalStateException("No hidden cards found in column: " + column);
	}
}

package com.suitandtiefinancial.baseball.player.boxxy;

import java.util.ArrayList;
import java.util.List;

import com.suitandtiefinancial.baseball.game.Card;

class Hand {
	private final List<Column> columns;
	private final int rows, cols;
	private int total = 0, hiddenCards = 9;

	public Hand(int rows, int cols) {
		columns = new ArrayList<Column>(cols);
		this.cols = cols;
		this.rows = rows;
		for (int column = 0; column < cols; column++) {
			columns.add(new Column(rows));
		}
	}
	
	public int getTotal() {
		return total;
	}
	
	public int getNumberOfHiddenCards() {
		return hiddenCards;
	}

	public boolean isColumnCollapsed(int column) {
		return columns.get(column).isCollapsed();
	}

	public boolean isCardRevealed(int row, int column) {
		if(isColumnCollapsed(column)) {
			throw new IllegalStateException();
		}
		return columns.get(column).getTile(row).isRevealed();
	}

	public Card getCard(int row, int column) {
		return columns.get(column).getTile(row).getCard();
	}

	public void setCard(Card c, int row, int column) {
		total += c.getValue();
		if(isCardRevealed(row, column)) {
			total -= getCard(row, column).getValue();
		}else {
			hiddenCards--;
		}
		columns.get(column).getTile(row).set(c);
		if(columns.get(column).canCollapse()) {
			total -= 3 * getCard(0, column).getValue();
			columns.get(column).collapse();
		}
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

	public int getHiddenCardsInColumn(int column) {
		int count = 0;
		for (Tile t : columns.get(column).tiles) {
			if (!t.isRevealed()) {
				count++;
			}
		}
		return count;
	}

	public int getRowOfFirstHiddenCardInColumn(int column) {
		for (int row = 0; row < rows; row++) {
			if (!isCardRevealed(row, column)) {
				return row;
			}
		}
		throw new IllegalStateException("No hidden cards found in` column: " + column);
	}

	private class Column {
		private final List<Tile> tiles;

		public Column(int rows) {
			tiles = new ArrayList<Tile>(rows);
			for (int row = 0; row < rows; row++) {
				tiles.add(new Tile());
			}
		}

		public boolean canCollapse() {
			if(isCollapsed()) {
				return false;
			}
			Card c = null;
			for(Tile t : tiles) {
				if(!t.isRevealed()) {
					return false;
				}
				if(c == null) {
					c = t.getCard();
				}else if (c != t.getCard()) {
					return false;
				}
			}
			return true;
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

}

package com.suitandtiefinancial.baseball.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Timothy on 7/6/18.
 * 
 */
class Hand {
	private Spot[][] spots;

	private class Spot {
		Card card = null;
		SpotState state = SpotState.FACE_DOWN;
	}

	Hand() {
		spots = new Spot[Game.ROWS][Game.COLUMNS];
		for (int row = 0; row < Game.ROWS; row++) {
			for (int column = 0; column < Game.COLUMNS; column++) {
				spots[row][column] = new Spot();
			}
		}
	}

	List<Card> replace(Card replacement, int row, int column) {
		Spot s = spots[row][column];
		if (s.state == SpotState.COLLAPSED) {
			throw new IllegalStateException("Tried to replace an already collapsed card.");
		}

		Card replacedCard = s.card;
		s.card = replacement;
		s.state = SpotState.FACE_UP;

		List<Card> returnCardList = new ArrayList<>(4);
		if (checkCollapse(column)) {
			returnCardList.addAll(collapse(column));
		}
		returnCardList.add(replacedCard);

		return returnCardList;
	}

	/** Flips a face-down card face-up 
	 *  Returns List of collapsed cards, or null if no cards collapsed.
	 */
	List<Card> flip(int row, int column) {
		Spot s = spots[row][column];
		if (s.state == SpotState.COLLAPSED) {
			throw new IllegalStateException("Tried to flip an already collapsed card");
		} else if (s.state == SpotState.FACE_UP) {
			throw new IllegalStateException("Tried to flip an already revealed card");
		}
		s.state = SpotState.FACE_UP;
		if (checkCollapse(column)) {
			return collapse(column);
		} else {
			return Collections.emptyList();
		}
	}

	private boolean checkCollapse(int column) {
		return spots[0][column].state == SpotState.FACE_UP && spots[1][column].state == SpotState.FACE_UP && spots[2][column].state == SpotState.FACE_UP
				&& spots[0][column].card.getValue() == spots[1][column].card.getValue()
				&& spots[1][column].card.getValue() == spots[2][column].card.getValue();

	}

	/**
	 * Collapses the given column, removing the card values from the hand and
	 * updating the total checkCollapse should be called before using this function
	 * 
	 * @param column
	 * @return List of cards collapsed
	 * @author Boxxy
	 */
	private List<Card> collapse(int column) {
		ArrayList<Card> returnList = new ArrayList<>(Game.ROWS);
		Spot s;
		for (int row = 0; row < Game.ROWS; row++) {
			s = spots[row][column];
			returnList.add(s.card);
			s.card = null;
			s.state = SpotState.COLLAPSED;
		}
		return returnList;
	}

	/** Used to populate the hand before the first turn. */
	void dealCard(Card draw, int row, int column) {
		spots[row][column].card = draw;
		spots[row][column].state = SpotState.FACE_DOWN;
	}

	SpotState getSpotState(int row, int column) {
		return spots[row][column].state;
	}
	
	Card peekCard(int row, int column) {
		return spots[row][column].card;
	}

	List<Card> revealAll() {
		List<Card> toReturn = new ArrayList<>();
		
		for (int row = 0; row < Game.ROWS; row++) {
			for (int column = 0; column < Game.COLUMNS; column++) {
				if (spots[row][column].state == SpotState.FACE_DOWN || spots[row][column].state == SpotState.FACE_DOWN_PEEKED) {
					toReturn.addAll(flip(row, column));
				}
			}
		}
		return toReturn;
	}
}

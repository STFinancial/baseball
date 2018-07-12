package com.suitandtiefinancial.baseball.game;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Timothy on 7/6/18.
 * 
 * TODO @Boxxy If we don't trust the AIs, then we can never give direct access to this object because of the public functions not having gaurdrails
 * However, it seems easier/faster if we trust the AIs and just give them full object access, as opposed to encapsulating behind events or something
 */
class Hand {

	private Spot[][] spots;

	private class Spot {
		boolean isRevealed = false;
		Card card = null;
	}
	
	private int total;

	public Hand() {
		spots = new Spot[Game.ROWS][Game.COLUMNS];
		for (int row = 0; row < Game.ROWS; row++) {
			for (int column = 0; column < Game.COLUMNS; column++) {
				spots[row][column] = new Spot();
			}
		}
		total = 0;
	}

	public List<Card> replace(Card replacement, int row, int column) {
		Spot s = spots[row][column];
		if (spots[row][column].card == null) {
			throw new IllegalStateException("Tried to replace an already collapsed card");
		}
		if (s.isRevealed) {
			total -= s.card.getValue();
		}
		Card replacedCard = s.card;

		s.card = replacement;
		total += replacement.getValue();
		s.isRevealed = true;

		List<Card> returnCardList = new ArrayList<Card>(4);
		if (checkCollapse(column)) {
			returnCardList.addAll(collapse(column));
		}
		returnCardList.add(replacedCard);

		return returnCardList;
	}

	/** Flips a face-down card face-up 
	 *  Returns List of collapsed cards, or null if no cards collapsed.
	 */
	public List<Card> flip(int row, int column) {
		if (spots[row][column].card == null) {
			throw new IllegalStateException("Tried to flip an already collapsed card");
		}else if (spots[row][column].isRevealed) {
			throw new IllegalStateException("Tried to flip an already revealed card");
		} 
		spots[row][column].isRevealed = true;
		total += spots[row][column].card.getValue();
		if (checkCollapse(column)) {
			return collapse(column);
		} else {
			return null;
		}
	}

	private boolean checkCollapse(int column) {
		return spots[0][column].isRevealed && spots[1][column].isRevealed && spots[2][column].isRevealed
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
		for (int row = 0; row < Game.ROWS; row++) {
			returnList.add(spots[row][column].card);
			total -= spots[row][column].card.getValue();
			spots[row][column].card = null;
		}
		return returnList;
	}

	public int getRevealedTotal() {
		return total;
	}

	public void dealCard(Card draw, int row, int column) {
		spots[row][column].card = draw;
		spots[row][column].isRevealed = false;
	}

	public Card viewCard(int row, int column) {
		if (spots[row][column].isRevealed) {
			return spots[row][column].card;
		} else {
			return null;
		}
	}
	
	public Card peekCard(int row, int column) {
		return spots[row][column].card;
	}

	public String displayRow(int row) {
		String s = "";
		for(int column = 0; column < Game.COLUMNS; column++) {
			
			if(!spots[row][column].isRevealed) {
				s+="X";
			}else if(spots[row][column].card == null){
				s += " ";
			}else {
				switch(spots[row][column].card) {
				case ACE:
					s+="A";
					break;
				case EIGHT:
					s+="8";
					break;
				case FIVE:
					s+="5";
					break;
				case FOUR:
					s+="4";
					break;
				case JACK:
					s+="J";
					break;
				case JOKER:
					s+="E";
					break;
				case KING:
					s+="K";
					break;
				case NINE:
					s+="9";
					break;
				case QUEEN:
					s+="Q";
					break;
				case SEVEN:
					s+="7";
					break;
				case SIX:
					s+="6";
					break;
				case TEN:
					s+="T";
					break;
				case THREE:
					s+="3";
					break;
				case TWO:
					s+="2";
					break;
				}
			}
		}
		return s;
	}

	public boolean isOut() {
		for(int row = 0; row < Game.ROWS; row++) {
			for(int column = 0; column < Game.COLUMNS; column++) {
				if(!spots[row][column].isRevealed)
					return false;
			}
		}
		return true;
	}

	public List<Card> revealAll() {
		List<Card> toReturn = null;
		List<Card> temp;
		
		for(int row = 0; row < Game.ROWS; row++) {
			for(int column = 0; column < Game.COLUMNS; column++) {
				if(!spots[row][column].isRevealed) {
					temp = flip(row, column);
					if(temp!=null) {
						if(toReturn == null) {
							toReturn = temp;
						}else {
							toReturn.addAll(temp);
						}
					}
				}
			}
		}
		return toReturn;
	}
}

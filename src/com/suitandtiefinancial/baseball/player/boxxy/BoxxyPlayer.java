package com.suitandtiefinancial.baseball.player.boxxy;

import java.awt.Point;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Move;
import com.suitandtiefinancial.baseball.game.MoveType;

public class BoxxyPlayer extends AbstractPlayer {

	Card worstCard;
	float goodCardThreshold = 4.5f;

	@Override
	void evaluateMovesPreCalculations() {

		worstCard = Card.JOKER;
		for (int column = 0; column < hand.getColumns(); column++) {
			if (hand.isColumnCollapsed(column)) {
				continue;
			}
			for (int row = 0; row < hand.getRows(); row++) {
				if (hand.isCardRevealed(row, column)) {
					if (hand.getCard(row, column).getValue() > worstCard.getValue()) {
						worstCard = hand.getCard(row, column);
					}
				}
			}
		}
	}

	@Override
	float evaluateDraw() {
		float base = 0, sum = 0;
		for (Card c : Card.values()) {
			sum += evaluateDraw(c);
		}
		return base + (sum / Card.values().length);
	}

	private float evaluateDraw(Card c) {
		// TODO make this evaluate as thh best of the 9 possible replaces and tie in
		// with the other evaluates
		if (c.getValue() > 0) {
			for (int column = 0; column < hand.getColumns(); column++) {
				if (hand.getCountOfCardInColumn(column, c) == 2) {
					return c.getValue() * 2 + cardEv;
				}
			}
		}
		if (c.getValue() < goodCardThreshold) {
			if (worstCard.getValue() > cardEv) {
				return worstCard.getValue() - c.getValue();
			} else {
				return cardEv - c.getValue();
			}
		} else {
			if (c.getValue() < worstCard.getValue()) {
				return worstCard.getValue() - c.getValue();
			}
		}
		return 0;
	}

	@Override
	float evaluateFlip(int column) {
		if (hand.getHiddenCardsInColumn(column) == 3) {
			return 3f;
		} else if (hand.getHiddenCardsInColumn(column) == 2) {
			return 1f;
		} else {
			return .5f; // TODO add some go out checks here
		}

	}

	@Override
	float evaluateReplaceHidden(Card newCard, int column) {
		// TODO go out checks
		if (hand.getCountOfCardInColumn(column, newCard) == 2 && newCard != Card.JOKER) {
			return newCard.getValue() * 2 + cardEv;
		}
		float base = cardEv - newCard.getValue(); 
		float additional = 0;
		
		if(hand.getCountOfCardInColumn(column, newCard) == 1 && newCard != Card.JOKER) {
			additional += 2f; //This should be smarter, like figuring odds that we can finisht he row we start adn the risk of not finishing
		}
		
		if(hand.getHiddenCardsInColumn(column) == 3) {
			additional += 5f;
		}else if (hand.getHiddenCardsInColumn(column) == 2) {
			additional += 1f;
		}
		
		return base + additional;
	}

	@Override
	float evaluateReplaceCard(Card newCard, Card oldCard, int row, int column) {
		if(newCard == oldCard) {
			return 0f;
		}
		if (hand.getCountOfCardInColumn(column, newCard) == 2 && newCard != Card.JOKER) {
			return newCard.getValue() * 2 + cardEv;
		}
		
		float base = oldCard.getValue() - newCard.getValue(); 
		float additional = 0;
		
		if(hand.getCountOfCardInColumn(column, newCard) == 1 && newCard != Card.JOKER) {
			additional += 2f; //This should be smarter, like figuring odds that we can finisht he row we start adn the risk of not finishing
		}
		
		return base + additional;
	}

}

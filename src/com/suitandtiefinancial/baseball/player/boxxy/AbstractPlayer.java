package com.suitandtiefinancial.baseball.player.boxxy;

import java.awt.Point;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Event;
import com.suitandtiefinancial.baseball.game.EventType;
import com.suitandtiefinancial.baseball.game.Game;
import com.suitandtiefinancial.baseball.game.GameView;
import com.suitandtiefinancial.baseball.game.Move;
import com.suitandtiefinancial.baseball.game.MoveType;
import com.suitandtiefinancial.baseball.player.Player;

abstract class AbstractPlayer implements Player {
	protected Hand myHand;
	protected Hand fastestHand;
	protected Hand bestHandBesidesMe;
	protected List<Hand> hands;
	private int index;
	protected GameView gv;
	protected Set<PossibleMove> possibleMoves= new HashSet<PossibleMove>();;
	Card drawnCard;
	private final EnumMap<Card, Integer> discard = new EnumMap<Card, Integer>(Card.class);
	private final EnumMap<Card, Integer> inPlay = new EnumMap<Card, Integer>(Card.class);
	private final EnumMap<Card, Integer> deck = new EnumMap<Card, Integer>(Card.class);
	private final EnumMap<Card, Integer> faceDown = new EnumMap<Card, Integer>(Card.class);
	private boolean shuffleHappened;
	float cardEv;

	@Override
	public void processEvent(Event event) {
		switch (event.getType()) {
		case FLIP:
			hands.get(event.getPlayerIndex()).setCard(event.getCard(), event.getRow(), event.getColumn());
			moveCard(faceDown, inPlay, event.getCard());
			break;
		case DISCARD:
			if (event.getTriggeringEvent().getType() == EventType.DRAW) {
				processDrawThenDiscard(event);
			} else if (event.getTriggeringEvent().getType() == EventType.COLLAPSE) {
				processCollapseDiscard(event);
			} else if (event.getTriggeringEvent().getType() == EventType.SET) {
				processSet(event);
			} else {
				throw new IllegalStateException();
			}
			break;
		case INITIAL_DISCARD:
			moveCard(faceDown, discard, event.getCard());
			break;
		case SHUFFLE:
			processShuffle();
			break;
		default:
			break;

		}
	}

	private void processDrawThenDiscard(Event event) {
		if (shuffleHappened) {
			moveCard(deck, discard, event.getCard());
		} else {
			moveCard(faceDown, discard, event.getCard());
		}
	}

	private void processCollapseDiscard(Event event) {
		moveCard(inPlay, discard, event.getCard());
	}

	private void processSet(Event discardEvent) {
		Event setEvent = discardEvent.getTriggeringEvent();
		Event drawEvent = setEvent.getTriggeringEvent();
		EnumMap<Card, Integer> setCardSource, discardCardSource;
		Hand h = hands.get(discardEvent.getPlayerIndex());
		if (drawEvent.getType() == EventType.DRAW) {
			if (shuffleHappened) {
				setCardSource = deck;
			} else {
				setCardSource = faceDown;
			}
		} else if (drawEvent.getType() == EventType.DRAW_DISCARD) {
			setCardSource = discard;
		} else {
			throw new IllegalStateException();
		}
		moveCard(setCardSource, inPlay, setEvent.getCard());

		if (h.isCardRevealed(setEvent.getRow(), setEvent.getColumn())) {
			discardCardSource = inPlay;
		} else {
			discardCardSource = faceDown;
		}
		moveCard(discardCardSource, discard, discardEvent.getCard());

		h.setCard(setEvent.getCard(), setEvent.getRow(), setEvent.getColumn());
	}

	private void processShuffle() {
		//Currently, shuffle happens after a draw attempt on an empty deck, the top discard is shuffled
		shuffleHappened = true;
		for (Card c : Card.values()) {
			deck.put(c, discard.get(c));
			discard.put(c, 0);
		}
	}

	private void moveCard(EnumMap<Card, Integer> origin, EnumMap<Card, Integer> destination, Card c) {
		origin.put(c, origin.get(c) - 1);
		destination.put(c, destination.get(c) + 1);
	}

	public void initialize(GameView gv, int index) {
		this.gv = gv;
		this.index = index;
		hands = new ArrayList<Hand>();
		for (int i = 0; i < gv.getNumberOfPlayers(); i++) {
			hands.add(new Hand(Game.ROWS, Game.COLUMNS));
		}
		myHand = hands.get(index);
		fastestHand = myHand;
		for (Card c : Card.values()) {
			faceDown.put(c, gv.getNumDecks() * c.getQuantity());
			discard.put(c, 0);
			inPlay.put(c, 0);
			deck.put(c, 0);
		}
		shuffleHappened = false;

	}

	private float calculateCardEv() {
		int number = 0;
		float total = 0;
		for (Card c : Card.values()) {
			number += faceDown.get(c);
			total += faceDown.get(c) * c.getValue();
		}
		return total / number;
	}

	public Move getOpener() {
		if (myHand.isCardRevealed(0, 0)) {
			return new Move(MoveType.FLIP, 0, 1);
		} else {
			return new Move(MoveType.FLIP, 0, 0);
		}
	}

	public Move getMove() {
		return getMoveInternal(null);
	}

	public Move getMoveWithDraw(Card c) {
		return getMoveInternal(c);
	}

	private Move getMoveInternal(Card c) {
		drawnCard = c;
		generateMoves(c);
		evaluateMoves();
		drawnCard = null;
		return highestScoringMove();
	}

	private void generateMoves(Card c) {
		int row, column;
		possibleMoves.clear();
		if (c == null) {
			possibleMoves.add(new PossibleMove(MoveType.DRAW));
			for (column = 0; column < myHand.getColumns(); column++) {
				if (myHand.getHiddenCardsInColumn(column) > 0) {
					row = myHand.getRowOfFirstHiddenCardInColumn(column);
					possibleMoves.add(new PossibleMove(MoveType.FLIP, row, column));
				}
			}
		} else {
			possibleMoves.add(new PossibleMove(MoveType.DECLINE_DRAWN_CARD));
		}

		MoveType mt = (c == null ? MoveType.REPLACE_WITH_DISCARD : MoveType.REPLACE_WITH_DRAWN_CARD);
		for (column = 0; column < myHand.getColumns(); column++) {
			if (myHand.getHiddenCardsInColumn(column) > 0) {
				row = myHand.getRowOfFirstHiddenCardInColumn(column);
				possibleMoves.add(new PossibleMove(mt, row, column));
			}
		}

		for (column = 0; column < myHand.getColumns(); column++) {
			if (myHand.isColumnCollapsed(column)) {
				continue;
			}
			for (row = 0; row < myHand.getRows(); row++) {
				if (myHand.isCardRevealed(row, column)) {
					possibleMoves.add(new PossibleMove(mt, row, column));
				}
			}
		}

	}

	private void evaluateMoves() {
		evaluateMovesPreCalculations();
		for (PossibleMove pm : possibleMoves) {
			pm.score = evaluateMove(pm.move);
		}
	}

	float evaluateMove(Move move) {
		switch (move.getMoveType()) {
		case DECLINE_DRAWN_CARD:
			return 0;
		case DRAW:
			return evaluateDraw();
		case FLIP:
			return evaluateFlip(move.getColumn());
		case REPLACE_WITH_DISCARD:
			return evaluateReplace(getDiscard(), move.getRow(), move.getColumn());
		case REPLACE_WITH_DRAWN_CARD:
			return evaluateReplace(drawnCard, move.getRow(), move.getColumn());
		default:
			throw new IllegalStateException();

		}
	}

	protected float evaluateReplace(Card newCard, int row, int column) {
		if (!myHand.isCardRevealed(row, column)) {
			return evaluateReplaceHidden(newCard, column);
		} else {
			return evaluateReplaceCard(newCard, myHand.getCard(row, column), row, column);
		}
	}

	abstract float evaluateReplaceHidden(Card newCard, int column);

	abstract float evaluateReplaceCard(Card newCard, Card oldCard, int row, int column);

	abstract float evaluateDraw();

	abstract float evaluateFlip(int column);

	private Move highestScoringMove() {
		float max = -100000000000f;
		Move best = null;
		for (PossibleMove pm : possibleMoves) {
			if (pm.score > max) {
				max = pm.score;
				best = pm.move;
			}
		}
		if(max < 0) {
			for (PossibleMove pm : possibleMoves) {
				System.out.println(pm.score + " " + pm.move);
			}
			DEBUG_PRINT();
			System.out.println(shuffleHappened);
			throw new IllegalStateException();
		}
		return best;
	}

	@Override
	public void showPeekedCard(int row, int column, Card c) {
		// We don't peek
	}

	protected Card getDiscard() {
		return gv.getDiscardUpCard();
	}

	protected void evaluateMovesPreCalculations() {
		cardEv = calculateCardEv();
		float bestOtherScoreSimulated = 100000;
		for (int player = 0; player < gv.getNumberOfPlayers(); player++) {
			Hand h = hands.get(player);

			if (h.getNumberOfHiddenCards() < fastestHand.getNumberOfHiddenCards()) {
				fastestHand = h;
			}

			if (player == index) {
				continue;
			}

			float simulatedScore = evaluateHand(h);
			if (simulatedScore < bestOtherScoreSimulated) {
				bestHandBesidesMe = h;
				bestOtherScoreSimulated = simulatedScore;
			}
		}
	}

	protected float evaluateHand(Hand h) {
		float simulatedScore = h.getTotal();
		if (h.getNumberOfHiddenCards() > 0) {
			simulatedScore += getEvFaceDown() * h.getNumberOfHiddenCards();
		}
		return simulatedScore;
	}
	
	protected float getEvFaceDown() {
		float sum = 0, count = 0;
		for(Card c : Card.values()) {
			count += faceDown.get(c);
			sum += faceDown.get(c) * c.getValue();
		}
		if(count == 0) {
			return 0f;
		}
		return 1f * sum / count;
	}
	
	protected float getEvDeck() {
		float sum = 0, count = 0;
		for(Card c : Card.values()) {
			count += deck.get(c);
			sum += deck.get(c) * c.getValue();
		}
		if(count == 0) {
			return 0f;
		}
		return 1f * sum / count;
	}

	protected int getCardCountFaceDown(Card c) {
		return faceDown.get(c);
	}

	protected int getCardCountDraw(Card c) {
		if(gv.getNumberOfCardsLeftIndeck() == 0) {
			return discard.get(c);
		}else if (shuffleHappened) {
			return deck.get(c);
		} else {
			return faceDown.get(c);
		}
	}

	protected int getTotalCardCountFaceDown() {
		int total = 0;
		for(Card c : Card.values()) {
			total += faceDown.get(c);
		}
		return total;
	}
	protected int getTotalCardCountDraw() {
		int total = 0;
		for(Card c : Card.values()) {
			total += faceDown.get(c);
		}
		return total;
	}
	
	private void DEBUG_PRINT() {
		print(deck);
		print(discard);
		print(inPlay);
		print(faceDown);
	}

	private void print(EnumMap<Card, Integer> map) {
		for(Card c : Card.values()) {
			System.out.print(map.get(c) + " ");
		}
		System.out.print("\n");
	}

}

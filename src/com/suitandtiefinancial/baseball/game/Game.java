package com.suitandtiefinancial.baseball.game;

import com.suitandtiefinancial.baseball.game.Rules.StartStyle;
import com.suitandtiefinancial.baseball.player.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 
 * @author Boxxy
 *
 */
public class Game {
	public static final int ROWS = 3;
	public static final int COLUMNS = 3;

	private final int numberOfPlayers;
	private final Rules rules;
	private final Shoe shoe;
	private final ArrayList<Hand> hands;
	private final ArrayList<HandView> handViews;
	private final ArrayList<Player> players;
	private final GameView view;
	// TODO(stfinancial): Maybe recreate an eventQueue in GameRecord?
	private final Queue<Event> eventQueue;
	public boolean DEBUG_PRINT = false;

	private int currentPlayerIndex = 0;
	private int round = 0;
	// TODO(stfinancial): Deprecate this and use startOfTurnEvent
	Card lastDrawnCard = null;
	// TODO(stfinancial): clarify what this actually is.
	private Event startOfTurnEvent = null;
	private int playerWentOut = -1;

	private boolean gameOver = false;

	public Game(int numberOfPlayers, int numberOfDecks, Rules rules) {
		this.numberOfPlayers = numberOfPlayers;
		eventQueue = new LinkedList<>();
		shoe = new Shoe(numberOfDecks);
		this.rules = rules;
		hands = new ArrayList<Hand>(numberOfPlayers);
		handViews = new ArrayList<HandView>(numberOfPlayers);
		createHandsAndHandViews();
		players = new ArrayList<Player>(numberOfPlayers);
		view = new GameView(this, rules, numberOfDecks, numberOfPlayers, players, handViews, shoe);

		dealHands();
		shoe.pushDiscard(shoe.draw());
		eventQueue.add(new Event(EventType.INITIAL_DISCARD, shoe.peekDiscard()));
	}

	private void createHandsAndHandViews() {
		for (int player = 0; player < numberOfPlayers; ++player) {
			Hand h = new Hand();
			hands.add(h);
			handViews.add(new HandView(h));
		}
	}

	private void dealHands() {
		Hand h;
	 	for (int playerIndex = 0; playerIndex < numberOfPlayers; playerIndex++) {
			h = hands.get(playerIndex);
			for (int row = 0; row < ROWS; row++) {
				for (int column = 0; column < COLUMNS; column++) {
					h.dealCard(shoe.draw(), row, column);
				}
			}
		}
	}

	public void addPlayer(Player p) {
		players.add(p);
		if (players.size() == numberOfPlayers) {
			processEventQueue();
		}
	}

	public void tick() {
		if (round < 2) {
			tickOpener();
		} else {
			tickGame();
		}
		if(round > 2000) {
			throw new IllegalStateException("Game has exceeded 2000 rounds");
		}
	}

	private void tickOpener() {
		if (players.size() != numberOfPlayers) {
			throw new IllegalStateException("Started game before all expected players were added");
		}
		Move m = players.get(currentPlayerIndex).getOpener();
		Hand h = hands.get(currentPlayerIndex);
		if (m.getMoveType() == MoveType.FLIP) {
			Event flipEvent = new Event(EventType.FLIP, currentPlayerIndex, h.peekCard(m.getRow(), m.getColumn()), m.getRow(), m.getColumn());
			eventQueue.add(flipEvent);
			h.processEvent(flipEvent);
		} else if (m.getMoveType() == MoveType.PEEK && rules.getStartStyle() != StartStyle.FLIP) {
			Event peekEvent = new Event(EventType.PEEK, currentPlayerIndex, m.getRow(), m.getColumn());
			h.processEvent(peekEvent);
			eventQueue.add(peekEvent);
			Card peekedCard = h.peekCard(m.getRow(), m.getColumn());
			players.get(currentPlayerIndex).showPeekedCard(m.getRow(), m.getColumn(), peekedCard);
		} else {
			throw new IllegalStateException("Illegal opening move of " + m + " by player " + currentPlayerIndex);
		}
		finishPlayerTurn();
	}

	private void tickGame() {
		if (lastDrawnCard == null) {
			tickStartOfTurn();
		} else {
			tickAfterDraw();
		}
	}

	private void tickStartOfTurn() {
		Move m = players.get(currentPlayerIndex).getMove();
		List<Event> events;
		Hand h;
		switch (m.getMoveType()) {
		case DRAW:
			if (shoe.isDeckEmpty()) {
				shoe.reset();
				// TODO(stfinancial): Add a triggering event to shuffle?
				eventQueue.add(new Event(EventType.SHUFFLE));
			}
			lastDrawnCard = shoe.draw();
			startOfTurnEvent = new Event(EventType.DRAW, currentPlayerIndex);
			eventQueue.add(startOfTurnEvent);
			break;
		case FLIP:
			h = hands.get(currentPlayerIndex);
			startOfTurnEvent = new Event(EventType.FLIP, currentPlayerIndex, h.peekCard(m.getRow(), m.getColumn()), m.getRow(), m.getColumn());
			eventQueue.add(startOfTurnEvent);
			events = h.processEvent(startOfTurnEvent);
			events.forEach(e -> { if (e.getType() == EventType.DISCARD) { shoe.pushDiscard(e.getCard()); }});
			eventQueue.addAll(events);
			break;
		case REPLACE_WITH_DISCARD:
			h = hands.get(currentPlayerIndex);
			Card fromDiscard = shoe.popDiscard();
			startOfTurnEvent = new Event(EventType.DRAW_DISCARD, currentPlayerIndex, fromDiscard);
			eventQueue.add(startOfTurnEvent);

			Event setEvent = new Event(EventType.SET, currentPlayerIndex, fromDiscard, m.getRow(), m.getColumn(), h.getSpotState(m.getRow(), m.getColumn())).withTriggeringEvent(startOfTurnEvent);
			eventQueue.add(setEvent);
			events = h.processEvent(setEvent);
			events.forEach(e -> { if (e.getType() == EventType.DISCARD) { shoe.pushDiscard(e.getCard()); }});
			eventQueue.addAll(events);
			break;
		default:
			throw new IllegalStateException("Illegal move " + m + " by player " + currentPlayerIndex);
		}
		finishPlayerTurn();
	}

	private void tickAfterDraw() {
		Move m = players.get(currentPlayerIndex).getMoveWithDraw(lastDrawnCard);
		switch (m.getMoveType()) {
		case DECLINE_DRAWN_CARD:
			shoe.pushDiscard(lastDrawnCard);
			eventQueue.add(new Event(EventType.DISCARD, currentPlayerIndex, lastDrawnCard).withTriggeringEvent(startOfTurnEvent));
			break;
		case REPLACE_WITH_DRAWN_CARD:
			Event setEvent = new Event(EventType.SET, currentPlayerIndex, lastDrawnCard, m.getRow(), m.getColumn(), hands.get(currentPlayerIndex).getSpotState(m.getRow(), m.getColumn())).withTriggeringEvent(startOfTurnEvent);
			eventQueue.add(setEvent);
			List<Event> events = hands.get(currentPlayerIndex).processEvent(setEvent);
			events.forEach(e -> { if (e.getType() == EventType.DISCARD) { shoe.pushDiscard(e.getCard()); }});
			eventQueue.addAll(events);
			break;
		default:
			throw new IllegalStateException("Illegal move " + m + " by player " + currentPlayerIndex);
		}
		lastDrawnCard = null;
		finishPlayerTurn();
	}

	private void finishPlayerTurn() {
		if (lastDrawnCard != null) {
			return; // Player will need to make another move after they see their drawn card
		}
		if (playerWentOut < 0) {
			if (HandUtils.isOut(hands.get(currentPlayerIndex))) {
				playerWentOut = currentPlayerIndex;
			}
		} else {
//			hands.get(currentPlayerIndex).revealAll(currentPlayerIndex).forEach((e) -> {
//				eventQueue.add(e);
//				if (e.getType() == EventType.DISCARD) shoe.pushDiscard(e.getCard());
//			});
			List<Event> events = hands.get(currentPlayerIndex).revealAll(currentPlayerIndex);
			eventQueue.addAll(events);
			events.forEach(e -> { if (e.getType() == EventType.DISCARD) { shoe.pushDiscard(e.getCard()); }});
		}

		currentPlayerIndex++;
		if (currentPlayerIndex == numberOfPlayers) {
			currentPlayerIndex = 0;
			round++;
		}

		if (currentPlayerIndex == playerWentOut) {
			gameOver = true;
			eventQueue.add(new Event(EventType.GAME_OVER));
		}

		processEventQueue();
	}

	private void processEventQueue() {
		Event e;
		while (!eventQueue.isEmpty()) {
			e = eventQueue.remove();
			if(this.DEBUG_PRINT) {
				System.out.println(e);
			}
			for (Player p: players) {
				p.processEvent(e);
			}
		}
	}

	public void printGameOver() {
		int minimum = 500000;
		int winner = 0;
		for (int playerIndex = 0; playerIndex < numberOfPlayers; playerIndex++) {
			int localTotal = HandUtils.getRevealedTotal(hands.get(playerIndex));
			System.out.println("Player " + playerIndex + " " + localTotal);
			if (localTotal < minimum) {
				winner = playerIndex;
				minimum = localTotal;
			}
		}
		System.out.println("Player " + winner + " wins with " + minimum);
	}

	@Override
	public String toString() {
		String s = "\n------------------------------------------------------------------------------------\n";

		for (int playerIndex = 0; playerIndex < numberOfPlayers; playerIndex++) {
			s += "P" + playerIndex + "   ";
		}
		s += "\t Discard: " + shoe.peekDiscard();
		s += "\n";
		for (int row = 0; row < Game.ROWS; row++) {
			for (int playerIndex = 0; playerIndex < numberOfPlayers; playerIndex++) {
				s += HandUtils.displayRow(hands.get(playerIndex), row) + "  ";
			}
			s += "\n";
		}
		s += "------------------------------------------------------------------------------------\n";
		return s;
	}

	public boolean isLastRound() {
		return playerWentOut != -1;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public int getWinner() {
		if (!gameOver) {
			throw new IllegalStateException();
		}
		int minimum = 50000, winner = -1;
		for (int playerIndex = 0; playerIndex < numberOfPlayers; playerIndex++) {
			int localTotal = HandUtils.getRevealedTotal(hands.get(playerIndex));
			System.out.println("Player " + playerIndex + " " + localTotal);
			if (localTotal < minimum) {
				winner = playerIndex;
				minimum = localTotal;
			}
		}
		return winner;
	}

	public int getRound() {
		return round;
	}

	public int getPlayerWhoWentOut() {
		return playerWentOut;
	}

	public GameView getGameView() { return view; }

	public GameRecord generateGameRecord(int focusPlayerIndex) {
		return new GameRecord(focusPlayerIndex, this);
	}

}

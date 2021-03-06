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

	/**
	 *
	 * @param event An event for the hand to process. Should have the {@link EventType type} SET, FLIP, or PEEK.
	 * @return A list of events generated by the provided event. Does not include the provided event in the return list.
	 */
	List<Event> processEvent(Event event) {
		List<Event> generatedEvents;
		switch (event.getType()) {
			case SET:
				generatedEvents = replace(event);
				break;
			case FLIP:
				generatedEvents = flip(event);
				break;
			case PEEK:
				spots[event.getRow()][event.getColumn()].state = SpotState.FACE_DOWN_PEEKED;
				generatedEvents = Collections.emptyList();
				break;
			default:
				throw new IllegalStateException("Hand can only process FLIP and SET events: " + event.getType());
		}
		return generatedEvents;
	}

	private List<Event> replace(Event setEvent) {
		Spot s = spots[setEvent.getRow()][setEvent.getColumn()];
		if (s.state == SpotState.COLLAPSED) {
			throw new IllegalStateException("Tried to replace an already collapsed card: " + setEvent.getType() + ", " + setEvent.getRow() + ", " + setEvent.getColumn() + ". " + setEvent.getTriggeringEvent().getType());
		}

		Card replacedCard = s.card;
		s.card = setEvent.getCard();
		s.state = SpotState.FACE_UP;

		List<Event> generatedEvents = new ArrayList<>(5);
		if (checkCollapse(setEvent.getColumn())) {
			Event collapseEvent = new Event(EventType.COLLAPSE, setEvent.getPlayerIndex(), setEvent.getColumn()).withTriggeringEvent(setEvent);
			generatedEvents.add(collapseEvent);
			generatedEvents.addAll(collapse(collapseEvent));
		}
		generatedEvents.add(new Event(EventType.DISCARD, setEvent.getPlayerIndex(), replacedCard).withTriggeringEvent(setEvent));

		return generatedEvents;
	}

	/** Flips a face-down card face-up 
	 *  Returns List of discard and collapse events.
	 */
	private List<Event> flip(Event flipEvent) {
		Spot s = spots[flipEvent.getRow()][flipEvent.getColumn()];
		if (s.state == SpotState.COLLAPSED) {
			throw new IllegalStateException("Tried to flip an already collapsed card");
		} else if (s.state == SpotState.FACE_UP) {
			throw new IllegalStateException("Tried to flip an already revealed card");
		}
		s.state = SpotState.FACE_UP;
		List<Event> generatedEvents;
		if (checkCollapse(flipEvent.getColumn())) {
			generatedEvents = new ArrayList<>(4);
			Event collapseEvent = new Event(EventType.COLLAPSE, flipEvent.getPlayerIndex(), flipEvent.getColumn()).withTriggeringEvent(flipEvent);
			generatedEvents.add(collapseEvent);
			generatedEvents.addAll(collapse(collapseEvent));
		} else {
			generatedEvents = Collections.emptyList();
		}
		return generatedEvents;
	}

	private boolean checkCollapse(int column) {
		return spots[0][column].state == SpotState.FACE_UP && spots[1][column].state == SpotState.FACE_UP && spots[2][column].state == SpotState.FACE_UP
				&& spots[0][column].card.getValue() == spots[1][column].card.getValue()
				&& spots[1][column].card.getValue() == spots[2][column].card.getValue();

	}

	/**
	 * Collapses the given column, removing the card values from the hand.
	 * checkCollapse should be called before using this function
	 * 
	 * @param collapseEvent
	 * @return List of generated discard events
	 * @author Boxxy
	 */
	private List<Event> collapse(Event collapseEvent) {
		ArrayList<Event> returnList = new ArrayList<>(Game.ROWS);
		Spot s;
		for (int row = 0; row < Game.ROWS; row++) {
			s = spots[row][collapseEvent.getColumn()];
			returnList.add(new Event(EventType.DISCARD, collapseEvent.getPlayerIndex(), s.card).withTriggeringEvent(collapseEvent));
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

	// TODO(stfinancial): Rename this function, as "peek" is overloaded as is.
	Card peekCard(int row, int column) {
		return spots[row][column].card;
	}

	/**
	 * Called at the end of a {@link com.suitandtiefinancial.baseball.player.Player Player's} final turn. Flips all face-down
	 * cards face-up, collapsing any made columns and discarding.
	 * @param currentPlayerIndex
	 * @return A list of events generated, including FLIP, and their respective COLLAPSE and DISCARD.
	 */
	List<Event> revealAll(int currentPlayerIndex) {
		List<Event> generatedEvents = new ArrayList<>();

		Event flipEvent;
		for (int row = 0; row < Game.ROWS; row++) {
			for (int column = 0; column < Game.COLUMNS; column++) {
				if (spots[row][column].state == SpotState.FACE_DOWN || spots[row][column].state == SpotState.FACE_DOWN_PEEKED) {
					flipEvent = new Event(EventType.FLIP, currentPlayerIndex, spots[row][column].card, row, column);
					generatedEvents.add(flipEvent);
					generatedEvents.addAll(flip(flipEvent));
				}
			}
		}
		return generatedEvents;
	}
}

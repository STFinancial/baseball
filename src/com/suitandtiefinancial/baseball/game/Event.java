package com.suitandtiefinancial.baseball.game;

/**
 * Represents an event in the {@link Game}. These events are public facing in the sense that they represent what an
 * observer would see when watching the game. Events are created by the {@link Game} such that, given a list of events,
 * one should be able to completely construct a public facing game state.
 */
public class Event {
    // TODO(stfinancial): This class is kind of a clusterfuck of constructors, maybe make subclasses?
    private final EventType type;
    private final Card card;
    private final int playerIndex;
    private final int row;
    private final int column;

    Event(EventType type) {
        this(type, -1, null, -1, -1);
    }

    Event(EventType type, Card card) {
        this(type, -1, card, -1, -1);
    }

    Event(EventType type, int playerIndex) {
        this(type, playerIndex, null, -1, -1);
    }

    Event(EventType type, int playerIndex, Card card) {
        this(type, playerIndex, card, -1, -1);
    }

    Event(EventType type, int playerIndex, int column) {
        this(type, playerIndex, null, -1, column);
    }

    Event(EventType type, int playerIndex, int row, int column) {
        this(type, playerIndex, null, row, column);
    }

    Event(EventType type, int playerIndex, Card card, int row, int column) {
        this.type = type;
        this.playerIndex = playerIndex;
        this.card = card;
        this.row = row;
        this.column = column;
    }

    public EventType getType() {
        return type;
    }

    public Card getCard() {
        if (card == null) {
            throw new IllegalStateException("This event does not contain card data: " + type);
        }
        return card;
    }

    public int getPlayerIndex() {
        if (playerIndex < 0) {
            throw new IllegalStateException("This event does not correspond to a particular player: " + type);
        }
        return playerIndex;
    }

    public int getRow() {
        if (row < 0) {
            throw new IllegalStateException("This event does not correspond to a row: " + type);
        }
        return row;
    }

    public int getColumn() {
        if (column < 0) {
            throw new IllegalStateException("This event does not correspond to a column: " + type);
        }
        return column;
    }
    
    public String toString() {
    	String s = "";
    	if(playerIndex >= 0) {
    		s += "Player " + playerIndex + " ";
    	}
    	s += type + " ";
    	if(card != null) {
    		s += "a " + card + " ";
    	}
    	if(row >= 0 && column >= 0) {
    		s += "at " + row + "," + column + " ";
    	}else if (column >= 0) {
    		s += "at column " + column + " ";
    	}
    	
    	return s;
    }
}

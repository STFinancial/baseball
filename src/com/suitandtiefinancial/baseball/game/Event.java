package com.suitandtiefinancial.baseball.game;

import java.util.List;

/**
 * Created by Timothy on 7/22/18.
 */
public class Event {
    // TODO(stfinancial): This class is kind of a clusterfuck of constructors, maybe make subclasses?
    private EventType type;

    Event(EventType type) {
        this.type = type;
    }

    Event(EventType type, Card card) {
        this.type = type;
    }

    Event(EventType type, int playerIndex) {
        this.type = type;
    }

    Event(EventType type, int playerIndex, Card card) {
        this.type = type;
    }

    Event(EventType type, int playerIndex, int column) {
        this.type = type;
    }

    Event(EventType type, int playerIndex, int row, int column) {
        this.type = type;
    }

    Event(EventType type, int playerIndex, Card card, int row, int column) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }
}

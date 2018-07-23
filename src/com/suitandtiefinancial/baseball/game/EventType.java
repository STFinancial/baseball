package com.suitandtiefinancial.baseball.game;

/**
 * Created by Timothy on 7/22/18.
 */
public enum EventType {
    /** The discard is combined with the shoe and shuffled. */
    SHUFFLE,
    /** The cards are dealt to each player face-down. */
    INITIAL_DEAL,
    /** The top card of the deck is placed face-up in the discard at the start of the game. */
    INITIAL_DISCARD,
    /** A player has peeked at one of their face-down cards. */
    PEEK,
    /** A player has drawn a card from the top of the deck. */
    DRAW,
    /** A player has flipped one of their face-down cards face-up. */
    FLIP,
    /** A player has drawn the top card of the discard pile. */
    DRAW_DISCARD,
    /** A player has replaced a card in their hand with a new card, placed face-up. */
    SET,
    /** A player has discarded a card. */
    DISCARD,
    /** A column has been collapsed. Note that this event type does not include the discarded cards. */
    COLLAPSE,
    /** The game has ended. */
    GAME_OVER;
}


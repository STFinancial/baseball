package com.suitandtiefinancial.baseball.game;

/**
 * Created by Timothy on 7/22/18.
 */
public enum EventType {
    SHUFFLE,
    INITIAL_DEAL,
    INITIAL_DISCARD,
    PEEK,
    DRAW,
    FLIP,
    DRAW_DISCARD,
    SET,
    DISCARD,
    COLLAPSE, // Does not include discards.
    GAME_OVER;
}


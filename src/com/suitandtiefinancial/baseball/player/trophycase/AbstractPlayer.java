package com.suitandtiefinancial.baseball.player.trophycase;

import com.suitandtiefinancial.baseball.game.Game;
import com.suitandtiefinancial.baseball.player.Player;

/**
 * Created by Timothy on 7/15/18.
 */
public abstract class AbstractPlayer implements Player {
    protected Hand hand;

    AbstractPlayer() {
        hand = new Hand(Game.ROWS, Game.COLUMNS);
    }

}

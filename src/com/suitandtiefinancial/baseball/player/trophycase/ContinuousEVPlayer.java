package com.suitandtiefinancial.baseball.player.trophycase;

import com.suitandtiefinancial.baseball.game.*;
import com.suitandtiefinancial.baseball.player.Player;

/**
 * Created by Timothy on 7/14/18.
 */
public class EVCollapsePlayer implements Player {
    GameView g;
    Hand hand;
    boolean firstOpener;

    EVCollapsePlayer() {
        hand = new Hand(Game.ROWS, Game.COLUMNS);
    }

    @Override
    public void initialize(GameView g, int index) {
        this.g = g;
        hand.clear();
        firstOpener = true;
    }

    @Override
    public Move getOpener() {
        if (firstOpener) {
            firstOpener = false;
            return new Move(MoveType.FLIP, 0, 0);
        } else {
            return new Move(MoveType.FLIP, 0, 1);
        }
    }

    @Override
    public Move getMove() {
        if (g.getDiscardUpCard().getValue() < )
    }

    @Override
    public Move getMoveWithDraw(Card c) {
        return null;
    }

    @Override
    public void showPeekedCard(int row, int column, Card c) {
        /* We never use (for now) this but will still implement for now */
        hand.setPeekedCard(c, row, column);
    }
}

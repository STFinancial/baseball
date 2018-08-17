package com.suitandtiefinancial.baseball.player.trophycase;

import com.suitandtiefinancial.baseball.game.Move;

/**
 * A pair of {@link Move} and its score (currently delta-EV). Can be sorted upon.
 */
class ScoredMove implements Comparable<ScoredMove> {
    private final Move move;
    private final double deltaEV;
    private final double deltaAffinity;

    ScoredMove(Move move, double deltaEV, double deltaAffinity) {
        this.deltaEV = deltaEV;
        this.move = move;
        this.deltaAffinity = deltaAffinity;
    }

    Move getMove() { return move; }
    double getDeltaEV() { return deltaEV; }
    double getDeltaAffinity() { return deltaAffinity; }

    @Override
    public int compareTo(ScoredMove o) {
        // TODO(stfinancial): More heavily incorporate delta affinity.
        if (deltaEV > o.deltaEV) {
            return 1;
        } else if (deltaEV == o.deltaEV) {
            if (deltaAffinity > o.deltaAffinity) {
                return -1;
            } else if (deltaAffinity < o.deltaAffinity) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return move.toString() + " with deltaEV: " + deltaEV + "  and deltaAffinity: " + deltaAffinity;
    }
}

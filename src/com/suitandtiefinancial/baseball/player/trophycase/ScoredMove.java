package com.suitandtiefinancial.baseball.player.trophycase;

import com.suitandtiefinancial.baseball.game.Move;

/**
 * A pair of {@link Move} and its score (currently delta-EV). Can be sorted upon.
 */
class ScoredMove implements Comparable<ScoredMove> {
    private final Move move;
    private final double score;

    ScoredMove(Move move, double score) {
        this.score = score;
        this.move = move;
    }

    Move getMove() { return move; }
    double getScore() { return score; }

    @Override
    public int compareTo(ScoredMove o) {
        return score - o.score > 0 ? 1 : -1;
    }

    @Override
    public String toString() {
        return move.toString() + " with score: " + score;
    }
}

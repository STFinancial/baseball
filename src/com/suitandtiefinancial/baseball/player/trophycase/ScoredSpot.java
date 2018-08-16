package com.suitandtiefinancial.baseball.player.trophycase;

/**
 * A pair class of {@link com.suitandtiefinancial.baseball.player.trophycase.Hand.Spot Spot} and double.
 */
class ScoredSpot {
    private final Hand.Spot spot;
    private final double score;

    ScoredSpot(Hand.Spot spot, double score) {
        this.spot = spot;
        this.score = score;
    }

    Hand.Spot getSpot() { return spot; }
    double getScore() { return score; }
}

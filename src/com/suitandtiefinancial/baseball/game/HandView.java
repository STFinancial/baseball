package com.suitandtiefinancial.baseball.game;

// TODO(stfinancial): It seems like there are way too many representations of a Hand, maybe unify them all somehow.
// TODO(stfinancial): Make this a nested class in Hand and just have Hand generate this to allow access to private members?

/**
 * Opposing player facing representation of a {@link Hand}.
 */
public class HandView {
    private Hand h;

    HandView(Hand h) {
        this.h = h;
    }

    public SpotState getSpotState(int row, int column) {
        return h.getSpotState(row, column);
    }

    public Card viewCard(int row, int column) {
        if (h.getSpotState(row, column) != SpotState.FACE_UP) {
            throw new IllegalStateException("Tried to view hidden or collapsed card, use peek if you have access or check for revealed first.");
        }
        return h.peekCard(row, column);
    }

    // TODO(stfinancial): Compute this on the fly instead of relying on hand?
    public int getRevealedTotal() {
        return HandUtils.getRevealedTotal(h);
    }

    // TODO(stfinancial): Return structured hand object.

}

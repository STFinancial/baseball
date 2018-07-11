package com.suitandtiefinancial.baseball.game;
/**
 * @TODO implement in game HIDDEN/PUBLIC peeking, Allowing to flip after declined draw, extra turn after going out
 * @author Boxxy
 *
 */
public class Rules {
	private final boolean playerGoingOutGetsExtraTurn;

	public enum StartStyle {
		FLIP, FLIP_OR_PEEK_HIDDEN, FLIP_OR_PEEK_PUBLIC;
	}

	private final StartStyle startStyle;
	private final boolean allowedToFlipAfterDeclinedDraw;

	public Rules(boolean playerGoingOutGetsExtraTurn, StartStyle startStyle, boolean allowedToFlipAfterDeclinedDraw) {
		this.playerGoingOutGetsExtraTurn = playerGoingOutGetsExtraTurn;
		this.startStyle = startStyle;
		this.allowedToFlipAfterDeclinedDraw = allowedToFlipAfterDeclinedDraw;
	}

	public static Rules HAWKEN_STANDARD() {
		return new Rules(false, StartStyle.FLIP_OR_PEEK_HIDDEN,false);
	}

	public boolean playerGoingOutGetsExtraTurn() {
		return playerGoingOutGetsExtraTurn;
	}

	public StartStyle getStartStyle() {
		return startStyle;
	}

	public boolean allowedToFlipAfterDeclinedDraw() {
		return allowedToFlipAfterDeclinedDraw;
	}
}

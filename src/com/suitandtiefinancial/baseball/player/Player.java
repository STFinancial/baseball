package com.suitandtiefinancial.baseball.player;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.GameView;
import com.suitandtiefinancial.baseball.game.Move;

public interface Player {
	/** Called at the beginning of each game. */
	public void initialize(GameView g, int index);
	/** Called twice before the turn phase begins. Can peek at 2 cards and choose to flip any number of them. */
	public Move getOpener();
	/** Called at the start of each turn during standard play. Can choose to draw, take discard, or flip. */
	public Move getMove();
	/** If opting to draw, this method is called to determine what to do with the drawn card. */
	public Move getMoveWithDraw(Card c);
	/** Called to show the player information about their peeked card. */
	public void showPeekedCard(int row, int column, Card c);
}

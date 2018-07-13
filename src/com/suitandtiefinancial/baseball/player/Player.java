package com.suitandtiefinancial.baseball.player;

import com.suitandtiefinancial.baseball.game.Card;
import com.suitandtiefinancial.baseball.game.Game;
import com.suitandtiefinancial.baseball.game.GameView;
import com.suitandtiefinancial.baseball.game.Move;

public interface Player {
	
		public void initialize(GameView g, int index);
		public Move getOpener();
		public Move getMove();
		public Move getMoveWithDraw(Card c);
		public void showPeekedCard(int row, int column, Card c);
}

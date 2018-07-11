package com.suitandtiefinancial.baseball;

import java.util.Random;

import com.suitandtiefinancial.baseball.game.Game;
import com.suitandtiefinancial.baseball.game.Rules;
import com.suitandtiefinancial.baseball.player.RandomPlayer;

public class TestBench {
	public static void main(String[] args) {
		int players = 6;
		Game g = new Game(players, 2, Rules.HAWKEN_STANDARD());
		g.DEBUG_PRINT = true;
		Random r = new Random();
		
		for(int playerIndex = 0; playerIndex < players; playerIndex++) {
			RandomPlayer p = new RandomPlayer(r);
			p.initalize(g, playerIndex);
			g.addPlayer(p);
		}
		
		System.out.println(g.toString());
		
		while(!g.isGameOver()) {
			g.tick();
			System.out.println(g.toString());
		}
		
		g.printGameOver();
		
	}
}

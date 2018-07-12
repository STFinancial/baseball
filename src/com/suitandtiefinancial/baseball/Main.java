package com.suitandtiefinancial.baseball;

import java.util.Random;

import com.suitandtiefinancial.baseball.game.Game;
import com.suitandtiefinancial.baseball.game.Rules;
import com.suitandtiefinancial.baseball.player.Player;
import com.suitandtiefinancial.baseball.player.RandomPlayer;
import com.suitandtiefinancial.baseball.player.RushPlayer;
import com.suitandtiefinancial.baseball.player.StraightFlipperPlayer;
import com.suitandtiefinancial.baseball.testbench.TestBench;

public class Main {
	public static void main(String[] args) {
		
		//doOneDetailed();
		doMultipleSummaryOnly(10000);

	}

	private static void doMultipleSummaryOnly(int numberOfGames) {
		TestBench tb = new TestBench(numberOfGames, false, getFocusPlayer(), getFillPlayers());
		tb.simulate();
		tb.printSummary();
		
	}

	private static Player getFocusPlayer() {
		return new RushPlayer();
	}

	private static Player[] getFillPlayers() {
		Player[] players = {new StraightFlipperPlayer(), new StraightFlipperPlayer(), new StraightFlipperPlayer(), new StraightFlipperPlayer(), new StraightFlipperPlayer()};
		return players;
	}
	
	private static void doOneDetailed() {
		TestBench tb = new TestBench(1, true, getFocusPlayer(), getFillPlayers());
		tb.simulate();
		System.out.println();
		tb.printHistory();
		System.out.println();
		tb.printSummary();
	}

}

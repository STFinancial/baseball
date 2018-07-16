package com.suitandtiefinancial.baseball;

import com.suitandtiefinancial.baseball.player.EVPlayer;
import com.suitandtiefinancial.baseball.player.Player;
import com.suitandtiefinancial.baseball.player.RushPlayer;
import com.suitandtiefinancial.baseball.player.boxxy.CollapsePlayer;
import com.suitandtiefinancial.baseball.testbench.TestBench;

public class Main {
	public static void main(String[] args) {
		
		doOneDetailed();
		doMultipleSummaryOnly(100000);

	}

	private static void doMultipleSummaryOnly(int numberOfGames) {
		TestBench tb = new TestBench(numberOfGames, false, getFocusPlayer(), getFillPlayers(5));
		tb.simulate();
		tb.printSummary();
	}

	private static Player getFocusPlayer() {
		return new CollapsePlayer();
	}

	private static Player[] getFillPlayers(int numberOfPlayers) {
		Player[] players = new Player[numberOfPlayers];
		for(int index = 0; index < numberOfPlayers; index++) {
			players[index] = new EVPlayer();
		}
		return players;
	}
	
	private static void doOneDetailed() {
		TestBench tb = new TestBench(1, true, getFocusPlayer(), getFillPlayers(5));
		tb.simulate();
		System.out.println();
		tb.printHistory();
		System.out.println();
		tb.printSummary();
	}

}

package com.suitandtiefinancial.baseball;

import com.suitandtiefinancial.baseball.player.EVPlayer;
import com.suitandtiefinancial.baseball.player.Player;
import com.suitandtiefinancial.baseball.player.boxxy.BoxxyPlayer;
import com.suitandtiefinancial.baseball.player.trophycase.ContinuousEVPlayer;
import com.suitandtiefinancial.baseball.testbench.TestBench;

public class Main {
	public static void main(String[] args) {
//		while (true) {
//			doOneDetailed(getFocusPlayer(), getEvPlayers(5));
//
//		}
		doOneDetailed(getFocusPlayer(), getEvPlayers(5));
//		doOneDetailed(getFocusPlayer(), getEvPlayers(5));
//		doOneDetailed(getFocusPlayer(), getEvPlayers(5));
//		doOneDetailed(getFocusPlayer(), getEvPlayers(5));
//		doOneDetailed(getFocusPlayer(), getEvPlayers(5));
//		doOneDetailed(getFocusPlayer(), getEvPlayers(5));
//		doOneDetailed(getFocusPlayer(), getEvPlayers(5));

		doMultipleSummaryOnly(1000000, getFocusPlayer(), getEvPlayers(5));
//		doOneDetailed(getFocusPlayer(), getBoxxyPlayers(5));
//		doMultipleSummaryOnly(100000, getFocusPlayer(), getEvPlayers(5));
//		doMultipleSummaryOnly(10000, getFocusPlayer(), getBoxxyPlayers(5));

	}

	private static Player getFocusPlayer() {
		return new ContinuousEVPlayer();
//		return new BoxxyPlayer(1.5f, 3f, 4f);
	}
	
	private static void doMultipleSummaryOnly(int numberOfGames, Player player, Player[] players) {
		TestBench tb = new TestBench(numberOfGames, false, player, players);
		tb.simulate();
		tb.printSummary();
	}

	private static Player[] getBoxxyPlayers(int numberOfPlayers) {
		System.out.println("\n\nSimulating against " + numberOfPlayers + " BoxxyPlayers\n");
		Player[] players = new Player[numberOfPlayers];
		for(int index = 0; index < numberOfPlayers; index++) {
			players[index] = new BoxxyPlayer(1 + index * 1, index * 2, index * 2);
		}
		return players;
	}
	
	private static Player[] getEvPlayers(int numberOfPlayers) {
		System.out.println("\n\nSimulating against " + numberOfPlayers + " EvPlayers\n");
		Player[] players = new Player[numberOfPlayers];
		for(int index = 0; index < numberOfPlayers; index++) {
			players[index] = new EVPlayer();
		}
		return players;
	}
	
	private static void doOneDetailed(Player player, Player[] players) {
		TestBench tb = new TestBench(1, true, player, players);
		tb.simulate();
		System.out.println();
		tb.printHistory();
		System.out.println();
		tb.printSummary();
	}

}

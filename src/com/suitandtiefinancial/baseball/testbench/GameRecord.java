package com.suitandtiefinancial.baseball.testbench;

import com.suitandtiefinancial.baseball.game.Game;

public class GameRecord {
	
	
	
	public final int playerIndex;
	public final int playerScore;
	public final int winnerIndex;
	public final int playerWhoWentOutFirst;
	public final int rounds;
	public final int winnerScore;
	public final int secondPlaceScore;
	public final float averageScore;
	
	public GameRecord(int playerIndex, Game g) {
		this.playerIndex = playerIndex;
		this.rounds = g.getRound();

		int tempSecond = 50000;
		int tempFirstIndex = -1;
		int tempFirst = 40000;
		int sum = 0;
		for(int player = 0; player < g.getNumberOfPlayers(); player++) {
			int tempTotal = g.getRevealedTotal(player);
			if(tempTotal < tempFirst) {
				tempFirst = tempTotal;
				tempFirstIndex = player;
				tempSecond = tempFirst;
			}else if (tempTotal < tempSecond) {
				tempSecond = tempTotal;
			}
			sum+=tempTotal;
		}
		averageScore = (1f*sum)/g.getNumberOfPlayers();
		winnerScore = tempFirst;
		secondPlaceScore = tempSecond;
		winnerIndex = tempFirstIndex;
		playerWhoWentOutFirst = g.getPlayerWhoWentOut();
		playerScore = g.getRevealedTotal(playerIndex);
	}
	
	public String toString() {
		String s = "Played at spot " + playerIndex + " with a score of " + playerScore + " in " + rounds + " rounds with an average score of " + averageScore +"\n";
		if(playerIndex == winnerIndex) {
			s += "Won, beating second at " + secondPlaceScore;
		}else {
			s += "Lost, to " + winnerIndex + " with a score of " + winnerScore;
		}
		if (playerWhoWentOutFirst == playerIndex) {
			s += " we went out first";
		}else if(playerWhoWentOutFirst == winnerIndex) {
			s += " the winner went out first";
		}else {
			s += " some other player went out first";
		}
		return s;
	}
}

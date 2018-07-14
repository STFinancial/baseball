package com.suitandtiefinancial.baseball.game;

public class GameRecord {
	public final int playerIndex;
	public final int playerScore;
	public final int winnerIndex;
	public final int playerWhoWentOutFirst;
	public final int rounds;
	public final int winnerScore;
	public final int secondPlaceScore;
	public final float averageScore;
	public final float averageCollapses;
	public final int playerCollapses;

	GameRecord(int playerIndex, Game g) {
		this.playerIndex = playerIndex;
		rounds = g.getRound();

		int tempSecond = 50000;
		int tempFirstIndex = -1;
		int tempFirst = 40000;
		int sum = 0;
		int tempTotalCollapses = 0;
		int tempPlayerCollapses = 0;
		for (int player = 0; player < g.getGameView().getNumberOfPlayers(); player++) {
			int tempTotal = g.getGameView().getRevealedTotal(player);
			if (tempTotal < tempFirst) {
				tempFirst = tempTotal;
				tempFirstIndex = player;
				tempSecond = tempFirst;
			} else if (tempTotal < tempSecond) {
				tempSecond = tempTotal;
			}
			sum += tempTotal;
			for(int column = 0; column < Game.COLUMNS; column++) {
				if(g.getGameView().isColumnCollapsed(player, column)) {
					tempTotalCollapses++;
					if(player == playerIndex) {
						tempPlayerCollapses++;
					}
				}
			}
		}
		averageCollapses = (1f * tempTotalCollapses) / g.getGameView().getNumberOfPlayers();
		playerCollapses = tempPlayerCollapses;
		averageScore = (1f * sum) / g.getGameView().getNumberOfPlayers();
		winnerScore = tempFirst;
		secondPlaceScore = tempSecond;
		winnerIndex = tempFirstIndex;
		playerWhoWentOutFirst = g.getPlayerWhoWentOut();
		playerScore = g.getGameView().getRevealedTotal(playerIndex);
	}

	public String toString() {
		String s = "Played at spot " + playerIndex + " with a score of " + playerScore + " in " + rounds + " rounds with an average score of " + averageScore +"\n";
		if (playerIndex == winnerIndex) {
			s += "Won, beating second at " + secondPlaceScore;
		} else {
			s += "Lost, to " + winnerIndex + " with a score of " + winnerScore;
		}
		if (playerWhoWentOutFirst == playerIndex) {
			s += " we went out first";
		} else if (playerWhoWentOutFirst == winnerIndex) {
			s += " the winner went out first";
		} else {
			s += " some other player went out first";
		}
		return s;
	}
}

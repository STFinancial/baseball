package com.suitandtiefinancial.baseball.game;

import java.util.ArrayList;
import java.util.List;

import com.suitandtiefinancial.baseball.game.Rules.StartStyle;
import com.suitandtiefinancial.baseball.player.Player;

/**
 * 
 * @author Boxxy
 *
 */
public class Game {

	private final int numberOfPlayers;
	private final Rules rules;
	private final Shoe shoe;
	private final ArrayList<Hand> hands;
	private final ArrayList<Player> players;
	public static final int ROWS = 3;
	public static final int COLUMNS = 3;
	public boolean DEBUG_PRINT = false;

	private int currentPlayerIndex = 0;
	private int round = 0;
	Card lastDrawnCard = null;
	private int playerWentOut = -1;

	private boolean gameOver = false;

	public Game(int numberOfPlayers, int numberOfDecks, Rules rules) {
		this.numberOfPlayers = numberOfPlayers;
		shoe = new Shoe(numberOfDecks);
		this.rules = rules;
		hands = new ArrayList<Hand>(numberOfPlayers);
		players = new ArrayList<Player>(numberOfPlayers);
		createHands();
		shoe.pushDiscard(shoe.draw());
	}

	private void createHands() {
		for (int playerIndex = 0; playerIndex < numberOfPlayers; playerIndex++) {
			Hand h = new Hand();
			for (int row = 0; row < ROWS; row++) {
				for (int column = 0; column < COLUMNS; column++) {
					h.dealCard(shoe.draw(), row, column);
				}
			}
			hands.add(h);
		}

	}

	public int addPlayer(Player p) {
		players.add(p);
		return players.size() - 1;
	}

	public Card viewCard(int player, int row, int column) {
		return hands.get(player).viewCard(row, column);
	}

	public boolean isColumnCollapsed(int player, int column) {
		return hands.get(player).peekCard(0, column) == null;
	}

	public void tick() {
		if (round < 2) {
			tickOpener();
		} else {
			tickGame();
		}
	}

	private void tickOpener() {
		if (players.size() != numberOfPlayers) {
			throw new IllegalStateException("Started game before all expected players were added");
		}
		Move m = players.get(currentPlayerIndex).getOpener();
		Hand h = hands.get(currentPlayerIndex);
		if (m.getMoveType() == MoveType.FLIP) {
			h.flip(m.getRow(), m.getColumn());
		} else if (m.getMoveType() == MoveType.PEEK && rules.getStartStyle() != StartStyle.FLIP) {
			Card peekedCard = h.peekCard(m.getRow(), m.getColumn());
			players.get(currentPlayerIndex).showPeekedCard(m.getRow(), m.getColumn(), peekedCard);
		} else {
			throw new IllegalStateException("Illegal opening move of " + m + " by player " + currentPlayerIndex);
		}
		broadcastMove(m);
		finishPlayerTurn();
	}

	private void tickGame() {
		if (lastDrawnCard == null) {
			tickStartOfTurn();
		} else {
			tickAfterDraw();
		}
	}

	private void tickStartOfTurn() {
		Move m = players.get(currentPlayerIndex).getMove();
		List<Card> toDiscard = null;
		switch (m.getMoveType()) {
		case DRAW:
			lastDrawnCard = shoe.draw();
			break;
		case FLIP:
			toDiscard = hands.get(currentPlayerIndex).flip(m.getRow(), m.getColumn());
			break;
		case REPLACE_WITH_DISCARD:
			Card fromDiscard = shoe.popDiscard();
			Hand h = hands.get(currentPlayerIndex);
			toDiscard = h.replace(fromDiscard, m.getRow(), m.getColumn());
			break;
		default:
			throw new IllegalStateException("Illegal move " + m + " by player " + currentPlayerIndex);
		}
		if (toDiscard != null) {
			shoe.pushDiscard(toDiscard);
		}
		broadcastMove(m);
		finishPlayerTurn();
	}

	private void tickAfterDraw() {
		Move m = players.get(currentPlayerIndex).getMoveWithDraw(lastDrawnCard);
		switch (m.getMoveType()) {
		case DECLINE_DRAWN_CARD:
			shoe.pushDiscard(lastDrawnCard);
			break;
		case REPLACE_WITH_DRAWN_CARD:
			Hand h = hands.get(currentPlayerIndex);
			List<Card> toDiscard = h.replace(lastDrawnCard, m.getRow(), m.getColumn());
			shoe.pushDiscard(toDiscard);
			break;
		default:
			throw new IllegalStateException("Illegal move " + m + " by player " + currentPlayerIndex);

		}
		lastDrawnCard = null;
		broadcastMove(m);
		finishPlayerTurn();
	}

	private void finishPlayerTurn() {
		if (lastDrawnCard != null) {
			return; // Player will need to make another move after they see their drawn card
		}
		if (playerWentOut < 0) {
			if (hands.get(currentPlayerIndex).isOut()) {
				playerWentOut = currentPlayerIndex;
			}
		} else {
			List<Card> toDiscard = hands.get(currentPlayerIndex).revealAll();
			if (toDiscard != null) {
				shoe.pushDiscard(toDiscard);
			}
		}

		currentPlayerIndex++;
		if (currentPlayerIndex == numberOfPlayers) {
			currentPlayerIndex = 0;
			round++;
		}

		if (currentPlayerIndex == playerWentOut) {
			gameOver = true;
		}
	}

	public void printGameOver() {
		int minimum = 500000;
		int winner = 0;
		for (int playerIndex = 0; playerIndex < numberOfPlayers; playerIndex++) {
			int localTotal = hands.get(playerIndex).getRevealedTotal();
			System.out.println("Player " + playerIndex + " " + localTotal);
			if (localTotal < minimum) {
				winner = playerIndex;
				minimum = localTotal;
			}
		}
		System.out.println("Player " + winner + " wins with " + minimum);
	}

	private void broadcastMove(Move m) {
		if (!DEBUG_PRINT) {
			return;
		}
		String s = "Player " + currentPlayerIndex + " just " + m;
		System.out.println(s);
	}

	@Override
	public String toString() {
		String s = "\n------------------------------------------------------------------------------------\n";
		s += "Discard: " + shoe.peekDiscard() + "  Round: " + round + "  Current Player: " + currentPlayerIndex;
		if (lastDrawnCard != null) {
			s += " holding " + lastDrawnCard;
		}
		s += "\n\n";

		for (int playerIndex = 0; playerIndex < numberOfPlayers; playerIndex++) {
			s += "P" + playerIndex + "   ";
		}
		s += "\n";
		for (int row = 0; row < Game.ROWS; row++) {
			for (int playerIndex = 0; playerIndex < numberOfPlayers; playerIndex++) {
				s += hands.get(playerIndex).displayRow(row) + "  ";
			}
			s += "\n";
		}
		s += "------------------------------------------------------------------------------------\n";
		return s;
	}

	public boolean isLastRound() {
		return playerWentOut != -1;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public int getWinner() {
		if (!gameOver) {
			throw new IllegalStateException();
		}
		int minimum = 50000, winner = -1;
		for (int playerIndex = 0; playerIndex < numberOfPlayers; playerIndex++) {
			int localTotal = hands.get(playerIndex).getRevealedTotal();
			System.out.println("Player " + playerIndex + " " + localTotal);
			if (localTotal < minimum) {
				winner = playerIndex;
				minimum = localTotal;
			}
		}
		return winner;
	}

	public Card getDiscardUpCard() {
		return shoe.peekDiscard();
	}

	public int getRound() {
		return round;
	}

	public int getPlayerWhoWentOut() {
		return playerWentOut;
	}

	public int getRevealedTotal(int player) {
		return hands.get(player).getRevealedTotal();
	}

	public int getNumberOfPlayers() {
		return numberOfPlayers;
	}

}

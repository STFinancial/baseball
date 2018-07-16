# baseball
Sim for the card game Baseball

## Terminology
* Hand - The 3x3 set of cards each player has. These cards are dealt face-down at the beginning of the game.
* Shoe - Set of card decks used for play. Each deck contains the standard 52 cards plus 2 jokers. The number of decks in the game will generally vary based on number of players.

## The Deal
9 cards are dealt to each player and arranged into 3 rows and 3 columns, placed face-down (not even the owning player sees the cards). This constitutes a player's "hand".

After the cards are dealt, the top card of the deck is flipped face up and placed into the discard pile.

## Initial Round
Then proceed clockwise around the table, with each player peeking at 2 face-down cards, flipping any of the 2, if they so choose.

## Following Rounds
The game then begins, moving clockwise. The player may choose to either:
* Flip one face-down card face-up
* Take the top card from the discard pile, and using it to replace any card in their hand placing the replaced card on top of the discard pile, with the newly placed card being flipped face-up.
* Draw a card from the shoe. The player may then discard the drawn card, or replace any card in their hand, discarding the replaced card and placing the drawn card face up.

Whenever a player forms a column with 3 face-up cards of the same rank (e.g. 3 jacks), the 3 cards are immediately placed into the discard pile. If a player creates 3 of the same rank in a column by replacing a card with a drawn or picked-up card, the 3 cards are placed into the discard first, with the replaced card ending on top of the discard.

## Ending the Game
When a player has no more cards face-down, they have "gone out". All players following this player will receive one more turn, flipping all face-down cards face-up at the end of their turn (if any column has 3 cards of the same rank, they are discarded as usual). The game ends when the player to the right of the player who "went-out" finishes their turn.

The winner is the player with the lowest point value sum of the cards in their hand.

## Scoring
The card point values are as follows:

##### Ace: 1

##### Two: 2

##### Three: 3

##### Four: 4

##### Five: 5

##### Six: 6

##### Seven: 7

##### Eight: 8

##### Nine: 9

##### Ten: 10

##### Jack: 15

##### Queen: 25

##### King: 0

##### Joker: -2

## Misc
If the shoe runs out of cards during the duration of the game, the discard pile is shuffled and becomes the new shoe.

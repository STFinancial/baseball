/**
 * Created by Timothy on 7/6/18.
 */
public enum Card {
    ACE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(15),
    QUEEN(25),
    KING(0),
    JOKER(-2);

    private final int pointValue;

    private Card(int pointValue) {
        this.pointValue = pointValue;
    }

    int getValue() {
        return pointValue;
    }
}

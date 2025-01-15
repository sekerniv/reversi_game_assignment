package reversi;


import java.util.Objects;

/**
 * ### NOTE: There's no need to make any changes to this class. ###
 *
 * Represents a move with its given score. The "score" depends on the context.
 * When ReversiGame use it, it will be the number of flips that this move causes.
 * When a bot use it, it means how good this move is in the "eyes of the bot". A higher score means a better move for that specific bot.
 * For example a greedy bot that tries to maximize the number of disks it has on the board will have a higher score for the move that flips the most disks,
 * while a bot that tries to get to the corners will have a higher score for a move that is closer to the corners. So the same exact move might have different score if the bot is different.
 *
 */
public class MoveScore {
    private int row;
    private int column;
    private int score;
    
    public MoveScore(int row, int column, int score) {
        this.row = row;
        this.column = column;
        this.score = score;
    }

    public int getRow() {
        return row;
    }
    
    public int getColumn() {
        return column;
    }

    public int getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        MoveScore moveScore = (MoveScore) o;
        return row == moveScore.row && column == moveScore.column && score == moveScore.score;
    }

}
package reversi;


/**
 * A class that represents a move and the score that a certain bot gives it. 
 * Note: a score is in the "eyes of the bot". A higher score means a better move for that specific bot.
 * For example a greedy bot that tries to maximize the number of disks it has on the board will have a higher score for the move that flips the most disks,
 * while a bot that tries to get to the corners will have a higher score for a move that is closer to the corners. So the same exact move might have different score if the bot is different.
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
}
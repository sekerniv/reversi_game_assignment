package reversi;

import java.util.Comparator;

/**
 *  This class is used to sort MoveScore objects by their score in descending order (best score move is first).
 */
public class MoveScoreComparator implements Comparator<MoveScore> {
    public int compare(MoveScore o1, MoveScore o2) {
        return  o2.getScore() - o1.getScore();
    }
}

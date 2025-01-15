package reversi;

import java.util.Random;

public class RandomPlayerBot implements ReversiBot {
    private Random rand = new Random();
    private final ReversiGame game;

    public RandomPlayerBot(ReversiGame game) {
        this.game = game;
    }

    public MoveScore getNextMove() {
        return getRandomMove();
    }

    public MoveScore getRandomMove() {
        MoveScore[] possibleMoves = this.game.getPossibleMoves();
        if (possibleMoves.length == 0) {
            return null;
        }

        return possibleMoves[rand.nextInt(possibleMoves.length)];
    }
}

package reversi;

import java.util.Arrays;
import java.util.Collections;

public class TwoStepsBot implements ReversiBot {

    private final ReversiGame game;

    public TwoStepsBot(ReversiGame game) {
		this.game = game;
	}

	public MoveScore getNextMove() {
		return getTwoStepsGreedyMove();
	}


	public MoveScore getTwoStepsGreedyMove() {
		MoveScore[] moveScores = getTwoStepsGreedyMoveScoreForMoves(this.game.getPossibleMoves());
		// adding randomness to the order of the moves
		Collections.shuffle(Arrays.asList(moveScores));
		
		Arrays.sort(moveScores, new MoveScoreComparator());
		if (moveScores.length == 0) {
			return null;
		}
		return moveScores[0];
	}

	private MoveScore[] getTwoStepsGreedyMoveScoreForMoves(MoveScore[] moves) {
		MoveScore[] twoStepsGreedyMoveBenefits = new MoveScore[moves.length];
		for (int i = 0; i < moves.length; i++) {
			twoStepsGreedyMoveBenefits[i] = getTwoStepsGreedyMove(moves[i]);
		}
		return twoStepsGreedyMoveBenefits;

	}

	private MoveScore getTwoStepsGreedyMove(MoveScore moveToEvaluate) {
		ReversiGame gameCopy = new ReversiGame(this.game);
		gameCopy.placeDisk(moveToEvaluate.getRow(), moveToEvaluate.getColumn());
		GreedyBot greedyBot = new GreedyBot(gameCopy);	
		MoveScore nextGreedyMove = greedyBot.getNextMove();
		int score;
		if (nextGreedyMove == null) {
			// if the next player has no possible moves we score this move as the number of disks that will be flipped
			score = moveToEvaluate.getScore();
		} else {
			score = moveToEvaluate.getScore() - nextGreedyMove.getScore();
		}
		return new MoveScore(moveToEvaluate.getRow(), moveToEvaluate.getColumn(), score);
	}
}
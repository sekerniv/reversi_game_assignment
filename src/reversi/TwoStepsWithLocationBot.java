package reversi;

import java.util.Arrays;
import java.util.Collections;

public class TwoStepsWithLocationBot implements ReversiBot {
	private static final int CORNER_BONUS = 3;
	private static final int EDGE_BONUS = 0;
    private final ReversiGame game;
	

    public TwoStepsWithLocationBot(ReversiGame game) {
		this.game = game;
	}

	public MoveScore getNextMove() {
		return getTwoStepsPreferLocationMove();
	}

	public MoveScore getTwoStepsPreferLocationMove() {
		MoveScore[] moveScores = getTwoStepsPreferLocationMoveScores(this.game.getPossibleMoves());
		if (moveScores.length == 0) {
			return null;
		}
		// adding randomness to the order of the moves
		Collections.shuffle(Arrays.asList(moveScores));
		Arrays.sort(moveScores, new MoveScoreComparator());
		
		return moveScores[0];
	}

	private static MoveScore addLocationBonusToMoveScores(MoveScore moveScore) {
		if (isInCorner(moveScore)) {
			return new MoveScore(moveScore.getRow(), moveScore.getColumn(), moveScore.getScore() + CORNER_BONUS);
		} else if (isOnEdge(moveScore)) {
			return new MoveScore(moveScore.getRow(), moveScore.getColumn(), moveScore.getScore() + EDGE_BONUS);
		} else {
			return moveScore;
		}
	}

	private static boolean isOnEdge(MoveScore moveBenefit){
		if(moveBenefit.getRow() == 0 || moveBenefit.getColumn() == 0 || moveBenefit.getRow() == 7 || moveBenefit.getColumn() == 7){
			return true;
		} else {
			return false;
		}
	}

	private static boolean isInCorner(MoveScore moveBenefit){
		if(moveBenefit.getRow() == 0 && moveBenefit.getColumn() == 0){
			return true;
		} else if(moveBenefit.getRow() == 0 && moveBenefit.getColumn() == 7){
			return true;
		} else if(moveBenefit.getRow() == 7 && moveBenefit.getColumn() == 0){
			return true;
		} else if(moveBenefit.getRow() == 7 && moveBenefit.getColumn() == 7){
			return true;
		} else {
			return false;
		}
	}

	private MoveScore getTwoStepsMoveScore(MoveScore move) {
		
		ReversiGame gameCopy = new ReversiGame(this.game);
		gameCopy.placeDisk(move.getRow(), move.getColumn());

		TwoStepsWithLocationBot nextMoveGreedyBot = new TwoStepsWithLocationBot(gameCopy);	

		// we're using the same logic to select the best opooponenet move, including the same scoring for location
		MoveScore nextMove = nextMoveGreedyBot.getNextGreedyMoveWithLocationBonus();
		int moveScore = addLocationBonusToMoveScores(move).getScore();
		int totalMoveScore;
		if (nextMove == null) {
			// if the next player has no possible moves we score this move as the number of disks that will be flipped
			totalMoveScore = moveScore;
		} else {
			totalMoveScore = moveScore - nextMove.getScore();
		}
		return new MoveScore(move.getRow(), move.getColumn(), totalMoveScore);
	}


	private MoveScore getNextGreedyMoveWithLocationBonus(){
		
		MoveScore[] possibleMoves = this.game.getPossibleMoves();
		// adding randomness to the order of the moves
		Collections.shuffle(Arrays.asList(possibleMoves));
		
		if (possibleMoves.length == 0) {
			return null;
		}
		MoveScore[] greedyWithLocationMoveScores =  addLocationBonusToMoveScores(possibleMoves);
		Arrays.sort(greedyWithLocationMoveScores, new MoveScoreComparator());
		return greedyWithLocationMoveScores[0];
	}

	private MoveScore[] addLocationBonusToMoveScores(MoveScore[] moves) {
		MoveScore[] twoStepsMoveScores = new MoveScore[moves.length];
		for (int i = 0; i < moves.length; i++) {
			twoStepsMoveScores[i] = addLocationBonusToMoveScores(moves[i]);
		}
		return twoStepsMoveScores;
	}

	private MoveScore[] getTwoStepsPreferLocationMoveScores(MoveScore[] moves) {
		MoveScore[] twoStepsMoveScores = new MoveScore[moves.length];
		for (int i = 0; i < moves.length; i++) {
			twoStepsMoveScores[i] = getTwoStepsMoveScore(moves[i]);
		}
		return twoStepsMoveScores;
	}

}
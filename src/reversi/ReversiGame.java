package reversi;

/**
 * The {@code ReversiGame} class represents a game of Reversi (also known as Othello).
 * This class manages the game state, including the board, current player, and the game logic required
 * to play Reversi. It provides methods to initialize the game, make moves, print the board, and determine
 * the game outcome.
 */
public class ReversiGame {
	public static int PLAYER_ONE = 1;
	public static int PLAYER_TWO = 2;

	private int[][] board;

	private int curPlayer;

	/**
	 * Initializes the board: all squares are 0 except the four disks in the
	 * middle
	 * Task 1: Implement this method
	 * 
	 * @return the initialized board
	 */
	public ReversiGame() {
		
	}

	public int[][] getBoard() {
		return this.board;
	}

	/**
	 * Task 1: Implement this method
	 * @return
	 */
	public int getCurPlayer() {
		return 0;
	}

	/**
	 * Task 2: Implement this method
	 * 
	 * Prints the board with the row/column indices to the console in the following format
	 * 
	 *    0 1 2 3 4 5 6 7 
	 *  0 0 0 0 1 0 0 0 0 
	 *  1 0 0 0 1 0 0 0 0 
	 *  2 0 0 1 2 2 2 0 0 
		3 0 0 0 1 2 0 0 0 
	 *	4 0 0 0 2 1 0 0 0 
	 *	5 0 0 0 0 0 0 0 0 
	 *	6 0 0 0 0 0 0 0 0 
	 *	7 0 0 0 0 0 0 0 0 

	 * *
	 */

	public void printBoard() {
		
	}

	/**
	 * * Task 3: Implement this method
	 * @param row
	 * @param col 
	 * @return true if the given row and column are on the board, false otherwise
	 */
	public boolean isOnBoard(int row, int col) {
		return true;
	}

	/**
	 * Task 3: Implement this method
	 * Returns the opponets player number (1 or 2) for a given player number (1 or 2)
	 * @param player
	 * @return
	 */
	public static int opponentPlayer(int player) {
		return 0;
	}

// ================================================================================  PART A2  =============================================================================== ======================================== ========================================
	/**
	 * Task 4: Implement this method
	 * @param row row of the move
	 * @param col column of the move
	 * @param rowInc row increment (-1, 0, or 1)
	 * @param colInc 
	 * @return the number of disks that were flipped in the given direction
	 */
	private int updateMoveDisksInSingleDirection(int row, int col, int rowInc, int colInc) {
		
		return 0;
	}

	
	/**
	 * Task 4: Implement this method
	 * @param player player number (1 or 2)
	 * @param row row of the move
	 * @param col column of the move
	 * @param rowInc row increment (-1, 0, or 1)
	 * @param columnInc column increment (-1, 0, or 1)
	 * @return the number of disks that would be flipped in the given direction
	 */
	private int calcFlipsInDirection(int player, int row, int col, int rowInc, int columnInc) {
		return 0;
	}

	/**
	 * Task 5: Implement this method
	 * @param row row of the move
	 * @param col column of the move
	 * @return true if the move was played, false if it failed to be played
	 */
	public boolean placeDisk(int row, int col) {
		System.out.println("Place disk: " + this.curPlayer + " at row: " + row + " column: " + col);

		return false;
	}
	
	

//  ================================================================================  END OF PART A  =============================================================================== ======================================== ======================================== 
	
//  ================================================================================  START PART 2A  =============================================================================== ======================================== ========================================
	
	/**
	 * Task 7: Implement this method
	 * @param player - the player that is making the move
	 * @param row - the row of the move
	 * @param col - the column of the move
	 * @return the benefit of the move (the number of disks that will be flipped)
	 */
	private int calcMoveBenefit(int player, int row, int col) {
		return 0;
	}
	
	/**
	 * Task 8: Implement this method
	 * 
	 * @return an array of all possible moves for the current player (the array
	 *         doesn't contain nulls). If there are no possible moves, return an
	 *         empty array. For each MoveScore the score will be calcMoveBenefit
	 */
	private MoveScore[] getPossibleMoves(int player) {
		return null;
	}
	
	/**
	 * @return an array of all possible moves for the current player
	 */
	public MoveScore[] getPossibleMoves() {
		return this.getPossibleMoves(this.curPlayer);
	}
	
	/**
	 * Task 9: Implement this method
	 * 
	 * A game is over if none of the players have a move to play (i.e. no empty
	 * squares left on the board or none of the players have a valid move)
	 * @return true if the game is over, false otherwise
	 */
	public boolean isGameOver() {		
		return false;
	}
	
	/**
	 * Task 10: Implement this method
	 * This method switches to the opponent player. If there's no move for the opponent player to play, the current player remains the same
	 * @return the current player after the switch
	 */
	public int switchToNextPlayablePlayer() {
		return 0;
	}

	
	/**
	 * Task 11: Implement this method
	 * A player wins if the game is over and the player has more pieces on the board than the opponent
	 * @return -1 if the game is not over, 0 for tie, 1 if player 1 wins, 2 if player 2 wins
	 */
	public int getWinner() {
		return -1;
	}
}

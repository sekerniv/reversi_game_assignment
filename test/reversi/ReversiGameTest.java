package reversi;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class ReversiGameTest {

	private ReversiGame game;

	final int[][] INITIAL_BOARD =
			{
					{0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 1, 2, 0, 0, 0},
					{0, 0, 0, 2, 1, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0},
			};

	@BeforeEach
	public void setUp() {
		game = new ReversiGame();
	}

	@Test
	void testConstructor() {
		assertTrue(Arrays.deepEquals(INITIAL_BOARD, this.game.getBoard()),
				"Expected board: \n" + printBoard(INITIAL_BOARD) + "\nBut got:\n" + printBoard(this.game.getBoard()));
		assertEquals(ReversiGame.PLAYER_ONE, this.game.getCurPlayer(),
				"Expected the player to be Player One");
	}

	@Test
	void testIsOnBoard() {
		assertTrue(this.game.isOnBoard(2, 3), "Expected location 2, 3 to be on the game board");
		assertTrue(this.game.isOnBoard(7, 7), "Expected location 7, 7 to be on the game board");
		assertTrue(this.game.isOnBoard(0, 0), "Expected location 0, 0 to be on the game board");

		assertFalse(this.game.isOnBoard(2, -1), "Expected location 2, -1 to not be on the game board");
		assertFalse(this.game.isOnBoard(-2, 5), "Expected location -2, 5 to not be on the game board");
		assertFalse(this.game.isOnBoard(8, 3), "Expected location 8, 3 to not be on the game board");
		assertFalse(this.game.isOnBoard(3, 8), "Expected location 3, 8 to not be on the game board");
	}

	@Test
	void testOpponentPlayer() {
		assertEquals(ReversiGame.PLAYER_TWO, ReversiGame.opponentPlayer(ReversiGame.PLAYER_ONE),
				"Expected opponent of Player One to be Player Two");
		assertEquals(ReversiGame.PLAYER_ONE, ReversiGame.opponentPlayer(ReversiGame.PLAYER_TWO),
				"Expected opponent of Player Two to be Player One");
	}

	@Test
	void testCalcFlipsInDirection_noFlips() {
		// No opponent disks to flip
		int row = 3, col = 3, rowInc = 1, colInc = 0; // Moving downward
		int flips = game.calcFlipsInDirection(ReversiGame.PLAYER_ONE, row, col, rowInc, colInc);
		assertEquals(0, flips,
				"Expected 0 flips for PLAYER_ONE at position (" + row + ", " + col + ") in direction (" + rowInc + ", " + colInc + "). Current board:\n" + printBoard(this.game.getBoard()));
	}

	@Test
	void testCalcFlipsInDirection_singleFlip_playerTwo() {
		// Player 2 is the current player, trying to flip Player 1's disk
		game.getBoard()[3][3] = ReversiGame.PLAYER_ONE; // Opponent's disk (Player 1)
		game.getBoard()[4][4] = ReversiGame.PLAYER_TWO; // Current player's disk (Player 2)

		int row = 2, col = 2, rowInc = 1, colInc = 1; // Moving diagonally down-right
		int flips = game.calcFlipsInDirection(ReversiGame.PLAYER_TWO, row, col, rowInc, colInc);
		assertEquals(1, flips,
				"Expected 1 flip for PLAYER_TWO at position (" + row + ", " + col + ") in direction (" + rowInc + ", " + colInc + "). Current board:\n" + printBoard(this.game.getBoard()));
	}

	@Test
	void testCalcFlipsInDirection_multipleFlips() {
		// Player 1 is the current player, trying to flip Player 2's disks
		game.getBoard()[3][3] = ReversiGame.PLAYER_TWO; // Opponent's disk (Player 2)
		game.getBoard()[4][4] = ReversiGame.PLAYER_TWO; // Opponent's disk (Player 2)
		game.getBoard()[2][2] = ReversiGame.PLAYER_ONE; // Current player's disk (Player 1)

		int row = 5, col = 5, rowInc = -1, colInc = -1; // Moving diagonally up-left
		int flips = game.calcFlipsInDirection(ReversiGame.PLAYER_ONE, row, col, rowInc, colInc);
		assertEquals(2, flips,
				"Expected 2 flips for PLAYER_ONE at position (" + row + ", " + col + ") in direction (" + rowInc + ", " + colInc + "). Current board:\n" + printBoard(this.game.getBoard()));
	}

	@Test
	void testCalcFlipsInDirection_noEndingPlayerDisk() {
		// No valid ending disk for the current player
		game.getBoard()[3][3] = ReversiGame.PLAYER_TWO;
		game.getBoard()[4][4] = ReversiGame.PLAYER_TWO;

		int row = 5, col = 5, rowInc = -1, colInc = -1; // Moving diagonally up-left
		int flips = game.calcFlipsInDirection(ReversiGame.PLAYER_ONE, row, col, rowInc, colInc);
		assertEquals(0, flips,
				"Expected 0 flips for PLAYER_ONE at position (" + row + ", " + col + ") in direction (" + rowInc + ", " + colInc + "). Current board:\n" + printBoard(this.game.getBoard()));
	}

	@Test
	void testCalcFlipsInDirection_edgeOfBoard() {
		// Flipping along the edge of the board
		game.getBoard()[0][1] = ReversiGame.PLAYER_TWO; // Opponent's disk
		game.getBoard()[0][2] = ReversiGame.PLAYER_ONE; // Current player's disk

		int row = 0, col = 3, rowInc = 0, colInc = -1; // Moving left along the top edge
		int flips = game.calcFlipsInDirection(ReversiGame.PLAYER_TWO, row, col, rowInc, colInc);
		assertEquals(1, flips,
				"Expected 1 flip for PLAYER_TWO at position (" + row + ", " + col + ") in direction (" + rowInc + ", " + colInc + "). Current board:\n" + printBoard(this.game.getBoard()));
	}

	@Test
	void testCalcFlipsInDirection_verticalFlips() {
		// Test vertical flips
		game.getBoard()[3][3] = ReversiGame.PLAYER_TWO; // Opponent's disk
		game.getBoard()[4][3] = ReversiGame.PLAYER_TWO; // Opponent's disk
		game.getBoard()[5][3] = ReversiGame.PLAYER_ONE; // Current player's disk

		int row = 2, col = 3, rowInc = 1, colInc = 0; // Moving up
		int flips = game.calcFlipsInDirection(ReversiGame.PLAYER_ONE, row, col, rowInc, colInc);
		assertEquals(2, flips,
				"Expected 2 flips for PLAYER_ONE at position (" + row + ", " + col + ") in direction (" + rowInc + ", " + colInc + "). Current board:\n" + printBoard(this.game.getBoard()));
	}

	@Test
	void testCalcFlipsInDirection_outOfBoundsEnd() {
		// End position is out of bounds
		game.getBoard()[6][6] = ReversiGame.PLAYER_TWO; // Opponent's disk
		game.getBoard()[7][7] = ReversiGame.PLAYER_ONE; // Current player's disk

		int row = 5, col = 5, rowInc = 1, colInc = 1; // Moving diagonally down-right
		int flips = game.calcFlipsInDirection(ReversiGame.PLAYER_ONE, row, col, rowInc, colInc);
		assertEquals(1, flips,
				"Expected 1 flip for PLAYER_ONE at position (" + row + ", " + col + ") in direction (" + rowInc + ", " + colInc + "). Current board:\n" + printBoard(this.game.getBoard()));
	}

	@Test
	void testUpdateMoveDisksInSingleDirection_noFlips() {
		// No opponent disks to flip
		int row = 3, col = 3, rowInc = 1, colInc = 0; // Moving downward
		int flips = game.updateMoveDisksInSingleDirection(row, col, rowInc, colInc);

		assertEquals(0, flips,
				"Expected 0 flips for PLAYER_ONE at position (" + row + ", " + col + ") in direction (" + rowInc + ", " + colInc + "). Current board:\n" + printBoard(game.getBoard()));
	}

	@Test
	void testUpdateMoveDisksInSingleDirection_multipleFlips() {
		// Player 1 flips multiple disks
		game.getBoard()[3][3] = ReversiGame.PLAYER_TWO; // Opponent's disk
		game.getBoard()[4][4] = ReversiGame.PLAYER_TWO; // Opponent's disk
		game.getBoard()[5][5] = ReversiGame.PLAYER_ONE; // Current player's disk

		int row = 2, col = 2, rowInc = 1, colInc = 1; // Moving diagonally down-right
		int flips = game.updateMoveDisksInSingleDirection(row, col, rowInc, colInc);

		assertEquals(2, flips,
				"Expected 2 flips for PLAYER_ONE at position (" + row + ", " + col + ") in direction (" + rowInc + ", " + colInc + "). Current board:\n" + printBoard(game.getBoard()));
		assertEquals(ReversiGame.PLAYER_ONE, game.getBoard()[3][3],
				"Expected the disk at (3, 3) to be flipped to PLAYER_ONE.");
		assertEquals(ReversiGame.PLAYER_ONE, game.getBoard()[4][4],
				"Expected the disk at (4, 4) to be flipped to PLAYER_ONE.");
	}


	@Test
	void testPlaceDisksFromInitialState1() {
		// Initial move by Player 1
		System.out.println("PLAYER_ONE plays at (2, 4)");
		assertTrue(this.game.placeDisk(2, 4),
				"Expected valid move for PLAYER_ONE at (2, 4). Current board:\n" + printBoard(this.game.getBoard()));
		System.out.println(printBoard(this.game.getBoard()));

		// Move by Player 2
		System.out.println("PLAYER_TWO plays at (4, 5)");
		assertTrue(this.game.placeDisk(4, 5),
				"Expected valid move for PLAYER_TWO at (4, 5). Current board:\n" + printBoard(this.game.getBoard()));
		System.out.println(printBoard(this.game.getBoard()));

		// Another move by Player 1
		System.out.println("PLAYER_ONE plays at (5, 5)");
		assertTrue(this.game.placeDisk(5, 5),
				"Expected valid move for PLAYER_ONE at (5, 5). Current board:\n" + printBoard(this.game.getBoard()));
		System.out.println(printBoard(this.game.getBoard()));

		// Move by Player 2
		System.out.println("PLAYER_TWO plays at (2, 3)");
		assertTrue(this.game.placeDisk(2, 3),
				"Expected valid move for PLAYER_TWO at (2, 3). Current board:\n" + printBoard(this.game.getBoard()));
		System.out.println(printBoard(this.game.getBoard()));

		// Another move by Player 1
		System.out.println("PLAYER_ONE plays at (2, 2)");
		assertTrue(this.game.placeDisk(2, 2),
				"Expected valid move for PLAYER_ONE at (2, 2). Current board:\n" + printBoard(this.game.getBoard()));
		System.out.println(printBoard(this.game.getBoard()));

		// Final move by Player 2
		System.out.println("PLAYER_TWO plays at (1, 3)");
		assertTrue(this.game.placeDisk(1, 3),
				"Expected valid move for PLAYER_TWO at (1, 3). Current board:\n" + printBoard(this.game.getBoard()));
		System.out.println(printBoard(this.game.getBoard()));
	}

	@Test
	void testGetPossibleMoves() {
		System.out.println(printBoard(this.game.getBoard()));
		MoveScore[] possibleMoves = game.getPossibleMoves();
		MoveScore[] expectedMoves = {
				new MoveScore(2, 4, 1),
				new MoveScore(3, 5, 1),
				new MoveScore(5, 3, 1),
				new MoveScore(4, 2, 1),

		};
		assertSameMoves(expectedMoves, possibleMoves, ReversiGame.PLAYER_ONE);

		game.placeDisk(2, 4); // PLAYER_ONE moves
		System.out.println(printBoard(this.game.getBoard()));

		possibleMoves = game.getPossibleMoves();
		expectedMoves = new MoveScore[]{
                new MoveScore(2, 3, 1),
                new MoveScore(2, 5, 1),
                new MoveScore(4, 5, 1),
        };
		assertSameMoves(expectedMoves, possibleMoves, ReversiGame.PLAYER_TWO);

		game.placeDisk(2, 5); // TWO moves
		System.out.println(printBoard(this.game.getBoard()));

		possibleMoves = game.getPossibleMoves();
		expectedMoves = new MoveScore[]{
				new MoveScore(2, 6, 1),
				new MoveScore(3, 5, 1),
				new MoveScore(5, 3, 1),
				new MoveScore(4, 2, 1),
		};

		assertSameMoves(expectedMoves, possibleMoves, ReversiGame.PLAYER_ONE);

		// Test for an empty board scenario (edge case)
		ReversiGame emptyGame = new ReversiGame();
		for (int row = 0; row < emptyGame.getBoard().length; row++) {
			Arrays.fill(emptyGame.getBoard()[row], 0);
		}
		System.out.println(printBoard(emptyGame.getBoard()));
		MoveScore[] movesForEmptyBoard = emptyGame.getPossibleMoves(ReversiGame.PLAYER_ONE);
		assertEquals(0, movesForEmptyBoard.length, "Expected 0 possible moves for an empty board.");
	}


	// ##### HELPER METHODS ######
	public static void assertSameMoves(MoveScore[] expectedMoves, MoveScore[] moves, int player) {
		System.out.println("Possible moves for player" + player +":");
		for (MoveScore move : expectedMoves) {
			System.out.println("Expected possible move: (" + move.getRow() + ", " + move.getColumn() + "), Flips: " + move.getScore());
		}
		for (MoveScore move : moves) {
			System.out.println("Moves received: (" + move.getRow() + ", " + move.getColumn() + "), Flips: " + move.getScore());
		}
		assertEquals(expectedMoves.length, moves.length, "Expected "+ expectedMoves.length +" possible moves for player" + player);

		List<MoveScore> movesList = Arrays.asList(moves);
		for(MoveScore expectedMove : expectedMoves) {
			assertTrue(movesList.contains(expectedMove), "Expected move (" + expectedMove.getRow() + ", " + expectedMove.getColumn() + ") with flips " + expectedMove.getScore() +".");
		}
	}
	/**
	 * Helper method to print the board
	 */
	private String printBoard(int[][] board) {
		StringBuilder sb = new StringBuilder();
		sb.append("  ");
		for (int i = 0; i < board.length; i++) {
			sb.append(i).append(" ");
		}
		sb.append("\n");

		for (int i = 0; i < board.length; i++) {
			sb.append(i).append(" ");
			for (int j = 0; j < board[i].length; j++) {
				sb.append(board[i][j]).append(" ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}

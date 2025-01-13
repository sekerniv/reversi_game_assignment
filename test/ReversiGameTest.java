package reversi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class ReversiGameTest {
	final int[][] EXPECTED_BOARD =
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
			
	@Test
	void testConstructor() {
		ReversiGame reversiGame = new ReversiGame();
		assertTrue(Arrays.deepEquals(EXPECTED_BOARD, reversiGame.getBoard()), "Expected board " + Arrays.deepToString(EXPECTED_BOARD));
	}
	
	@Test
	void testPlaceDisksFromInitialState1() {
		ReversiGame reversiGame = new ReversiGame();
		assertTrue(reversiGame.placeDisk(2, 4), "expected to be valid move for initial board and 2, 4");
		assertTrue(reversiGame.placeDisk(4, 5), "expected to be valid move for board "+ reversiGame.getBoard() + " and 4, 5");
		assertTrue(reversiGame.placeDisk(5, 5), "expected to be valid move for board "+ reversiGame.getBoard() + " and 5, 5");
		assertTrue(reversiGame.placeDisk(2, 3), "expected to be valid move for board "+ reversiGame.getBoard() + " and 5, 5");
		assertTrue(reversiGame.placeDisk(2, 2), "expected to be valid move for board "+ reversiGame.getBoard() + " and 5, 5");
		assertTrue(reversiGame.placeDisk(1, 3), "expected to be valid move for board "+ reversiGame.getBoard() + " and 5, 5");
	}
	
	@Test
	void testPlaceDisksFromInitialState2() {
		ReversiGame reversiGame = new ReversiGame();
		assertTrue(reversiGame.placeDisk(2, 4), "expected to be valid move for initial board and 1, 2, 4");
	}
	
	@Test
	void testIsValidMoveInitialStatePlayer1False() {
		ReversiGame reversiGame = new ReversiGame();
		assertFalse(reversiGame.placeDisk(3, 2), "expected to be invalid move for initial board and player 1, 3, 2");
		assertFalse(reversiGame.placeDisk(5, 4), "expected to be invalid move for initial board and player 1, 5, 4");
		
		assertFalse(reversiGame.placeDisk(0, 0), "expected to be invalid move for initial board and player 1, 0, 0");
		assertFalse(reversiGame.placeDisk(3, 3), "expected to be invalid move for initial board and player 1, 3, 3");
		assertFalse(reversiGame.placeDisk(3, 4), "expected to be invalid move for initial board and player 1, 3, 4");
		assertFalse(reversiGame.placeDisk(5, 2), "expected to be invalid move for initial board and player 1, 5, 2");
	}

	@Test
	void testIsGameOverFalseForInitialBoard() {
		ReversiGame reversiGame = new ReversiGame();
		assertFalse(reversiGame.isGameOver(), "expected to be false for initial board");
	}
	
	@Test
	void testGetWinnerNoWinnerForInitialBoard() {
		ReversiGame reversiGame = new ReversiGame();
		assertEquals(reversiGame.getWinner(), -1, "expected to be -1 for initial board");
	}
}
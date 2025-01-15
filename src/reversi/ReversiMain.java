package reversi;
import java.util.Scanner;

/**
 * The main class for the Reversi game.
 * This class contains the main method, which serves as the entry point for the game.
 * It initializes the game board, processes user/bot input for moves, prints the board and declares the winner in the end.
 **/
public class ReversiMain {
	public static void main(String[] args) {
		twoBots();
	}

	public static void twoPlayers() {
		Scanner scanner = new Scanner(System.in);
		ReversiGame rg = new ReversiGame();
		while(!rg.isGameOver()) {
			rg.printBoard();
			System.out.println("Player " + rg.getCurPlayer() + " please enter your row and column");
			int row = scanner.nextInt();
			int column = scanner.nextInt();
			while (!rg.placeDisk(row, column)) {
				System.out.println("Illegal move! Player " + rg.getCurPlayer() + " please enter your row and column");
				row = scanner.nextInt();
				column = scanner.nextInt();
			}
		}

		if (rg.getWinner() == ReversiGame.PLAYER_ONE) {
			System.out.println("Player 1 wins!");
		} else if (rg.getWinner() == ReversiGame.PLAYER_TWO) {
			System.out.println("Player 2 wins!");
		} else {
			System.out.println("It's a tie!");
		}

		scanner.close();
	}

	public static void twoBots() {
		int player1Wins = 0;
		int player2Wins = 0;
		for (int i = 0; i < 1000; i++) {
			ReversiGame rg = new ReversiGame();
			TwoStepsWithLocationBot bot1 = new TwoStepsWithLocationBot(rg);
			TwoStepsBot bot2 = new TwoStepsBot(rg);
			while (!rg.isGameOver()) {
				rg.printBoard();
				MoveScore nextMove;
				if (rg.getCurPlayer() == ReversiGame.PLAYER_ONE) {
					System.out.println("TwoStepsWithLocationBot is playing...");
					nextMove = bot1.getNextMove();
				} else {
					System.out.println("TwoStepsBot is playing...");
					nextMove = bot2.getNextMove();
				}
				if(nextMove == null) {
					System.out.println("No possible moves for player " + rg.getCurPlayer() + ". Why wasn't this game terminated?");
					throw new RuntimeException("No possible moves for player " + rg.getCurPlayer() + " we should have skipped the turn");
				}
				rg.placeDisk(nextMove.getRow(), nextMove.getColumn());
			}
			rg.printBoard();
			System.out.println("Game #" + i + " is over!");
			if (rg.getWinner() == ReversiGame.PLAYER_ONE) {
				player1Wins++;
				System.out.println("Player 1 wins!");
			} else if (rg.getWinner() == ReversiGame.PLAYER_TWO) {
				player2Wins++;
				System.out.println("Player 2 wins!");
			} else {
				System.out.println("It's a tie!");
			}
		}
		System.out.println("Player 1 won " + player1Wins + " times and player2 won " + player2Wins + " times");

	}



}
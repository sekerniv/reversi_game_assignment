package reversi;

import java.util.Random;

/**
* This is a template for a bot.
* Give your bot a uniqie name and rename this class and the file name.
* 
* PLEASE NAME YOUR BOT WITH A NAME THAT ENDS WITH "Bot" (e.g. FlipMasterBot or ReversiConquerorBot).
*/
public class RenameThisClassMyReversiBot implements ReversiBot {

    private final ReversiGame game;

    public RenameThisClassMyReversiBot(ReversiGame game) {
		this.game = game;
	}

	public MoveScore getNextMove() {
		return getRandomMove();
	}

	

	public MoveScore getRandomMove(){
		MoveScore[] possibleMoves = this.game.getPossibleMoves();
		if (possibleMoves.length == 0) {
			return null;
		}
		Random rand = new Random();
		return possibleMoves[rand.nextInt(possibleMoves.length)];		
	}

}
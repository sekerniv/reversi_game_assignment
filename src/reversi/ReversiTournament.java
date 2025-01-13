package reversi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReversiTournament {

    private static final int NUM_OF_GAMES = 1000;
    private static boolean VERBOSE = false;
    private static boolean PAUSE_FOR_INPUT = false;

    public static void main(String[] args)
            throws Exception {
        TournamentContestant[] contestants = loadContestants();
        if (contestants.length < 2) {
            System.out.println("Not enough contestants to run a tournament!");
            return;
        }

        // Randomly order competitions
        Collections.shuffle(Arrays.asList(contestants));

        competeRoundRobin(contestants);
        Arrays.sort(contestants, (a, b) -> b.score - a.score);
        printLeaderboard(contestants);
    }

    private static void competeRoundRobin(TournamentContestant[] contestants)
            throws ReflectiveOperationException, RuntimeException, IOException {
        System.out.println("\r\nStarting roubdrobin tournament with " + contestants.length + " contestants");
        for (int i = 0; i < contestants.length; i++) {
            for (int j = i + 1; j < contestants.length; j++) {
                int result = matchBots(contestants[i], contestants[j]);
                if (result > 0) {
                    contestants[i].score += 1;
                } else if (result < 0) {
                    contestants[j].score += 1;
                } else { // no points for tie

                }
                printLeaderboard(contestants);
                if (PAUSE_FOR_INPUT) {
                    System.out.println("Press enter to continue");
                    System.in.read();
                }
            }
        }
    }

    private static void printLeaderboard(TournamentContestant[] contestants) {
        System.out.println();
        System.out.println("==============================================");
        System.out.println("============== Leaderboard: ==================");
        System.out.println("==============================================");
        for (int i = 0; i < contestants.length; i++) {
            System.out.println(contestants[i].botClass.getSimpleName() + " " + contestants[i].score + " points");
        }
        System.out.println("==============================================");
    }

    private static int matchBots(
            TournamentContestant contestant1,
            TournamentContestant contestant2) throws ReflectiveOperationException, IOException {
        System.out.println("Starting match between " + contestant1.botClass.getSimpleName() + " and "
                + contestant2.botClass.getSimpleName());
        if (PAUSE_FOR_INPUT) {

            System.out.println("Are you ready to rubmble? Press enter to start");
            System.in.read();
        }

        SingleRoundScore contestant1MatchScore1 = new SingleRoundScore(contestant1, 0);
        SingleRoundScore contestant1MatchScore2 = new SingleRoundScore(contestant2, 0);

        SingleRoundScore[] contestantsWithScores = { contestant1MatchScore1, contestant1MatchScore2 };

        int ties = 0;

        for (int i = 0; i < NUM_OF_GAMES; i++) {

            if (VERBOSE) {
                System.out.println("Game " + (i + 1) + " out of " + NUM_OF_GAMES);
            }

            ReversiGame game = new ReversiGame();

            // "flipping coin" to decide who plays first
            Collections.shuffle(Arrays.asList(contestantsWithScores));

            // Creating a new instance by calling the constructor with the game
            // The players order matches the order of the contestantsWithScores array in
            // this single game
            ReversiBot[] players = { contestantsWithScores[0].constructor.newInstance(game),
                    contestantsWithScores[1].constructor.newInstance(game) };

            while (!game.isGameOver()) {
                if (VERBOSE)
                    game.printBoard();
                MoveScore nextMove = players[game.getCurPlayer() - 1].getNextMove();
                if (nextMove == null) {
                    throw new RuntimeException(
                            "The game is not over but the bot returned null move - something is off!");
                }
                game.placeDisk(nextMove.getRow(), nextMove.getColumn());

            }

            if (game.getWinner() == ReversiGame.PLAYER_ONE) {
                contestantsWithScores[0].singleRoundScore++;
            } else if (game.getWinner() == ReversiGame.PLAYER_TWO) {
                contestantsWithScores[1].singleRoundScore++;
            } else {
                // no points for tie
                ties++;
            }

        }

        System.out.println("Match is over!");
        System.out.println(contestantsWithScores[0].singleRoundScore > contestantsWithScores[1].singleRoundScore
                ? contestant1.botClass.getSimpleName() + " wins!"
                : contestant2.botClass.getSimpleName() + " wins!");
        System.out.println("Results: " + contestantsWithScores[0].contestant.botClass.getSimpleName() + " "
                + contestantsWithScores[0].singleRoundScore + " points, "
                + contestantsWithScores[1].contestant.botClass.getSimpleName() + " "
                + contestantsWithScores[1].singleRoundScore + " points, " + ties + " ties");

        return contestant1MatchScore1.singleRoundScore - contestant1MatchScore2.singleRoundScore;
    }

    private static TournamentContestant[] loadContestants()
            throws IOException, ReflectiveOperationException {

        Package currentPackage = ReversiTournament.class.getPackage();

        String packageName = currentPackage.getName();

        String path = ReversiTournament.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = URLDecoder.decode(path, StandardCharsets.UTF_8.name());
        final File directory = new File(path, packageName.replace('.', '/'));

        // Assuming that all the bots are in the same package as the tournament and end
        // with "Bot.class"
        File[] botFiles = directory.listFiles(pathname -> pathname.getName().endsWith("Bot.class"));

        List<TournamentContestant> contestants = new ArrayList<>();
        for (int i = 0; i < botFiles.length; i++) {
            @SuppressWarnings("unchecked")
            Class<? extends ReversiBot> botClass = (Class<? extends ReversiBot>) Class
                    .forName(packageName + "." + botFiles[i].getName().replace(".class", ""));
            if (botClass.isInterface()) {
                continue;
            }
            contestants.add(new TournamentContestant(botClass));
        }
        System.out.println("Loaded " + contestants.size() + " contestants: ");
        contestants.forEach((contestant) -> {
            System.out.println(contestant.botClass.getSimpleName());
        });
        return contestants.toArray(new TournamentContestant[contestants.size()]);
    }

    private static class TournamentContestant {
        private final Class<? extends ReversiBot> botClass;
        private int score; // 3 points for win, 1 point for tie, 0 points for loss

        TournamentContestant(Class<? extends ReversiBot> botClass) {
            this.botClass = botClass;
            score = 0;
        }

    }

    private static class SingleRoundScore {
        private final TournamentContestant contestant;
        private final Constructor<? extends ReversiBot> constructor;
        private int singleRoundScore;

        public SingleRoundScore(TournamentContestant contestant, int singleRoundScore)
                throws ReflectiveOperationException {
            this.contestant = contestant;
            this.singleRoundScore = singleRoundScore;
            // tiny optimization. Storing the constructor instead of using reflection in
            // each iteration
            this.constructor = contestant.botClass.getConstructor(ReversiGame.class);
        }
    }

}

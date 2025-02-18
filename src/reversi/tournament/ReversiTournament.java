package reversi.tournament;

import reversi.MoveScore;
import reversi.ReversiBot;
import reversi.ReversiGame;

import java.io.File;
import java.io.FilenameFilter;
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

    // The bots are assumed to reside in the "reversi" package
    private static final String BOT_PACKAGE = "reversi";

    public static void main(String[] args) throws Exception {
        List<TournamentContestant> contestantList = loadContestants();
        TournamentContestant[] contestants = contestantList.toArray(new TournamentContestant[contestantList.size()]);
        if (contestants.length < 2) {
            System.out.println("Not enough contestants to run a tournament!");
            return;
        }
        Collections.shuffle(Arrays.asList(contestants));
        competeRoundRobin(contestants);
        Arrays.sort(contestants, (a, b) -> b.score - a.score);
        printLeaderboard(contestants);
    }

    // Creates round-robin matches from the list of contestants.
    public static List<Match> createMatches(List<TournamentContestant> contestants) {
        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < contestants.size(); i++) {
            for (int j = i + 1; j < contestants.size(); j++) {
                matches.add(new Match(contestants.get(i), contestants.get(j)));
            }
        }
        return matches;
    }

    private static void competeRoundRobin(TournamentContestant[] contestants)
            throws ReflectiveOperationException, RuntimeException, IOException {
        System.out.println("\r\nStarting round robin tournament with " + contestants.length + " contestants");
        for (int i = 0; i < contestants.length; i++) {
            for (int j = i + 1; j < contestants.length; j++) {
                int result = matchBots(contestants[i], contestants[j]);
                if (result > 0) {
                    contestants[i].score += 1;
                } else if (result < 0) {
                    contestants[j].score += 1;
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
        for (TournamentContestant tc : contestants) {
            System.out.println(tc.botClass.getSimpleName() + " " + tc.score + " points");
        }
        System.out.println("==============================================");
    }

    // Loads all bot classes from the BOT_PACKAGE ("reversi").
    // Skips any class whose name contains "RenameThisClass" (printing a message on the command line).
    public static List<TournamentContestant> loadContestants() throws Exception {
        List<TournamentContestant> list = new ArrayList<>();
        String packageName = BOT_PACKAGE;
        String path = ReversiTournament.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = URLDecoder.decode(path, StandardCharsets.UTF_8.name());
        File directory = new File(path, packageName.replace('.', '/'));
        File[] botFiles = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("Bot.class");
            }
        });
        if (botFiles != null) {
            for (File file : botFiles) {
                String className = file.getName().replace(".class", "");
                if (className.contains("RenameThisClass")) {
                    System.out.println("Skipping bot: " + className);
                    continue;
                }
                @SuppressWarnings("unchecked")
                Class<? extends ReversiBot> botClass = (Class<? extends ReversiBot>) Class.forName(packageName + "." + className);
                if (!botClass.isInterface()) {
                    list.add(new TournamentContestant(botClass));
                }
            }
        }
        System.out.println("Loaded " + list.size() + " contestants: ");
        for (TournamentContestant tc : list) {
            System.out.println(tc.botClass.getSimpleName());
        }
        return list;
    }

    private static int matchBots(TournamentContestant contestant1, TournamentContestant contestant2)
            throws ReflectiveOperationException, IOException {
        System.out.println("Starting match between " + contestant1.botClass.getSimpleName() + " and " +
                contestant2.botClass.getSimpleName());
        if (PAUSE_FOR_INPUT) {
            System.out.println("Are you ready to rumble? Press enter to start");
            System.in.read();
        }
        SingleRoundScore score1 = new SingleRoundScore(contestant1, 0);
        SingleRoundScore score2 = new SingleRoundScore(contestant2, 0);
        SingleRoundScore[] scores = { score1, score2 };
        int ties = 0;
        for (int i = 0; i < NUM_OF_GAMES; i++) {
            if (VERBOSE) {
                System.out.println("Game " + (i + 1) + " out of " + NUM_OF_GAMES);
            }
            ReversiGame game = new ReversiGame();
            Collections.shuffle(Arrays.asList(scores));
            ReversiBot[] players = { scores[0].constructor.newInstance(game), scores[1].constructor.newInstance(game) };
            while (!game.isGameOver()) {
                if (VERBOSE)
                    game.printBoard();
                MoveScore nextMove = players[game.getCurPlayer() - 1].getNextMove();
                if (nextMove == null) {
                    throw new RuntimeException("The game is not over but the bot returned null move - something is off!");
                }
                game.placeDisk(nextMove.getRow(), nextMove.getColumn());
            }
            if (game.getWinner() == ReversiGame.PLAYER_ONE) {
                scores[0].singleRoundScore++;
            } else if (game.getWinner() == ReversiGame.PLAYER_TWO) {
                scores[1].singleRoundScore++;
            } else {
                ties++;
            }
        }
        System.out.println("Match is over!");
        System.out.println(scores[0].singleRoundScore > scores[1].singleRoundScore
                ? contestant1.botClass.getSimpleName() + " wins!"
                : contestant2.botClass.getSimpleName() + " wins!");
        System.out.println("Results: " + scores[0].contestant.botClass.getSimpleName() + " " + scores[0].singleRoundScore
                + " points, " + scores[1].contestant.botClass.getSimpleName() + " " + scores[1].singleRoundScore
                + " points, " + ties + " ties");
        return scores[0].singleRoundScore - scores[1].singleRoundScore;
    }

    // Inner classes for tournament management

    public static class TournamentContestant {
        public final Class<? extends ReversiBot> botClass;
        public int score;
        public TournamentContestant(Class<? extends ReversiBot> botClass) {
            this.botClass = botClass;
            score = 0;
        }
    }

    public static class Match {
        public TournamentContestant contestant1;
        public TournamentContestant contestant2;
        public Match(TournamentContestant c1, TournamentContestant c2) {
            contestant1 = c1;
            contestant2 = c2;
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
            this.constructor = contestant.botClass.getConstructor(ReversiGame.class);
        }
    }
}

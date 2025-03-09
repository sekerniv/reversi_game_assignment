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

    // Number of games each match will play
    public static final int NUM_OF_GAMES = 20;
    // Controls verbose printing of board states
    public static boolean VERBOSE = false;
    // If true, pause for input between matches
    public static boolean PAUSE_FOR_INPUT = false;
    // Package name where bots are located
    private static final String BOT_PACKAGE = "reversi";

    // Main entry point for a console-based tournament
    public static void main(String[] args) throws Exception {
        List<TournamentContestant> contestantList = loadContestants();
        TournamentContestant[] contestants = contestantList.toArray(new TournamentContestant[0]);
        if (contestants.length < 2) {
            System.out.println("Not enough contestants to run a tournament!");
            return;
        }
        Collections.shuffle(Arrays.asList(contestants));
        competeRoundRobin(contestants);
        Arrays.sort(contestants, (a, b) -> b.score - a.score);
        printLeaderboard(contestants);
    }

    // Public so the UI can call it
    public static List<Match> createMatches(List<TournamentContestant> contestants) {
        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < contestants.size(); i++) {
            for (int j = i + 1; j < contestants.size(); j++) {
                matches.add(new Match(contestants.get(i), contestants.get(j)));
            }
        }
        return matches;
    }

    // Public so the UI can call it
    public static List<TournamentContestant> loadContestants() throws Exception {
        List<TournamentContestant> list = new ArrayList<>();
        String path = ReversiTournament.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = URLDecoder.decode(path, StandardCharsets.UTF_8);
        File directory = new File(path, BOT_PACKAGE.replace('.', '/'));
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
                Class<? extends ReversiBot> botClass =
                        (Class<? extends ReversiBot>) Class.forName(BOT_PACKAGE + "." + className);
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

    // Public so the UI can call or reuse if needed
    public static void printLeaderboard(TournamentContestant[] contestants) {
        System.out.println();
        System.out.println("==============================================");
        System.out.println("============== Leaderboard: ==================");
        System.out.println("==============================================");
        for (TournamentContestant tc : contestants) {
            System.out.println(tc.botClass.getSimpleName() + " " + tc.score + " points");
        }
        System.out.println("==============================================");
    }

    // The main round-robin logic (private, not necessarily needed by UI)
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

    // The actual match between two bots, playing NUM_OF_GAMES times
    private static int matchBots(TournamentContestant contestant1, TournamentContestant contestant2)
            throws ReflectiveOperationException, IOException {
        System.out.println("Starting match between "
                + contestant1.botClass.getSimpleName() + " and "
                + contestant2.botClass.getSimpleName());
        if (PAUSE_FOR_INPUT) {
            System.out.println("Are you ready to rumble? Press enter to start");
            System.in.read();
        }
        SingleRoundScore s1 = new SingleRoundScore(contestant1);
        SingleRoundScore s2 = new SingleRoundScore(contestant2);
        int ties = 0;
        for (int i = 0; i < NUM_OF_GAMES; i++) {
            ReversiGame game = new ReversiGame();
            boolean c1IsPlayerOne = (i % 2 == 0);
            ReversiBot botP1 = c1IsPlayerOne
                    ? s1.constructor.newInstance(game)
                    : s2.constructor.newInstance(game);
            ReversiBot botP2 = c1IsPlayerOne
                    ? s2.constructor.newInstance(game)
                    : s1.constructor.newInstance(game);
            while (!game.isGameOver()) {
                ReversiBot currentBot = (game.getCurPlayer() == ReversiGame.PLAYER_ONE)
                        ? botP1 : botP2;
                MoveScore move = currentBot.getNextMove();
                if (move == null) {
                    throw new RuntimeException("Game is not over, but the bot returned a null move");
                }
                game.placeDisk(move.getRow(), move.getColumn());
            }
            if (game.getWinner() == ReversiGame.PLAYER_ONE) {
                if (c1IsPlayerOne) {
                    s1.singleRoundScore++;
                } else {
                    s2.singleRoundScore++;
                }
            } else if (game.getWinner() == ReversiGame.PLAYER_TWO) {
                if (c1IsPlayerOne) {
                    s2.singleRoundScore++;
                } else {
                    s1.singleRoundScore++;
                }
            } else {
                ties++;
            }
        }
        System.out.println("Match is over!");
        int diff = s1.singleRoundScore - s2.singleRoundScore;
        String winnerMsg;
        if (diff > 0) {
            winnerMsg = contestant1.botClass.getSimpleName() + " wins!";
        } else if (diff < 0) {
            winnerMsg = contestant2.botClass.getSimpleName() + " wins!";
        } else {
            winnerMsg = "It's a tie!";
        }
        System.out.println(winnerMsg);
        System.out.println("Results: "
                + contestant1.botClass.getSimpleName() + " "
                + s1.singleRoundScore + " points, "
                + contestant2.botClass.getSimpleName() + " "
                + s2.singleRoundScore + " points, "
                + ties + " ties");
        return diff;
    }

    // Public so UI can reference the contestants. Some fields might be accessed from the UI.
    public static class TournamentContestant {
        public final Class<? extends ReversiBot> botClass;
        public int score;

        public TournamentContestant(Class<? extends ReversiBot> botClass) {
            this.botClass = botClass;
            this.score = 0;
        }
    }

    // Public so UI can reference the match structure
    public static class Match {
        public TournamentContestant contestant1;
        public TournamentContestant contestant2;
        public Match(TournamentContestant c1, TournamentContestant c2) {
            this.contestant1 = c1;
            this.contestant2 = c2;
        }
    }

    // We can keep this private, not used by UI
    private static class SingleRoundScore {
        private final TournamentContestant contestant;
        private final Constructor<? extends ReversiBot> constructor;
        private int singleRoundScore;

        public SingleRoundScore(TournamentContestant contestant) throws ReflectiveOperationException {
            this.contestant = contestant;
            this.constructor = contestant.botClass.getConstructor(ReversiGame.class);
            this.singleRoundScore = 0;
        }
    }
}

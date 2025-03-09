package reversi.tournament;

import reversi.MoveScore;
import reversi.ReversiBot;
import reversi.ReversiGame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class ReversiTournamentUI extends JFrame {

    private static final int MOVE_DELAY = 300;
    private boolean doubleRoundRobin = true;

    private GamePanel gamePanel;
    private JTextArea leaderboardArea;
    private JTextArea matchesArea;
    private JButton tournamentStartButton;
    private JButton startMatchButton;
    private JButton pauseButton;

    private JLabel matchInfoLabel;
    private JLabel diskCountLabel;

    private List<ReversiTournament.Match> matches;
    private List<ReversiTournament.TournamentContestant> contestants;
    private int currentMatchIndex = 0;

    private ReversiGame currentGame;
    private ReversiBot currentBot1;
    private ReversiBot currentBot2;
    private ReversiTournament.TournamentContestant[] currentPlayers;

    private Timer moveTimer;
    private Map<Integer, String> matchResults = new HashMap<>();

    public ReversiTournamentUI() {
        setTitle("Reversi Tournament - Bot vs Bot");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        add(topPanel, BorderLayout.NORTH);

        // Match info label with leading spaces for left padding
        matchInfoLabel = new JLabel("    Press 'Start Tournament' to load bots", SwingConstants.LEFT);
        matchInfoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(matchInfoLabel);

        // Disk count label with leading spaces
        diskCountLabel = new JLabel("    Black: 0, White: 0", SwingConstants.LEFT);
        diskCountLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        topPanel.add(diskCountLabel);

        // Main board in the center
        gamePanel = new GamePanel();
        // The GamePanel itself has a preferredSize set in its constructor (8 * CELL_SIZE)
        add(gamePanel, BorderLayout.CENTER);

        // Leaderboard on the right
        leaderboardArea = new JTextArea();
        leaderboardArea.setEditable(false);
        JScrollPane scrollPaneLeaderboard = new JScrollPane(leaderboardArea);
        scrollPaneLeaderboard.setPreferredSize(new Dimension(200, 0));

        // Matches area on the left
        matchesArea = new JTextArea();
        matchesArea.setEditable(false);
        matchesArea.setLineWrap(false);
        matchesArea.setWrapStyleWord(false);
        // Add some margin (left padding) in the text area
        matchesArea.setMargin(new Insets(0, 10, 0, 0));

        JScrollPane scrollPaneMatches = new JScrollPane(
                matchesArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        // Make the left panel wide enough
        scrollPaneMatches.setPreferredSize(new Dimension(500, 0));

        add(scrollPaneLeaderboard, BorderLayout.EAST);
        add(scrollPaneMatches, BorderLayout.WEST);

        // Bottom panel
        JPanel buttonPanel = new JPanel();
        tournamentStartButton = new JButton("Start Tournament");
        startMatchButton = new JButton("Start Match");
        pauseButton = new JButton("Pause");
        startMatchButton.setEnabled(false);
        pauseButton.setEnabled(false);

        buttonPanel.add(tournamentStartButton);
        buttonPanel.add(startMatchButton);
        buttonPanel.add(pauseButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Listeners
        tournamentStartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tournamentStartButton.setEnabled(false);
                startTournament();
            }
        });

        startMatchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startMatchButton.setEnabled(false);
                if (currentGame == null || currentGame.isGameOver()) {
                    prepareNextMatch();
                }
                pauseButton.setEnabled(true);
                startCurrentMatch();
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (moveTimer != null) {
                    if (moveTimer.isRunning()) {
                        moveTimer.stop();
                        pauseButton.setText("Resume");
                    } else {
                        moveTimer.start();
                        pauseButton.setText("Pause");
                    }
                }
            }
        });

        // Force the window to respect preferred sizes of all components
        pack();
        setLocationRelativeTo(null); // center on screen
    }

    private void startTournament() {
        try {
            contestants = ReversiTournament.loadContestants();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading bots.");
            return;
        }
        if (contestants.size() < 2) {
            JOptionPane.showMessageDialog(this, "Not enough bots for a tournament!");
            return;
        }

        matches = ReversiTournament.createMatches(contestants);
        if (doubleRoundRobin) {
            List<ReversiTournament.Match> extended = new ArrayList<>();
            for (ReversiTournament.Match m : matches) {
                extended.add(m);
                extended.add(new ReversiTournament.Match(m.contestant2, m.contestant1));
            }
            matches = extended;
        }

        currentMatchIndex = 0;
        matchResults.clear();
        updateLeaderboard();
        updateMatchesPanel();
        prepareNextMatch();
    }

    private void prepareNextMatch() {
        if (currentMatchIndex >= matches.size()) {
            matchInfoLabel.setText("    Tournament Finished!");
            startMatchButton.setEnabled(false);
            pauseButton.setEnabled(false);
            return;
        }
        ReversiTournament.Match match = matches.get(currentMatchIndex);

        if (!doubleRoundRobin) {
            ReversiTournament.TournamentContestant[] playersOrder = {
                    match.contestant1, match.contestant2
            };
            Collections.shuffle(Arrays.asList(playersOrder));
            currentPlayers = playersOrder;
        } else {
            currentPlayers = new ReversiTournament.TournamentContestant[] {
                    match.contestant1, match.contestant2
            };
        }

        currentGame = new ReversiGame();
        try {
            currentBot1 = currentPlayers[0].botClass
                    .getConstructor(ReversiGame.class).newInstance(currentGame);
            currentBot2 = currentPlayers[1].botClass
                    .getConstructor(ReversiGame.class).newInstance(currentGame);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        gamePanel.setBoard(currentGame.getBoard());
        gamePanel.repaint();

        String info = String.format(
                "    Match %d of %d - %s (Black) vs %s (White)",
                currentMatchIndex + 1,
                matches.size(),
                currentPlayers[0].botClass.getSimpleName(),
                currentPlayers[1].botClass.getSimpleName()
        );
        matchInfoLabel.setText(info);

        updateDiskCountLabel();
        startMatchButton.setEnabled(true);
        pauseButton.setText("Pause");
        pauseButton.setEnabled(false);

        updateMatchesPanel();
    }

    private void startCurrentMatch() {
        moveTimer = new Timer(MOVE_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentMatchIndex >= matches.size()) {
                    moveTimer.stop();
                    return;
                }

                if (currentGame.isGameOver()) {
                    moveTimer.stop();
                    pauseButton.setEnabled(false);

                    int winner = currentGame.getWinner();
                    if (winner == ReversiGame.PLAYER_ONE) {
                        currentPlayers[0].score++;
                        matchResults.put(currentMatchIndex,
                                currentPlayers[0].botClass.getSimpleName() + " won");
                    } else if (winner == ReversiGame.PLAYER_TWO) {
                        currentPlayers[1].score++;
                        matchResults.put(currentMatchIndex,
                                currentPlayers[1].botClass.getSimpleName() + " won");
                    } else {
                        matchResults.put(currentMatchIndex, "Draw");
                    }

                    updateLeaderboard();

                    currentMatchIndex++;
                    if (currentMatchIndex < matches.size()) {
                        matchInfoLabel.setText(
                                "    Match over. Final board displayed. Press 'Start Match' to continue."
                        );
                        startMatchButton.setEnabled(true);
                        updateMatchesPanel();
                    } else {
                        matchInfoLabel.setText("    Tournament Finished!");
                        startMatchButton.setEnabled(false);
                        updateMatchesPanel();
                        showChampion();
                    }
                } else {
                    MoveScore move;
                    if (currentGame.getCurPlayer() == ReversiGame.PLAYER_ONE) {
                        move = currentBot1.getNextMove();
                    } else {
                        move = currentBot2.getNextMove();
                    }
                    if (move != null) {
                        currentGame.placeDisk(move.getRow(), move.getColumn());
                        gamePanel.setBoard(currentGame.getBoard());
                        gamePanel.repaint();
                        updateDiskCountLabel();
                    } else {
                        currentGame.switchToNextPlayablePlayer();
                    }
                }
            }
        });
        moveTimer.start();
        pauseButton.setEnabled(true);
    }

    private void showChampion() {
        Collections.sort(contestants, (a, b) -> b.score - a.score);
        ReversiTournament.TournamentContestant champion = contestants.get(0);

        String asciiTrophy =
                "  ___________\n" +
                        " '._==_==_=.'\n" +
                        " .-\\:      /-.\n" +
                        "| (|:.     |) |\n" +
                        " '-|:.     |-'\n" +
                        "   \\::.    /\n" +
                        "    '::. .'\n" +
                        "      ) (\n" +
                        "    _.' '._\n" +
                        "   `-------`\n";

        leaderboardArea.append("\n\n*** FINAL RESULTS ***\n");
        leaderboardArea.append("WINNER: " + champion.botClass.getSimpleName() + " !!!\n");
        leaderboardArea.append(asciiTrophy);
    }

    private void updateLeaderboard() {
        Collections.sort(contestants, (a, b) -> b.score - a.score);

        leaderboardArea.setText("");
        leaderboardArea.append("====== Leaderboard ======\n");
        for (ReversiTournament.TournamentContestant tc : contestants) {
            leaderboardArea.append(tc.botClass.getSimpleName() + ": " + tc.score + "\n");
        }
        leaderboardArea.append("=========================\n\n");
    }

    private void updateMatchesPanel() {
        if (matches == null) return;

        matchesArea.setText("");
        for (int i = 0; i < matches.size(); i++) {
            ReversiTournament.Match m = matches.get(i);
            String blackName = m.contestant1.botClass.getSimpleName();
            String whiteName = m.contestant2.botClass.getSimpleName();

            String status;
            if (i < currentMatchIndex) {
                // Completed
                String result = matchResults.getOrDefault(i, "Unknown");
                status = " - " + result;
            } else if (i == currentMatchIndex) {
                // Current => use <-- arrow
                status = "  <-- IN PROGRESS";
            } else {
                // Future
                status = "";
            }

            String line = String.format(
                    "%2d) %s (Black) vs %s (White)%s",
                    i + 1, blackName, whiteName, status
            );
            matchesArea.append(line + "\n");
        }
    }

    private void updateDiskCountLabel() {
        if (currentGame == null) return;
        int[][] board = currentGame.getBoard();
        int blackCount = 0;
        int whiteCount = 0;
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] == ReversiGame.PLAYER_ONE) {
                    blackCount++;
                } else if (board[row][col] == ReversiGame.PLAYER_TWO) {
                    whiteCount++;
                }
            }
        }
        diskCountLabel.setText("    Black: " + blackCount + ", White: " + whiteCount);
    }

    /**
     * Inner class for drawing an 8×8 Reversi board at a fixed cell size.
     */
    private class GamePanel extends JPanel {
        private final int CELL_SIZE = 80;
        private int[][] board;

        public GamePanel() {
            // Ensure an 8×8 board has an 80×80 cell size => 640×640 total
            setPreferredSize(new Dimension(8 * CELL_SIZE, 8 * CELL_SIZE));
            board = new int[8][8];
        }

        public void setBoard(int[][] newBoard) {
            board = new int[newBoard.length][];
            for (int i = 0; i < newBoard.length; i++) {
                board[i] = newBoard[i].clone();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(new Color(0, 128, 0));
            g.fillRect(0, 0, getWidth(), getHeight());

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    g.setColor(Color.BLACK);
                    g.drawRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);

                    int cell = board[row][col];
                    if (cell == ReversiGame.PLAYER_ONE) {
                        g.setColor(Color.BLACK);
                        g.fillOval(col * CELL_SIZE + 5, row * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                    } else if (cell == ReversiGame.PLAYER_TWO) {
                        g.setColor(Color.WHITE);
                        g.fillOval(col * CELL_SIZE + 5, row * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ReversiTournamentUI ui = new ReversiTournamentUI();
            ui.setVisible(true);
        });
    }
}

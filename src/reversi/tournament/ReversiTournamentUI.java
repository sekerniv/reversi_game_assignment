package reversi.tournament;

import reversi.MoveScore;
import reversi.ReversiBot;
import reversi.ReversiGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

public class ReversiTournamentUI extends JFrame {

    // Delay between moves in milliseconds
    private static final int MOVE_DELAY = 300;

    private GamePanel gamePanel;
    private JTextArea leaderboardArea;
    private JButton tournamentStartButton;
    private JButton startMatchButton;
    private JButton pauseButton;
    private JLabel matchInfoLabel;

    // List of contestants and matches for the tournament
    private java.util.List<ReversiTournament.TournamentContestant> contestants;
    private java.util.List<ReversiTournament.Match> matches;
    private int currentMatchIndex = 0;

    private ReversiGame currentGame;
    private ReversiBot currentBot1; // PLAYER_ONE (Black)
    private ReversiBot currentBot2; // PLAYER_TWO (White)
    // The two contestants for the current match: index 0 = Black, index 1 = White
    private ReversiTournament.TournamentContestant[] currentPlayers;

    // Timer that executes moves every MOVE_DELAY milliseconds
    private Timer moveTimer;

    public ReversiTournamentUI() {
        setTitle("Reversi Tournament - Bot vs Bot");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Label displaying match information
        matchInfoLabel = new JLabel("Press 'Start Tournament' to load bots", SwingConstants.CENTER);
        matchInfoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(matchInfoLabel, BorderLayout.NORTH);

        // Game board panel in the center
        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);

        // Leaderboard area on the right side
        leaderboardArea = new JTextArea(20, 20);
        leaderboardArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(leaderboardArea);
        add(scrollPane, BorderLayout.EAST);

        // Button panel at the bottom: "Start Tournament", "Start Match", and "Pause/Resume"
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

        // Action listener for "Start Tournament" button
        tournamentStartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tournamentStartButton.setEnabled(false);
                startTournament();
            }
        });

        // Action listener for "Start Match" button.
        startMatchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startMatchButton.setEnabled(false);
                if (currentGame == null || currentGame.isGameOver()) {
                    prepareNextMatch();
                }
                pauseButton.setEnabled(true);
                startCurrentMatch();
            }
        });

        // Action listener for "Pause/Resume" button
        pauseButton.addActionListener(new ActionListener() {
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

        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    // Loads bots and creates matches using ReversiTournament's static methods.
    private void startTournament() {
        try {
            contestants = ReversiTournament.loadContestants();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading bots.");
            return;
        }
        if (contestants.size() < 2) {
            JOptionPane.showMessageDialog(this, "Not enough bots for tournament!");
            return;
        }
        matches = ReversiTournament.createMatches(contestants);
        currentMatchIndex = 0;
        updateLeaderboard();
        prepareNextMatch();
    }

    // Prepares the next match (resetting the board and game state)
    private void prepareNextMatch() {
        if (currentMatchIndex >= matches.size()) {
            matchInfoLabel.setText("Tournament Finished!");
            startMatchButton.setEnabled(false);
            pauseButton.setEnabled(false);
            return;
        }
        ReversiTournament.Match match = matches.get(currentMatchIndex);
        // Randomly assign order; first bot becomes PLAYER_ONE (Black)
        ReversiTournament.TournamentContestant[] playersOrder =
                new ReversiTournament.TournamentContestant[] { match.contestant1, match.contestant2 };
        Collections.shuffle(java.util.Arrays.asList(playersOrder));
        currentPlayers = playersOrder;
        currentGame = new ReversiGame();
        try {
            currentBot1 = currentPlayers[0].botClass.getConstructor(ReversiGame.class).newInstance(currentGame);
            currentBot2 = currentPlayers[1].botClass.getConstructor(ReversiGame.class).newInstance(currentGame);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        gamePanel.setBoard(currentGame.getBoard());
        gamePanel.repaint();
        String info = "Match " + (currentMatchIndex + 1) + " of " + matches.size() + " - " +
                currentPlayers[0].botClass.getSimpleName() + " (Black) vs " +
                currentPlayers[1].botClass.getSimpleName() + " (White)";
        matchInfoLabel.setText(info);
        startMatchButton.setEnabled(true);
        pauseButton.setText("Pause");
        pauseButton.setEnabled(false);
    }

    // Runs the current match using a Timer; when finished, the final board remains displayed.
    private void startCurrentMatch() {
        moveTimer = new Timer(MOVE_DELAY, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currentGame.isGameOver()) {
                    moveTimer.stop();
                    pauseButton.setEnabled(false);
                    leaderboardArea.append("Match: " + currentPlayers[0].botClass.getSimpleName() +
                            " (Black) vs " + currentPlayers[1].botClass.getSimpleName() + " (White)\n");
                    int winner = currentGame.getWinner();
                    if (winner == ReversiGame.PLAYER_ONE) {
                        currentPlayers[0].score += 1;
                        leaderboardArea.append("Win: " + currentPlayers[0].botClass.getSimpleName() + "\n");
                    } else if (winner == ReversiGame.PLAYER_TWO) {
                        currentPlayers[1].score += 1;
                        leaderboardArea.append("Win: " + currentPlayers[1].botClass.getSimpleName() + "\n");
                    } else {
                        leaderboardArea.append("Draw.\n");
                    }
                    updateLeaderboard();
                    currentMatchIndex++;
                    if (currentMatchIndex < matches.size()) {
                        matchInfoLabel.setText("Match over. Final board displayed. Press 'Start Match' to continue.");
                        startMatchButton.setEnabled(true);
                    } else {
                        matchInfoLabel.setText("Tournament Finished!");
                        startMatchButton.setEnabled(false);
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
                    } else {
                        currentGame.switchToNextPlayablePlayer();
                    }
                }
            }
        });
        moveTimer.start();
        pauseButton.setEnabled(true);
    }

    // Updates the leaderboard area (sorting contestants by descending score)
    private void updateLeaderboard() {
        Collections.sort(contestants, (a, b) -> b.score - a.score);
        leaderboardArea.append("====== Leaderboard ======\n");
        for (ReversiTournament.TournamentContestant tc : contestants) {
            leaderboardArea.append(tc.botClass.getSimpleName() + ": " + tc.score + "\n");
        }
        leaderboardArea.append("=========================\n\n");
    }

    // Inner class for drawing the game board
    private class GamePanel extends JPanel {
        private final int CELL_SIZE = 60;
        private int[][] board;
        public GamePanel() {
            setPreferredSize(new Dimension(8 * CELL_SIZE, 8 * CELL_SIZE));
            board = new int[8][8];
        }
        public void setBoard(int[][] board) {
            this.board = new int[board.length][];
            for (int i = 0; i < board.length; i++) {
                this.board[i] = board[i].clone();
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
                    if (board != null) {
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
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                new ReversiTournamentUI().setVisible(true);
            }
        });
    }
}

package byog.lab6;

//import edu.princeton.cs.algs4.Draw;
//import edu.princeton.cs.algs4.In;
//import edu.princeton.cs.algs4.ST;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    private int width;
    private int height;
    private int round;
    private Random rand;
    private boolean gameOver;
    private boolean playerTurn;
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};
    private boolean correct;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        int seed = Integer.parseInt(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, int seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        //: Initialize random number generator
        rand = new Random(seed);
    }

    public String generateRandomString(int n) {
        //Generate random string of letters of length n
        String s = "";
        for (int i = 0; i < n; i++) {
            s += CHARACTERS[Math.abs(rand.nextInt() % CHARACTERS.length)];
        }
        return s;
    }

    public void drawFrame(String s, int wait) {
        // Take the string and display it in the center of the screen
        // If game is not over, display relevant game information at the top of the screen
        StdDraw.clear(Color.BLACK);
        if (!gameOver) {
            StdDraw.setPenRadius(0.005);
            StdDraw.setPenColor(Color.YELLOW);
            StdDraw.line(0, 38, 40, 38);
            StdDraw.setFont(new Font("Arial", Font.BOLD, 15));
            StdDraw.text(3, 39, "Round: " + Integer.toString(round));
            if (!playerTurn) {
                StdDraw.text(width / 2, 39, "Watch!");
            } else {
                StdDraw.text(width / 2, 39, "Type!");
            }
            if (correct) {
                StdDraw.text(35, 39,
                        ENCOURAGEMENT[Math.abs(rand.nextInt() % ENCOURAGEMENT.length)]);
            }
        }

        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text(width / 2, height / 2, s);
        StdDraw.show();
        StdDraw.pause(wait);
    }

    public void flashSequence(String letters) {
        // Display each character in letters, making sure to blank the screen between letters
        for (char i : letters.toCharArray()) {
            drawFrame(Character.toString(i), 1000);
            StdDraw.clear(Color.BLACK);
            drawFrame("", 500);
        }
    }

    public String solicitNCharsInput(int n) {
        // Read n letters of player input
        String s = "";
        int i = 0;
        while (i < n) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                s += c;
                drawFrame(s, 0);
                i++;
            }
        }
        StdDraw.pause(500);
        return s;
    }

    public void startGame() {
        // Set any relevant variables before the game starts
        round = 1;
        gameOver = false;
        playerTurn = false;
        String randString;
        String userInput;
        correct = false;
        drawFrame("LET'S SEE HOW GOOD YOUR MEMORY IS", 2000);
        drawFrame("GETTING READY", 1000);
        for (int i = 3; i > 0; i--) {
            drawFrame(Integer.toString(i), 1000);
        }
        drawFrame("", 1000);

        // Establish Game loop
        int i = 0;
        while (i < CHARACTERS.length) {
            randString = generateRandomString(round);
            flashSequence(randString);
            playerTurn = true;
            drawFrame("YOUR TURN", 0);
            userInput = solicitNCharsInput(round);
            if (!userInput.equals(randString)) {
                gameOver = true;
                break;
            }
            correct = true;
            drawFrame("CORRECTO AMIGO!", 1000);
            correct = false;
            drawFrame("LET'S MOVE ON", 1000);
            playerTurn = false;
            round++;
            i++;
        }

        if (i == CHARACTERS.length - 1) {
            drawFrame("YOU'RE THE CHAMPION", 0);
        } else {
            drawFrame("GAME OVER!", 1000);
            drawFrame("YOU ARE AT ROUND: " + Integer.toString(round), 0);
        }
    }

}

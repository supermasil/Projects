package byog.Core;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;
import javafx.scene.layout.CornerRadii;

import java.awt.*;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.Random;

// import java.lang.reflect.WildcardType;

public class Game implements Serializable {
    /* Feel free to change the width and height. */
    public static final int WIDTH = 46;
    public static final int HEIGHT = 46;
    private static Random RANDOM;
    // THESE ARE GOING TO BE SERIALIZED
    Position playerPos; // SAVE FOR PLAYER POSITION
    TETile[][] finalWorldFrame; // SAVE FOR THE MAP
    String fileName = "SavedGame.txt";
    TERenderer ter = new TERenderer();
    String life;
    double timer;
    double mapCoverage;
    int level;
    int itemCount;
    boolean hitTheWall;
    boolean doorUnlocked;


    /**
     * Method used for playing a fresh game. The game should start from the main menu.
     */
    public void playWithKeyboard() {
        launchMenu();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (c == 'N') {
                    String seed = enterSeed();
                    finalWorldFrame = playWithInputString("N" + seed + "S"); // BUILD A NEW WORLD
                    playGame();
                } else if (c == 'L') {
                    loadGame();
                    playGame();
                } else if (c == 'Q') {
                    System.out.println("\nQuitting the game");
                    System.exit(0);
                }
            }
        }
    }

    /**
     * Method used for autograding and testing the game code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The game should
     * behave exactly as if the user typed these characters into the game after playing
     * playWithKeyboard. If the string ends in ":q", the same world should be returned as if the
     * string did not end with q. For example "n123sss" and "n123sss:q" should return the same
     * world. However, the behavior is slightly different. After playing with "n123sss:q", the game
     * should save, and thus if we then called playWithInputString with the string "l", we'd expect
     * to get the exact same world back again, since this corresponds to loading the saved game.
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] playWithInputString(String input) {
        // Fill out this method to run the game using the input passed in,
        // and return a 2D tile representation of the world that would have been
        // drawn if the same inputs had been given to playWithKeyboard().

        // CHECK IF THE ARGUMENT IS CORRECT
        if (!((input.contains("N") && input.contains("S") && input.length() > 2)
                || input.contains("L"))) {
            System.out.println("\nThere's something wrong with the args");
            System.exit(0);
        }
        // NEW GAME
        finalWorldFrame = initializeWorld();
        life = "♥♥♥♥♥";
        timer = 120;
        level = 1;
        doorUnlocked = false;
        hitTheWall = false;
        itemCount = 0;

        System.out.println("\n" + life);
        input = input.toUpperCase();
        if (input.substring(0, 1).equals("N")) {
            System.out.println("\nNew game started");
            // CREATE A RANDOM OBJECT FROM THE SEED THE USER SPECIFIED
            int s = input.indexOf('S');
            long seed = Long.parseLong(input.substring(1, s));
            RANDOM = new Random(seed);
            Position initPos = new Position(WIDTH / 2, HEIGHT / 2);
            // START OFF AT THE MIDDLE SO THE CHANCE OF FILLING UP IS HIGHER
            Size initSize = new Size();

            // DRAW THE FIRST ROOM
            while (!checkIfTheresSpace(initPos, initSize)) {
                initPos = new Position();
                initSize = new Size();
            }
            drawARoom(initPos, initSize);
            playerPos = initialPlayerPos(initPos, initSize);

            // DRAW OTHER ROOMS
            while (true) { // KEEP DRAWING UNTIL THERE'S NO POSSIBLE SPACE
                Position p = null;
                for (int i = 10; p == null && i > 0; i--) { //  GIVE IT 10 SHOTS FOR DIFFERENT SIZES
                    Size nextSize = new Size();

                    for (int k = 10; p == null && k > 0; k--) {
                        // GIVE IT 10 SHOTS FOR SAME SIZE, DIFFERENT GATES
                        p = nextRoomLocation(initPos, initSize, nextSize);
                    }

                    if (p != null) {
                        initPos = p; // FOUND A LOCATION FOR THE NEXT ROOM
                        initSize = nextSize; // SET THE SIZE TO THE CURRENT ONE
                    }
                }
                if (p == null) { // GIVE UP
                    break;
                }
            }

            mapCoverage /= (WIDTH * HEIGHT); // CALCULATE MAP COVERAGE
            timer *= mapCoverage; // CALCULATE TIMER


            addGoldenDoor(initPos, initSize); // ADD THE GOLDEN DOOR IN THE LAST ROOM ADDED
            // SET THE PLAYER AT A RANDOM POSITION IN THE ROOM
            movePlayer(input.substring(s + 1)); // MOVE THE PLAYER BASED ON THE STRING AFTER 'S'
        } else if (input.substring(0, 1).equals("L")) { // LOAD THE GAME AND MOVE THE PLAYER
            loadGame();
            movePlayer(input);
        }

        if (input.contains(":Q")) { // IF THIS IS INCLUDED THEN SAVE AND QUIT
            saveGame();
        }

        return finalWorldFrame;
    }

    /**
     * LAUNCH THE MENU FOR PLAYWITHKEYBOARD()
     */
    private void launchMenu() {
        ter.initialize(WIDTH, HEIGHT + 2, Color.DARK_GRAY); // LEAVE 3 SPACES FOR THE HUD ON TOP
        StdDraw.clear(Color.DARK_GRAY);
        StdDraw.picture(WIDTH / 2, HEIGHT / 2 + 1, "flappirates.png", WIDTH, HEIGHT + 2);
        StdDraw.setPenColor(Color.white);
        StdDraw.setFont(new Font("Algerian", Font.BOLD, 50));
        //StdDraw.text(WIDTH / 2, HEIGHT - 5, "THE PIRATES OF CARIBBEAN");
        StdDraw.setFont(new Font("Algerian", Font.BOLD, 30));
        StdDraw.text(WIDTH / 2, 20, "(N)ew Game");
        StdDraw.text(WIDTH / 2, 17, "(L)oad Game");
        StdDraw.text(WIDTH / 2, 14, "(Q)uit");
        StdDraw.show();
    }

    /**
     *
     * @return THE SEED USER ENTERS FOR PLAYWITHKEYBOARD()
     */
    private String enterSeed() {
        String s = "";
        mainLoop:
        while (true) {
            StdDraw.clear(Color.LIGHT_GRAY);
            StdDraw.picture(WIDTH / 2, HEIGHT / 2 + 1, "seed.png", WIDTH, HEIGHT + 2);
            StdDraw.setFont(new Font("Algerian", Font.BOLD, 30));
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.textRight(WIDTH - 2, HEIGHT * 4 / 5, "Exchange seeds for GOLD");
            StdDraw.textRight(WIDTH - 2, HEIGHT * 4 / 5 - 2, "Ending with 's'");
            StdDraw.textRight(WIDTH - 2, HEIGHT * 4 / 5 - 7, s);
            StdDraw.show();
            if (StdDraw.hasNextKeyTyped()) {
                if (s.length() >= 10) { // MAKE SURE IT WON'T OVERFLOW LONG
                    break mainLoop;
                }
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if ((int) c >= 48 && (int) c <= 57 || c == 'S') { // ONLY ACCEPT VALID LETTERS
                    System.out.print(c);
                    if (c == 'S') {
                        StdDraw.clear(Color.LIGHT_GRAY);
                        StdDraw.picture(WIDTH / 2, HEIGHT / 2 + 1, "gamerules.png", WIDTH, HEIGHT + 2);
                        StdDraw.show();
                        StdDraw.pause(5000);
                        break mainLoop; // BREAK OUT OF THE WHOLE LOOP
                    }
                    s += c;
                }
            }

        }
        return (s.isEmpty()) ? "" : s;
        // RETURN NULL IF SEED IS EMPTY, CHECKED IN PLAYWITHKEYBOARD()
    }

    /**
     * SAVE THE GAME TO FILENAME
     */
    private void saveGame() {
        try {
            FileOutputStream file = new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(this);
            System.out.println("\nGame has been saved");
            out.close();
            file.close();
        } catch (IOException e) {
            System.out.println("Can't save game");
        }
    }

    /**
     * LOAD THE GAME PROPERLY
     */
    private void loadGame() {
        try {
            FileInputStream file = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(file);
            Game game = (Game) in.readObject();
            finalWorldFrame = game.finalWorldFrame;
            playerPos = game.playerPos;
            fileName = game.fileName;
            life = game.life;
            timer = game.timer;
            mapCoverage = game.mapCoverage;
            level = game.level;
            itemCount = game.itemCount;
            System.out.println("\nGame loaded");
            in.close();
            file.close();
        } catch (IOException e) {
            System.out.println("There's no saved game to load (IOExp)");
        } catch (ClassNotFoundException e) {
            System.out.println("There's no saved game to load (ClassExp)");
        }
    }

    /**
     * MOVE THE PLAYER AROUND IN REAL TIME AND SAVE WHEN ":Q" IS ENTER
     */
    private void playGame() {
        System.out.println("Playing game");
        ter.initialize(WIDTH + 2, HEIGHT + 2, Color.BLACK); // MAKE SURE THE FONT IS ORIGINAL
        String s = "";
        mainLoop:
        while (true) {
            if (hitTheWall) { // CHANGE COLOR OF HEADER IF HIT THE WALL
                StdDraw.clear(Color.RED);
            } else {
                StdDraw.clear(Color.BLACK);
            }
             // RENDERFRAME DOESN"T HAVE CLEAR ANYMORE
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                s += c; System.out.print(c + " ");
                if (s.contains(":Q")) {
                    saveGame(); //System.exit(0); // EXIT GAME
                    launchMenu();
                    return;
                }
                movePlayer(Character.toString(c)); // JUNK KEYS ARE TAKEN CARE OF
            }

            hUD();
            ter.renderFrame(finalWorldFrame);
            // SHOW INFO OF THE TILE
            timer -= 0.0045;
            if (doorUnlocked) {
                nextLevel();
                return;
            }
            if(gameOver()) {
                return; // END GAME
            }
            StdDraw.show(); // renderFrame doesn't have show() anymore
        }
    }

    /**
     * SHOW INFO OF THE TILE WHERE THE CURSOR IS AT
     */
    private void hUD() {
        int x = (int) StdDraw.mouseX() % WIDTH; // ROUND NUMBER DOWN
        int y = (int) StdDraw.mouseY() % HEIGHT;
        StdDraw.setPenColor(Color.YELLOW);
        StdDraw.textLeft(1, HEIGHT + 1, finalWorldFrame[x][y].description());
        StdDraw.textLeft(5, HEIGHT + 1, "Life: " + life);
        StdDraw.textRight(WIDTH, HEIGHT + 1, "Coverage: " + String.format("%.00f", mapCoverage * 100) + "%");
        StdDraw.textRight(WIDTH - 17, HEIGHT + 1, "Timer: " + String.format("%.00f", timer) + "s");
        StdDraw.textRight(WIDTH - 12, HEIGHT + 1, "Level: " + Integer.toString(level));
        StdDraw.textRight(WIDTH - 7, HEIGHT + 1, "Items: " + Integer.toString(itemCount));
        if (hitTheWall) {
            StdDraw.textRight(WIDTH - 22, HEIGHT + 1, "Ouch!");
        }
    }

    /** Initialize an 2D array with empty tiles
     * @return a grid of initialized TETiles
     */
    private static TETile[][] initializeWorld() {
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[0].length; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        return world;
    }

    /**
     * OBJECT CONTAINING COORDINATES
     */
    private class Position implements Serializable {
        int x;
        int y;
        private Position(int xpos, int ypos) {
            x = xpos;
            y = ypos;
        }
        // RANDOM POSITION
        private Position() {
            x = RandomUtils.uniform(RANDOM, WIDTH); // SOMEWHERE BETWEEN 0 AND WIDTH
            y = RandomUtils.uniform(RANDOM, HEIGHT); // SOMEWHERE BETWEEN 0 AND HEIGHT
        }
    }

    /**
     * OBJECT CONTAINING RANDOM WIDTH AND HEIGHT
     */
    private class Size {
        int width = 3;
        int height = 3;
        private Size() {
            int i = RandomUtils.uniform(RANDOM, 1, 4);
            switch (i) {
                case 1: // Horizontal hallway
                    width = RandomUtils.uniform(RANDOM, 3, 6);
                    break;
                case 2: // Vertical hallway
                    height = RandomUtils.uniform(RANDOM, 3, 6);
                    break;
                case 3: // Normal room
                    width = RandomUtils.uniform(RANDOM, 3, 12);
                    height = RandomUtils.uniform(RANDOM, 3, 12);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * DRAW THE TOP AND BOTTOM ROWS OF A ROOM
     * @param p Location to build the room
     * @param s Size of the room
     */
    private void draw2RowsOfWalls(Position p, Size s) {
        for (int i = 0; i < s.width; i++) {
            finalWorldFrame[p.x + i][p.y] = TETile.colorVariant(Tileset.WALL, 30, 30, 30, RANDOM); // DRAW FIRST ROW OF WALLS
            finalWorldFrame[p.x + i][p.y + s.height - 1] = Tileset.WALL; // DRAW SECOND ROW OF WALLS
        }
    }

    /**
     * BUILD THE MIDDLE ROWS OF THE ROOM
     * @param p Location to build the room
     * @param s Size of the room
     */
    private void drawMiddleRowOfWall(Position p, Size s) {
        finalWorldFrame[p.x][p.y] = TETile.colorVariant(Tileset.WALL, 30, 30, 30, RANDOM); // FIRST TILE
        finalWorldFrame[p.x + s.width - 1][p.y] = TETile.colorVariant(Tileset.WALL, 30, 30, 30, RANDOM); // LAST TILE
        TETile n = TETile.colorVariant(Tileset.FLOOR, 100, 100, 100, RANDOM);
        for (int i = 1; i < s.width - 1; i++) {
            finalWorldFrame[p.x + i][p.y] = n; // FILL IT IN WITH FLOOR
        }
    }

    /**
     * DRAW A COMPLETE ROOM
     * @param p Location to build the room
     * @param s Size of the room
     */
    private void drawARoom(Position p, Size s) {
        mapCoverage += (s.width * s.height);
        draw2RowsOfWalls(p, s);
        for (int i = 1; i < s.height - 1; i++) {
            drawMiddleRowOfWall(new Position(p.x, p.y + i), s);
        }
    }

    /**
     * ADD THE LOCKED_DOOR TO THE DESIRED ROOM
     * @param p Location to build the room
     * @param s Size of the room
     */
    private void addGoldenDoor(Position p, Size s) {
       for (int i = p.x; i < p.x + s.width; i++) {
           for (int k = p.y; k < p.y + s.height; k++) {
               if (!((i == p.x && k == p.y) || (i == p.x && k == p.y + s.height - 1) || (i == p.x + s.width - 1 && k == p.y)
                       || (i == p.x + s.width - 1 && k == p.y + s.height - 1)) && finalWorldFrame[i][k].description().equals("wall")) {
                   finalWorldFrame[i][k] = Tileset.LOCKED_DOOR;
                   return;
               }
           }
       }
    }

    private void addItem (Position p, Size s, TETile item) {
        int numItem = 1;
        for (int i = 0; i < numItem; i++) {
            int x = RandomUtils.uniform(RANDOM, p.x + 1, p.x + s.width - 1);
            int y = RandomUtils.uniform(RANDOM, p.y + 1, p.y + s.height - 1);
            finalWorldFrame[x][y] = Tileset.COIN;
            itemCount++;
        }

    }

    /**
     * CHECK IF THERE'S ENOUGH SPACE TO BUILD A ROOM
     * @param p Location to build the room
     * @param s Size of the room
     * @return TRUE IF THERE'S SPACE
     */
    private boolean checkIfTheresSpace(Position p, Size s) {

        // CHECK IF SPACE IS AVAILABLE FIRST
        if (p.x < 0 || p.y < 0 || p.x + s.width - 1 >= WIDTH || p.y + s.height - 1 >= HEIGHT) {
            return false;
        }

        // CHECK IF ALL THE TILES ARE FREE
        for (int h = p.y; h < p.y + s.height; h++) {
            for (int w = p.x; w < p.x + s.width; w++) {
                if (finalWorldFrame[w][h] != Tileset.NOTHING) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * CHOOSE A TILE ON THE WALLS TO ADD ROOM. EXCLUDE THE 4 CORNERS
     * @param p Location of the the current room
     * @param s size of the current room
     * @return
     */
    private Position randomGate(Position p, Size s) {
        int side = RandomUtils.uniform(RANDOM, 1, 5); // SIDE 1 to 4
        Position randomPos = null;
        switch (side) {
            case 1: // BOTTOM SIDE
                randomPos = new Position(RandomUtils.uniform
                        (RANDOM, p.x + 1, p.x + s.width - 1), p.y);
                break;
            case 2: // TOP SIDE
                randomPos = new Position(RandomUtils.uniform
                        (RANDOM, p.x + 1, p.x + s.width - 1), p.y + s.height - 1);
                break;
            case 3: // LEFT SIDE
                randomPos = new Position(p.x, RandomUtils.uniform
                        (RANDOM, p.y + 1, p.y + s.height - 1));
                break;
            case 4: // RIGHT SIDE
                randomPos = new Position(p.x + s.width - 1, RandomUtils.uniform
                        (RANDOM, p.y + 1, p.y + s.height - 1));
                break;
            default:
                break;
        }
        return randomPos;
    }

    /**
     * @param p Location of the current room
     * @param o Size of the current room
     * @param n Size of the room to be added
     * @return THE LOCATION FOR THE NEXT ROOM
     */
    private Position nextRoomLocation(Position p, Size o, Size n) {
        Position nullPos = null;
        Position g = randomGate(p, o);
        // GATE IS ON THE LEFT OR THE RIGHT SIDE
        if (g.x == p.x || g.x == p.x + o.width - 1) {
            int xPos = -1;
            if (g.x == p.x) { // Left side
                xPos = g.x - n.width;
                if (xPos < 0) { // TERMINATE EARLY
                    return nullPos;
                }
            } else if (g.x == p.x + o.width - 1) { // Right side
                if (xPos + n.width >= WIDTH) {
                    return nullPos; // TERMINATE EARLY
                }
                xPos = g.x + 1;
            }

            int maxYpos = g.y + 2 - n.height;
            for (int i = 10; i > 0; i--) { // Give it 10 tries
                Position p1 = new Position(xPos, RandomUtils.
                        uniform(RANDOM, Math.max(0, maxYpos), g.y));
                if (checkIfTheresSpace(p1, n)) {
                    // FIND A GOOD LOCATION, WILL TAKE CARE OF OUT OF BOUND CASES
                    drawARoom(p1, n);
                    addItem(p1, n, Tileset.COIN); // ADD ITEMS TO THE ROOM
                    finalWorldFrame[g.x][g.y] = Tileset.FLOOR;
                    int gx1 = g.x == p.x ? g.x - 1 : g.x + 1;
                    finalWorldFrame[gx1][g.y] = Tileset.FLOOR;
                    return p1;
                }
            }
            // GATE IS ON THE TOP OR BOTTOM SIDE
        } else if (g.y == p.y || g.y == p.y + o.height - 1) {
            int yPos = -1;
            if (g.y == p.y) { // Bottom
                yPos = g.y - n.height;
                if (yPos < 0) { // TERMINATE EARLY
                    return nullPos;
                }
            } else if (g.y == p.y + o.height - 1) { // Top side

                if (yPos + n.height > HEIGHT) { // TERMINATE EARLY
                    return nullPos;
                }
                yPos = g.y + 1;
            }

            int minXpos = g.x + 2 - n.width;
            for (int i = 10; i > 0; i--) { // GIVE IT 10 SHOTS
                Position p1 = new Position(RandomUtils.
                        uniform(RANDOM, Math.max(0, minXpos), g.x), yPos);
                if (checkIfTheresSpace(p1, n)) {
                    // FIND A GOOD LOCATION, WILL TAKE CARE OF OUT OF BOUND CASES
                    drawARoom(p1, n);
                    addItem(p1, n, Tileset.COIN); // ADD ITEMS TO THE ROOM
                    finalWorldFrame[g.x][g.y] = Tileset.FLOOR;
                    int gy1 = g.y == p.y ? g.y - 1 : g.y + 1;
                    finalWorldFrame[g.x][gy1] = Tileset.FLOOR;
                    //System.out.println(TETile.toString(t)); // SHOULD BE DELETED
                    return p1;
                }
            }
        }
        return nullPos;
    }

    /**
     * @param p Location of the room the player will be in
     * @param s Size of that room
     * @return THE RANDOM POSITION OF THE PLAYER IN THE ROOM CONTAINING GOLDEN DOOR
     */
    private Position initialPlayerPos(Position p, Size s) {
        return new Position(RandomUtils.uniform(RANDOM, p.x + 1, p.x + s.width - 1),
                RandomUtils.uniform(RANDOM, p.y + 1, p.y + s.height - 1));
    }

    /**
     * MOVE THE PLAYER AROUND
     * @param input The seed to move the player
     */
    private void movePlayer(String input) {
        finalWorldFrame[playerPos.x][playerPos.y] = Tileset.CHARACTER; // SET THE PLAYER
        Position newPos = playerPos;
        Position nextPos = playerPos;
        TETile t = null;
        for (char i : input.toCharArray()) {
            switch (i) {
                case 'W':
                    nextPos = new Position(playerPos.x, playerPos.y + 1);
                    t = finalWorldFrame[nextPos.x][nextPos.y];
                    if (!(t.description().equals("wall") || t.description().equals("locked door"))) {
                        newPos = nextPos; hitTheWall = false;
                    } break;
                case 'S':
                    nextPos = new Position(playerPos.x, playerPos.y - 1);
                    t = finalWorldFrame[nextPos.x][nextPos.y];
                    if (!(t.description().equals("wall") || t.description().equals("locked door"))) {
                        newPos = nextPos; hitTheWall = false;
                    } break;
                case 'A':
                    nextPos = new Position(playerPos.x - 1, playerPos.y);
                    t = finalWorldFrame[nextPos.x][nextPos.y];
                    if (!(t.description().equals("wall") || t.description().equals("locked door"))) {
                        newPos = nextPos; hitTheWall = false;
                    } break;
                case 'D':
                    nextPos = new Position(playerPos.x + 1, playerPos.y);
                    t = finalWorldFrame[nextPos.x][nextPos.y];
                    if (!(t.description().equals("wall") || t.description().equals("locked door"))) {
                        newPos = nextPos; hitTheWall = false;
                    } break;
                default:
                    break;
            }

            if (finalWorldFrame[nextPos.x][nextPos.y].description().equals("wall")
                    || (finalWorldFrame[nextPos.x][nextPos.y].description().equals("locked door") && itemCount != 0)) {
                hitTheWall();
            } else if (finalWorldFrame[nextPos.x][nextPos.y].description().equals("locked door") && itemCount == 0) {
                //finalWorldFrame[newPos.x][newPos.y] = Tileset.UNLOCKED_DOOR;
                doorUnlocked = true; return;
            } else if (finalWorldFrame[newPos.x][newPos.y].description().equals("coin")) {
                itemCount--;
            }

            finalWorldFrame[playerPos.x][playerPos.y] = Tileset.FLOOR;
            finalWorldFrame[newPos.x][newPos.y] = Tileset.CHARACTER;
            playerPos = newPos;
        }
    }

    private void hitTheWall() {
        if(life.length() > 0) {
            life = life.substring(1);
            hitTheWall = true;
        }
    }

    private boolean gameOver() {
        if (life.isEmpty() || timer < 0) {
            ter.initialize(WIDTH, HEIGHT + 2, Color.DARK_GRAY);
            StdDraw.clear(Color.DARK_GRAY);
            StdDraw.setPenColor(Color.RED);
            StdDraw.setFont(new Font("Algerian", Font.BOLD, 50));
            StdDraw.picture(WIDTH / 2, HEIGHT / 2 + 1, "gameover.png", WIDTH, HEIGHT + 2);

            StdDraw.show();
            StdDraw.pause(3000);
            launchMenu();
            return true;
        }
        return false;
    }

    private void nextLevel() {
        String newSeed = "";
        String currentLife = life; // SAVE LIFE
        int newLevel = level + 1; // SAVE LEVEL
        Random RANDOM = new Random();
        for (int i = 0; i < 7; i++) {
            newSeed += Math.abs(RandomUtils.uniform(RANDOM, 1, 10));
        }
        System.out.println("\nNew Seed: " + newSeed);
        finalWorldFrame = playWithInputString("N" + newSeed + "S");
        life = currentLife + "♥";
        level = newLevel;
        playGame();
    }
}

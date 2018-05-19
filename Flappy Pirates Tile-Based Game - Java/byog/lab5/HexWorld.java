package byog.lab5;
import javafx.geometry.Pos;
import org.junit.Test;
import static org.junit.Assert.*;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;
    private static int N = 5; // The length of the longest column in the tesslated hex
    public static Random RANDOM = new Random();

    public static class Position {
        int x;
        int y;
        private Position(int xpos, int ypos) {
            x = xpos;
            y = ypos;
        }
    }

    /**
     * Computes the width of row i for a size s hexagon.
     * @param s The size of the hex.
     * @param i The row number where i = 0 is the bottom row.
     * @return
     */
    public static int hexRowWidth(int s, int i) {
        int effectiveI = i;
        if (i >= s) {
            effectiveI = 2 * s - 1 - effectiveI;
        }

        return s + 2 * effectiveI;
    }

    /**
     * Computesrelative x coordinate of the leftmost tile in the ith
     * row of a hexagon, assuming that the bottom row has an x-coordinate
     * of zero. For example, if s = 3, and i = 2, this function
     * returns -2, because the row 2 up from the bottom starts 2 to the left
     * of the start position, e.g.
     *   xxxx
     *  xxxxxx
     * xxxxxxxx
     * xxxxxxxx <-- i = 2, starts 2 spots to the left of the bottom of the hex
     *  xxxxxx
     *   xxxx
     *
     * @param s size of the hexagon
     * @param i row num of the hexagon, where i = 0 is the bottom
     * @return
     */
    public static int hexRowOffset(int s, int i) {
        int effectiveI = i;
        if (i >= s) {
            effectiveI = 2 * s - 1 - effectiveI;
        }
        return -effectiveI;
    }

    /** Calculate the longest row length of a hex
     *
     * @param s length of each side
     * @return longest row length
     */
    public static int longestRow(int s) {
        int length = s;
        for (int i = 1; i < s; i++) {
            length += 2;
        }
        return length;
    }
    /** Adds a row of the same tile.
     * @param world the world to draw on
     * @param p the leftmost position of the row
     * @param width the number of tiles wide to draw
     * @param t the tile to draw
     */
    public static void addRow(TETile[][] world, Position p, int width, TETile t) {
        for (int xi = 0; xi < width; xi += 1) {
            int xCoord = p.x + xi;
            int yCoord = p.y;
            world[xCoord][yCoord] = TETile.colorVariant(t, 32, 32, 32, RANDOM);
        }
    }

    /**
     * Adds a hexagon to the world.
     * @param world the world to draw on
     * @param p the bottom left coordinate of the hexagon
     * @param s the size of the hexagon
     * @param t the tile to draw
     */
    public static void addHexagon(TETile[][] world, Position p, int s, TETile t) {

        if (s < 2) {
            throw new IllegalArgumentException("Hexagon must be at least size 2.");
        }

        // hexagons have 2*s rows. this code iterates up from the bottom row,
        // which we call row 0.
        for (int yi = 0; yi < 2 * s; yi += 1) {
            int thisRowY = p.y + yi;

            int xRowStart = p.x + hexRowOffset(s, yi);
            Position rowStartP = new Position(xRowStart, thisRowY);

            int rowWidth = hexRowWidth(s, yi);

            addRow(world, rowStartP, rowWidth, t);
        }
    }

    /** Initialize the hexagon with empty tiles
     *
     * @param s side length of the hex
     * @return a grid of initialized TETiles
     */
    public static TETile[][] initializeHexagon(int s) {
        int l = longestRow(s);
        TETile[][] world = new TETile[l * N - (N / 2) * (l - s)][s * 2 * N];
        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[0].length; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        return world;
    }

    /** Initialize the TERenderer
     *
     * @param s side length
     * @return a TERenderer
     */
    public static TERenderer initializeTERenderer(int s) {
        int l = longestRow(s);
        TERenderer ter = new TERenderer();
        ter.initialize(l * N - (N / 2) * (l - s), s * 2 * N, Color.black);
        return ter;
    }

    /** Return a random BIOME
     *
     * @param x a number between 0 and 10
     * @return a string
     */
    public static TETile randomBiome (int x) {
        switch (x) {
            case 0:
                return Tileset.PLAYER;
            case 1:
                return Tileset.WALL;
            case 2:
                return Tileset.FLOOR;
            case 3:
                return Tileset.GRASS;
            case 4:
                return Tileset.WATER;
            case 5:
                return Tileset.FLOWER;
            case 6:
                return Tileset.LOCKED_DOOR;
            case 7:
                return Tileset.UNLOCKED_DOOR;
            case 8:
                return Tileset.SAND;
            case 9:
                return Tileset.MOUNTAIN;
            case 10:
                return Tileset.TREE;
        }
        return Tileset.NOTHING;
    }

    /** Draw a column of N hexes
     *
     * @param world a grid of TETile
     * @param p the first position to draw
     * @param s length of each side
     * @param numHex how many hexes
     * @return the initial position
     */
    public static Position drawNHexes(TETile[][] world, Position p, int s, int numHex) {
        for (int i = p.y; i < s * 2 * numHex + p.y; i += s *2) {
            Position p1 = new Position(p.x, i);
            TETile biome = randomBiome(RANDOM.nextInt(11) ); // There are only 11 biomes
            addHexagon(world, p1, s, biome);
        }
        return p;
    }

    /** Tesselate the hexes
     *
     * @param s the length of each side
     */
    public static void tesselatedHex(int s) {
        TERenderer ter = initializeTERenderer(s);
        TETile[][] world = initializeHexagon(s);
        int X = world.length / 2 - (s / 2);
        int T = N;

        // Draw the center column
        drawNHexes(world, new Position(X, 0), s, T);

        Position lastLeft = new Position(X, 0);
        Position lastRight = new Position(X, 0);

        // Draw the side columns
        for (int i = 0; i < N / 2; i++) {
            Position left = new Position(lastLeft.x - s * 2 + 1, lastLeft.y + s);
            Position right = new Position(lastRight.x + s * 2 - 1, lastRight.y + s);
            lastLeft = drawNHexes(world, left, s, T - 1);
            lastRight = drawNHexes(world, right, s, T - 1);
            T -= 1;
        }
        ter.renderFrame(world);
    }

    public static void main(String[] args) {

        N = 9;
        tesselatedHex(5);
    }
}

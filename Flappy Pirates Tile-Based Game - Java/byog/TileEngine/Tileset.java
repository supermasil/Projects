package byog.TileEngine;

import java.awt.Color;

/**
 * Contains constant tile objects, to avoid having to remake the same tiles in different parts of
 * the code.
 *
 * You are free to (and encouraged to) create and add your own tiles to this file. This file will
 * be turned in with the rest of your code.
 *
 * Ex:
 *      world[x][y] = Tileset.FLOOR;
 *
 * The style checker may crash when you try to style check this file due to use of unicode
 * characters. This is OK.
 */

public class Tileset {
    public static final TETile PLAYER
            = new TETile('@', Color.white, Color.black, "player"); // PLAYER
    public static final TETile WALL
            = new TETile('▓', new Color(216, 128, 128), Color.darkGray, // WALL
                                                "wall");
    public static final TETile FLOOR
            = new TETile('·', new Color(128, 192, 128), Color.black, // FLOOR
                                                "floor");
    public static final TETile NOTHING
            = new TETile('~', Color.black, Color.DARK_GRAY, "nothing");
    public static final TETile NOTHING1
            = new TETile('~', Color.black, Color.RED, "nothing");
    public static final TETile GRASS
            = new TETile('"', Color.green, Color.black, "grass"); // GRASS
    public static final TETile WATER
            = new TETile('≈', Color.blue, Color.black, "water"); // WATER
    public static final TETile FLOWER
            = new TETile('❀', Color.magenta, Color.pink, "flower"); // FLOWER
    public static final TETile LOCKED_DOOR
            = new TETile('▢', Color.yellow, Color.yellow, // LOCKED_DOOR
                                                "locked door");
    public static final TETile UNLOCKED_DOOR
            = new TETile('▢', Color.orange, Color.black, // UNLOCKED_DOOR
                                                "unlocked door");
    public static final TETile SAND
            = new TETile('▒', Color.yellow, Color.black, "sand"); // SAND
    public static final TETile MOUNTAIN
            = new TETile('▲', Color.gray, Color.black, "mountain"); // MOUNTAIN
    public static final TETile TREE
            = new TETile('♠', Color.green, Color.black, "tree"); // TREE
    public static final TETile CHARACTER
            = new TETile(' ', Color.BLACK, Color.BLACK, "flappy", "character.png");
    public static final TETile COIN
            = new TETile(' ', Color.BLACK, Color.BLACK, "coin", "coin.png");
}



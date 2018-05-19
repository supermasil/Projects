import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    private final double ULLON = MapServer.ROOT_ULLON;
    private final double LRLON = MapServer.ROOT_LRLON;
    private final double ULLAT = MapServer.ROOT_ULLAT;
    private final double LRLAT = MapServer.ROOT_LRLAT;
    private final double XDIST = LRLON - ULLON;
    private final double YDIST = ULLAT - LRLAT;
    private final int MAXDEPTH = 7;
    LinkedList<Double> lonDPPList = new LinkedList<>();
    Map<String, Object> dummy;
    Map<String, Object> results;
    private boolean corCaseA, corCaseB;

    public Rasterer() {
        // DO ALL THE HARDWORK HERE TO REDUCE RUNTIME
        dummy = new HashMap<>();
        results = new HashMap<>();
        //Create lonDPP linkedList
        for (int d = 0; d <= MAXDEPTH; d++) {
            double dthlonDPP = XDIST / (Math.pow(2, d) * MapServer.TILE_SIZE);
            lonDPPList.add(dthlonDPP);
        }

        // Initiate the dummy
        dummy.put("render_grid", null);
        dummy.put("raster_ul_lon", ULLON);
        dummy.put("raster_ul_lat", ULLAT);
        dummy.put("raster_lr_lon", LRLON);
        dummy.put("raster_lr_lat", LRLAT);
        dummy.put("depth", 0);
        dummy.put("query_success", false);

    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     * The grid of images must obey the following properties, where image in the
     * grid is referred to as a "tile".
     * <ul>
     * <li>The tiles collected must cover the most longitudinal distance per pixel
     * (LonDPP) possible, while still covering less than or equal to the amount of
     * longitudinal distance per pixel in the query box for the user viewport size. </li>
     * <li>Contains all tiles that intersect the query bounding box that fulfill the
     * above condition.</li>
     * <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     * forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {

        double ullon = params.get("ullon");
        double lrlon = params.get("lrlon");
        double ullat = params.get("ullat");
        double lrlat = params.get("lrlat");
        double width = params.get("w"); // Dont need the height; // Dont need the height
        double requestedLonDPP = (lrlon - ullon) / width;
        // HAVE TO DO IT HERE BEFORE THE CHECK BELOW
        int depth;

        // False query check
        if (ullon > lrlon || ullat < lrlat || (ullon < ULLON
                && ullat > ULLAT && lrlon > LRLON && lrlat < LRLAT)) {
            return dummy;
        }

        // IMPORTANT: HAVE TO CALCULATE BEFORE THE ASSIGNMENTS BELOW
        if (ullon < ULLON) {
            ullon = ULLON;
        }
        if (ullat > ULLAT) {
            ullat = ULLAT;
        }
        if (lrlon > LRLON) {
            lrlon = LRLON;
        } // Set up flag to know when to subtract 1 in renderGrid
        if (lrlat < LRLAT) {
            lrlat = LRLAT;
        } // Set up flag to know when to subtract 1 in renderGrid

        depth = depthCal(requestedLonDPP);
        rastered(ullon, lrlon, ullat, lrlat, depth);
        return results;
    }

    /**
     * Takes in the coordinates, depth and put the calculated info into the result map
     * which will be used to return in getMapRaster
     *
     * @param ullon
     * @param lrlon
     * @param ullat
     * @param lrlat
     * @param depth
     */
    private void rastered(double ullon, double lrlon, double ullat, double lrlat, int depth) {

        double xDist = XDIST / Math.pow(2, depth); // xLength of every tile
        double yDist = YDIST / Math.pow(2, depth); // yLength of every tile


        double b = ((lrlon - ULLON) / xDist);
        double d = ((ULLAT - lrlat) / yDist);

        // IMPORTANT: HAVE TO CHECK IF THE LRLON AND LRLAT ARE RIGHT AT THE BORDER LINE
        // SUBTRACT ONE IF THEY ARE
        // ULLON AND ULLAT DON'T MATTER
        int ullonCoor = (int) ((ullon - ULLON) / xDist);
        int lrlonCoor = b - (int) b == 0 ? (int) b - 1 : (int) b;
        int ullatCoor = (int) ((ULLAT - ullat) / yDist);
        int lrlatCoor = d - (int) d == 0 ? (int) d - 1 : (int) d;
/*
        System.out.println(ullonCoor);
        System.out.println(lrlonCoor);
        System.out.println(ullatCoor);
        System.out.println(lrlatCoor);*/

        String[][] grid = renderGrid(ullonCoor, lrlonCoor, ullatCoor, lrlatCoor, depth);

        results.put("render_grid", grid);
        results.put("raster_ul_lon", ULLON + xDist * ullonCoor);
        results.put("raster_ul_lat", ULLAT - yDist * ullatCoor);
        results.put("raster_lr_lon", ULLON + xDist * (lrlonCoor + 1));
        results.put("raster_lr_lat", ULLAT - yDist * (lrlatCoor + 1));
        results.put("depth", depth);
        results.put("query_success", true);
    }

    /**
     * @param ullonCoor
     * @param lrlonCoor
     * @param ullatCoor
     * @param lrlatCoor
     * @param depth
     * @return a 2D array containing names of the images needed to display in front end
     */
    private String[][] renderGrid(int ullonCoor, int lrlonCoor,
                                  int ullatCoor, int lrlatCoor, int depth) {

        String[][] result = new String[lrlatCoor - ullatCoor + 1][lrlonCoor - ullonCoor + 1];

        String s = "d" + depth + "_"; // Init string for depth

        int ullon1 = ullonCoor;
        for (int i = 0; i <= lrlonCoor - ullonCoor; i++) {
            // Start at the upper right intersecting tile
            String x = s + "x" + ullon1 + "_"; // String for x coor
            int ullat1 = ullatCoor; // Reset it
            for (int k = 0; k <= lrlatCoor - ullatCoor; k++) {
                String y = x + "y" + ullat1 + ".png"; // String for y coor
                //System.out.println(y);
                result[k][i] = y; // k is row, i is col
                ullat1++;
            }
            ullon1++;
        }
        return result;
    }

    /**
     * @param requestedLonDPP
     * @return the depth needed for the best display in the front end
     */
    private int depthCal(double requestedLonDPP) {
        int d = 0;
        for (double i : lonDPPList) {
            if (i < requestedLonDPP) {
                return d;
            }
            d++;
        }
        return MAXDEPTH;
    }
}

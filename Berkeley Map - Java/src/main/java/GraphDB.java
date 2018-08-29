import org.xml.sax.SAXException;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     *
     * @param dbPath Path to the XML file to be parsed.
     */

    // Use Node and Way as value for easier removal
    final Map<Long, Node> nodes = new TreeMap<>();
    final Map<Long, Way> ways = new TreeMap<>();
    final Map<Long, HashMap<Long, Long>> nodeToNode = new TreeMap<>();
    final Trie trieTree = new Trie();
    final Map<String, LinkedList<Node>> cleanedLocation = new HashMap<>();
    //final List<String> a = new LinkedList<>();

    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    void addNode(Node n) {
        this.nodes.put(n.id, n);
    }

    Node retrieveNode(long id) {
        return nodes.get(id);
    }

    void removeNode(long id) {
        this.nodes.remove(id);
    } // This will remove

    void addWay(Way w) {
        this.ways.put(w.id, w);
    }

    Way retrieveWay(long id) {
        return ways.get(id);
    }

    void addNodetoNode(Node n) {
        nodeToNode.put(n.id, new HashMap<>());
    }

    HashMap<Long, Long> retrieveNodeToNode(long id) {
        return nodeToNode.get(id);
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        LinkedList<Long> save = new LinkedList();
        for (long i : nodeToNode.keySet()) {
            if (nodeToNode.get(i).isEmpty()) {
                removeNode(i);
                save.add(i); // Can't remove here as it will cause the loop to be corrupted
            }
        }

        for (long i : save) {
            nodeToNode.remove(i);
        }
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     *
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
        return nodes.keySet();
    }

    /**
     * Returns ids of all vertices adjacent to v.
     *
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        LinkedList<Long> iter = new LinkedList<>();
        if (nodeToNode.containsKey(v)) {
            for (HashMap.Entry<Long, Long> i : nodeToNode.get(v).entrySet()) {
                iter.add(i.getKey());
            }
        }
        return iter;
    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     *
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double dphi = Math.toRadians(lat(w) - lat(v));
        double dlambda = Math.toRadians(lon(w) - lon(v));

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

<<<<<<< HEAD
    double lonLatDistance(double lon1, double lat1, double lon2, double lat2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dphi = Math.toRadians(lat2 - lat1);
        double dlambda = Math.toRadians(lon2 - lon1);
=======
    static double distance(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);
>>>>>>> 2cfc2b1dac98fb8b9d02a37db8a6a061ba4c02f5

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     *
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
<<<<<<< HEAD
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double lambda1 = Math.toRadians(lon(v));
        double lambda2 = Math.toRadians(lon(w));
=======
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);
>>>>>>> 2cfc2b1dac98fb8b9d02a37db8a6a061ba4c02f5

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     *
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {

        /*double multiply = lon * lat;
        double minDistance = Double.MAX_VALUE;

        for(Map.Entry<Double, Node> n : nodesByLonLat.entrySet()) {
           if()
        }*/
        double minDistance = Long.MAX_VALUE;
        long id = 0;
        for (long i : nodes.keySet()) {
            double distance = lonLatDistance(lon, lat, nodes.get(i).lon, nodes.get(i).lat);
            if (distance < minDistance) {
                minDistance = distance;
                id = nodes.get(i).id;
            }
        }
        return id;
    }

    /**
     * Gets the longitude of a vertex.
     *
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {
        return nodes.containsKey(v) ? nodes.get(v).lon : -Double.MAX_VALUE;
    }

    /**
     * Gets the latitude of a vertex.
     *
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {
        return nodes.containsKey(v) ? nodes.get(v).lat : -Double.MAX_VALUE;
    }

    static class Node {
        long id;
        double lat;
        double lon;
        Map<String, String> extraInfo;

        Node(String id, String lat, String lon) {
            this.id = Long.parseLong(id);
            this.lat = Double.parseDouble(lat);
            this.lon = Double.parseDouble(lon);
            this.extraInfo = new HashMap<>();
        }
    }

    static class Way {
        long id;
        //Set<Long> ref;
        Map<String, String> extraInfo;

        Way(String id) {
            this.id = (long) Double.parseDouble(id);
            //ref = new HashSet<>();
            extraInfo = new HashMap<>();
        }
    }

    void addTrie(String s) {
        s = cleanString(s);
        trieTree.insert(s);


    }

    void addCleanedName(String s, Node n) {
        s = cleanString(s);
        if (!cleanedLocation.containsKey(s)) {
            LinkedList<Node> l = new LinkedList<>();
            l.add(n);
            cleanedLocation.put(s, l);
        } else {
            cleanedLocation.get(s).add(n);
        }
    }

    /**
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     *
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        return this.trieTree.nodesStartsWith(prefix);
    }

    /**
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     *
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" : Number, The latitude of the node. <br>
     * "lon" : Number, The longitude of the node. <br>
     * "name" : String, The actual name of the node. <br>
     * "id" : Number, The id of the node. <br>
     */
    public List<Map<String, Object>> getLocations(String locationName) {

        List<Map<String, Object>> result = new ArrayList<>();
        locationName = cleanString(locationName);
        if (cleanedLocation.containsKey(locationName)) {
            for (Node n : cleanedLocation.get(locationName)) {
                Map<String, Object> m = new HashMap<>();
                m.put("lat", n.lat);
                m.put("lon", n.lon);
                m.put("name", n.extraInfo.get("name"));
                m.put("id", n.id);
                System.out.println(m);
                result.add(m);
            }
        }
        return result;
    }


    private class Trie {

        class TrieNode {
            char c;
            HashMap<Character, TrieNode> children = new HashMap<Character, TrieNode>();
            boolean endOfAWord;

            TrieNode() {
            }

            TrieNode(char c) {
                this.c = c;
            }
        }

        private TrieNode root;

        Trie() {
            root = new TrieNode();
        }

        // Inserts a word into the trie.
        public void insert(String word) {
            TrieNode t = root;
            for (char c : word.toCharArray()) {

                if (t.children.containsKey(c)) {
                    t = t.children.get(c);
                } else {
                    t.children.put(c, new TrieNode(c));
                    t = t.children.get(c);
                }
            }

            t.endOfAWord = true; // MARK END OF A WORD, DOESN'T NEED TO BE A LEAF
        }

        public List<String> nodesStartsWith(String prefix) {
            prefix = cleanString(prefix);
            TrieNode t = root;

            if (prefix.isEmpty()) {
                return new ArrayList<>();
            }
            // Go down to the trienode of the end char of the prefix
            for (char c : prefix.toCharArray()) {
                if (t.children.containsKey(c)) {
                    t = t.children.get(c);
                } else {
                    return new ArrayList<>();
                }
            }
            List<String> l = stringBuilding(t, prefix);
            List<String> result = new ArrayList<>();
            int i = 0;
            for (String s : l) {
                //System.out.println(s);
                if (cleanedLocation.containsKey(s)) {
                    for (Node n : cleanedLocation.get(s)) {
                        if (!result.contains(n.extraInfo.get("name"))) { // MAKE SURE IT WON"T DUPLICATE
                            result.add(n.extraInfo.get("name"));
                        }
                    }
                }
            }

            return result;
        }

        public List<String> stringBuilding(TrieNode t, String prefix) {
            List<String> recursiveString = new LinkedList<>();
            if (t.endOfAWord) {
                recursiveString.add(prefix);
                // IMPORTANT: DON'T RETURN HERE JUST IN CASE THERE ARE LONGER WORDS BEHIND
            }

            for (TrieNode in : t.children.values()) {
                String s = Character.toString(in.c);
                for (String q : stringBuilding(in, prefix + s)) {
                    recursiveString.add(q);
                }
            }
            Collections.sort(recursiveString);
            return recursiveString;
        }
    }
}

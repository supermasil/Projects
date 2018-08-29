import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.PriorityQueue;
import java.util.List;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    private static GraphDB graph;

    /**
     * Return a List of longs representing the shortest path from the node
     * closest to a start location and the node closest to the destination
     * location.
     *
     * @param g       The graph to use.
     * @param stlon   The longitude of the start location.
     * @param stlat   The latitude of the start location.
     * @param destlon The longitude of the destination location.
     * @param destlat The latitude of the destination location.
     * @return A list of node id's in the order visited on the shortest path.
     */
    public static List<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                          double destlon, double destlat) {
        PriorityQueue<SearchNode> fringe = new PriorityQueue<>(SearchNode.getPriorityComparator());
        HashMap<Long, SearchNode> optimization = new HashMap<>();
        long target = g.closest(destlon, destlat);
        long source = g.closest(stlon, stlat);
        SearchNode current = new SearchNode(g, source, target, null);
        boolean isGoal = current.isGoal();

        fringe.add(current);
        int step = 0;
        while (!current.isGoal() && !fringe.isEmpty()) { // To avoid popping off empty PQ
            current = fringe.poll();
            optimization.put(current.id, current);
            for (long s : current.neighbors()) {
                SearchNode newNode = new SearchNode(g, s, target, current);
                if (!optimization.containsKey(s)) {
                    fringe.add(newNode);
                    optimization.put(s, newNode);
                } else {
                    SearchNode z = optimization.get(s); // retrieve the node
                    if (z.gn > newNode.gn) {
                        // Update necessary information
                       /* z.gn = newNode.gn;
                        z.prev = current;
                        z.priority = z.gn + z.estimatedDistanceToGoal();*/
                        fringe.remove(optimization.get(s));
                        fringe.add(newNode); // This is ok too, remove then reinsert
                    }
                }
            }
            isGoal = current.isGoal(); // To make sure the goal is achievable
        }
        return isGoal ? current.solutions() : new ArrayList<>();
    }

    /**
     * Create the list of directions corresponding to a route on the graph.
     *
     * @param g     The graph to use.
     * @param route The route to translate into directions. Each element
     *              corresponds to a node from the graph in the route.
     * @return A list of NavigatiionDirection objects corresponding to the input
     * route.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        if (route.size() < 2) {
            return null;
        }

        List<NavigationDirection> result = new ArrayList<>();

        // The first two nodes
        long previousNode = route.get(1);
        long previousWay = g.retrieveNodeToNode(route.get(0)).get(route.get(1));
        double cumDistance = g.distance(route.get(0), route.get(1));
        double previousBearing = g.bearing(route.get(0), route.get(1));
        String navi = "Start on ";

        for (long s : route) {
            if (s != route.get(0) && s != route.get(1)) { // Exclude the first 2 nodes

                double currentBearing = g.bearing(previousNode, s);
                double bearing = currentBearing - previousBearing;

                long currentWay = g.retrieveNodeToNode(previousNode).get(s);
                String currentWayName = g.retrieveWay(currentWay).extraInfo.get("name");
                currentWayName = currentWayName == null ? "" : currentWayName;
                String previousWayName = g.retrieveWay(previousWay).extraInfo.get("name");
                previousWayName = previousWayName == null ? "" : previousWayName;

                // Different way or last node
                if (!currentWayName.equals(previousWayName) || s == route.get(route.size() - 1)) {
                    navi += previousWayName;

                    cumDistance = s == route.get(route.size() - 1)
                            ? cumDistance + g.distance(previousNode, s)
                            : cumDistance; // Add the distance for the last node

                    navi += " and continue for " + cumDistance + " miles.";
                    result.add(NavigationDirection.fromString(navi));
                    //nodeForBearing = previousNode;
                    //System.out.println(NavigationDirection.fromString(navi));
                    navi = "";
                    cumDistance = g.distance(previousNode, s);
                } else {
                    cumDistance += g.distance(previousNode, s);
                }

                previousBearing = currentBearing;
                previousNode = s;
                previousWay = currentWay;

                if (navi.equals("")) { // New direction
                    if (bearing >= -15 && bearing <= 15) {
                        navi += "Go straight on ";
                    } else if (bearing < -15 && bearing >= -30) {
                        navi += "Slight left on ";
                    } else if (bearing > 15 && bearing <= 30) {
                        navi += "Slight right on ";
                    } else if (bearing < -30 && bearing >= -100) {
                        navi += "Turn left on ";
                    } else if (bearing > 30 && bearing <= 100) {
                        navi += "Turn right on ";
                    } else if (bearing < -100) {
                        navi += "Sharp left on ";
                    } else if (bearing > 100) {
                        navi += "Sharp right on ";
                    }
                }
            }
        }
/*        for (NavigationDirection i : result) {
            System.out.println(i.toString());
        }*/
        return result;
    }


    private static class SearchNode implements Comparable<SearchNode> {
        long id;
        long targetId;
        double priority;
        double gn; // best known distance from source
        SearchNode prev;
        GraphDB g;

        SearchNode(GraphDB graph, long source, long target, SearchNode previous) {
            g = graph;
            id = source;
            targetId = target;
            prev = previous;
            gn = previous == null ? 0 : prev.gn
                    + g.distance(prev.id, id); // Best known distance so far
            priority = gn + estimatedDistanceToGoal();
        }

        private static Comparator<SearchNode> getPriorityComparator() {
            return new PriortyComparator();
        }

        double estimatedDistanceToGoal() {
            return gn + g.distance(id, targetId);
        }

        Iterable<Long> neighbors() {
            return g.adjacent(id);
        }

        public int compareTo(SearchNode s) {
            if (this.priority - s.priority == 0) {
                return 0;
            } else if (this.priority - s.priority < 0) {
                return -1;
            } else {
                return 1;
            }
        }

        boolean isGoal() {
            return id == targetId;
        }

        List<Long> solutions() {
            List<Long> l = new ArrayList<>();
            SearchNode s = this;
            while (s != null) {
                //System.out.println(s.Id + " " +l.size());
                l.add(s.id);
                s = s.prev;
            }
            Collections.reverse(l);
            return l;
        }

        private static class PriortyComparator implements Comparator<SearchNode> {
            public int compare(SearchNode a, SearchNode b) {
                return a.compareTo(b);
            }
        }

    }

    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /**
         * Integer constants representing directions.
         */
        public static final int START = 0;
        public static final int STRAIGHT = 1;
        public static final int SLIGHT_LEFT = 2;
        public static final int SLIGHT_RIGHT = 3;
        public static final int RIGHT = 4;
        public static final int LEFT = 5;
        public static final int SHARP_LEFT = 6;
        public static final int SHARP_RIGHT = 7;

        /**
         * Number of directions supported.
         */
        public static final int NUM_DIRECTIONS = 8;

        /**
         * A mapping of integer values to directions.
         */
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        /**
         * Default name for an unknown way.
         */
        public static final String UNKNOWN_ROAD = "unknown road";

        /** Static initializer. */
        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /**
         * The direction a given NavigationDirection represents.
         */
        int direction;
        /**
         * The name of the way I represent.
         */
        String way;
        /**
         * The distance along this way I represent.
         */
        double distance;

        /**
         * Create a default, anonymous NavigationDirection.
         */
        public NavigationDirection() {
            this.direction = STRAIGHT;
            this.way = UNKNOWN_ROAD;
            this.distance = 0.0;
        }

        /**
         * Takes the string representation of a navigation direction and converts it into
         * a Navigation Direction object.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // not a valid nd
                return null;
            }
        }

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                        && way.equals(((NavigationDirection) o).way)
                        && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}

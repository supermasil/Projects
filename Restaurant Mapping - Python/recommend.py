"""A Yelp-powered Restaurant Recommendation Program"""

from abstractions import *
from data import ALL_RESTAURANTS, CATEGORIES, USER_FILES, load_user_file
from ucb import main, trace, interact
from utils import distance, mean, zip, enumerate, sample
from visualize import draw_map

##################################
# Phase 2: Unsupervised Learning #
##################################


def find_closest(location, centroids):
    #Location: A pair containing latutude and longitude
    #Centroids: A list of the centers of the clusters
    #Cluster: A list of restaurants group around a centroid 
    """Return the centroid in centroids that is closest to location.
    If multiple centroids are equally close, return the first one.

    >>> find_closest([3.0, 4.0], [[0.0, 0.0], [2.0, 3.0], [4.0, 3.0], [5.0, 5.0]])
    [2.0, 3.0]
    """
    # BEGIN Question 3
    return min(centroids, key = lambda x: distance(location, x))
    # END Question 3


def group_by_first(pairs):
    """Return a list of pairs that relates each unique key in the [key, value]
    pairs to a list of all values that appear paired with that key.

    Arguments:
    pairs -- a sequence of pairs

    >>> example = [ [1, 2], [3, 2], [2, 4], [1, 3], [3, 1], [1, 2] ]
    >>> group_by_first(example)
    [[2, 3, 2], [2, 1], [4]]
    """
    keys = []
    for key, _ in pairs:
        if key not in keys:
            keys.append(key)
    # keys = [1, 2, 3]
    return [[y for x, y in pairs if x == key] for key in keys]
    # Compare x in pairs with key in Keys and return the y's associated with x


def group_by_centroid(restaurants, centroids):
    """Return a list of clusters, where each cluster contains all restaurants
    nearest to a corresponding centroid in centroids. Each item in
    restaurants should appear once in the result, along with the other
    restaurants closest to the same centroid.
    """
    # BEGIN Question 4
    s = [[find_closest(restaurant_location(i), centroids), i] for i in restaurants]
    # A list of [closet centroid to restaurant, restaurant]
    return group_by_first(s)
    # A list of restaurants closet to every centroid
    # END Question 4


def find_centroid(cluster):
    #Cluster is a list of restaurants
    """Return the centroid of the locations of the restaurants in cluster."""
    # BEGIN Question 5
    locations = [restaurant_location(i) for i in cluster]
    #[[latitude, longitude], [latitude, longitude],...]
    latitude = [x for x, y in locations]
    longitude = [y for x, y in locations]
    return [mean(latitude), mean(longitude)]
    # END Question 5


def k_means(restaurants, k, max_updates=100):
    """Use k-means to group restaurants by location into k clusters."""
    assert len(restaurants) >= k, 'Not enough restaurants to cluster'
    old_centroids, n = [], 0
    # Select initial centroids randomly by choosing k different restaurants
    centroids = [restaurant_location(r) for r in sample(restaurants, k)]
    
    while old_centroids != centroids and n < max_updates:
        old_centroids = centroids
        # BEGIN Question 6
        clusters = group_by_centroid(restaurants, centroids)
        centroids = [find_centroid(i) for i in clusters]
        #Create new centroids
        # END Question 6
        n += 1
    return centroids


################################
# Phase 3: Supervised Learning #
################################


def find_predictor(user, restaurants, feature_fn):
    """Return a rating predictor (a function from restaurants to ratings),
    for a user by performing least-squares linear regression using feature_fn
    on the items in restaurants. Also, return the R^2 value of this model.

    Arguments:
    user -- A user
    restaurants -- A sequence of restaurants
    feature_fn -- A function that takes a restaurant and returns a number
    """
    reviews_by_user = {review_restaurant_name(review): review_rating(review)
                       for review in user_reviews(user).values()}
    # A dict of {R_name: rating]
    xs = [feature_fn(r) for r in restaurants]
    ys = [reviews_by_user[restaurant_name(r)] for r in restaurants]

    # BEGIN Question 7
    xx2, yy2 = [(i - mean(xs))**2 for i in xs], [(k - mean(ys))**2 for k in ys]
    xx, yy = [(i - mean(xs)) for i in xs], [(k - mean(ys)) for k in ys]
    xy = [xx[i]*yy[i] for i in range(0, len(xx))]

    """Sxx = Σi (xi - mean(x))2
       Syy = Σi (yi - mean(y))2
       Sxy = Σi (xi - mean(x)) (yi - mean(y))
       b = Sxy / Sxx
       a = mean(y) - b * mean(x)
       R2 = Sxy2 / (Sxx Syy)"""

    b, a, r_squared = sum(xy) / sum(xx2), mean(ys) - (sum(xy) / sum(xx2)) * mean(xs), \
                      sum(xy)**2 / (sum(xx2) * sum(yy2)) 
                       
    # END Question 7
    def predictor(restaurant):
        return b * feature_fn(restaurant) + a
    # This function will receive an restaurant and return the predicted rating

    return predictor, r_squared



def best_predictor(user, restaurants, feature_fns):
    """Find the feature within feature_fns that gives the highest R^2 value
    for predicting ratings by the user; return a predictor using that feature.

    Arguments:
    user -- A user
    restaurants -- A list of restaurants
    feature_fns -- A sequence of functions that each takes a restaurant
    """
    reviewed = user_reviewed_restaurants(user, restaurants)
    # A list of restaurants reviewed by the user

    # BEGIN Question 8
    d = {x: y for x, y in [find_predictor(user, reviewed, i) for i in feature_fns]}
    # Create a dictionary straight from a lisbt 
    # Alternative way: d = dict([find_predictor(user, reviewed, i) for i in feature_fns])

    return max(d, key = lambda x : d[x])
    # The best predictor function with highest r_square
 
    """ Alternative way with tuple
    d = [find_predictor(user, reviewed, i) for i in feature_fns]
    return max(d, key = lambda x: x[1])[0] # Max will return the whole smaller list [a, b] but we only need a"""
    # END Question 8


def rate_all(user, restaurants, feature_fns):
    """Return the predicted ratings of restaurants by user using the best
    predictor based on a function from feature_fns.

    Arguments:
    user -- A user
    restaurants -- A list of restaurants
    feature_fns -- A sequence of feature functions
    """
    predictor = best_predictor(user, ALL_RESTAURANTS, feature_fns)
    # The best predictor funcion with highest R_square
    reviewed = user_reviewed_restaurants(user, restaurants)
    # A list of restaurants reviewed by the users
    # BEGIN Question 9
    """
    Alternative way:
    d = {}
    for i in restaurants:
        if i in reviewed:
            d.update({restaurant_name(i): user_rating(user, restaurant_name(i))})
            # Update known restaurant
        else:
            d.update({restaurant_name(i): predictor(i)})
            # Update unknown restaurant
    """
    rated =  {restaurant_name(i): user_rating(user, restaurant_name(i)) for i in reviewed}
    unrated = {restaurant_name(i): predictor(i) for i in restaurants if i not in reviewed}
    rated.update(unrated) # THIS FUNCTION DOESN'T RETURN ANYTHING SO WE CAN'T RETURN IT
    
    return rated

    #return d
    # END Question 9


def search(query, restaurants):
    """Return each restaurant in restaurants that has query as a category.

    Arguments:
    query -- A string
    restaurants -- A sequence of restaurants
    """
    # BEGIN Question 10
    return [i for i in restaurants if query in restaurant_categories(i)]
    # A list of restaurant that has query as a category
    # END Question 10


def feature_set():
    """Return a sequence of feature functions."""
    return [lambda r: mean(restaurant_ratings(r)),
            restaurant_price,
            lambda r: len(restaurant_ratings(r)),
            lambda r: restaurant_location(r)[0],
            lambda r: restaurant_location(r)[1]]


@main
def main(*args):
    import argparse
    parser = argparse.ArgumentParser(
        description='Run Recommendations',
        formatter_class=argparse.RawTextHelpFormatter
    )
    parser.add_argument('-u', '--user', type=str, choices=USER_FILES,
                        default='test_user',
                        metavar='USER',
                        help='user file, e.g.\n' +
                        '{{{}}}'.format(','.join(sample(USER_FILES, 3))))
    parser.add_argument('-k', '--k', type=int, help='for k-means')
    parser.add_argument('-q', '--query', choices=CATEGORIES,
                        metavar='QUERY',
                        help='search for restaurants by category e.g.\n'
                        '{{{}}}'.format(','.join(sample(CATEGORIES, 3))))
    parser.add_argument('-p', '--predict', action='store_true',
                        help='predict ratings for all restaurants')
    parser.add_argument('-r', '--restaurants', action='store_true',
                        help='outputs a list of restaurant names')
    args = parser.parse_args()

    # Output a list of restaurant names
    if args.restaurants:
        print('Restaurant names:')
        for restaurant in sorted(ALL_RESTAURANTS, key=restaurant_name):
            print(repr(restaurant_name(restaurant)))
        exit(0)

    # Select restaurants using a category query
    if args.query:
        restaurants = search(args.query, ALL_RESTAURANTS)
    else:
        restaurants = ALL_RESTAURANTS

    # Load a user
    assert args.user, 'A --user is required to draw a map'
    user = load_user_file('{}.dat'.format(args.user))

    # Collect ratings
    if args.predict:
        ratings = rate_all(user, restaurants, feature_set())
    else:
        restaurants = user_reviewed_restaurants(user, restaurants)
        names = [restaurant_name(r) for r in restaurants]
        ratings = {name: user_rating(user, name) for name in names}

    # Draw the visualization
    if args.k:
        centroids = k_means(restaurants, min(args.k, len(restaurants)))
    else:
        centroids = [restaurant_location(r) for r in restaurants]
    draw_map(centroids, restaurants, ratings)
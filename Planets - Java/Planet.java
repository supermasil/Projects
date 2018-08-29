/**
 *  Create a Planet instance to do the calculations
 */

public class Planet {
	public double xxPos; // Position of the planet on X axis
	public double yyPos; // Position of the planet on Y axis
	public double xxVel; // Velocity of the planet on X axis
	public double yyVel; // Velocity of the planet on Y axis
	public double mass;  // Mass of the planet on X axis
	public String imgFileName;
	private static final double G = 6.67e-11;

	/**
     *  Constructor of Planet class
     *
     *  @param  xP    Received X Position
     *  @param  yP    Received Y Position
     *  @param  XV    Received X Veloctity
     *  @param  yV    Received Y Velocity
     *  @param  m     Received mass
     *  @param  image Received Planet's image
     */
	public Planet(double xP, double yP, double xV, double yV, double m, String img) {
		xxPos = xP;
		yyPos = yP;
		xxVel = xV;
		yyVel = yV;
		mass = m;
		imgFileName = img;
	}

	/**
     *  Contructor of Planet class, create a copy out of p
     *
     *  @param  p    A Planet instance
     */
	public Planet(Planet p) {
		xxPos = p.xxPos;
		yyPos = p.yyPos;
		xxVel = p.xxVel;
		yyVel = p.yyVel;
		mass  = p.mass;
		imgFileName = p.imgFileName;
	}

	/**
     *  Calculate the distance between two planets
     *
     *  @param  p    A Planet instance
     */
	public double calcDistance(Planet p) {
		return Math.sqrt(Math.pow((p.xxPos - xxPos), 2) + Math.pow((p.yyPos - yyPos), 2));
	}

	/**
     *  Calculate the force exerted between the two planets
     *
     *  @param  p    A Planet instance
     */
	public double calcForceExertedBy(Planet p) {
		return G * p.mass * mass / Math.pow(calcDistance(p),2);

	}

	/**
     *  Calculate the force exerted on x axis between the two planets
     *
     *  @param  p    A Planet instance
     */
	public double calcForceExertedByX (Planet p) {
		return calcForceExertedBy(p) * (p.xxPos - xxPos) / calcDistance(p);
	}

	/**
     *  Calculate the force exerted on y axis between the two planets
     *
     *  @param  p    A Planet instance
     */
	public double calcForceExertedByY (Planet p) {
		return calcForceExertedBy(p) * (p.yyPos - yyPos) / calcDistance(p);
	}

	/**
     *  Calculate the net force exerted on x axis between the two planets
     *
     *  @param  p    A array of Planet instances
     */
	public double calcNetForceExertedByX (Planet[] p) {
		double totalForceExertedByX = 0;
		for (Planet i : p) {
			if (!this.equals(i)) {
				totalForceExertedByX += calcForceExertedByX(i);
			}
		}

		return totalForceExertedByX;
	}

	/**
     *  Calculate the net force exerted on y axis between the two planets
     *
     *  @param  p    A array of Planet instances
     */
	public double calcNetForceExertedByY (Planet[] p) {
		double totalForceExertedByY = 0;
		for (Planet i : p) {
			if (!this.equals(i)) {
				totalForceExertedByY += calcForceExertedByY(i);
			}
		}

		return totalForceExertedByY;
	}

	/**
     *  Update the current positions and velocities
     *
     *  @param  dt    Time period
     *  @param  fX    Net force exerted on X axis
     *  @param  fY    Net force exerted on Y axis
     */
	public void update (double dt, double fX, double fY) {
		xxVel = xxVel + dt * fX / mass;
		yyVel = yyVel + dt * fY / mass;
		xxPos = xxPos + dt * xxVel;
		yyPos = yyPos + dt * yyVel;
	}

	/**
     *  Draw the planet at its position
     */
	public void draw() {
		StdDraw.picture(xxPos, yyPos, "./images/" + imgFileName);
	}
}

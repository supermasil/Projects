import java.util.*;

/**
 *  Create a NBody instance to read the file and run the simulation
 */

public class NBody {
	/**
     *  Read the radius from the input file
     *
     *  @param  fileName    A file that contains info of the universe
     */
	public static double readRadius (String fileName) {
		In in = new In(fileName);
		in.readInt();
		return in.readDouble();
	}

	/**
     *  Read the planets' information from the input file and return an array of planets
     *
     *  @param  fileName    A file that contains info of the universe
     */
	public static Planet[] readPlanets (String fileName) {
		In in = new In(fileName);
		int size = in.readInt();
		Planet[] p = new Planet[size]; // Can just create Planet[5] but it's not interesting enough
		// Skip the first two lines
		in.readDouble(); // to skip the line

		for (int i = 0; i < p.length; i++) {
			double xxPos = in.readDouble();
			double yyPos = in.readDouble();
			double xxVel = in.readDouble();
			double yyVel = in.readDouble();
			double mass  = in.readDouble();
			String image = in.readString();
			p[i] = new Planet(xxPos, yyPos, xxVel, yyVel, mass, image);

		/*** This will work if we don't know the size
		while (true) {
			Planet[] temp = new Planet[p.length + 1]; // Create a new array to copy data over
			// Has to declare right here because try block has its own scope
			double xxPos; double yyPos; double xxVel; double yyVel;	double mass; String image;

			// Try not to read the wrong info from the file
			try {
				xxPos = in.readDouble();
				yyPos = in.readDouble();
				xxVel = in.readDouble();
				yyVel = in.readDouble();
				mass  = in.readDouble();
				image = in.readString();
			}

			catch (InputMismatchException e) {
				break;
			}
			
			//Copy data to the temp array
			int index = 0;
			for (Planet c : p) {
				temp[index] = c;
				index += 1;
			}

			temp[p.length] = new Planet(xxPos, yyPos, xxVel, yyVel, mass, image);
			p = temp;
		*/
		}
		return p;
	}


	public static void main(String[] args) {
		double T = Double.parseDouble(args[0]);
		double dt = Double.parseDouble(args[1]);
		String filename = args[2];
		double radius = readRadius(filename);
		Planet[] p = readPlanets(filename); 

		// Drawing the background and the starting positions
		StdAudio.play("2001.mid");
		StdDraw.setScale(-radius, radius);
		StdDraw.picture(0, 0, "./images/starfield.jpg");
		for (Planet i : p) {
			i.draw();
		}

		StdDraw.enableDoubleBuffering();
		double time = 0;
		while (time <= T) {
			double[] xForces = new double[p.length];
			double[] yForces = new double[p.length];

			// Calculate the net forces on every planet and save them to the array
			int counter = 0;
			for (Planet i : p) {
				xForces[counter] = i.calcNetForceExertedByX(p);
				yForces[counter] = i.calcNetForceExertedByY(p);
				counter += 1;
			}
			
			// Update position of every planet
			counter = 0;
			for (Planet i : p) {
				i.update(dt, xForces[counter], yForces[counter]);
				counter += 1;
			}

			// Redraw the background on top of the canvas and redraw the planets
			StdDraw.picture(0, 0, "./images/starfield.jpg");
			for (Planet i : p) {
				i.draw();
			}

			StdDraw.show(); // Show the offscreen buffer
			StdDraw.pause(10); // Delay for 10 ms
			time += dt;

		}

		// Print out the final state of the universe
		StdOut.printf("%d\n", p.length);
		StdOut.printf("%.2e\n", radius);
		for (int i = 0; i < p.length; i++) {
		    StdOut.printf("%11.4e %11.4e %11.4e %11.4e %11.4e %12s\n",
                  p[i].xxPos, p[i].yyPos, p[i].xxVel,
                  p[i].yyVel, p[i].mass, p[i].imgFileName);   
		}


	}
}
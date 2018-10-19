package saco;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import physics.Physics;
import ui.SimController;

public class SACOHelper extends TimerTask {
	/*
	 * Specifies whether or not the ants drop food pellets anywhere in the world
	 * using a probability, or they have to walk all the way back to their nests in
	 * order to drop food pellets.
	 */
	private boolean fixedNests;

	// Probability that the ant will pick up a food particle.
	private static final double P_PICK = 1;

	private int t = 0; // Elapsed time.

	private ArrayList<Ant> ants;

	// Variable for determining how much influence the pheromone levels have on the
	// ant's movement.
	private static final double ALPHA = 2;

	// Variable that specifies the pheromone evaporation rate.
	private static final double RHO = 0.2;

	// This is the stopping condition of the algorithm.
	private double maxTimeElapsed;

	private Location[][] world;

	public SACOHelper(Location[][] world, boolean fixedNests, int t, int red, int green, int blue) {
		super();
		this.fixedNests = fixedNests;
		this.t = 0;
		this.world = world;

		maxTimeElapsed = t;
		ants = new ArrayList<>();

		initRandomPheromones(world);
		initNests(world, red, green, blue);
	}

	private void initNests(Location[][] world, int red, int green, int blue) {
		for (int i = 0; i < red; i++) {
			Ant a = new Ant(Colony.RED, randNest());
			ants.add(a);
		}

		for (int i = 0; i < green; i++) {
			Ant a = new Ant(Colony.GREEN, randNest());
			ants.add(a);
		}

		// Initialise the blue nest and place ants in it.
		Location lBlue = world[220][420];
		lBlue.setNest(true);
		for (int i = 0; i < blue; i++) {
			Ant a = new Ant(Colony.BLUE, randNest());
			ants.add(a);
		}
	}

	private Location randNest() {
		Random r = new Random();
		int x = r.nextInt(500);
		int y = r.nextInt(500);
		Location l = world[x][y];
		l.setNest(true);
		return l;
	}

	private void initRandomPheromones(Location[][] world) {
		Random r = new Random();
		/*
		 * Initialize the locations in the world with random pheromones int the range
		 * [1, ?].
		 */
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world[i].length; j++) {
				// Initialise pheromone value with a small round number.
				double[] tempPh = new double[3];
				tempPh[0] = r.nextInt(3) + 1; // Red pheromone level.
				tempPh[1] = r.nextInt(3) + 1; // Green pheromone level.
				tempPh[2] = r.nextInt(3) + 1; // Blue pheromone level.

				world[i][j].setPhLevels(tempPh);

				// -- DEBUG CODE!! --//
				// System.out.println("F: " + world[i][j].getFoodContents() + ". P: " +
				// world[i][j].getPheromone());
			}
		}
	}

	// -- This method implements the SACO algorithm as in the textbook. --//
	public void run() {
		if (t < maxTimeElapsed) {
			for (Ant a : ants) {
				/*
				 * While ant has not reached a location that contains food (destination node)...
				 */
				while (!pickFood(a)) {
					move(a, world); // Select next location based on transition probabilty equation.
				}

				// System.out.println("picked something");
			}

			// Pheromone evaporation
			for (int i = 0; i < world.length; i++) {
				for (int j = 0; j < world[i].length; j++) {
					evaporatePh(world[i][j]);
				}
			}
			// System.out.println("pheromones evaporated.");
			/*
			 * Each ant now deposits pheromone to each cell that it visited on its way to
			 * find food.
			 */
			for (Ant a : ants) {
				int tripLength = a.getPath().size();
				while (!a.getPath().isEmpty()) {

					Location l = a.getPath().pop();
					double phFromAnt = 2 / tripLength;
					switch (a.getColony()) {
					case RED:
						l.addR(l.getPhLevels()[0] + phFromAnt);
						break;
					case GREEN:
						l.addG(l.getPhLevels()[1] + phFromAnt);
						break;
					case BLUE:
						l.addB(l.getPhLevels()[2] + phFromAnt);
						break;
					}
				}
			}
			// System.out.println("Pheromone deposits done.");
			/*
			 * All the ants now walk back in their routes and will drop food particles based
			 * on a probability. This portion is not in SACO. This is experimental.
			 */
			for (Ant a : ants) {
				while (a.isCarryingFood()) {
					if (!atNest(a)) {
						// System.out.println("will not drop");
						takeStep(a, world);
						/*
						 * System.out.println("step back home." + a.getLocation().getX() + ", " +
						 * a.getLocation().getY() + ". " + a.getColony());
						 */
					} else {
						// System.out.println("will drop");
						drop(a, world);
						a.getPath().removeAllElements();
						a.getRoute().removeAllElements();
						/*
						 * System.out.println( "drop" + a.getLocation().getX() + ", " +
						 * a.getLocation().getY() + ". " + a.getColony());
						 */
					}
				}
			}

			t++; // Go to the next iteration.
			Platform.runLater(() -> {
				SimController.updateView(t);
			});
		} else {
			cancel();
			System.out.println("Done.");
		}
	}

	private boolean atNest(Ant a) {
		return a.getLocation().isNest();
	}

	/*
	 * NB!! Not the best method of dropping food: we still need to fix this.
	 */
	private void drop(Ant a, Location[][] world) {
		a.setCarryingFood(false);
		switch (a.getColony()) {
		case RED:
			a.getLocation().setFoodContents(Color.RED);
			break;
		case GREEN:
			a.getLocation().setFoodContents(Color.GREEN);
			break;
		case BLUE:
			a.getLocation().setFoodContents(Color.BLUE);
			break;
		}
	}

	private boolean willDrop(Ant a) {
		double maxDistance = Physics.distance(0, 0, 500, 500);
		double distance = Physics.distance(a.getLocation().getX(), a.getLocation().getY(), a.getNest().getX(),
				a.getNest().getY());

		// Calculate the probability that the ant will drop the pellet.
		double prDrop = (maxDistance - distance) / maxDistance;
		double p = Math.random(); // Generate a random value in [0, 1];

		// If this value is less than the probability, the ant will drop the food
		// particle.
		if (p < prDrop)
			return true;

		return false;
	}

	private void takeStep(Ant a, Location[][] world) {
		Move m = a.getRoute().pop();
		Location l = null;
		switch (m) {
		case UP:
			l = world[a.getLocation().getX()][a.getLocation().getY() - 1];
			a.setLocation(l);
			break;
		case DOWN:
			l = world[a.getLocation().getX()][a.getLocation().getY() + 1];
			a.setLocation(l);
			break;
		case LEFT:
			l = world[a.getLocation().getX() - 1][a.getLocation().getY()];
			a.setLocation(l);
			break;
		case RIGHT:
			l = world[a.getLocation().getX() + 1][a.getLocation().getY()];
			a.setLocation(l);
			break;
		}
	}

	/*
	 * This method does pheromone evaporation on a location using the evaporation
	 * equation.
	 */
	private void evaporatePh(Location l) {
		double newR = (1 - RHO) * l.getPhLevels()[0];
		double newG = (1 - RHO) * l.getPhLevels()[1];
		double newB = (1 - RHO) * l.getPhLevels()[2];

		l.addR(newR);
		l.addR(newG);
		l.addR(newB);
	}

	private boolean pickFood(Ant a) {
		if (canEat(a)) {
			// Ant picks up pellet based on a probability.
			double p = Math.random();
			if (p < P_PICK) {
				pick(a);
				/*
				 * System.out.println( "pick" + a.getLocation().getX() + ", " +
				 * a.getLocation().getY() + ". " + a.getColony());
				 */
				return true;
			}
		}
		return false;
	}

	private void pick(Ant a) {
		// -- Ant can only eat food that is good for its colony.
		a.setCarryingFood(true);
		a.getLocation().setFoodContents(Color.WHITE);
	}

	private boolean canEat(Ant a) {
		if (a.getPath().isEmpty() || a.getLocation().isNest()) {
			// Ant is still in its nest. It cannot eat. It has to move out.
			return false;
		}

		switch (a.getColony()) {
		// Ant can only eat food that is good for its colony.
		case RED:
			// System.out.println(a.getLocation().getFoodContents().getR() + " (" +
			// a.getColony() + ")");
			return a.getLocation().getFoodContents() == Color.RED;
		case GREEN:
			// System.out.println(a.getLocation().getFoodContents().getG() + " (" +
			// a.getColony() + ")");
			return a.getLocation().getFoodContents() == Color.GREEN;
		case BLUE:
			// System.out.println(a.getLocation().getFoodContents().getB() + " (" +
			// a.getColony() + ")");
			return a.getLocation().getFoodContents() == Color.BLUE;
		}

		return false;
	}

	private void move(Ant a, Location[][] world) {
		double sum = sumPheromones(a, world);
		int x, y;
		double[] tProbs = new double[4];

		// Calculate transition probabiltiy for up cell.
		x = a.getLocation().getX();
		y = a.getLocation().getY() - 1;
		if (isValidLocation(x, y)) {
			Location up = world[x][y];
			if (up.isNest())
				tProbs[0] = -3;
			else {
				double pTransition = calcTProbability(a.getColony(), up.getPhLevels(), sum);
				tProbs[0] = pTransition;
			}
		} else {
			/*
			 * This is an invalid position, so the probabilty of visiting it is 0.
			 */
			tProbs[0] = -3;
		}

		// Calculate transition probabiltiy for down cell.
		x = a.getLocation().getX();
		y = a.getLocation().getY() + 1;
		if (isValidLocation(x, y)) {
			Location down = world[x][y];
			if (down.isNest())
				tProbs[1] = -3;
			else {
				double pTransition = calcTProbability(a.getColony(), down.getPhLevels(), sum);
				tProbs[1] = pTransition;
			}
		} else {
			/*
			 * This is an invalid position, so the probabilty of visiting it is 0.
			 */
			tProbs[1] = -3;
		}

		// Calculate transition probabiltiy for left cell.
		x = a.getLocation().getX() - 1;
		y = a.getLocation().getY();
		if (isValidLocation(x, y)) {
			Location left = world[x][y];
			if (left.isNest())
				tProbs[2] = -3;
			else {
				double pTransition = calcTProbability(a.getColony(), left.getPhLevels(), sum);
				tProbs[2] = pTransition;
			}
		} else {
			/*
			 * This is an invalid position, so the probabilty of visiting it is 0.
			 */
			tProbs[2] = -3;
		}

		// Calculate transition probabiltiy for right cell.
		x = a.getLocation().getX() + 1;
		y = a.getLocation().getY();
		if (isValidLocation(x, y)) {
			Location right = world[x][y];
			if (right.isNest())
				tProbs[3] = -3;
			else {
				double pTransition = calcTProbability(a.getColony(), right.getPhLevels(), sum);
				tProbs[3] = pTransition;
			}
		} else {
			/*
			 * This is an invalid position, so the probabilty of visiting it is 0.
			 */
			tProbs[3] = -3;
		}

		pickNextLocation(a, world, tProbs);
	}

	private void pickNextLocation(Ant a, Location[][] world, double[] tProbs) {
		boolean pathFound = false;
		int indexToRemove = -1;

		while (!pathFound) {
			double maxProb = -1; // Initialize with a value that we know for sure is not in the array.
			int maxIndex = -1;

			if (indexToRemove >= 0) {
				// invalidate this location in the array.
				tProbs[indexToRemove] = -2;
			}

			for (int i = 0; i < tProbs.length; i++) {
				if (tProbs[i] > maxProb) {
					maxProb = tProbs[i];
					maxIndex = i;
				}
			}

			Location nextLoc = null;
			switch (maxIndex) {
			case 0:
				//System.out.println(tProbs[0]);
				// System.out.println("Moving up - " + maxProb);
				// Ant has to move up.
				nextLoc = world[a.getLocation().getX()][a.getLocation().getY() - 1];
				if (!visited(a, nextLoc)) {
					/*
					 * Update route stack with opposite direction. This will benefit the ant when it
					 * is returning to its nest.
					 */
					a.getRoute().push(Move.DOWN);

					a.getPath().push(nextLoc);
					/*System.out.println("Path found up." + a.getLocation().getX() + ", " + a.getLocation().getY() + ". "
							+ a.getColony());*/
					a.setLocation(nextLoc);
					pathFound = true;
				} else {
					// System.out.println("been there.");
					indexToRemove = 0;
				}
				break;
			case 1:
				//System.out.println(tProbs[1]);

				// System.out.println("Moving down " + maxProb);

				// Ant has to move down.
				nextLoc = world[a.getLocation().getX()][a.getLocation().getY() + 1];
				if (!visited(a, nextLoc)) {
					/*
					 * Update route stack with opposite direction. This will benefit the ant when it
					 * is returning to its nest.
					 */
					a.getRoute().push(Move.UP);

					a.getPath().push(nextLoc);
					/*System.out.println("Path found down." + a.getLocation().getX() + ", " + a.getLocation().getY()
							+ ". " + a.getColony());*/
					a.setLocation(nextLoc);
					pathFound = true;

				} else {
					// System.out.println("been there.");
					indexToRemove = 1;
				}
				break;
			case 2:
				//System.out.println(tProbs[2]);

				// System.out.println("Moving left " + maxProb);

				// Ant has to move left.
				nextLoc = world[a.getLocation().getX() - 1][a.getLocation().getY()];
				if (!visited(a, nextLoc)) {
					/*
					 * Update route stack with opposite direction. This will benefit the ant when it
					 * is returning to its nest.
					 */
					a.getRoute().push(Move.RIGHT);

					a.getPath().push(nextLoc);
					/*System.out.println("Path found left." + a.getLocation().getX() + ", " + a.getLocation().getY()
							+ ". " + a.getColony());*/
					a.setLocation(nextLoc);
					pathFound = true;
				} else {
					// System.out.println("been there.");
					indexToRemove = 2;
				}
				break;
			case 3:
				//System.out.println(tProbs[3]);

				// System.out.println("Moving right " + maxProb);

				// Ant has to move right.
				nextLoc = world[a.getLocation().getX() + 1][a.getLocation().getY()];
				if (!visited(a, nextLoc)) {
					/*
					 * Update route stack with opposite direction. This will benefit the ant when it
					 * is returning to its nest.
					 */
					a.getRoute().push(Move.LEFT);

					a.getPath().push(nextLoc);
					/*System.out.println("Path found right." + a.getLocation().getX() + ", " + a.getLocation().getY()
							+ ". " + a.getColony());*/
					a.setLocation(nextLoc);
					pathFound = true;
				} else {
					// System.out.println("been there.");
					indexToRemove = 3;
				}
				break;
			default:
				makeRandomMove(a, tProbs);
				pathFound = true;
				break;
			}
		}
	}

	private void makeRandomMove(Ant a, double[] tProbs) {
		boolean found = false;
		Location nextLoc = null;
		while (!found) {
			int p = new Random().nextInt(4);
			if (p == 0) {
				// Start by trying to move up.
				if (isValidLocation(a.getLocation().getX(), a.getLocation().getY() - 1)) {
					nextLoc = world[a.getLocation().getX()][a.getLocation().getY() - 1];
					/*
					 * Update route stack with opposite direction. This will benefit the ant when it
					 * is returning to its nest.
					 */
					a.getRoute().push(Move.DOWN);
					a.getPath().push(nextLoc);
					/*System.out.println("Random Path found up." + a.getLocation().getX() + ", " + a.getLocation().getY()
							+ ". " + a.getColony());*/
					a.setLocation(nextLoc);
					found = true;
				}

			}
			if (p == 1) {
				// 2nd attempt is down.
				if (isValidLocation(a.getLocation().getX(), a.getLocation().getY() + 1)) {

					nextLoc = world[a.getLocation().getX()][a.getLocation().getY() + 1];
					/*
					 * Update route stack with opposite direction. This will benefit the ant when it
					 * is returning to its nest.
					 */
					a.getRoute().push(Move.UP);
					a.getPath().push(nextLoc);
					/*System.out.println("Random Path found down." + a.getLocation().getX() + ", "
							+ a.getLocation().getY() + ". " + a.getColony());*/
					a.setLocation(nextLoc);
					found = true;
				}
			}
			if (p == 2) {
				// 3rd attempt is left.
				if (isValidLocation(a.getLocation().getX() - 1, a.getLocation().getY())) {

					nextLoc = world[a.getLocation().getX() - 1][a.getLocation().getY()];
					/*
					 * Update route stack with opposite direction. This will benefit the ant when it
					 * is returning to its nest.
					 */
					a.getRoute().push(Move.RIGHT);
					a.getPath().push(nextLoc);
					/*System.out.println("Random Path found left." + a.getLocation().getX() + ", "
							+ a.getLocation().getY() + ". " + a.getColony());*/
					a.setLocation(nextLoc);
					found = true;
				}
			}
			if (p == 3) {
				// Last attempt is right.
				if (isValidLocation(a.getLocation().getX() + 1, a.getLocation().getY())) {

					nextLoc = world[a.getLocation().getX() + 1][a.getLocation().getY()];
					/*
					 * Update route stack with opposite direction. This will benefit the ant when it
					 * is returning to its nest.
					 */
					a.getRoute().push(Move.LEFT);
					a.getPath().push(nextLoc);
					/*System.out.println("RandomPath found right." + a.getLocation().getX() + ", "
							+ a.getLocation().getY() + ". " + a.getColony());*/
					a.setLocation(nextLoc);
					found = true;
				}
			}
		}
	}

	private boolean visited(Ant a, Location nextLoc) {
		for (Location l : a.getPath()) {
			if (l.getX() == nextLoc.getX() && l.getY() == nextLoc.getY())
				return true;
		}
		return false;
	}

	private double calcTProbability(Colony c, double[] phLevels, double sum) {
		// Raise values to the power of alpha.
		double p = 0;
		switch (c) {
		case RED:
			p = phLevels[0];
		case GREEN:
			p = phLevels[1];
		case BLUE:
			p = phLevels[2];
		}

		double num = Math.pow(p, ALPHA);
		double den = Math.pow(sum, ALPHA);

		// Calculate the transition probabilty.
		double pTransition = num / den;
		return pTransition;
	}

	// -- This function calculates the transition probabilty. --//
	private double sumPheromones(Ant a, Location[][] world) {
		// Note: Ant can only move in 4 directions.
		double sum = 0;
		int index = -1;

		// A way to access the correct pheromone levels.
		switch (a.getColony()) {
		case RED:
			index = 0;
			break;
		case GREEN:
			index = 1;
			break;
		case BLUE:
			index = 2;
			break;
		}

		sum = a.getLocation().getPhLevels()[index];
		int x, y;
		Location l = a.getLocation();

		// Get pheromone concentration for up cell.
		x = l.getX();
		y = l.getY() - 1;
		if (isValidLocation(x, y)) {
			Location temp = world[x][y];
			sum += temp.getPhLevels()[index]; // Add the cell's pheromone to the sum.
		}

		// Get pheromone concentration for down cell.
		x = l.getX();
		y = l.getY() + 1;
		if (isValidLocation(x, y)) {
			Location temp = world[x][y];
			sum += temp.getPhLevels()[index]; // Add the cell's pheromone to the sum.
		}

		// Get pheromone concentration for left cell.
		x = l.getX() - 1;
		y = l.getY();
		if (isValidLocation(x, y)) {
			Location temp = world[x][y];
			sum += temp.getPhLevels()[index]; // Add the cell's pheromone to the sum.
		}

		// Get pheromone concentration for right cell.
		x = l.getX() + 1;
		y = l.getY();
		if (isValidLocation(x, y)) {
			Location temp = world[x][y];
			sum += temp.getPhLevels()[index]; // Add the cell's pheromone to the sum.
		}
		return sum;
	}

	private boolean isValidLocation(int x, int y) {
		return (x >= Physics.MIN_X && x < Physics.MAX_X) && (y >= Physics.MIN_Y && y < Physics.MAX_Y);
	}

	public boolean isFixedNests() {
		return fixedNests;
	}

	public void setFixedNests(boolean fixedNests) {
		this.fixedNests = fixedNests;
	}
}

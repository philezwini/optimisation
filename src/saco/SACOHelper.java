package saco;

import java.util.ArrayList;
import java.util.Random;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import physics.Physics;
import ui.SimController;

public class SACOHelper extends TimerTask {
	// Probability that the ant will pick up a food particle.
	private static final double P_PICK = 1;

	// private static final double P_DROP = 0.98;

	private int t = 0; // Elapsed time.

	private ArrayList<Ant> ants;
	private int numRedAnts;
	private int numGreenAnts;
	private int numBlueAnts;

	// Variable for determining how much influence the pheromone levels have on the
	// ant's movement.
	private static final double ALPHA = 2;

	// Variable that specifies the pheromone evaporation rate.
	private static final double RHO = 0.5;

	private static final int INIT_MAX_PH = 3;

	// This is the stopping condition of the algorithm.
	private double maxTimeElapsed;

	private Location[][] world;

	public SACOHelper(Location[][] world, boolean fixedNests, int maxTimeElapsed, int red, int green, int blue) {
		super();
		this.world = world;

		t = 0;
		numRedAnts = 0;
		numGreenAnts = 0;
		numBlueAnts = 0;

		this.maxTimeElapsed = maxTimeElapsed;
		ants = new ArrayList<>();

		initRandomPheromones(world);
		initNests(world, red, green, blue);
	}

	private void initNests(Location[][] world, int red, int green, int blue) {

		for (int i = 0; i < red; i++) {
			Location lRed = randNest(Colony.RED);
			Ant a = new Ant(Colony.RED, lRed);
			ants.add(a);
			numRedAnts++;
		}

		for (int i = 0; i < green; i++) {
			Location lGreen = randNest(Colony.GREEN);
			Ant a = new Ant(Colony.GREEN, lGreen);
			ants.add(a);
			numGreenAnts++;
		}

		for (int i = 0; i < blue; i++) {
			Location lBlue = randNest(Colony.BLUE);
			Ant a = new Ant(Colony.BLUE, lBlue);
			ants.add(a);
			numBlueAnts++;
		}
	}

	private Location randNest(Colony colony) {
		Random r = new Random();
		int x = r.nextInt(world.length);
		int y = r.nextInt(world.length);
		Location l = world[x][y];

		// Make sure that an ant's nest is always initially an empty space.
		while (!isValidNest(l, colony)) {
			x = r.nextInt(world.length);
			y = r.nextInt(world.length);
			l = world[x][y];
		}

		// Make this location a nest for the given colony.
		l.setNest(true, colony);
		return l;
	}

	/*
	 * This function is used to ensure that (1) the nest being chosen for the ant
	 * contains food relevant to the ant and (2) it is not already a nest.
	 */
	private boolean isValidNest(Location l, Colony c) {

		// The location contains food relevant the ant
		if (l.getFoodContents() != toColor(c))
			return false;

		// This is already a nest for another ant.
		if (l.isNest())
			return false;

		return true;
	}

	private void initRandomPheromones(Location[][] world) {
		Random r = new Random();
		/*
		 * Initialize the locations in the world with random pheromones in the range [1,
		 * INIT_MAX_PH].
		 */
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world[i].length; j++) {
				// Initialise pheromone value with a small round number.
				double[] tempPh = new double[3];
				tempPh[0] = r.nextInt(INIT_MAX_PH) + 1; // Red pheromone level.
				tempPh[1] = r.nextInt(INIT_MAX_PH) + 1; // Green pheromone level.
				tempPh[2] = r.nextInt(INIT_MAX_PH) + 1; // Blue pheromone level.

				world[i][j].setPhLevels(tempPh);
			}
		}
	}

	// -- This method implements the SACO algorithm as in the textbook. --//
	public void run() {
		if (t < maxTimeElapsed) {
			for (Ant a : ants) {
				/*
				 * While ant has not reached a location that contains food relevant for its
				 * colong (a.k.a. destination node)...
				 */
				while (!pickFood(a)) {
					move(a, world); // Select next location based on transition probabilty equation.
				}
			}

			// Pheromone evaporation
			for (int i = 0; i < world.length; i++) {
				for (int j = 0; j < world[i].length; j++) {
					evaporatePh(world[i][j]);
				}
			}

			/*
			 * Pheromone deposits. The pheromone deposits are inversely proportional to the
			 * length of the paths that the ants travelled to get food. The longer the path,
			 * the lower the pheromone deposit for each location along that path. Function:
			 * ph = numAntsInColony / tripLength;
			 */
			for (Ant a : ants) {
				int tripLength = a.getPath().size();
				while (!a.getPath().isEmpty()) {
					Location l = a.getPath().pop();
					double phFromAnt = 0;

					if (a.getColony() == Colony.RED) {
						phFromAnt = numRedAnts / tripLength;
						l.setR(l.getPhLevels()[0] + phFromAnt);
					}

					if (a.getColony() == Colony.GREEN) {
						phFromAnt = numGreenAnts / tripLength;
						l.setG(l.getPhLevels()[1] + phFromAnt);
					}

					if (a.getColony() == Colony.BLUE) {
						phFromAnt = numBlueAnts / tripLength;
						l.setB(l.getPhLevels()[2] + phFromAnt);
					}
				}
			}

			/*
			 * In this loop, the ants walk back to their nests using the route stack. The
			 * ant drops a food particle around its current nest, making that new location a
			 * nest as well. This is done so that nests grow close to each other. NB: A
			 * "nest" is not a cluster. It is simply a location. Therefore, a cluster is a
			 * collection of "nests".
			 */
			for (Ant a : ants) {
				while (a.isCarryingFood()) {
					/*
					 * double d = Physics.distance(a.getLocation().getX(), a.getLocation().getY(),
					 * a.getNest().getX(), a.getNest().getY()); double maxD =
					 * Physics.distance(Physics.MIN_X, Physics.MIN_Y, Physics.MAX_X, Physics.MAX_Y);
					 * 
					 * double p = (maxD - d) / maxD;
					 * 
					 * if (p < P_DROP) { drop(a, world); a.getPath().removeAllElements();
					 * a.getRoute().removeAllElements(); }
					 */

					if (!atNest(a)) {
						takeStep(a, world);
					} else {
						drop(a, world);

						// Empty the path and route stacks.
						a.getPath().removeAllElements();
						a.getRoute().removeAllElements();
					}

				}
			}

			t++; // Go to the next iteration.

			// Update the scene.
			Platform.runLater(() -> {
				SimController.updateView(t);
			});
		} else {
			// Stopping conditions have been met.
			cancel();
			System.out.println("Done.");
		}
	}

	private boolean atNest(Ant a) {
		return a.getLocation().isNest() && (a.getLocation().getResColony() == a.getColony());
	}

	/*
	 * This methods helps the ant drop the food it is carrying. An ant can only drop
	 * food when in its own nest.
	 */
	private void drop(Ant a, Location[][] world) {
		a.setCarryingFood(false);

		if ((a.getLocation().getResColony() == a.getColony()) && a.getLocation().getFoodContents() == Color.WHITE) {
			// This is a new nest.
			a.getLocation().setFoodContents(toColor(a.getColony()));
			return;
		}

		// This is not a new nest. It must be expanded.
		expandNest(a);
	}

	/*
	 * This function searches for an empty space around an existing nest to drop
	 * food particles.
	 */
	private void expandNest(Ant a) {
		int d = 1;
		boolean emptySpaceFound = false;
		int x = a.getLocation().getX(), y = a.getLocation().getY();

		while (!emptySpaceFound) {
			// North West
			if (isValidLocation(x - d, y - d) && !world[x - d][y - d].isNest()) {
				world[x - d][y - d].setFoodContents(toColor(a.getColony()));
				world[x - d][y - d].setNest(true, a.getColony());
				emptySpaceFound = true;
				continue;
			}

			// North.
			if (isValidLocation(x, y - d) && !world[x][y - d].isNest()) {
				world[x][y - d].setFoodContents(toColor(a.getColony()));
				world[x][y - d].setNest(true, a.getColony());
				emptySpaceFound = true;
				continue;
			}

			// North East.
			if (isValidLocation(x + d, y - d) && !world[x + d][y - d].isNest()) {
				world[x + d][y - d].setFoodContents(toColor(a.getColony()));
				world[x + d][y - d].setNest(true, a.getColony());
				emptySpaceFound = true;
				continue;
			}

			// East
			if (isValidLocation(x + d, y) && !world[x + d][y].isNest()) {
				world[x + d][y].setFoodContents(toColor(a.getColony()));
				world[x + d][y].setNest(true, a.getColony());
				emptySpaceFound = true;
				continue;
			}

			// South East.
			if (isValidLocation(x + d, y + d) && !world[x + d][y + d].isNest()) {
				world[x + d][y + d].setFoodContents(toColor(a.getColony()));
				world[x + d][y + d].setNest(true, a.getColony());
				emptySpaceFound = true;
				continue;
			}

			// South.
			if (isValidLocation(x, y + d) && !world[x][y + d].isNest()) {
				world[x][y + d].setFoodContents(toColor(a.getColony()));
				world[x][y + d].setNest(true, a.getColony());
				emptySpaceFound = true;
				continue;
			}

			// South West.
			if (isValidLocation(x - d, y + d) && !world[x - d][y + d].isNest()) {
				world[x - d][y + d].setFoodContents(toColor(a.getColony()));
				world[x - d][y + d].setNest(true, a.getColony());
				emptySpaceFound = true;
				continue;
			}

			// West.
			if (isValidLocation(x - d, y) && !world[x - d][y].isNest()) {
				world[x - d][y].setFoodContents(toColor(a.getColony()));
				world[x - d][y].setNest(true, a.getColony());
				emptySpaceFound = true;
				continue;
			}
			d++;
		}
	}

	private Color toColor(Colony colony) {
		if (colony == Colony.RED)
			return Color.RED;

		if (colony == Colony.GREEN)
			return Color.GREEN;

		if (colony == Colony.BLUE)
			return Color.BLUE;

		try {
			throw new Exception("Unrecognised Colony!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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

		// Update location with the new pheromone values.
		l.setR(newR);
		l.setG(newG);
		l.setB(newB);
	}

	private boolean pickFood(Ant a) {
		if (canEat(a)) {
			// Ant picks up food particle based on a probability.
			double p = Math.random();
			if (p < P_PICK) {
				pick(a);
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
		if (a.getPath().isEmpty()) {
			// Ant has to move atleast one step before it can eat. This encourages
			// exploration of the search space.
			return false;
		}

		if (a.getLocation().isNest()) {
			// Ant cannot eat from a nest.
			return false;
		}

		if (a.getLocation().getFoodContents() != toColor(a.getColony())) {
			// Ant can only eat food suitable for its colony.
			return false;
		}

		return true;
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
				// Ant has to move up.
				nextLoc = world[a.getLocation().getX()][a.getLocation().getY() - 1];
				// Make sure that this location is not already in the stack to avoid loops.
				if (!visited(a, nextLoc)) {
					/*
					 * Update the route stack with opposite direction. The ant will use this stack
					 * when it returns to its nest.
					 */
					a.getRoute().push(Move.DOWN);
					a.getPath().push(nextLoc);
					a.setLocation(nextLoc);
					pathFound = true;
				} else {
					indexToRemove = 0;
				}
				break;
			case 1:
				// Ant has to move down.
				nextLoc = world[a.getLocation().getX()][a.getLocation().getY() + 1];
				if (!visited(a, nextLoc)) {
					a.getRoute().push(Move.UP);
					a.getPath().push(nextLoc);
					a.setLocation(nextLoc);
					pathFound = true;
				} else {
					indexToRemove = 1;
				}
				break;
			case 2:
				// Ant has to move left.
				nextLoc = world[a.getLocation().getX() - 1][a.getLocation().getY()];
				if (!visited(a, nextLoc)) {
					a.getRoute().push(Move.RIGHT);
					a.getPath().push(nextLoc);
					a.setLocation(nextLoc);
					pathFound = true;
				} else {
					indexToRemove = 2;
				}
				break;
			case 3:
				// Ant has to move right.
				nextLoc = world[a.getLocation().getX() + 1][a.getLocation().getY()];
				if (!visited(a, nextLoc)) {
					a.getRoute().push(Move.LEFT);
					a.getPath().push(nextLoc);
					a.setLocation(nextLoc);
					pathFound = true;
				} else {
					indexToRemove = 3;
				}
				break;
			default:
				// maxIndex is -1. The ant has exhausted all its options. It has to make a
				// random move.
				makeRandomMove(a, tProbs);
				pathFound = true;
				break;
			}
		}
	}

	private void makeRandomMove(Ant a, double[] tProbs) {
		boolean found = false; // Keeps track of whether a random location has been found.
		Location nextLoc = null;

		while (!found) {
			int p = new Random().nextInt(4);
			if (p == 0) {
				// Start by trying to move up.
				if (isValidLocation(a.getLocation().getX(), a.getLocation().getY() - 1)) {
					nextLoc = world[a.getLocation().getX()][a.getLocation().getY() - 1];
					a.getRoute().push(Move.DOWN);
					a.getPath().push(nextLoc);
					a.setLocation(nextLoc);
					found = true;
				}

			}
			if (p == 1) {
				// 2nd attempt is down.
				if (isValidLocation(a.getLocation().getX(), a.getLocation().getY() + 1)) {
					nextLoc = world[a.getLocation().getX()][a.getLocation().getY() + 1];
					a.getRoute().push(Move.UP);
					a.getPath().push(nextLoc);
					a.setLocation(nextLoc);
					found = true;
				}
			}
			if (p == 2) {
				// 3rd attempt is left.
				if (isValidLocation(a.getLocation().getX() - 1, a.getLocation().getY())) {
					nextLoc = world[a.getLocation().getX() - 1][a.getLocation().getY()];
					a.getRoute().push(Move.RIGHT);
					a.getPath().push(nextLoc);
					a.setLocation(nextLoc);
					found = true;
				}
			}
			if (p == 3) {
				// Last attempt is right.
				if (isValidLocation(a.getLocation().getX() + 1, a.getLocation().getY())) {
					nextLoc = world[a.getLocation().getX() + 1][a.getLocation().getY()];
					a.getRoute().push(Move.LEFT);
					a.getPath().push(nextLoc);
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
}

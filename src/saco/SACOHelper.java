package saco;

import java.util.ArrayList;
import java.util.Random;

public class SACOHelper {
	/*
	 * Specifies whether or not the ants drop food pellets anywhere in the world
	 * using a probability, or they have to walk all the way back to their nests in
	 * order to drop food pellets.
	 */
	private boolean fixedNests;

	// Probability that the ant will drop/pick a food particle.
	private double pDrop, pPickUp;

	private int t = 0; // Elapsed time.

	private ArrayList<Ant> ants;

	// Variable for determining how much influence the pheromone levels have on the
	// ant's movement.
	private static final double ALPHA = 1;

	// This is the stopping condition of the algorithm.
	private static final double MAX_TIME_ELAPSED = 100;

	public SACOHelper(Location[][] world, boolean fixedNests, int t, int red, int green, int blue) {
		super();
		this.fixedNests = fixedNests;
		t = 0;
		ants = new ArrayList<>();

		initRandomPheromones(world);
		initNests(world, red, green, blue);
	}

	private void initNests(Location[][] world, int red, int green, int blue) {
		// Initialise the red nest and place ants in it.
		Location lRed = world[170][200];
		lRed.setNest(true);
		for (int i = 0; i < red; i++) {
			Ant a = new Ant(Colony.RED, lRed);
			ants.add(a);
			lRed.setNumAnts(lRed.getNumAnts() + 1);
		}

		// Initialise the green nest and place ants in it.
		Location lGreen = world[410][300];
		lGreen.setNest(true);
		for (int i = 0; i < green; i++) {
			Ant a = new Ant(Colony.GREEN, lGreen);
			ants.add(a);
			lGreen.setNumAnts(lGreen.getNumAnts() + 1);
		}

		// Initialise the blue nest and place ants in it.
		Location lBlue = world[220][420];
		lBlue.setNest(true);
		for (int i = 0; i < blue; i++) {
			Ant a = new Ant(Colony.BLUE, lBlue);
			ants.add(a);
			lBlue.setNumAnts(lBlue.getNumAnts() + 1);
		}

		// -- DEBUG CODE!! --//
		/*
		 * for(int i = 0; i < world.length; i++) { for(int j = 0; j < world[i].length;
		 * j++) { if(world[i][j].isNest()) System.out.println("Nest:" +
		 * world[i][j].getNumAnts()); } }
		 */
	}

	private void initRandomPheromones(Location[][] world) {
		Random r = new Random();
		/*
		 * Initialize the locations in the world with random pheromones int the range
		 * [1, 5].
		 */
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world[i].length; j++) {
				world[i][j].setPheromone(r.nextInt(5) + 1);

				// -- DEBUG CODE!! --//
				// System.out.println("F: " + world[i][j].getFoodContents() + ". P: " +
				// world[i][j].getPheromone());
			}
		}
	}

	//-- This method implements the SACO algorithm as in the textbook. --//
	public void run() {
		while (t < MAX_TIME_ELAPSED) {
			for(Ant a : ants) {
				while(!a.isCarryingFood()) {
					move(a);
				}
			}
		}
	}

	private void move(Ant a) {
		sumAllPheronomes(a);
		double num = Math.pow(a.getLocation().getPheromone(), ALPHA);
	}

	private void sumAllPheronomes(Ant a) {
		//Note: Ant can only move in 4 directions.
		int sum = 0;
		int x, y;
		Location l = a.getLocation();
		
		
	}

	public boolean isFixedNests() {
		return fixedNests;
	}

	public void setFixedNests(boolean fixedNests) {
		this.fixedNests = fixedNests;
	}
}

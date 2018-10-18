package saco;

public class Location {
	private int pheromone; //Pheromone concentration.
	private boolean isNest;
	private Coordinate foodContents;
	private int numAnts;
	
	public Location(Coordinate foodContents) {
		this.foodContents = foodContents;
		setNest(false);
	}
	
	public Coordinate getFoodContents() {
		return foodContents;
	}
	
	public void setFoodContents(Coordinate foodContents) {
		this.foodContents = foodContents;
	}

	public int getPheromone() {
		return pheromone;
	}

	public void setPheromone(int pheromone) {
		this.pheromone = pheromone;
	}

	public boolean isNest() {
		return isNest;
	}

	public void setNest(boolean isNestLoc) {
		this.isNest = isNestLoc;
	}

	public int getNumAnts() {
		return numAnts;
	}

	public void setNumAnts(int numAnts) {
		this.numAnts = numAnts;
	}
}

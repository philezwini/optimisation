package saco;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Location {
	private double[] phLevels; // Pheromone concentration.
	private boolean isNest;
	private Color foodContents;
	private int x; // x-coordinate of the location.
	private int y; // y-coordinate of the location.

	private Circle visual;

	public Location(Color foodContents, int x, int y) {
		this.foodContents = foodContents;
		this.x = x;
		this.y = y;

		phLevels = new double[3];
		setNest(false);
	}

	public Color getFoodContents() {
		return foodContents;
	}

	public void setFoodContents(Color foodContents) {
		this.foodContents = foodContents;
	}

	public double[] getPhLevels() {
		return phLevels;
	}

	public void setPhLevels(double[] phLevels) {
		this.phLevels = phLevels;
	}

	public boolean isNest() {
		return isNest;
	}

	public void setNest(boolean isNestLoc) {
		this.isNest = isNestLoc;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Circle getVisual() {
		return visual;
	}

	public void setVisual(Circle visual) {
		this.visual = visual;
	}

	public void addR(double phLevel) {
		phLevels[0] += phLevel;
	}

	public void addG(double phLevel) {
		phLevels[1] += phLevel;
	}

	public void addB(double phLevel) {
		phLevels[2] += phLevel;
	}
}

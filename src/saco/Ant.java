package saco;

import java.util.Stack;

public class Ant {
	private Colony colony;
	private boolean carryingFood;
	private Location nest;
	private Location location;
	private Stack<Location> path;
	private Stack<Move> route;
	
	public Ant(Colony colony, Location nest) {
		super();
		this.colony = colony;
		this.nest = nest;
		location = this.nest;
		
		carryingFood = false;
		
		setPath(new Stack<>());
		setRoute(new Stack<>());
	}

	public Colony getColony() {
		return colony;
	}

	public void setColony(Colony colony) {
		this.colony = colony;
	}

	public boolean isCarryingFood() {
		return carryingFood;
	}

	public void setCarryingFood(boolean carryingFood) {
		this.carryingFood = carryingFood;
	}

	public Location getNest() {
		return nest;
	}

	public void setNest(Location nest) {
		this.nest = nest;
	}

	public Stack<Location> getPath() {
		return path;
	}

	public void setPath(Stack<Location> path) {
		this.path = path;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Stack<Move> getRoute() {
		return route;
	}

	public void setRoute(Stack<Move> route) {
		this.route = route;
	}
}

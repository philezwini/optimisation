package physics;

public class Physics {
	public static final int MIN_X = 0;
	public static final int MAX_X = 500;
	public static final int MIN_Y = 0;
	public static final int MAX_Y = 500;

	public static double distance(double x1, double y1, double x2, double y2) {
		double arg1 = Math.pow(x2 - x1, 2);
		double arg2 = Math.pow(y2 - y1, 2);
		return Math.sqrt(arg1 + arg2);
	}
}

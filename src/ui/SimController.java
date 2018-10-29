package ui;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Timer;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import physics.Physics;
import saco.Location;
import saco.SACOHelper;

public class SimController extends Controller {
	private static Location[][] world;
	private int width, height;
	private Timer timer;
	SACOHelper saco;

	@FXML
	private AnchorPane rootPane;

	@FXML
	private TextField txtIterations, txtRed, txtGreen, txtBlue, txtElapsed;
	private static TextField s_txtElapsed;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// Get the user's preferred size of the world.
		width = 300;
		height = 300;
		
		Physics.initialize(width, height);
		rootPane.setPrefSize(width, height);
		
		s_txtElapsed = txtElapsed;
		s_txtElapsed.setText("0");
		initWorld();// Initialize the world.
	}

	@FXML
	private void btnStartClick() {
		int t = Integer.parseInt(txtIterations.getText());
		int r = Integer.parseInt(txtRed.getText());
		int g = Integer.parseInt(txtGreen.getText());
		int b = Integer.parseInt(txtBlue.getText());

		saco = new SACOHelper(world, true, t, r, g, b);
		timer = new Timer();
		timer.scheduleAtFixedRate(saco, 0, 100);
	}

	@FXML
	private void btnSopClick() {
		saco.cancel();
		System.out.println("Simulation stopped.");
	}

	// -- Draws the world on the AnchorPane. --//
	private void drawWorld() {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				Color col = world[i][j].getFoodContents();
				Circle c = new Circle(1);
				c.setTranslateX(i);
				c.setTranslateY(j);
				c.setFill(col);

				world[i][j].setVisual(c);
				rootPane.getChildren().add(c);
			}
		}
	}

	// -- Initialized the world. Obtain width and height from user before calling.
	// --//
	private void initWorld() {
		world = new Location[width][height];

		// Initialize each pixel with random numbers.
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				world[i][j] = new Location(randColor(), i, j);
			}
		}
		drawWorld();
	}

	// -- Returns a random value between 0 and 255 both inclusive. --//
	private Color randColor() {
		int rand = new Random().nextInt(7);
		if (rand == 0)
			return Color.RED;
		if (rand == 1)
			return Color.GREEN;
		if (rand == 2)
			return Color.BLUE;
		if(rand == 3)
			return Color.WHITE;
		if(rand == 4)
			return Color.WHITE;
		if(rand == 5)
			return Color.WHITE;
		if(rand == 6)
			return Color.WHITE;

		return null;
	}

	@FXML
	void btnClickClick(MouseEvent e) {
		System.out.println("x = " + e.getSceneX() + ", y = " + e.getSceneY());
	}

	/*
	 * This method updates the world so that it reflects what is happening in the
	 * algorithm.100 10
	 */
	public static void updateView(int t) {
		s_txtElapsed.setText(t + "");

		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world[i].length; j++) {
				Location l = world[i][j];
				l.getVisual().setFill(l.getFoodContents());
			}
		}
	}
}

package ui;

import java.net.URL;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import saco.Coordinate;
import saco.Location;
import saco.SACOHelper;

public class SimController extends Controller {
	private Location[][] world;
	private int width, height;
	
	@FXML private AnchorPane rootPane;
	
	@FXML private TextField txtIterations, txtRed, txtGreen, txtBlue;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		//Get the user's preferred size of the world.
		width = 500;
		height = 500;
		
		if(width == -1 || height == -1) {
			//Make sure that both the width and height were successfully obtained from the user.
			try {
				throw new Exception("Invalid dimensions for game world.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		initWorld();//Initialize the world.
	}
	
	@FXML private void btnStartClick(){
		drawWorld();
		
		int t = Integer.parseInt(txtIterations.getText());
		int r = Integer.parseInt(txtRed.getText());
		int g = Integer.parseInt(txtGreen.getText());
		int b = Integer.parseInt(txtBlue.getText());
		
		SACOHelper saco = new SACOHelper(world, false, t, r, g, b);
	}

	//-- Draws the world on the AnchorPane. --//
	private void drawWorld() {
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				int r = world[i][j].getFoodContents().getR();
				int g = world[i][j].getFoodContents().getG();
				int b = world[i][j].getFoodContents().getB();
				
				Circle c = new Circle(1);
				c.setTranslateX(i);
				c.setTranslateY(j);
				c.setFill(Color.rgb(r, g, b));
				
				rootPane.getChildren().add(c);
			}
		}
	}
	
	// -- Initialized the world. Obtain width and height from user before calling. --//
	private void initWorld() {
		//Width or height cannot be zero.
		if (width == 0 || height == 0) {
			makeCustomAlert("Please enter appropriate dimensions for the world.");
			return;
		}

		world = new Location[width][height];

		// Initialize each pixel with random numbers.
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				//Give the pixel a value between 0 and 255.
				Coordinate c = new Coordinate(randPixelValue(), randPixelValue(), randPixelValue());
				world[i][j] = new Location(c);
			}
		}
	}
	
	//-- Returns a random value between 0 and 255 both inclusive. --//
	private int randPixelValue() {
		return new Random().nextInt(256);
	}
	
	@FXML
	void btnClickClick(MouseEvent e) {
		System.out.println("x = " + e.getSceneX() + ", y = " + e.getSceneY());
	}

	/*//--This function gets an integer value from the user. --//
	private int getDimension(String name) {
		TextInputDialog in = new TextInputDialog();
		in.setTitle(name);
		in.setHeaderText(name + " of world required.");
		in.setContentText("Enter the " + name + " of the plane:");
		Optional<String> dim = in.showAndWait();
		
		if(dim.isPresent())
			return Integer.parseInt(dim.get());
		
		return -1;
	}*/
}

package ui;

import java.util.Optional;

import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

//Abstract class for storing functions that will be common to different types of controllers.
public abstract class Controller implements Initializable {

	private static Stage stage;

	// method for handling closing how the program will close.
	public static void closeWindow(Stage stage, boolean confirmClose) {
		if(Controller.stage == null)
			Controller.stage = stage; // Get the scene that contains the node passed as a
															// parameter.
		// Check if the program is allowed to close without confirming with the user
		// first.
		if (!confirmClose) {
			Controller.stage.close();
			return; // Return control to the caller.
		}
		makeCloseAlert(Controller.stage); // Confirm before closing.
	}
	
	// Helper function for first confirming with the user before closing the window.
	private static void makeCloseAlert(Stage stage) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Close Application");
		alert.setHeaderText("You are about to exit SACO Clutering.");
		alert.setContentText("Are you sure you want to exit?");
		findCenter(alert);
		Optional<ButtonType> result = alert.showAndWait(); // Wait for the user's input before returning control to the
															// caller.
		if (result.get() == ButtonType.OK)
			stage.close();
	}

	public static void makeCustomAlert(String text) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("StraFinder");
		alert.setHeaderText(text);
		findCenter(alert);
		alert.showAndWait();
	}

	// Helper function for ensuring that the alert box passed as a parameter is
	// always at the center of the parent window.
	private static void findCenter(Alert alert) {
		double x1 = stage.getX();
		double y1 = stage.getY();
		double x2 = x1 + stage.getWidth();
		double y2 = y1 + stage.getHeight();
		double xa = 0.5 * (x1 + x2);
		double ya = 0.5 * (y1 + y2);
		double xf = xa - 185;
		double yf = ya - 70;
		alert.setX(xf);
		alert.setY(yf);
	}
}


import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import ui.SimController;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {
	private Stage window; // This is our main stage.

	@Override
	public void start(Stage primaryStage) {
		this.window = primaryStage;
		loadFXML(); // Load the scene from the FXML file responsible for rendering the start scene.
	}

	private void loadFXML() {
		try {
			Parent parent = FXMLLoader.load(getClass().getResource("ui/Sim.fxml"));
			window.setScene(new Scene(parent));
			window.setTitle("SACO Clustering");
			window.setOnCloseRequest(e -> {
				e.consume(); // Consume the event so we can handle it manually.
				SimController.closeWindow(window, true);
			});
			window.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}

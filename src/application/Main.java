package application;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

	// define your offsets here
	private double xOffset = 0;
	private double yOffset = 0;

	@Override
	public void start(Stage primaryStage) {
		try {
			Parent login = FXMLLoader.load(getClass().getResource("Login.fxml"));
			primaryStage.initStyle(StageStyle.DECORATED);
			primaryStage.setMaximized(false);

			login.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					xOffset = event.getSceneX();
					yOffset = event.getSceneY();
				}
			});

			login.setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					primaryStage.setX(event.getScreenX() - xOffset);
					primaryStage.setY(event.getScreenY() - yOffset);
				}
			});

			Scene scene = new Scene(login);
			primaryStage.setScene(scene);
			primaryStage.show();

//    	  FXMLLoader loader= new FXMLLoader(getClass().getResource("Root.fxml"));
//    	  AnchorPane root = loader.load();
//    	  AppController controller = loader.getController();
//    	  controller.setPrimaryStage(primaryStage);
//    	  controller.setRoot(root);
//    	  Scene scene = new Scene(root);			
//    	  primaryStage.setTitle("BlueMarble");
//    	  primaryStage.setScene(scene);
//    	  primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}

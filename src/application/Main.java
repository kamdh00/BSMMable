package application;


import Controller.LoginController;
import conn.PlayerClient;
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
	PlayerClient SocketConnect;
	LoginController loginController;
	String id;
	@Override
	public void start(Stage primaryStage) {
		try {
			SocketConnect = new PlayerClient("127.0.0.1");			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));						
			Parent login = loader.load();
			loginController = loader.getController();
			loginController.setSocket(SocketConnect);
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

		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	

	@Override
	public void stop() throws Exception {
		
		if (!loginController.getId().equals("success")) {
			System.out.println(loginController.getId());
			SocketConnect.getOutMsg().println("Finish/"+loginController.getId());
			System.out.println("stop!");
		}
		super.stop();
	}


	public static void main(String[] args) {
		launch(args);
	}
}
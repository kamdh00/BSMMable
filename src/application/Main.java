package application;

import Controller.AppController;
import Controller.LoginController;
import conn.PlayerClient;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

	// define your offsets here
	private double xOffset = 0;
	private double yOffset = 0;
	PlayerClient SocketConnect;
	LoginController loginController;
	AppController appController;
	String myId;
	String yourId;
	FXMLLoader loader;
	Parent login;
	AnchorPane root;
	int closecnt = 0;

	public void setMyId(String myId) {
		this.myId = myId;
	}

	public void setYourId(String yourId) {
		this.yourId = yourId;
	}

	@Override
	public void init() throws Exception {
		SocketConnect = new PlayerClient("127.0.0.1");
		FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("Login.fxml"));
		login = loginLoader.load();
		loginController = loginLoader.getController();

		FXMLLoader rootLoader = new FXMLLoader(getClass().getResource("Root.fxml"));
		root = rootLoader.load();
		appController = rootLoader.getController();
		appController.setSocket(SocketConnect);
		loginController.setSocket(SocketConnect);
		loginController.setRoot(root);
		loginController.setAppController(appController);
		super.init();
	}

	@Override
	public void start(Stage primaryStage) {
		try {
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
		SocketConnect.getOutMsg().println("CheckMatching");
		super.stop();
		System.exit(0);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
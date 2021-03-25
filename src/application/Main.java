package application;
	
import Controller.AppController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class Main extends Application {
	@Override
   public void start(Stage primaryStage) {
      try {         
         FXMLLoader loader= new FXMLLoader(getClass().getResource("root.fxml"));
         Parent root = loader.load();         
         AppController controller = loader.getController();
         controller.setPrimaryStage(primaryStage);
         Scene scene = new Scene(root);         
         primaryStage.setTitle("BlueMarble");         
         primaryStage.setScene(scene);
         primaryStage.show();         
      } catch(Exception e) {
         e.printStackTrace();
      }
   }
	
	public static void main(String[] args) {
		launch(args);
	}
}

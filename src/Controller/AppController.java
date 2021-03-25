package Controller;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class AppController implements Initializable {
   @FXML
   Button btn1;
   @FXML
   Circle C1;
   @FXML
   StackPane Dice1;
   @FXML
   StackPane Dice2;
   @FXML
   ImageView DiceImg1;
   @FXML
   ImageView DiceImg2;
   @FXML
   Text DiceText1;
   @FXML
   Text DiceText2;

   private static int TOP = 0;
   private static int LEFT = 1;
   private static int BOTTOM = 2;
   private static int RIGHT = 3;
   int position = BOTTOM;
   int d;
   private Stage primaryStage;   
   @Override
   public void initialize(URL location, ResourceBundle resources) {
      btn1.setOnAction((event) ->dialogTest(event)); 
      
      
   }

   
   public void dialogTest(ActionEvent e){
      int t=diceAction();
      Thread thread = new Thread() {         
         @Override
         public void run() {   
            btn1.setDisable(true);
            for (int i = 0; i < t; i++) {
               Platform.runLater(()->{
                  
                  setPosition(C1.getLayoutX(), C1.getLayoutY());
               });
               try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
            }
            btn1.setDisable(false);
            Platform.runLater(()->{
               Stage dialog = new Stage(StageStyle.UTILITY);
                dialog.initModality(Modality.WINDOW_MODAL); dialog.initOwner(primaryStage);
                dialog.setTitle("확인"); AnchorPane anchorPane = null; try { anchorPane =
                (AnchorPane) FXMLLoader.load(getClass().getResource("dialogtest.fxml")); }
                catch (IOException e2) { e2.printStackTrace(); } Scene scene = new
                Scene(anchorPane);
                dialog.setScene(scene);
                dialog.show();
            });
             
            
         }
         
      };
      
      thread.setDaemon(true);
      thread.start();
      
   }

   public int diceAction() {
      Random random = new Random();
      String[] str= {"dice1.JPG","dice2.JPG","dice3.JPG","dice4.JPG","dice5.JPG","dice6.JPG"};      
      int d1 = random.nextInt(6) + 1;
      int d2 = random.nextInt(6) + 1;
      Image img = new Image("file:../../resources/images/"+str[d1-1]);
      Image img2 = new Image("file:../../resources/images/"+str[d2-1]);
      DiceImg1.setImage(img);
      DiceImg2.setImage(img2);
//      DiceText1.setText(String.valueOf(d1));
//      DiceText2.setText(String.valueOf(d2));
      return d1 + d2;
   }

   public void setPosition(double curX, double curY) {
      if (position == BOTTOM) {
         C1.setLayoutX(curX - (890 - 140) / 9);
         if ((C1.getLayoutX() - (890 - 140) / 9) < 0) {
            position = LEFT;
         }
      } else if (position == LEFT) {
         C1.setLayoutY(curY - (610 - 220) / 5);
         if ((C1.getLayoutY() - (610 - 220) / 5) < 110) {
            position = TOP;
         }
      } else if (position == TOP) {
         C1.setLayoutX(curX + (890 - 140) / 9);
         if ((C1.getLayoutX() + (890 - 140) / 9) > 890) {
            position = RIGHT;
         }
      } else if (position == RIGHT) {
         C1.setLayoutY(curY + (610 - 220) / 5);
         if ((C1.getLayoutY() + (610 - 220) / 5) > 500) {
            position = BOTTOM;
         }
      }
   }
   public void setPrimaryStage(Stage primaryStage) {
      this.primaryStage = primaryStage;
      
   }
   
   

}
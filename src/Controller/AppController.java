package Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.VLineTo;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class AppController implements Initializable {
	@FXML
	Button btn1;
	@FXML
	Button BuyPlanet;
	@FXML
	Circle C1,C2;
	@FXML
	StackPane Dice1;
	@FXML
	StackPane Dice2;
	@FXML
	ImageView DiceImg1;
	@FXML
	ImageView DiceImg2;
	@FXML
	TextField Money1;

	private static int TOP = 0;
	private static int LEFT = 1;
	private static int BOTTOM = 2;
	private static int RIGHT = 3;
	int posi[] = {BOTTOM,BOTTOM};
	private static int money = 3000000;
	private static String user[] = {"kam","yoo"}; // 임시로 하드코딩 추후 로그인 한 사용자를 Main에서 받는것으로 대체 필요
	int position[] = {0,0};	
	int d1;
	int d2;

	int cntX[] = {0,0};
	int cntY[] = {0,0};
	int buy[];
	int turn = 0;
	private Stage primaryStage;
	private AnchorPane root;
	ArrayList<PieceXY> pieceXY = new ArrayList<PieceXY>();
	ArrayList<PlanetData> planetData = new ArrayList<PlanetData>();
	PathTransition pathTransition = new PathTransition();
	Path path;
	
	Queue<Integer> randQueue = new LinkedList<Integer>();	// 카드 이미지 번호 랜덤으로 저장
	Random rand = new Random();
	

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setSecretCard();
		
		Money1.setText("보유금액 : " + money + "원");
		Money1.setEditable(false);
		btn1.setOnAction((event) -> rollTheDice(event));
	}

	public void rollTheDice(ActionEvent e) {

		Thread thread = new Thread() {

			@Override
			public void run() {
				Platform.runLater(() -> {
					setDice();
				});
				Platform.runLater(() -> {
					setDiceImage();
				});
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				btn1.setDisable(true);
				path = new Path();
				Platform.runLater(() -> {
					for (int i = 0; i < d1 + d2; i++) {
						setPosition();
					}
					pathTransition.setDuration(Duration.millis(150 * (d1 + d2)));
					pathTransition.setPath(path);
					if(turn==0)
					{
						pathTransition.setNode(C1);						
					}
					else {
						pathTransition.setNode(C2);						
					}
					pathTransition.play();

				});
				try {
					Thread.sleep(150 * (d1 + d2) + 300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Platform.runLater(() -> {
					btn1.setDisable(false);
					showDialog();
					if (turn==0) {
						turn=1;
					}
					else {
						turn=0;
					}
				});
			}

		};
		thread.setDaemon(true);
		thread.start();
	}

	public void setSecretCard() {
		int count = 0;
		while(count < 30) {
			int check = rand.nextInt(30);
			if(!randQueue.contains(check)) {
				randQueue.add(check);
				count++;
			}
		}
	}
	
	public void setDice() {
		Random random = new Random();
		d1 = random.nextInt(6) + 1;
		d2 = random.nextInt(6) + 1;
	}

	public void setDiceImage() {
		String[] str = { "dice1.PNG", "dice2.PNG", "dice3.PNG", "dice4.PNG", "dice5.PNG", "dice6.PNG" };
		Image img = new Image("file:../../resources/images/" + str[d1 - 1]);
		Image img2 = new Image("file:../../resources/images/" + str[d2 - 1]);
		DiceImg1.setImage(img);
		DiceImg2.setImage(img2);
	}
	
	/*
	 * 카드 번호에 따라 실행되는 동작 함수 
	*/
	public void actionSecretCard(int num) {
		switch(num) {
			case 0:	// 소유한 본인땅의 모든 건물 삭제				
				for(int i=0;i<planetData.size();i++) {
					if(user.equals(planetData.get(i).name)) {
						PlanetData pd = new PlanetData(planetData.get(i).name, user[turn], planetData.get(i).data, planetData.get(i).price,0,planetData.get(i).count);
						planetData.set(i,pd);
					}
				}
				break;
			case 1:	// 후원금 30만원 받음
				money += 300000;
				Money1.setText("보유금액 : " + money + "원");
				break;
			case 2:	// 상대방에게 돈 30만원 뺏어옴
				money += 300000;
				Money1.setText("보유금액 : " + money + "원");
				// 상대편 금액 -30만원 로직 추가해야함. socket으로 -30만원 전송
				break;
			case 3:	// 상대땅 하나 가져오기
				takeLand();
				break;
			default:
				money += 1000000;
				Money1.setText("보유금액 : " + money + "원");
				break;
		}
		
	}

	public void showDialog() {
		String name = planetData.get(position[turn]).name;
		String owner = planetData.get(position[turn]).owner;
		String data = planetData.get(position[turn]).data;
		int price = planetData.get(position[turn]).price;
		int building = planetData.get(position[turn]).building;
		int count = planetData.get(position[turn]).count;
		int tmp = turn;
		
		AnchorPane anchorPane = null;
		Stage dialog = new Stage(StageStyle.UTILITY);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(primaryStage);
		dialog.setTitle("확인");

		if (!owner.equals(user[turn]) && !owner.equals("X") && price != 0) { // 내 땅이 아니면 이용료 지불

			try {
				anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("dialog2.fxml"));
			} catch (IOException e2) {
				e2.printStackTrace();
			}

			Label BuyMessage = (Label) anchorPane.lookup("#BuyMessage");
			Label MyMoney = (Label) anchorPane.lookup("#MyMoney");

			if (money < price) {
				BuyMessage.setText("보유금액이 부족합니다.");
				MyMoney.setText("보유금액 : " + money + "원");
			} else {
				BuyMessage.setText(name + "의 통행료 " + price + "원을 지불을 하였습니다.");
				money -= price;
				MyMoney.setText("보유금액 : " + money + "원");
				Money1.setText("보유금액 : " + money + "원");
			}

			Button Complete = (Button) anchorPane.lookup("#Complete");
			Complete.setOnAction(event -> dialog.close());

			Scene scene = new Scene(anchorPane);
			dialog.setScene(scene);
			dialog.show();

		} else {
			try {
				if(name.equals("비밀카드")) {
					anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("secretCard.fxml"));
					ImageView secretCard = (ImageView) anchorPane.lookup("#secretCard");
					int cardNum;
					if(randQueue.poll() == null) {	// 모든 비밀카드가 오픈되고 나면 비밀카드 다시 채워줌
						setSecretCard();
						cardNum = randQueue.poll();
					}else {
						cardNum = randQueue.poll();
					}
					String imgName = cardNum +".png";
					Image img = new Image("file:../../resources/images/secretCard/" + imgName);
					secretCard.setImage(img);
					Button Complete = (Button) anchorPane.lookup("#Complete");
					Complete.setOnAction(event -> dialog.close());
					actionSecretCard(cardNum);
				}else {
					anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("dialogtest.fxml"));
					BuyPlanet = (Button) anchorPane.lookup("#BuyPlanet");
					BuyPlanet.setVisible(true);
					Label PlanetName = (Label) anchorPane.lookup("#PlanetName");
					Label PlanetOwner = (Label) anchorPane.lookup("#PlanetOwner");
					Label PlanetData = (Label) anchorPane.lookup("#PlanetData");
					PlanetName.setText(name);
					PlanetOwner.setText(owner);
					PlanetData.setText(data);
					if (!owner.equals("X") || price == 0) {
						BuyPlanet.setVisible(false);
					}
					
					BuyPlanet.setOnAction(event -> BuyLand(name, owner, data, price, building,tmp));
					Button Complete = (Button) anchorPane.lookup("#Complete");
					Complete.setOnAction(event -> dialog.close());
				}
			} catch (IOException e2) {
				e2.printStackTrace();
			}



			Scene scene = new Scene(anchorPane);
			dialog.setScene(scene);
			dialog.show();
		}

	}
	
	public void takeLand() {
		Stage dialog = new Stage(StageStyle.UTILITY);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(primaryStage);
		dialog.setTitle("상대땅 가져오기");		
		AnchorPane anchorPane = null;

		try {
			anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("landChange.fxml"));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

	public void BuyLand(String name, String owner, String data, int price, int building, int turn) {
		Stage dialog = new Stage(StageStyle.UTILITY);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(primaryStage);
		dialog.setTitle("구매 확인");
		AnchorPane anchorPane = null;

		try {
			anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("dialog2.fxml"));
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		Label BuyMessage = (Label) anchorPane.lookup("#BuyMessage");
		Label MyMoney = (Label) anchorPane.lookup("#MyMoney");

		Button Complete = (Button) anchorPane.lookup("#Complete");

		Complete.setOnAction(event -> dialog.close());
		Rectangle rec[]=new Rectangle[2];
		rec[0]=new Rectangle(pieceXY.get(position[turn]-1).pieceX+10, pieceXY.get(position[turn]-1).pieceY,40,40);
		rec[0].setFill(Color.DODGERBLUE);
		rec[1]=new Rectangle(pieceXY.get(position[turn]-1).pieceX+10, pieceXY.get(position[turn]-1).pieceY,40,40);
		rec[1].setFill(Color.RED);
		ImageView flag= new ImageView();

		if (money < price) {
			BuyMessage.setText("보유금액이 부족합니다.");
			MyMoney.setText("보유금액 : " + money + "원");
		} else {
			if(planetData.get(position[turn]).owner.equals("X"))
			{
				planetData.get(position[turn]).owner=user[turn];
			}
			if(!planetData.get(position[turn]).owner.equals(user[turn])) {
				planetData.get(position[turn]).count=0;	
			}
			if(planetData.get(position[turn]).count==0) {
				Image im = new Image("file:../../resources/images/flag"+(turn+1)+".PNG");
				flag.setLayoutX(pieceXY.get(position[turn]-1).pieceX);
				flag.setLayoutY(pieceXY.get(position[turn]-1).pieceY);
				flag.setFitHeight(30);
				flag.setFitWidth(20);
				flag.setImage(im);
				while(root.getChildren().size()!=6) {
					root.getChildren().remove(root.getChildren().size()-1);
				}
				root.getChildren().add(flag);				
				planetData.get(position[turn]).count++;
				planetData.get(position[turn]).owner=user[turn];
				BuyMessage.setText("구매 완료하였습니다.");
				money -= price;
				MyMoney.setText("보유금액 : " + money + "원");
				Money1.setText("보유금액 : " + money + "원");
			}
			else if(planetData.get(position[turn]).count==1) {				
				root.getChildren().add(rec[turn]);
				planetData.get(position[turn]).count++;

				planetData.get(position[turn]).owner=user[turn];
				BuyMessage.setText("구매 완료하였습니다.");
				money -= price;
				MyMoney.setText("보유금액 : " + money + "원");
				Money1.setText("보유금액 : " + money + "원");
			}
		}
		BuyPlanet.setVisible(false);
		Scene scene = new Scene(anchorPane);
		dialog.setScene(scene);
		dialog.show();
	}

	public void setPlanetData() {

	}

	public void setPosition() {

		path.getElements().add(new MoveTo(cntX[turn], cntY[turn]));
		if (posi[turn] == BOTTOM) {
			path.getElements().add(new HLineTo(cntX[turn] - 83));
			cntX[turn] -= 83;

			if (cntX[turn] == -747) {
				posi[turn] = LEFT;
			}
		} else if (posi[turn] == LEFT) {
			path.getElements().add(new VLineTo(cntY[turn] - 83));
			cntY[turn] -= 83;
			if (cntY[turn] == -415) {
				posi[turn] = TOP;
			}

		} else if (posi[turn] == TOP) {
			path.getElements().add(new HLineTo(cntX[turn] + 83));
			cntX[turn] += 83;
			if (cntX[turn] == 0) {
				posi[turn] = RIGHT;
			}
		} else if (posi[turn] == RIGHT) {
			path.getElements().add(new VLineTo(cntY[turn] + 83));
			cntY[turn] += 83;
			if (cntY[turn] == 0) {
				posi[turn] = BOTTOM;
			}
		}
		position[turn]++;
		if (position[turn] == pieceXY.size()) {
			position[turn] = 0;
			money += 200000;
			Money1.setText("보유금액:" + money + "원");
		}
	}
	
	public void setRoot(AnchorPane root) {
    	this.root = root;
    }

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;

	}

	public AppController() {
		pieceXY.add(new PieceXY(740, 505));
		pieceXY.add(new PieceXY(655, 505));
		pieceXY.add(new PieceXY(572, 505));
		pieceXY.add(new PieceXY(487, 505));
		pieceXY.add(new PieceXY(405, 505));
		pieceXY.add(new PieceXY(320, 505));
		pieceXY.add(new PieceXY(237, 505));
		pieceXY.add(new PieceXY(155, 505));
		pieceXY.add(new PieceXY(70, 505));
		pieceXY.add(new PieceXY(70, 425));
		pieceXY.add(new PieceXY(70, 348));
		pieceXY.add(new PieceXY(70, 266));
		pieceXY.add(new PieceXY(70, 186));
		pieceXY.add(new PieceXY(70, 105));
		pieceXY.add(new PieceXY(155, 105));
		pieceXY.add(new PieceXY(237, 105));
		pieceXY.add(new PieceXY(320, 105));
		pieceXY.add(new PieceXY(405, 105));
		pieceXY.add(new PieceXY(487, 105));
		pieceXY.add(new PieceXY(572, 105));
		pieceXY.add(new PieceXY(655, 105));
		pieceXY.add(new PieceXY(740, 105));
		pieceXY.add(new PieceXY(822, 105));
		pieceXY.add(new PieceXY(822, 186));
		pieceXY.add(new PieceXY(822, 266));
		pieceXY.add(new PieceXY(822, 348));
		pieceXY.add(new PieceXY(822, 425));
		pieceXY.add(new PieceXY(822, 505));

		planetData.add(new PlanetData("지구", "X", "수고비 20만원", 200000, 0, 0));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0, 0));
		planetData.add(new PlanetData("화성", "X", "20만원", 200000, 0, 0));
		planetData.add(new PlanetData("목성", "X", "15만원", 150000, 0, 0));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0, 0));
		planetData.add(new PlanetData("토성", "X", "8만원", 80000, 0, 0));
		planetData.add(new PlanetData("천왕성", "X", "13만원", 130000, 0, 0));
		planetData.add(new PlanetData("해왕성", "X", "25만원", 250000, 0, 0));
		planetData.add(new PlanetData("물병자리", "X", "12만원", 120000, 0, 0));
		planetData.add(new PlanetData("워프", "X", "이동", 0, 0, 0));
		planetData.add(new PlanetData("명왕성", "X", "40만원", 400000, 0, 0));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0, 0));
		planetData.add(new PlanetData("안드로메다", "X", "45만원", 450000, 0, 0));
		planetData.add(new PlanetData("북극성", "X", "44만원", 440000, 0, 0));
		planetData.add(new PlanetData("블랙홀", "X", "2턴 멈춤", 0, 0, 0));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0, 0));
		planetData.add(new PlanetData("달", "X", "100만원", 1000000, 0, 0));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0, 0));
		planetData.add(new PlanetData("태양", "X", "120만원", 1200000, 0, 0));
		planetData.add(new PlanetData("시리우스", "X", "90만원", 900000, 0, 0));
		planetData.add(new PlanetData("프로키온", "X", "95만원", 950000, 0, 0));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0, 0));
		planetData.add(new PlanetData("베크록스", "X", "80만원", 800000, 0, 0));
		planetData.add(new PlanetData("조난기지", "X", "1턴멈춤 기부 50만원", 500000, 0, 0));
		planetData.add(new PlanetData("아크록스", "X", "65만원", 650000, 0, 0));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0, 0));
		planetData.add(new PlanetData("수성", "X", "55만원", 550000, 0, 0));
		planetData.add(new PlanetData("금성", "X", "60만원", 600000, 0, 0));

	}

}
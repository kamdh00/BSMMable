package Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
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
	TextField Money1;

	private static int TOP = 0;
	private static int LEFT = 1;
	private static int BOTTOM = 2;
	private static int RIGHT = 3;
	int posi = BOTTOM;
	private static int money = 3000000;
	private static String user = "kam"; // 임시로 하드코딩 추후 로그인 한 사용자를 Main에서 받는것으로 대체 필요
	int position = 0;
	int d1;
	int d2;

	int cntX = 0;
	int cntY = 0;
	private Stage primaryStage;
	ArrayList<PieceXY> pieceXY = new ArrayList<PieceXY>();
	ArrayList<PlanetData> planetData = new ArrayList<PlanetData>();
	PathTransition pathTransition = new PathTransition();
	Path path;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
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
					pathTransition.setNode(C1);
					pathTransition.play();

				});
				try {
					Thread.sleep(50 * (d1 + d2) + 300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Platform.runLater(() -> {
					btn1.setDisable(false);
					showDialog();
				});
			}

		};
		thread.setDaemon(true);
		thread.start();
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

	public void showDialog() {
		String name = planetData.get(position).name;
		String owner = planetData.get(position).owner;
		String data = planetData.get(position).data;
		int price = planetData.get(position).price;
		AnchorPane anchorPane = null;

		if (!owner.equals(user) && !owner.equals("X") && price != 0) { // 내 땅이 아니면 이용료 지불
			Stage dialog = new Stage(StageStyle.UTILITY);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.initOwner(primaryStage);
			dialog.setTitle("확인");

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

			Stage dialog = new Stage(StageStyle.UTILITY);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.initOwner(primaryStage);
			dialog.setTitle("확인");
			try {
				anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("dialogtest.fxml"));

			} catch (IOException e2) {
				e2.printStackTrace();
			}

			Button Complete = (Button) anchorPane.lookup("#Complete");
			Complete.setOnAction(event -> dialog.close());

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

			BuyPlanet.setOnAction(event -> BuyLand(name, owner, data, price));

			Scene scene = new Scene(anchorPane);
			dialog.setScene(scene);
			dialog.show();
		}

	}

	public void BuyLand(String name, String owner, String data, int price) {
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

		if (money < price) {
			BuyMessage.setText("보유금액이 부족합니다.");
			MyMoney.setText("보유금액 : " + money + "원");
		} else {
			PlanetData pd = new PlanetData(name, user, data, price);
			planetData.set(position - 1, pd); // 구매 완료시 땅 소유주 접속 user 명으로 변경
			BuyMessage.setText("구매 완료하였습니다.");
			money -= price;
			MyMoney.setText("보유금액 : " + money + "원");
			Money1.setText("보유금액 : " + money + "원");
		}
		BuyPlanet.setVisible(false);
		Scene scene = new Scene(anchorPane);
		dialog.setScene(scene);
		dialog.show();
	}

	public void setPlanetData() {

	}

	public void setPosition() {

		path.getElements().add(new MoveTo(cntX, cntY));
		if (posi == BOTTOM) {
			path.getElements().add(new HLineTo(cntX - 83));
			cntX -= 83;

			if (cntX == -747) {
				posi = LEFT;
			}
		} else if (posi == LEFT) {
			path.getElements().add(new VLineTo(cntY - 83));
			cntY -= 83;
			if (cntY == -415) {
				posi = TOP;
			}

		} else if (posi == TOP) {
			path.getElements().add(new HLineTo(cntX + 83));
			cntX += 83;
			if (cntX == 0) {
				posi = RIGHT;
			}
		} else if (posi == RIGHT) {
			path.getElements().add(new VLineTo(cntY + 83));
			cntY += 83;
			if (cntY == 0) {
				posi = BOTTOM;
			}
		}
		position++;
		if (position == pieceXY.size()) {
			position = 0;
			money += 200000;
			Money1.setText("보유금액:" + money + "원");
		}
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

		planetData.add(new PlanetData("지구", "X", "수고비 20만원", 200000));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0));
		planetData.add(new PlanetData("화성", "X", "20만원", 200000));
		planetData.add(new PlanetData("목성", "X", "15만원", 150000));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0));
//      planetData.add(new PlanetData("토성", "X", "8만원", 80000));
//      planetData.add(new PlanetData("천왕성", "X", "13만원", 130000));
//      planetData.add(new PlanetData("해왕성", "X", "25만원", 250000));
//      planetData.add(new PlanetData("물병자리", "X", "12만원", 120000));

		// 통행료 지불 테스트를 위해 임시 owner 설정
		planetData.add(new PlanetData("토성", "Big", "8만원", 80000));
		planetData.add(new PlanetData("천왕성", "Big", "13만원", 130000));
		planetData.add(new PlanetData("해왕성", "Big", "25만원", 250000));
		planetData.add(new PlanetData("물병자리", "Big", "12만원", 120000));

		planetData.add(new PlanetData("워프", "X", "이동", 0));
		planetData.add(new PlanetData("명왕성", "X", "40만원", 400000));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0));
		planetData.add(new PlanetData("안드로메다", "X", "45만원", 450000));
		planetData.add(new PlanetData("북극성", "X", "44만원", 440000));
		planetData.add(new PlanetData("블랙홀", "X", "2턴 멈춤", 0));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0));
		planetData.add(new PlanetData("달", "X", "100만원", 1000000));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0));
		planetData.add(new PlanetData("태양", "X", "120만원", 1200000));
		planetData.add(new PlanetData("시리우스", "X", "90만원", 900000));
		planetData.add(new PlanetData("프로키온", "X", "95만원", 950000));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0));
		planetData.add(new PlanetData("베크록스", "X", "80만원", 800000));
		planetData.add(new PlanetData("조난기지", "X", "1턴멈춤 기부 50만원", 500000));
		planetData.add(new PlanetData("아크록스", "X", "65만원", 650000));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0));
		planetData.add(new PlanetData("수성", "X", "55만원", 550000));
		planetData.add(new PlanetData("금성", "X", "60만원", 600000));

	}

}
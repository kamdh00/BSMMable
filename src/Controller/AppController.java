package Controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.ResourceBundle;

import conn.DBConn;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class AppController implements Initializable {
	@FXML
	Button btn1;
	Button BuyPlanet, CostPlanet, choiceLand;
	@FXML
	Circle C1, C2;
	@FXML
	StackPane Dice1, Dice2;
	@FXML
	ImageView DiceImg1, DiceImg2;
	@FXML
	TextField Money1, Money2;
	Label PlanetOwner;
	@FXML
	Label myName, yourName, myWinLose, yourWinLose;	
	private static int TOP = 0;
	private static int LEFT = 1;
	private static int BOTTOM = 2;
	private static int RIGHT = 3;
	int posi[] = { BOTTOM, BOTTOM };
	private static int money[] = { 200000, 200000 };
	private static String user[] = { "kam", "yoo" }; // 임시로 하드코딩 추후 로그인 한 사용자를 Main에서 받는것으로 대체 필요
	int position[] = { 0, 0 };
	int d1;
	int d2;

//	int cardNum = 22; // test용
	int Cardposi = 0; // 카드로 나온 포지션.
	int bre[] = { 0, 0 };

	int cnt1 = 0;// 상대방 땅 개수
	int cnt2 = 0;// 내땅 개수
	int cntX[] = { 0, 0 };
	int cntY[] = { 0, 0 };
	int buy[];
	int turn = 0;
	int curturn = 0;
	int win = 1, lose = 1;
	private Stage primaryStage;
	private AnchorPane root;
	ArrayList<PieceXY> pieceXY = new ArrayList<PieceXY>();
	ArrayList<PlanetData> planetData = new ArrayList<PlanetData>();
	PathTransition pathTransition = new PathTransition();
	Path path;
	Popup doublePopup = new Popup();
	Label doubleLabel = new Label();
	Queue<Integer> randQueue = new LinkedList<Integer>(); // 카드 이미지 번호 랜덤으로 저장
	Random rand = new Random();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setSecretCard();
		doubleLabel.setTextFill(Color.WHITE);
		doubleLabel.setText("더블!! 한번 더");
		doubleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 30px");
		doublePopup.getContent().add(doubleLabel);
		Money1.setText("보유금액 : " + money[0] + "원");
		Money1.setEditable(false);
		Money2.setText("보유금액 : " + money[1] + "원");
		Money2.setEditable(false);
		btn1.setOnAction((event) -> rollTheDice(event));
		checkMoney();
	}
	
	public void checkMoney() {
		Thread thread = new Thread() {

			@Override
			public void run() {
				while (money[curturn] >= 0) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				Platform.runLater(() -> {
					Stage findialog = new Stage(StageStyle.UTILITY);
					findialog.initModality(Modality.WINDOW_MODAL);
					findialog.initOwner(primaryStage);
					AnchorPane anchorPane = null;
					try {
						anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("fin_dialog.fxml"));
					} catch (IOException e2) {
						e2.printStackTrace();
					}
					Label FinMessage = (Label) anchorPane.lookup("#FinMessage");
					FinMessage.setText("승리 : " + user[(curturn + 1) % 2]);
					Button FinButton = (Button) anchorPane.lookup("#FinButton");
					FinButton.setOnAction(e -> {
						findialog.close();
						Connection con = null;
						PreparedStatement psmt = null;
						try {
							con = DBConn.getConnection();
						} catch (SQLException e1) {
							System.out.println("DB연결실패");
						}
						String sql = "UPDATE MEMBER SET WIN=" + (++win) + " where id= '" + user[(curturn + 1) % 2]
								+ "'";
						try {
							psmt = con.prepareStatement(sql);
							psmt.executeUpdate();
							// psmt.executeQuery();
						} catch (SQLException e1) {
							System.out.println("db에러");
						}
						sql = "UPDATE MEMBER SET LOSE=" + (++lose) + " where id= '" + user[curturn] + "'";
						try {
							psmt = con.prepareStatement(sql);
							psmt.executeUpdate();
							// psmt.executeQuery();
						} catch (SQLException e1) {
							System.out.println("db에러");
						}

						primaryStage.close();
					});
					Scene scene = new Scene(anchorPane);
					findialog.setScene(scene);
					findialog.show();

				});

			}

		};
		thread.setDaemon(true);
		thread.start();
	}

	public void setWinLose(int win, int lose) {
		this.win = win;
		this.lose = lose;
		myWinLose.setText("승 : " + win + " 패 : " + lose);
	}

	public void setUser(String id) {
		this.user[0] = id;
		myName.setText(id + " 님");
	}

	public void rollTheDice(ActionEvent e) {
		curturn = turn;
		if (doublePopup.isShowing()) {
			doublePopup.hide();
		}
		Thread thread = new Thread() {

			@Override
			public void run() {
				if (bre[curturn] != 0) {
					bre[curturn]--;
					curturn = (curturn + 1) % 2;
					turn = curturn;

				}
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
					pathTransition.setDuration(Duration.millis(150));
					pathTransition.setPath(path);
					if (curturn == 0) {
						pathTransition.setNode(C1);
					} else {
						pathTransition.setNode(C2);
					}
					pathTransition.play();

				});
				try {
					Thread.sleep(150 + 300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Platform.runLater(() -> {
					
					btn1.setDisable(false);
					showDialog();
					
					if (d1 != d2) {

						if (curturn == 0) {
							turn = 1;
						} else {
							turn = 0;
						}

					} else {
						bre[curturn] = 0;

						doublePopup.show(primaryStage, 880, 720);

					}

				});
			}

		};
		thread.setDaemon(true);
		thread.start();
	}

	public void setSecretCard() {
		int count = 0;
		while (count < 30) {
			int check = rand.nextInt(30);
			if (!randQueue.contains(check)) {
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
	public void actionSecretCard(int num, int turn) {
		switch (num) {
		case 0: // 소유한 본인땅의 모든 건물 삭제
			for (int i = 0; i < planetData.size(); i++) {
				if (user[turn].equals(planetData.get(i).owner)) {
					for (int j = 1; j < planetData.get(i).building.size(); j++) {
						root.getChildren().remove(planetData.get(i).building.get(j));

					}
					planetData.get(i).count = 1;

				}
			}
			break;
		case 1: // 후원금 30만원 받음
			money[turn] += 300000;
			if (turn == 0) {
				Money1.setText("보유금액 : " + money[turn] + "원");
			} else {
				Money2.setText("보유금액 : " + money[turn] + "원");
			}
			break;
		case 2: // 상대방에게 돈 30만원 뺏어옴
			money[turn] += 300000;
			if (turn == 0) {
				money[1] -= 300000;
				Money1.setText("보유금액 : " + money[turn] + "원");
				Money2.setText("보유금액 : " + money[1] + "원");
			} else {
				money[0] -= 300000;
				Money1.setText("보유금액 : " + money[0] + "원");
				Money2.setText("보유금액 : " + money[turn] + "원");
			}
			// 상대편 금액 -30만원 로직 추가해야함. socket으로 -30만원 전송
			break;
		case 3: // 상대땅 하나 가져오기
			takeLand(0);
			break;
		case 4:
			money[turn] -= 1000000;
			if (turn == 0) {
				Money1.setText("보유금액 : " + money[turn] + "원");
			} else {
				Money2.setText("보유금액 : " + money[turn] + "원");
			}
			break;
		case 5:
			Cardposi = 1;
			int posi2 = position[turn];
			setPosition2(posi2);
			break;

		case 6:
			money[turn] -= 300000;
			if (turn == 0) {
				money[1] += 300000;
				Money1.setText("보유금액 : " + money[turn] + "원");
				Money2.setText("보유금액 : " + money[1] + "원");
			} else {
				money[0] += 300000;
				Money1.setText("보유금액 : " + money[0] + "원");
				Money2.setText("보유금액 : " + money[turn] + "원");
			}
			// 상대편 금액 +30만원 로직 추가해야함. socket으로 +30만원 전송
			break;

		case 7: // 블랙홀로 이동시키기.
			Cardposi = 2;
			posi2 = position[turn];
			setPosition2(posi2);
			break;

		case 8:
		case 10:
			money[turn] -= 500000;
			if (turn == 0) {
				Money1.setText("보유금액 : " + money[turn] + "원");
			} else {
				Money2.setText("보유금액 : " + money[turn] + "원");
			}
			break;

		case 9:// 당신의 땅과 상대편의 땅을 바꿉니다.
			takeLand(1);
			break;
		case 11: // 가장 비싼 땅을 반액에 팔음. 선물이 지어진 경우 반액에 처분.
			int max = 0;
			int tmp = 0;
			System.out.println("money" + money[turn]);
			for (int i = 0; i < planetData.size(); i++) {
				if (user[turn].equals(planetData.get(i).owner)) {

					max = (max > planetData.get(i).price) ? max : planetData.get(i).price;

				}

			}
			for (int i = 0; i < planetData.size(); i++) {
				if (user[turn].equals(planetData.get(i).owner)) {
					for (int j = 0; j < planetData.get(i).building.size(); j++) {
						if (max == planetData.get(i).price) {
							root.getChildren().remove(planetData.get(i).building.get(j));
							planetData.get(i).owner = "X";
							planetData.get(i).count = 0;
							if (tmp == 0) {
								money[turn] -= ((max + ((max / 2) * (planetData.get(i).building.size() - 1))) / 2);
								tmp++;
							}

						}
					}
				}
			}
			if (turn == 0) {
				Money1.setText("보유금액 : " + money[turn] + "원");
			} else {
				Money2.setText("보유금액 : " + money[turn] + "원");
			}
			break;
		case 12: // 모든 땅 반납
			for (int i = 0; i < planetData.size(); i++) {
				if (user[turn].equals(planetData.get(i).owner)) {
					for (int j = 0; j < planetData.get(i).building.size(); j++) {
						root.getChildren().remove(planetData.get(i).building.get(j));
					}
					planetData.get(i).owner = "X";
					planetData.get(i).count = 0;
				}
			}
			break;
		case 13:
			money[turn] = money[turn] - (money[turn] / 2);
			if (turn == 0) {
				Money1.setText("보유금액 : " + money[turn] + "원");
			} else {
				Money2.setText("보유금액 : " + money[turn] + "원");
			}
			break;

		case 14:
			// 지구로 돌아감. 수고비 받지 못함. 20만원 차감.
			Cardposi = 3;
			posi2 = position[turn];
			setPosition2(posi2);
			money[turn] -= 200000;
			if (turn == 0) {
				Money1.setText("보유금액 : " + money[turn] + "원");
			} else {
				Money2.setText("보유금액 : " + money[turn] + "원");
			}
			break;
		case 15: // 한턴 쉽니다.
			bre[curturn] = 1;
			brecalcu();
			break;
		case 16:
			// 지구로 돌아갑니다. 수고비를 받습니다.
			Cardposi = 3;
			posi2 = position[turn];
			setPosition2(posi2);
			break;
		case 17: // 워프로 이동.
			Cardposi = 4;
			posi2 = position[turn];
			setPosition2(posi2);
			try {
				Thread.sleep(150);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Platform.runLater(() -> {
				showDialog();
			});

			break;
		case 18: // 상대편 금액 -100만원 로직 추가해야함. socket으로 -100만원 전송
			money[turn] += 1000000;
			if (turn == 0) {
				money[1] -= 1000000;
				Money1.setText("보유금액 : " + money[turn] + "원");
				Money2.setText("보유금액 : " + money[1] + "원");
			} else {
				money[0] -= 1000000;
				Money1.setText("보유금액 : " + money[0] + "원");
				Money2.setText("보유금액 : " + money[turn] + "원");
			}
			break;
		case 19: // 원하는 땅 자신의 땅으로 만들음.
			takeLand(2);
			break;
		case 20: // 건물 두개 지을 땅 선택 (자신의 땅이여야함. 땅이 없을시 무효.)
			takeLand(3);
			break;

		case 21:
			money[turn] += 100000;
			if (turn == 0) {
				Money1.setText("보유금액 : " + money[turn] + "원");
			} else {
				Money2.setText("보유금액 : " + money[turn] + "원");
			}
			break;
		case 22: // 상대방 조난 기지에 보내기
			Cardposi = 1;
			if (turn == 0) {
				posi2 = position[1];
				setPosition2(posi2);
				curturn = 1;
			} else if (turn == 1) {
				posi2 = position[0];
				setPosition2(posi2);
				curturn = 0;
			}
			break;
		// 서버 소켓..
		case 23: // 통행료 없이 땅 지나가기
			break;

		case 24: // 10만원 내고 비밀 카드 한 장 더 뽑기
			money[turn] -= 100000;
			if (turn == 0) {
				Money1.setText("보유금액 : " + money[turn] + "원");
			} else {
				Money2.setText("보유금액 : " + money[turn] + "원");
			}
			Platform.runLater(() -> {
				oneMore();
			});
			break;

		case 25: // 상대편 금액 +30만원 로직 추가해야함. socket으로 +30만원 전송
			money[turn] += 200000;
			if (turn == 0) {
				money[1] -= 200000;
				Money1.setText("보유금액 : " + money[turn] + "원");
				Money2.setText("보유금액 : " + money[1] + "원");
			} else {
				money[0] -= 200000;
				Money1.setText("보유금액 : " + money[0] + "원");
				Money2.setText("보유금액 : " + money[turn] + "원");
			}
			break;

		case 26: // 주사위 한번 더
			bre[(curturn + 1) % 2]++;
			break;
		case 27:
			money[turn] += 200000;
			if (turn == 0) {
				Money1.setText("보유금액 : " + money[turn] + "원");
			} else {
				Money2.setText("보유금액 : " + money[turn] + "원");
			}
			break;
		case 28:
			money[turn] = 0;
			if (turn == 0) {
				Money1.setText("보유금액 : " + money[turn] + "원");
			} else {
				Money2.setText("보유금액 : " + money[turn] + "원");
			}
			break;

		case 29: // 5칸 더 앞으로 전진.
			Cardposi = 6;
			posi2 = position[turn];
			setPosition2(posi2);
			break;

		}

	}

	public void showDialog() {
		String name = planetData.get(position[curturn]).name;
		String owner = planetData.get(position[curturn]).owner;
		String data = planetData.get(position[curturn]).data;
		int price = planetData.get(position[curturn]).price;

		int count = planetData.get(position[curturn]).count;

		AnchorPane anchorPane = null;
		Stage dialog = new Stage(StageStyle.UTILITY);
		Stage dialog2 = new Stage(StageStyle.UTILITY);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(primaryStage);
		dialog.setTitle("확인");
		dialog2.initModality(Modality.WINDOW_MODAL);
		dialog2.initOwner(primaryStage);
		dialog2.setTitle("확인");

		if (!owner.equals(user[curturn]) && !owner.equals("X") && price != 0) { // 내 땅이 아니면 이용료 지불

			try {
				anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("dialog2.fxml"));
			} catch (IOException e2) {
				e2.printStackTrace();
			}

			Label BuyMessage = (Label) anchorPane.lookup("#BuyMessage");
			Label MyMoney = (Label) anchorPane.lookup("#MyMoney");

			if (money[curturn] < price + (price / 2) * (count - 1)) {
				BuyMessage.setText("보유금액이 부족합니다.");
				MyMoney.setText("보유금액 : " + money[curturn] + "원");
			} else {
				if (count == 1) {
					BuyMessage.setText(name + "의 통행료 " + price + "원을 지불을 하였습니다.");
					money[curturn] -= price;
					MyMoney.setText("보유금액 : " + money[curturn] + "원");
					if (curturn == 0) {
						money[1] += price;
						Money1.setText("보유금액 : " + money[0] + "원");
						Money2.setText("보유금액 : " + money[1] + "원");
					} else {
						money[0] += price;
						Money1.setText("보유금액 : " + money[0] + "원");
						Money2.setText("보유금액 : " + money[1] + "원");
					}
				} else if (count == 2) {
					BuyMessage.setText(name + "의 통행료 " + price * 1.5 + "원을 지불을 하였습니다.");
					money[curturn] -= price;
					MyMoney.setText("보유금액 : " + money[curturn] + "원");
					if (curturn == 0) {
						money[1] += price * 1.5;
						Money1.setText("보유금액 : " + money[0] + "원");
						Money2.setText("보유금액 : " + money[1] + "원");
					} else {
						money[0] += price * 1.5;
						Money1.setText("보유금액 : " + money[0] + "원");
						Money2.setText("보유금액 : " + money[1] + "원");
					}
				} else if (count == 3) {
					BuyMessage.setText(name + "의 통행료 " + price * 2 + "원을 지불을 하였습니다.");
					money[curturn] -= price + price;
					MyMoney.setText("보유금액 : " + money[curturn] + "원");
					if (curturn == 0) {
						money[1] += price * 2;
						Money1.setText("보유금액 : " + money[0] + "원");
						Money2.setText("보유금액 : " + money[1] + "원");
					} else {
						money[0] += price * 2;
						Money1.setText("보유금액 : " + money[0] + "원");
						Money2.setText("보유금액 : " + money[1] + "원");
					}
				} else if (count == 4) {
					BuyMessage.setText(name + "의 통행료 " + price * 2.5 + "원을 지불을 하였습니다.");
					money[curturn] -= price * 2.5;
					MyMoney.setText("보유금액 : " + money[curturn] + "원");
					if (curturn == 0) {
						money[1] += price * 2.5;
						Money1.setText("보유금액 : " + money[0] + "원");
						Money2.setText("보유금액 : " + money[1] + "원");
					} else {
						money[0] += price * 2.5;
						Money1.setText("보유금액 : " + money[0] + "원");
						Money2.setText("보유금액 : " + money[1] + "원");
					}
				}

			}

			Button Complete = (Button) anchorPane.lookup("#Complete");
			Complete.setOnAction(event -> dialog.close());

			Scene scene = new Scene(anchorPane);
			dialog.setScene(scene);
			dialog.setAlwaysOnTop(true);
			dialog.show();

		} // 이용료 지불 끝

		try {
			if (name.equals("비밀카드")) {
				anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("secretCard.fxml"));
				ImageView secretCard = (ImageView) anchorPane.lookup("#secretCard");
				int cardNum;

				if (randQueue.size() == 0) { // 모든 비밀카드가 오픈되고 나면 비밀카드 다시 채워줌
					setSecretCard();
					cardNum = randQueue.poll();
				} else {
					cardNum = randQueue.poll();
				}

				String imgName = cardNum + ".png";
				Image img = new Image("file:../../resources/images/secretCard/" + imgName);
				secretCard.setImage(img);
				Button Complete = (Button) anchorPane.lookup("#Complete");
				Complete.setOnAction(event -> {
					dialog2.close();
				});
				actionSecretCard(cardNum, curturn);
			}

			else {
				anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("dialogtest.fxml"));
				BuyPlanet = (Button) anchorPane.lookup("#BuyPlanet");
				BuyPlanet.setVisible(true);
				CostPlanet = (Button) anchorPane.lookup("#CostPlanet");
				CostPlanet.setVisible(true);
				Label PlanetName = (Label) anchorPane.lookup("#PlanetName");
				PlanetOwner = (Label) anchorPane.lookup("#PlanetOwner");
				Label PlanetData = (Label) anchorPane.lookup("#PlanetData");
				PlanetName.setText(name);
				PlanetOwner.setText(owner);
				PlanetData.setText(data);
				// 구입 금액 셋팅
				int tp = 0;
				if (owner.equals(user[curturn])) {
					if (count == 0) {
						tp = price;
						CostPlanet.setText("구입 금액 : " + tp);
					} else {
						tp = (price / 2);
						CostPlanet.setText("구입 금액 : " + tp);
					}

				} else {
					if (count == 0) {
						tp = price;
						CostPlanet.setText("구입 금액 : " + tp);
					} else {
						tp = price + (price / 2) * (count - 1);
						CostPlanet.setText("구입 금액 : " + tp);
					}
				}

				if (price == 0 || (position[curturn]) == 0 || (count == 4 && owner.equals(user[turn]))) {
					BuyPlanet.setVisible(false);
					CostPlanet.setVisible(false);
				}

				BuyPlanet.setOnAction(event -> {
					BuyLand(name, owner, data, price, curturn);
				});
				Button Complete = (Button) anchorPane.lookup("#Complete");
				if (name.equals("워프")) {
					setWarp(anchorPane, dialog2);
					Complete.setVisible(false);
				} else if (name.equals("조난기지")) {
					bre[(curturn + 1) % 2] = 1;
					brecalcu();
				} else if (name.equals("블랙홀")) {
					bre[(curturn + 1) % 2] = 2;
					brecalcu();
				} else {
					Complete.setVisible(true);
				}
				Complete.setOnAction(event -> {
					dialog2.close();
				});
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		Scene scene = new Scene(anchorPane);
		dialog2.setScene(scene);
		dialog2.show();

	}

	public void brecalcu() {
		if (bre[curturn] >= bre[(curturn + 1) % 2]) {
			bre[curturn] = bre[curturn] - bre[(curturn + 1) % 2];
			bre[(curturn + 1) % 2] = 0;
		} else if (bre[0] == bre[1]) {
			bre[0] = 0;
			bre[1] = 0;
		} else {
			bre[(curturn + 1) % 2] = bre[(curturn + 1) % 2] - bre[curturn];
			bre[curturn] = 0;
		}

	}

	public void oneMore() {
		AnchorPane anchorPane = null;

		Stage dialog3 = new Stage(StageStyle.UTILITY);
		dialog3.initModality(Modality.WINDOW_MODAL);
		dialog3.initOwner(primaryStage);
		dialog3.setTitle("알림");

		try {
			anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("dialog3.fxml"));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Label secretCard1 = (Label) anchorPane.lookup("#secretCard1");
		Label secretCard2 = (Label) anchorPane.lookup("#secretCard2");
		Button Yes = (Button) anchorPane.lookup("#Yes");
		;
		Button No = (Button) anchorPane.lookup("#No");
		;

		Yes.setOnAction((EventHandler<ActionEvent>) new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Platform.runLater(() -> {
					showDialog();
					dialog3.close();
				});
			}
		});
		No.setOnAction(event -> dialog3.close());

		Scene scene = new Scene(anchorPane);
		dialog3.setScene(scene);
		dialog3.show();

	}

	// 워프기능
	public void setWarp(AnchorPane anchorPane, Stage dialog) {
		// btn1.setDisable(true);
		Button button = new Button();
		Label label = new Label();
		ChoiceBox<String> choiceBox = new ChoiceBox<String>();
		label.setText("이동할 위치 선택 : ");
		label.setLayoutX(200);
		label.setLayoutY(100);

		choiceBox.setLayoutX(310);
		choiceBox.setLayoutY(95);
		choiceBox.setPrefWidth(100);

		int cnt = 1;
		for (int i = 0; i < planetData.size(); i++) {
			if (planetData.get(i).name.equals("비밀카드")) {
				choiceBox.getItems().add(planetData.get(i).name + cnt++);
			} else if (planetData.get(i).name.equals("워프")) {
				choiceBox.getItems().add(planetData.get(i).name + " 이동X");
			} else {
				choiceBox.getItems().add(planetData.get(i).name);
			}

		}
		choiceBox.setValue("워프 이동X");		
		button.setLayoutX(260);
		button.setLayoutY(150);
		button.setPrefWidth(100);
		button.setText("선택");
		anchorPane.getChildren().add(button);
		anchorPane.getChildren().add(choiceBox);
		anchorPane.getChildren().add(label);
		button.setOnAction(event -> getChoiceWarp(choiceBox, dialog));

	}

	// 선택 버튼 누르면 이동
	private void getChoiceWarp(ChoiceBox<String> choiceBox, Stage dialog) {
		int index = choiceBox.getItems().indexOf(choiceBox.getValue());
		int move = 0;
		if (index >= position[curturn]) {
			move = index - position[curturn];
		} else {
			move = 19 + index;
		}
		int movecount = move;
		System.out.println(move);
		dialog.close();
		System.out.println(position[curturn]);
		Thread thread = new Thread() {
			@Override
			public void run() {
				path = new Path();
				Platform.runLater(() -> {
					btn1.setDisable(true);
					for (int i = 0; i < movecount; i++) {
						setPosition();
					}
					pathTransition.setDuration(Duration.millis(150 * (movecount)));
					pathTransition.setPath(path);
					if (curturn == 0) {
						pathTransition.setNode(C1);
					} else {
						pathTransition.setNode(C2);
					}
					pathTransition.play();

				});
				try {
					Thread.sleep(150 * (movecount) + 300);
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

	/*
	 * index = 0 -> 상대땅 가져오기(비밀카드 3번) index = 1 -> 상대땅과 내땅 교환 (비밀카드 9번) index = 2 ->
	 * 원하는땅 자신의 것으로 (비밀카드 19번)
	 */
	public void takeLand(int index) {
		Stage dialog = new Stage(StageStyle.UTILITY);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(primaryStage);
		if (index == 0) {
			dialog.setTitle("상대땅 가져오기");
		} else if (index == 1) {
			dialog.setTitle("땅 교환");
		} else if (index == 1) {
			dialog.setTitle("원하는 땅 내땅으로");
		}
		AnchorPane takeLandPane = null;

		try {
			takeLandPane = (AnchorPane) FXMLLoader.load(getClass().getResource("landChange.fxml"));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		ChoiceBox yourLand = (ChoiceBox) takeLandPane.lookup("#yourLand");
		ChoiceBox myLand = (ChoiceBox) takeLandPane.lookup("#myLand");
		Label myLandLabel = (Label) takeLandPane.lookup("#myLandLabel");
		Label yourLandLabel = (Label) takeLandPane.lookup("#yourLandLabel");
		if (index != 1 && index != 3) {
			myLand.setVisible(false);
			myLandLabel.setVisible(false);
		}

		switch (index) {
		case 0:
			for (PlanetData pd : planetData) {
				if (!pd.getOwner().equals(user[curturn]) && !pd.getOwner().equals("X")) {
					yourLand.getItems().add(pd.name);
				}
			}
			break;
		case 1:
			for (PlanetData pd : planetData) {
				if (!pd.getOwner().equals(user[curturn]) && !pd.getOwner().equals("X")) {
					yourLand.getItems().add(pd.name);
					cnt1++;
				} else if (pd.getOwner().equals(user[curturn])) {
					myLand.getItems().add(pd.name);
					cnt2++;
				}
			}
			break;
		case 2:
			for (PlanetData pd : planetData) {
				if (!pd.getOwner().equals(user[curturn]) && pd.getPrice() != 0) {
					yourLand.getItems().add(pd.name);
				}
			}
			break;
		case 3:
			yourLand.setVisible(false);
			yourLandLabel.setVisible(false);
			for (PlanetData pd : planetData) {
				if (pd.getOwner().equals(user[curturn])) {
					if (pd.count <= 2) {
						myLand.getItems().add(pd.name);
						cnt1++;
					}
				}
			}
			break;
		}

		Button Complete = (Button) takeLandPane.lookup("#Complete");
		Complete.setOnAction(event -> dialog.close());
		choiceLand = (Button) takeLandPane.lookup("#choiceLand");
		if (index == 1) {
			if (cnt1 != 0 && cnt2 != 0) {
				choiceLand.setOnAction(event -> changeLand(yourLand, myLand));
				cnt1 = 0;
				cnt2 = 0;
			} else {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("알림");
				alert.setHeaderText("카드 무효화");
				alert.setContentText("내땅 혹은 상대방 땅이 없으니 이 카드는 무효화 됩니다.");
				alert.showAndWait();
				cnt1 = 0;
				cnt2 = 0;
				return;
			}
		} else if (index == 3) {
			if (cnt1 != 0) {
				choiceLand.setOnAction(event -> getChoice(myLand, index));
				cnt1 = 0;
			} else {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("알림");
				alert.setHeaderText("카드 무효화");
				alert.setContentText("땅이 없으므로 이 카드는 무효화 됩니다.");
				alert.showAndWait();
				cnt1 = 0;
				cnt2 = 0;
				return;
			}
		} else {
			choiceLand.setOnAction(event -> getChoice(yourLand, index));
		}

		Scene scene = new Scene(takeLandPane);
		dialog.setScene(scene);
		dialog.show();
	}

// 비밀카드로 땅 교환시 동작 함수
	private void changeLand(ChoiceBox<String> choiceYourBox, ChoiceBox<String> choiceMyBox) {
		String[] name = { choiceYourBox.getValue(), choiceMyBox.getValue() };
		int nextTurn = (curturn + 1) % 2;
		System.out.println(curturn + "," + nextTurn + ":" + name[0] + "," + name[1]);
		if (name[0] == null || name[1] == null) {
			return;
		}

		for (int i = 0; i < planetData.size(); i++) {
			if (planetData.get(i).name.equals(name[0])) { // 선택한 상대 땅일때 동작
				// 플래그 변경
				System.out.println("상대땅 변경" + curturn);
				ChangeLandFlag(i, curturn); // 땅 가져올때 플래그들 변경 함수
				// 소유자 변경
				if (curturn == 0) {
					planetData.get(i).owner = user[0];
				} else {
					planetData.get(i).owner = user[1];
				}
			} else if (planetData.get(i).name.equals(name[1])) { // 선택한 내땅
				System.out.println("내땅 변경" + nextTurn);
				ChangeLandFlag(i, nextTurn); // 땅 가져올대 플래그들 변경 함수
				// 소유자 변경
				if (nextTurn == 0) {
					planetData.get(i).owner = user[0];
				} else {
					planetData.get(i).owner = user[1];
				}
			}
		}

		choiceLand.setVisible(false);
	}

	// 상대땅 가져올때 설치되어 있는 플래그들 변경 함수
	public void ChangeLandFlag(int landPosition, int userNum) {
		System.out.println(userNum + "," + landPosition);
		for (int i = 0; i < planetData.get(landPosition).building.size(); i++) {
			root.getChildren().remove(planetData.get(landPosition).building.get(i));
			if (userNum == 0) {
				if (i == 0) {
					Image im = new Image("file:../../resources/images/flag" + (userNum + 1) + ".PNG");
					((ImageView) planetData.get(landPosition).building.get(i)).setImage(im);
				} else {
					((Rectangle) planetData.get(landPosition).building.get(i)).setFill(Color.DODGERBLUE);
				}
			} else {
				if (i == 0) {
					Image im = new Image("file:../../resources/images/flag" + (userNum + 1) + ".PNG");
					((ImageView) planetData.get(landPosition).building.get(i)).setImage(im);
				} else {
					((Rectangle) planetData.get(landPosition).building.get(i)).setFill(Color.RED);
				}
			}

			root.getChildren().add((Node) planetData.get(landPosition).building.get(i));
		}
	}

	// 플래그 설치 함수
	public void setFlag(int landPosition, int userNum) {
		ImageView flag = new ImageView();
		Image im = new Image("file:../../resources/images/flag" + (userNum + 1) + ".PNG");
		flag.setLayoutX(pieceXY.get(landPosition - 1).pieceX);
		flag.setLayoutY(pieceXY.get(landPosition - 1).pieceY);
		flag.setFitHeight(30);
		flag.setFitWidth(20);
		flag.setImage(im);
		root.getChildren().add(flag);
		planetData.get(landPosition).building.add(flag);
	}

	// 비밀카드로 땅 선택시 동작 함수
	private void getChoice(ChoiceBox<String> choiceBox, int index) {
		String name = choiceBox.getValue();
		ImageView flag = new ImageView();
		
		if (name == null) {
			return;
		}

		for (int i = 0; i < planetData.size(); i++) {
			if (planetData.get(i).name.equals(name)) {
				// 선택한 땅일때 동작
				if (index == 3) {
					if (planetData.get(i).count == 1) {
						setBuilding(i, -33);
						setBuilding(i, -10);
						planetData.get(i).count += 2;
					} else if (planetData.get(i).count == 2) {
						setBuilding(i, -10);
						setBuilding(i, 13);
						planetData.get(i).count += 2;
					}

				} else {
					// 플래그 추가 혹은 변경
					if (!planetData.get(i).owner.equals("X")) { // 상대땅 가져오기 일때만 기존 플레그 제거
						ChangeLandFlag(i, curturn); // 땅 가져올대 플래그들 변경 함수
						if (turn == 0) {
							planetData.get(i).owner = user[1];
						} else {
							planetData.get(i).owner = user[0];
						}

					} else {
						setFlag(i, curturn); // 플래그 설치 함수
						planetData.get(i).count++;
						planetData.get(i).owner = user[turn];
					}
					// 소유자 변경
				}
			}
		}

		choiceLand.setVisible(false);
	}

	// 건물 설치 함수 setBuilding(현재 위치값, 건물 설치 조정값)
	public void setBuilding(int landPosition, int located) {
		Rectangle rec[] = new Rectangle[2];
		rec[0] = new Rectangle(pieceXY.get(landPosition - 1).pieceX + located,
				pieceXY.get(landPosition - 1).pieceY - 30, 20, 20);
		rec[0].setFill(Color.DODGERBLUE);
		rec[1] = new Rectangle(pieceXY.get(landPosition - 1).pieceX + located,
				pieceXY.get(landPosition - 1).pieceY - 30, 20, 20);
		rec[1].setFill(Color.RED);
		root.getChildren().add(rec[curturn]);
		planetData.get(landPosition).building.add(rec[curturn]);
	}

	public void BuyLand(String name, String owner, String data, int price, int turn) {
		Stage dialog = new Stage(StageStyle.UTILITY);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(primaryStage);
		dialog.setTitle("구매 확인");
		int cost = 0;
		if (planetData.get(position[turn]).count == 0) {
			cost = price;
		} else {
			cost = price + (price / 2) * (planetData.get(position[turn]).count - 1);
		}
		AnchorPane anchorPane = null;
		int nextTurn = (turn + 1) % 2;

		try {
			anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("dialog2.fxml"));
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		Label BuyMessage = (Label) anchorPane.lookup("#BuyMessage");
		Label MyMoney = (Label) anchorPane.lookup("#MyMoney");

		Button Complete = (Button) anchorPane.lookup("#Complete");

		Complete.setOnAction(event -> dialog.close());
//      Rectangle rec[] = new Rectangle[2];

//      ImageView flag = new ImageView();
		if (money[turn] < cost) {
			BuyMessage.setText("보유금액이 부족합니다.");
			MyMoney.setText("보유금액 : " + money[turn] + "원");
		} else {
			if (planetData.get(position[turn]).owner.equals("X")) {
				planetData.get(position[turn]).owner = user[turn];
				PlanetOwner.setText(user[turn]);
			}
			if (!planetData.get(position[turn]).owner.equals(user[turn])) { // 내땅이 아닐경우
				ChangeLandFlag(position[turn], curturn); // 땅 가져올대 플래그들 변경 함수

				if (turn == 0) {
					money[0] -= cost;
					money[1] += cost;
					MyMoney.setText("보유금액 : " + money[0] + "원");
					Money1.setText("보유금액 : " + money[0] + "원");
					Money2.setText("보유금액 : " + money[1] + "원");
				} else {
					money[0] += cost;
					money[1] -= cost;
					MyMoney.setText("보유금액 : " + money[1] + "원");
					Money1.setText("보유금액 : " + money[0] + "원");
					Money2.setText("보유금액 : " + money[1] + "원");
				}
				PlanetOwner.setText(user[turn]);
				planetData.get(position[turn]).owner = user[turn];
				BuyMessage.setText("구매 완료하였습니다.");
			} else { // 빈땅 이거나가 내땅인경우
				if (planetData.get(position[turn]).count == 0) { // 빈당일때
					setFlag(position[turn], curturn);
					planetData.get(position[turn]).count++;
					planetData.get(position[turn]).owner = user[turn];
					BuyMessage.setText("구매 완료하였습니다.");
					if (owner.equals("X")) {
						money[turn] -= price;
						MyMoney.setText("보유금액 : " + money[turn] + "원");
						if (turn == 0) {
							Money1.setText("보유금액 : " + money[turn] + "원");
						} else {
							Money2.setText("보유금액 : " + money[turn] + "원");
						}
					} else {
						MyMoney.setText("보유금액 : " + money[turn] + "원");
						if (turn == 0) {
							money[0] -= cost;
							money[1] += cost;
							MyMoney.setText("보유금액 : " + money[0] + "원");
							Money1.setText("보유금액 : " + money[0] + "원");
							Money2.setText("보유금액 : " + money[1] + "원");
						} else {
							money[0] += cost;
							money[1] -= cost;
							MyMoney.setText("보유금액 : " + money[1] + "원");
							Money1.setText("보유금액 : " + money[0] + "원");
							Money2.setText("보유금액 : " + money[1] + "원");
						}
					}

				} else if (planetData.get(position[turn]).count == 1) {
					setBuilding(position[turn], -33); // 건물 설치 함수 호출
					planetData.get(position[turn]).count++;

					planetData.get(position[turn]).owner = user[turn];
					BuyMessage.setText("구매 완료하였습니다.");
					money[turn] -= price / 2;
					MyMoney.setText("보유금액 : " + money[turn] + "원");
					if (turn == 0) {
						Money1.setText("보유금액 : " + money[turn] + "원");
					} else {
						Money2.setText("보유금액 : " + money[turn] + "원");
					}
				} else if (planetData.get(position[turn]).count == 2) {
					setBuilding(position[turn], -10); // 건물 설치 함수 호출
					planetData.get(position[turn]).count++;

					planetData.get(position[turn]).owner = user[turn];
					BuyMessage.setText("구매 완료하였습니다.");
					money[turn] -= price / 2;
					MyMoney.setText("보유금액 : " + money[turn] + "원");
					if (turn == 0) {
						Money1.setText("보유금액 : " + money[turn] + "원");
					} else {
						Money2.setText("보유금액 : " + money[turn] + "원");
					}
				} else if (planetData.get(position[turn]).count == 3) {
					setBuilding(position[turn], 13); // 건물 설치 함수 호출
					planetData.get(position[turn]).count++;

					planetData.get(position[turn]).owner = user[turn];
					BuyMessage.setText("구매 완료하였습니다.");
					money[turn] -= price / 2;
					MyMoney.setText("보유금액 : " + money[turn] + "원");
					if (turn == 0) {
						Money1.setText("보유금액 : " + money[turn] + "원");
					} else {
						Money2.setText("보유금액 : " + money[turn] + "원");
					}
				} else {
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("알림");
					alert.setHeaderText("구매 불가능");
					alert.setContentText("이미 지을 수 있는 빌딩 개수의최대입니다.");
					alert.showAndWait();
					cnt1 = 0;
					cnt2 = 0;
					return;
				}
			}

		}
		CostPlanet.setVisible(false);
		BuyPlanet.setVisible(false);
		Scene scene = new Scene(anchorPane);
		dialog.setScene(scene);
		dialog.show();
	}

	public void setPosition() {

		path.getElements().add(new MoveTo(cntX[curturn], cntY[curturn]));
		if (posi[curturn] == BOTTOM) {
			path.getElements().add(new HLineTo(cntX[curturn] - 83));
			cntX[curturn] -= 83;

			if (cntX[curturn] == -747) {
				posi[curturn] = LEFT;
			}
		} else if (posi[curturn] == LEFT) {
			path.getElements().add(new VLineTo(cntY[curturn] - 83));
			cntY[curturn] -= 83;
			if (cntY[curturn] == -415) {
				posi[curturn] = TOP;
			}

		} else if (posi[curturn] == TOP) {
			path.getElements().add(new HLineTo(cntX[curturn] + 83));
			cntX[curturn] += 83;
			if (cntX[curturn] == 0) {
				posi[curturn] = RIGHT;
			}
		} else if (posi[curturn] == RIGHT) {
			path.getElements().add(new VLineTo(cntY[curturn] + 83));
			cntY[curturn] += 83;
			if (cntY[curturn] == 0) {
				posi[curturn] = BOTTOM;
			}
		}
		position[curturn]++;
		if (position[curturn] == pieceXY.size()) {
			position[curturn] = 0;
			money[curturn] += 200000;
			if (curturn == 0) {
				Money1.setText("보유금액:" + money[curturn] + "원");
			} else {
				Money2.setText("보유금액:" + money[curturn] + "원");
			}

		}
	}

	// 비밀 카드 상황에 따른 이동 메서드.
	public void setPosition2(int move) {
		if (Cardposi == 1) { // 5번째 카드, 조난 기지로.
			if (move <= 23) {
				move = 23 - move;
			} else {
				move = 51 - move;
			}
		} else if (Cardposi == 2) {// 7번째 카드 블랙홀로
			if (move < 14) {
				move = 14 - move;
			} else {
				move = move + 14 - (move - 14) * 2;
			}
		} else if (Cardposi == 3) {
			move = 28 - move;
		} else if (Cardposi == 4) {
			if (move < 9) {
				move = 9 - move;
			} else {
				move = 37 - move;
			}
		} else if (Cardposi == 5) {
			if (move <= 23) {
				move = 23 - move;
			} else {
				move = 26;
			}

		} else if (Cardposi == 6) {
			move = 5;
		}
		int movecount = move;
		Thread thread = new Thread() {
			@Override
			public void run() {
				path = new Path();
				Platform.runLater(() -> {
					for (int i = 0; i < movecount; i++) {
						setPosition();
					}
					pathTransition.setDuration(Duration.millis(150 * (movecount)));
					pathTransition.setPath(path);
					if (curturn == 0) {
						pathTransition.setNode(C1);
					} else {
						pathTransition.setNode(C2);
					}
					pathTransition.play();

				});
				try {
					Thread.sleep(150 * (movecount) + 300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		};
		thread.setDaemon(true);
		thread.start();
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

		planetData.add(new PlanetData("지구", "X", "수고비 20만원", 0, 0));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0));
		planetData.add(new PlanetData("화성", "X", "20만원", 200000, 0));
		planetData.add(new PlanetData("목성", "X", "15만원", 150000, 0));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0));
		planetData.add(new PlanetData("토성", "X", "8만원", 80000, 0));
		planetData.add(new PlanetData("천왕성", "X", "13만원", 130000, 0));
		planetData.add(new PlanetData("해왕성", "X", "25만원", 250000, 0));
		planetData.add(new PlanetData("물병자리", "X", "12만원", 120000, 0));
		planetData.add(new PlanetData("워프", "X", "이동", 0, 0));
		planetData.add(new PlanetData("명왕성", "X", "40만원", 400000, 0));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0));
		planetData.add(new PlanetData("안드로메다", "X", "45만원", 450000, 0));
		planetData.add(new PlanetData("북극성", "X", "44만원", 440000, 0));
		planetData.add(new PlanetData("블랙홀", "X", "2턴 멈춤", 0, 0));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0));
		planetData.add(new PlanetData("달", "X", "100만원", 1000000, 0));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0));
		planetData.add(new PlanetData("태양", "X", "120만원", 1200000, 0));
		planetData.add(new PlanetData("시리우스", "X", "90만원", 900000, 0));
		planetData.add(new PlanetData("프로키온", "X", "95만원", 950000, 0));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0));
		planetData.add(new PlanetData("베크록스", "X", "80만원", 800000, 0));
		planetData.add(new PlanetData("조난기지", "X", "1턴멈춤 기부 50만원", 0, 0));
		planetData.add(new PlanetData("아크록스", "X", "65만원", 650000, 0));
		planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0));
		planetData.add(new PlanetData("수성", "X", "55만원", 550000, 0));
		planetData.add(new PlanetData("금성", "X", "60만원", 600000, 0));

	}

}
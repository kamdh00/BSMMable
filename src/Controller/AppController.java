package Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;

import conn.PlayerClient;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
	Button btn1, StartGame, btnGiveup;
	Button BuyPlanet, CostPlanet, choiceLand;
	@FXML
	ImageView C1, C2;
	@FXML
	StackPane Dice1, Dice2;
	@FXML
	ImageView DiceImg1, DiceImg2;
	@FXML
	TextField Money1, Money2;
	Label PlanetOwner;
	@FXML
	Label myName, yourName, myWinLose, yourWinLose;
	@FXML
	Label card1, card2,Waiting,Double;
	@FXML
	Label cardcnt1, cardcnt2;
	private static int TOP = 0;
	private static int LEFT = 1;
	private static int BOTTOM = 2;
	private static int RIGHT = 3;
	int posi[] = { BOTTOM, BOTTOM };
	private static int money[] = { 2000000, 2000000 };
	private String user[] = { "kam", "yoo" }; // 임시로 하드코딩 추후 로그인 한 사용자를 Main에서 받는것으로 대체 필요
	int position[] = { 0, 0 };
	int d1;
	int d2;

	int cardNum = 1; // test용
	int Cardposi = 0; // 카드로 나온 포지션.
	int bre = 0;

	int card[] = { 0, 0 }; // 카드 소지수.
	int cardpass = 0;

	int cnt1 = 0;// 상대방 땅 개수
	int cnt2 = 0;// 내땅 개수
	int cntX[] = { 15, 15 };
	int cntY[] = { 20, 20 };
	int buy[];
	int turn = 0;
	int win = 1, lose = 1;
	private Stage primaryStage;
	private AnchorPane root;
	ArrayList<PieceXY> pieceXY = new ArrayList<PieceXY>();
	ArrayList<PlanetData> planetData = new ArrayList<PlanetData>();
	PathTransition pathTransition = new PathTransition();
	Path path;	
	Popup passpopup = new Popup();	
	// Queue<Integer> randQueue = new LinkedList<Integer>(); // 카드 이미지 번호 랜덤으로 저장
	ArrayList<Integer> secretCardList = new ArrayList<Integer>(); // 카드 이미지 번호 저장
	Random rand = new Random();
	Label MyMoney;
	PlayerClient SocketConnect;
	int myturn = -1;
	int yourturn;
	String id=null;
	int ready=0;
	ArrayList<Stage> checkdialog = new ArrayList<Stage>();
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setSecretCard();
		Double.setVisible(false);
		Waiting.setVisible(false);
		Money1.setText("보유금액 : " + money[0] + "원");
		Money1.setEditable(false);
		Money2.setText("보유금액 : " + money[1] + "원");
		Money2.setEditable(false);
		btn1.setDisable(true);
		btnGiveup.setDisable(true);
		btn1.setOnAction((event) -> rollTheDice());
		StartGame.setOnAction((event) -> StartGame());
		btnGiveup.setOnAction((event) -> {
			SocketConnect.getOutMsg().println("GameResult/" + user[yourturn] + "/" + user[myturn]);
			gameFinish(user[yourturn]);
		});
	}
	public void StartGame() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				int cnt=0;
				String text[] = {"Player Waiting","Player Waiting.","Player Waiting..","Player Waiting...","Player Waiting....","Player Waiting...."};
				SocketConnect.getOutMsg().println("Ready/"+id+"/"+win+"/"+lose);				
				Platform.runLater(()->{
					StartGame.setVisible(false);
					btnGiveup.setVisible(false);
					Waiting.setVisible(true);
				});
				
				while (true) {
					if (ready==1) {
						Platform.runLater(()->{
							Waiting.setText("Game Start!");
							btnGiveup.setVisible(true);
						});
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {						
							e.printStackTrace();
						}
						Platform.runLater(()->{
							Waiting.setVisible(false);
						});
						
						break;
					}
					int t = cnt++;
					Platform.runLater(()->{
						Waiting.setText(text[t]);
					});
					
					if (cnt==6) {
						cnt=0;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {						
						e.printStackTrace();
					}
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
		
	}
	public void setLogin(String login) {
		String[] rmsg = null;
		String msg = login;
		rmsg = msg.split("/");
		id=rmsg[0];		
		win = Integer.parseInt(rmsg[1]);
		lose = Integer.parseInt(rmsg[2]);
		Platform.runLater(()->{
			myName.setText(id);
			myWinLose.setText("승 : "+win+" 패 : "+lose);
		});
		
	}	
	public void setSocket(PlayerClient SocketConnect) {
		this.SocketConnect = SocketConnect;
	}

	public void getMsg() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				String[] rmsg = null;
				String msg = null;
				while (true) {
					try {					
						msg = SocketConnect.getInMsg().readLine();
						System.out.println(msg);
					} catch (IOException e1) {
						System.out.println("소켓에러");
					}
					if (msg != null) {
						rmsg = msg.split("/");
						// SocketConnect.getOutMsg().println("Dice/"+myturn+"/"+movedice"/"+d1+"/"+d2);
						if (rmsg[0].equals("Start")) {
							String tname = rmsg[1];
							int twin = Integer.parseInt(rmsg[2]);
							int tlose = Integer.parseInt(rmsg[3]);
							myturn = Integer.parseInt(rmsg[4]);
							Platform.runLater(() -> {
								yourName.setText(tname);
								yourWinLose.setText("승 : " + twin + " 패 : " + tlose);

							});

							yourturn = (myturn + 1) % 2;
							user[myturn] = id;
							user[yourturn] = tname;
							System.out.println("user[0] : " + user[0] + " user[1] : " + user[1]);
							if (myturn == 0) {
								btn1.setDisable(false);
								btnGiveup.setDisable(false);
							}
							ready = 1;
						} else if (rmsg[0].equals("Dice")) {
							int movedice = Integer.parseInt(rmsg[2]);
							d1 = Integer.parseInt(rmsg[3]);
							d2 = Integer.parseInt(rmsg[4]);

							if (Integer.parseInt(rmsg[1]) != myturn) {
								System.out.println("내턴 아님...");
								Platform.runLater(() -> {
									setDiceImage();
									moveDice(d1 + d2, movedice);
								});
							}
							System.out.println("현재턴:" + turn);

						}
						// ChangeTurn/turn
						else if (rmsg[0].equals("ChangeTurn")) {
							turn = Integer.parseInt(rmsg[1]);
							if (myturn != turn) {
								Platform.runLater(() -> {
									btn1.setDisable(true);
									btnGiveup.setDisable(true);
								});

							} else {
								Platform.runLater(() -> {
									btn1.setDisable(false);
									btnGiveup.setDisable(false);
								});
							}

						}
						// SocketConnect.getOutMsg().println("MoveDice/" +myturn+"/"+ movedice + "/" +
						// movecount);
						else if (rmsg[0].equals("MoveDice") && Integer.parseInt(rmsg[1]) != myturn) {
							int movedice = Integer.parseInt(rmsg[2]);
							int movecount = Integer.parseInt(rmsg[3]);
							Platform.runLater(() -> {
								moveDice(movecount, movedice);
							});

						}
						// 소켓
						// SocketConnect.getOutMsg().println("SetFlag/"+position[turn]+"/"+curturn);
						else if (rmsg[0].equals("SetFlag") && Integer.parseInt(rmsg[2]) != myturn) {
							int tp = Integer.parseInt(rmsg[1]);
							int tt = Integer.parseInt(rmsg[2]);
							Platform.runLater(() -> {
								setFlag(tp, tt);
							});

						}
						// 소켓
						// SocketConnect.getOutMsg()
						// .println("SetBuilding/" + position[turn] + "/" +myturn+"/"+usernum+"/"+
						// locate);
						else if (rmsg[0].equals("SetBuilding") && Integer.parseInt(rmsg[2]) != myturn) {
							int position = Integer.parseInt(rmsg[1]);
							int curturn = Integer.parseInt(rmsg[3]);
							int locate = Integer.parseInt(rmsg[4]);
							Platform.runLater(() -> {
								setBuilding(position, locate, curturn);

							});

						} else if (rmsg[0].equals("Money")) {
							money[0] = Integer.parseInt(rmsg[1]);
							money[1] = Integer.parseInt(rmsg[2]);
							Platform.runLater(() -> {
								Money1.setText("보유금액 : " + money[myturn] + "원");
								Money2.setText("보유금액 : " + money[yourturn] + "원");
							});

							//SocketConnect.getOutMsg().println("Card/" + card[0] + "/" + card[1]);
						}else if(rmsg[0].equals("Card")) {
							card[0]=Integer.parseInt(rmsg[1]);
							card[1]=Integer.parseInt(rmsg[2]);
							Platform.runLater(()->{
								cardcnt1.setText(card[myturn]+"");
								cardcnt2.setText(card[turn]+"");
							});
							
						} else if(rmsg[0].equals("removeFlag") ) {//SocketConnect.getOutMsg().println("removeFlag/" + i + 1);
							int i=Integer.parseInt(rmsg[1]);
							int count=Integer.parseInt(rmsg[2]);
							
							Platform.runLater(() -> {
								for(int j=count;j<planetData.get(i).building.size();j++) {
									root.getChildren().remove(planetData.get(i).building.get(j));
									
								}
								
								if(count==0) {
									planetData.get(i).owner = "X";
									planetData.get(i).count = 0;
								}else if(count==1) {
									planetData.get(i).count = 1;
								}
							});
						}
						
						// 소켓
						// SocketConnect.getOutMsg().println("ChangeLandFlag/" + position[turn] + "/"
						// +curturn);
						//SocketConnect.getOutMsg().println("ChangeLandFlag/" + landPosition + "/" + userNum + "/" + changePosition+ "/" +yournum);
						else if (rmsg[0].equals("ChangeLandFlag") && Integer.parseInt(rmsg[2]) != myturn) {
							int tp = Integer.parseInt(rmsg[1]);
							int tt = Integer.parseInt(rmsg[2]);
							int yourtp = Integer.parseInt(rmsg[3]);
							int yourtt = Integer.parseInt(rmsg[4]);
							Platform.runLater(() -> {
								ChangeLandFlag(tp, tt , yourtp, yourtt);
							});

						}
						// 내턴 아닐때 비밀카드 보여주기
						else if (rmsg[0].equals("ShowSecretCard") && Integer.parseInt(rmsg[1]) != myturn) {
							int tturn = Integer.parseInt(rmsg[1]);
							int num = Integer.parseInt(rmsg[2]);
							Platform.runLater(() -> {
								showSecretCard(num, tturn);
							});

						}
						// 상대방 쉬게만드는 경우
						else if (rmsg[0].equals("NextTurnBre") && Integer.parseInt(rmsg[1]) != myturn) {
							bre = Integer.parseInt(rmsg[2]);
						}
						// 종료
						else if (rmsg[0].equals("GameResult")) {
							String winner = rmsg[1];
							Platform.runLater(()->{
								gameFinish(winner);
							});
						}
						msg = null;
					}
				}
			}
		};

		thread.start();
	}
	
	public void setMoney(int mymoney, int yourmoney) {
		money[myturn] += mymoney;
		money[yourturn] += yourmoney;
		try {
			MyMoney.setText("보유금액 : " + money[myturn] + "원");
		} catch (Exception e) {
			e.getMessage();
		}
		Platform.runLater(() -> {
			Money1.setText("보유금액 : " + money[myturn] + "원");
			Money2.setText("보유금액 : " + money[yourturn] + "원");
		});
		SocketConnect.getOutMsg().println("Money/" + money[0] + "/" + money[1]);
		for (int i = 0; i < money.length; i++) {
			if (money[i]<0) {
				SocketConnect.getOutMsg().println("GameResult/" + user[(i + 1) % 2] + "/" + user[i] + "/" + 1);
				break;
			}			
		}
			
		
	}

	public void setMyData(String name, int win, int lose) {
		Thread thread = new Thread() {
			@Override
			public void run() {

				Platform.runLater(() -> {
					myName.setText(name);
					myWinLose.setText("승 : " + win + " 패 : " + lose);
				});
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	public void setYourData(String name, int win, int lose) {
		Platform.runLater(() -> {
			yourName.setText(name);
			yourWinLose.setText("승 : " + win + " 패 : " + lose);
		});
	}

	public void moveDice(int num, int movedice) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				path = new Path();
				Platform.runLater(() -> {
					for (int i = 0; i < num; i++) {
						setPosition(movedice);
					}
					pathTransition.setDuration(Duration.millis(150 + 50 * num));
					pathTransition.setPath(path);
					if (movedice == 0) {
						pathTransition.setNode(C1);
					} else {
						pathTransition.setNode(C2);
					}
					pathTransition.play();

				});
				try {
					Thread.sleep(150 + 50 * num + 300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
			}

		};
		thread.setDaemon(true);
		thread.start();
	}

	public void rollTheDice() {
		Double.setVisible(false);
		Thread thread = new Thread() {
			@Override
			public void run() {

				Platform.runLater(() -> {
					btn1.setDisable(true);
					btnGiveup.setDisable(true);
				});
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
				if (bre != 0) {
					if (d1 != d2) {
						Platform.runLater(() -> {
							Double.setText(planetData.get(position[myturn]).name + " 탈출 실패 (더블 일경우 탈출)");
							Double.setVisible(true);
						});
						bre--;
						turn = yourturn;
						SocketConnect.getOutMsg().println("ChangeTurn/" + turn);
						return;
					} else {
						bre=0;
						Platform.runLater(() -> {
							Double.setText("탈출 성공 ! 더블!! 한번 더");
							Double.setVisible(true);
						});
					}
				} else {
					if (d1 != d2) {
						turn = yourturn;
					} else {
						Platform.runLater(() -> {
							Double.setText("더블!! 한번 더");
							Double.setVisible(true);
						});

					}
				}
				SocketConnect.getOutMsg().println("Dice/" + myturn + "/" + myturn + "/" + d1 + "/" + d2);
				path = new Path();
				Platform.runLater(() -> {
					for (int i = 0; i < d1 + d2; i++) {
						setPosition(myturn);
					}
					pathTransition.setDuration(Duration.millis(150 + 50 * (d1 + d2)));
					pathTransition.setPath(path);
					if (myturn == 0) {
						pathTransition.setNode(C1);
					} else {
						pathTransition.setNode(C2);
					}
					pathTransition.play();

				});
				try {
					Thread.sleep(150 + 50 * (d1 + d2) + 100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Platform.runLater(() -> {
					if (card[myturn] > 0) {
						if (planetData.get(position[myturn]).owner.equals(user[yourturn])) {
							pass();
						} else {
							showDialog();
						}
					} else {
						showDialog();
					}
				});
			}

		};
		thread.setDaemon(true);
		thread.start();
	}

	public void setSecretCard() {
		for (int i = 0; i < 30; i++) {
			secretCardList.add(i);
		}
	}
	/*
	 * 카드 번호에 따라 실행되는 동작 함수
	 */

	public void actionSecretCard(int num) {
		switch (num) {
		case 0: // 소유한 본인땅의 모든 건물 삭제

			for (int i = 0; i < planetData.size(); i++) {
				if (user[myturn].equals(planetData.get(i).owner)) {
					for (int j = 1; j < planetData.get(i).building.size(); j++) {
						SocketConnect.getOutMsg().println("removeFlag/" + i + "/"+ 1);
					}
				}
			}

			break;
		case 1: // 후원금 30만원 받음
			setMoney(30000, 0);
			break;
		case 2: // 상대방에게 돈 30만원 뺏어옴
			setMoney(300000, -300000);
			// 상대편 금액 -30만원 로직 추가해야함. socket으로 -30만원 전송
			break;
		case 3: // 상대땅 하나 가져오기
			takeLand(0);
			break;
		case 4:// money[myturn] -= 1000000;
			setMoney(-100000, 0);
			break;
		case 5:
			bre = 1;
			Cardposi = 1;
			int posi2 = position[myturn];
			turn=yourturn;
			setPosition2(posi2, myturn);
			break;

		case 6:
			setMoney(-300000, 300000);
			// 상대편 금액 +30만원 로직 추가해야함. socket으로 +30만원 전송
			break;
		case 7: // 블랙홀로 이동시키기.
			bre = 2;
			Cardposi = 2;
			posi2 = position[myturn];
			turn=yourturn;
			setPosition2(posi2, myturn);
			break;
		case 8:
		case 10:// money[myturn] -= 500000;
			setMoney(-500000, 0);
			break;

		case 9:// 당신의 땅과 상대편의 땅을 바꿉니다.
			takeLand(1);
			break;
		case 11:// 가장 비싼 땅을 반액에 팔음. 건물이 지어진 경우 반액에 처분.
			int max = 0;
			int tmp = 0;
			// System.out.println("money" + money[turn]);
			for (int i = 0; i < planetData.size(); i++) {
				if (user[myturn].equals(planetData.get(i).owner)) {
					max = (max > planetData.get(i).price) ? max : planetData.get(i).price;
				}

			}
			for (int i = 0; i < planetData.size(); i++) {
				if (user[myturn].equals(planetData.get(i).owner)) {
					for (int j = 0; j < planetData.get(i).building.size(); j++) {
						if (max == planetData.get(i).price) {
							SocketConnect.getOutMsg().println("removeFlag/" + i + "/" + 0);
							if (tmp == 0) {
								int tmoney = ((max + ((max / 2) * (planetData.get(i).building.size() - 1))) / 2);
								setMoney(-tmoney, 0);
								tmp++;
							}
						}
					}
				}
			}
			break;
		case 12:
			// 모든 땅 반납
			for (int i = 0; i < planetData.size(); i++) {
				if (user[myturn].equals(planetData.get(i).owner)) {
					for (int j = 0; j < planetData.get(i).building.size(); j++) {
						SocketConnect.getOutMsg().println("removeFlag/" + i + "/" + 0);
					}
					planetData.get(i).owner = "X";
					planetData.get(i).count = 0;
				}
			}
			break;
		case 13:
			setMoney(-money[myturn] - (money[myturn] / 2), 0);
			break;

		case 14:
			// 지구로 돌아감. 수고비 받지 못함. 20만원 차감.
			Cardposi = 3;
			posi2 = position[myturn];
			setPosition2(posi2, myturn);
			setMoney(-200000, 0);
			break;
		case 15: // 한턴 쉽니다.
			bre = 1;
			turn=yourturn;
			break;
		case 16:
			// 지구로 돌아갑니다. 수고비를 받습니다.
			Cardposi = 3;
			posi2 = position[myturn];
			setPosition2(posi2, myturn);
			break;
		case 17: // 워프로 이동.
			Cardposi = 4;
			posi2 = position[myturn];
			setPosition2(posi2, myturn);
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
			setMoney(1000000, -1000000);
			break;
		case 19: // 원하는 땅 자신의 땅으로 만들음.
			takeLand(2);
			break;
		case 20: // 건물 두개 지을 땅 선택 (자신의 땅이여야함. 땅이 없을시 무효.)
			takeLand(3);
			break;

		case 21:
			setMoney(100000, 0);
			break;
		case 22: // 상대방 조난 기지에 보내기
			Cardposi = 1;
			posi2 = position[yourturn];
			setPosition2(posi2, yourturn);
			SocketConnect.getOutMsg().println("NextTurnBre/" + myturn + "/" + 1);
			break;
		// 서버 소켓..
		case 23: // 통행료 없이 땅 지나가기
			card[myturn]++;
			Platform.runLater(() -> {
				cardcnt1.setText(card[myturn] + "");
				cardcnt2.setText(card[turn] + "");
			});
			SocketConnect.getOutMsg().println("Card/" + card[0] + "/" + card[1]);
			break;

		case 24: // 10만원 내고 비밀 카드 한 장 더 뽑기
			setMoney(-100000, 0);
			Platform.runLater(() -> {
				oneMore();
			});
			break;

		case 25: // 상대편 금액 +20만원 로직 추가해야함. socket으로 +20만원 전송
			setMoney(200000, -200000);

			break;

		case 26: // 주사위 한번 더
			turn = myturn;
			break;
		case 27:
			setMoney(200000, 0);
			break;
		case 28:
			setMoney(-money[myturn], 0);
			break;

		case 29:
			// 5칸 더 앞으로 전진.
			Cardposi = 6;
			posi2 = position[myturn];
			setPosition2(posi2, myturn);
			break;

		}
	}
	
	public void removeFlag(Object x) {
		String building=x.toString();
		SocketConnect.getOutMsg().println("removeFlag/" + building);
	}

	// 내 턴 아닐때 비밀카드 내용 보여주기
	public void showSecretCard(int num, int actionTurn) {
		System.out.println("num : " + num);
		int cardNum;
		if (secretCardList.size() == 0) { // 모든 비밀카드가 오픈되고 나면 비밀카드 다시 채워줌
			setSecretCard();
			cardNum = secretCardList.get(num);
			secretCardList.remove(num);
		} else {
			cardNum = secretCardList.get(num);
			secretCardList.remove(num);
		}
		// 테스트용
		cardNum = 23;
		System.out.println("cardNum : " + cardNum);
		System.out.println("secretCardList size : " + secretCardList.size());
		AnchorPane anchorPane = null;
		Stage dialog = new Stage(StageStyle.UTILITY);
		try {
			anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("secretCard.fxml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		ImageView secretCard = (ImageView) anchorPane.lookup("#secretCard");
		String imgName = cardNum + ".png";
		Image img = new Image("file:../../resources/images/secretCard/" + imgName);
		secretCard.setImage(img);
		Button Complete = (Button) anchorPane.lookup("#Complete");
		//int tmp = cardNum;
		Complete.setOnAction(event -> {
			dialog.close();
			// 5 7 14 16 17 22 29
			if (myturn==actionTurn) {
				SocketConnect.getOutMsg().println("ChangeTurn/" + turn);
			}
			checkdialog.remove(dialog);

		});
		dialog.setAlwaysOnTop(true);
		if (myturn == actionTurn) {
			actionSecretCard(cardNum);
		}
		Scene scene = new Scene(anchorPane);
		dialog.setScene(scene);
		dialog.setX(primaryStage.getX() + 260);
		dialog.setY(primaryStage.getY() + 59.5);
		dialog.show();
		checkdialog.add(dialog);
	}

	public int setSecretCardNum() {
		Random random = new Random();
		return random.nextInt(secretCardList.size());
	}

	public void setDice() {
		Random random = new Random();
		d1 = random.nextInt(6) + 1;
		d2 = random.nextInt(6) + 1;

		// 테스트 위해 주사위수 임의 조작
//      if(turn == 0) {
//         d1 = 3;
//         d2 = 4;
//      }else {
//         d1 = 2;
//         d2 = 3;
//      }		
	}

	public void setDiceImage() {
		String[] str = { "dice1.PNG", "dice2.PNG", "dice3.PNG", "dice4.PNG", "dice5.PNG", "dice6.PNG" };
		Image img = new Image("file:../../resources/images/" + str[d1 - 1]);
		Image img2 = new Image("file:../../resources/images/" + str[d2 - 1]);
		DiceImg1.setImage(img);
		DiceImg2.setImage(img2);

	}
	public void gameFinish(String winner) {
		Stage dialog = new Stage(StageStyle.UTILITY);
		dialog.initModality(Modality.WINDOW_MODAL);
		AnchorPane anchorPane = null;
		try {
			anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("fin_dialog.fxml"));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		Label FinMessage = (Label) anchorPane.lookup("#FinMessage");
		Button FinButton = (Button) anchorPane.lookup("#FinButton");
		FinMessage.setText(winner+" 승리");
		FinButton.setOnAction(e->{
			dialog.close();
			SocketConnect.getOutMsg().println("GameFinish");
			primaryStage.close();
		});
		Scene scene = new Scene(anchorPane);
		dialog.setScene(scene);
		dialog.setAlwaysOnTop(true);
		dialog.show();
		for (int i = 0; i < checkdialog.size(); i++) {
			checkdialog.get(i).close();			
		}
	}

	public void showDialog() {
		String name = planetData.get(position[myturn]).name;
		String owner = planetData.get(position[myturn]).owner;
		String data = planetData.get(position[myturn]).data;
		int price = planetData.get(position[myturn]).price;

		int count = planetData.get(position[myturn]).count;

		AnchorPane anchorPane = null;
		Stage dialog = new Stage(StageStyle.UTILITY);
		Stage dialog2 = new Stage(StageStyle.UTILITY);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(primaryStage);
		dialog.setTitle("확인");
		dialog2.initModality(Modality.WINDOW_MODAL);
		dialog2.initOwner(primaryStage);
		dialog2.setTitle("확인");

		if (!owner.equals(user[myturn]) && !owner.equals("X") && price != 0) { // 내 땅이 아니면 이용료 지불

			try {
				anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("dialog2.fxml"));
			} catch (IOException e2) {
				e2.printStackTrace();
			}

			Label BuyMessage = (Label) anchorPane.lookup("#BuyMessage");
			MyMoney = (Label) anchorPane.lookup("#MyMoney");
			
			BuyMessage.setText(name + "의 통행료 " + (price + (price/2)*(count-1)) + "원을 지불을 하였습니다.");
			setMoney(-price + (price/2)*(count-1), price + (price/2)*(count-1));
			
			Button Complete = (Button) anchorPane.lookup("#Complete");
			Complete.setOnAction(event -> {
				dialog.close();
				checkdialog.remove(dialog);
			});

			Scene scene = new Scene(anchorPane);
			dialog.setScene(scene);
			dialog.setAlwaysOnTop(true);
			dialog.show();
			checkdialog.add(dialog);

		} // 이용료 지불 끝

		try {
			if (name.equals("비밀카드")) {
				int num = setSecretCardNum();
				// 소켓
				SocketConnect.getOutMsg().println("ShowSecretCard/" + myturn + "/" + num);
				showSecretCard(num, myturn);

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
				if (owner.equals(user[myturn])) {
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

				if (price == 0 || (position[myturn]) == 0 || (count == 4 && owner.equals(user[myturn]))) {
					BuyPlanet.setVisible(false);
					CostPlanet.setVisible(false);
				}

				BuyPlanet.setOnAction(event -> {
					BuyLand(name, owner, price);
				});
				Button Complete = (Button) anchorPane.lookup("#Complete");
				if (name.equals("워프")) {
					setWarp(anchorPane, dialog2);
					Complete.setVisible(false);
				} else if (name.equals("조난기지")) {
					setMoney(-500000,0);
					bre = 1;
					turn=yourturn;
				} else if (name.equals("블랙홀")) {
					bre = 2;
					turn=yourturn;
				} else {
					Complete.setVisible(true);
				}
				Complete.setOnAction(event -> {
					dialog2.close();
					SocketConnect.getOutMsg().println("ChangeTurn/" + turn);
					checkdialog.remove(dialog2);

				});
				Scene scene = new Scene(anchorPane);
				dialog2.setScene(scene);
				dialog2.show();
				checkdialog.add(dialog2);
			}
		} catch (IOException e2) {
			e2.printStackTrace();
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
		planetData.get(landPosition).count++;
		planetData.get(landPosition).owner = user[userNum];
		if (myturn == userNum) {
			// 소켓
			SocketConnect.getOutMsg().println("SetFlag/" + landPosition + "/" + userNum);
		}

	}

	// 건물 설치 함수 setBuilding(현재 위치값, 건물 설치 조정값)
	public void setBuilding(int landPosition, int located, int usernum) {
		Rectangle rec[] = new Rectangle[2];
		rec[0] = new Rectangle(pieceXY.get(landPosition - 1).pieceX + located,
				pieceXY.get(landPosition - 1).pieceY - 30, 20, 20);
		rec[0].setFill(Color.DODGERBLUE);
		rec[1] = new Rectangle(pieceXY.get(landPosition - 1).pieceX + located,
				pieceXY.get(landPosition - 1).pieceY - 30, 20, 20);
		rec[1].setFill(Color.RED);
		root.getChildren().add(rec[usernum]);
		planetData.get(landPosition).building.add(rec[usernum]);
		planetData.get(landPosition).owner = user[usernum];
		planetData.get(landPosition).count++;
		if (myturn == usernum) {
			// 소켓
			SocketConnect.getOutMsg()
					.println("SetBuilding/" + landPosition + "/" + myturn + "/" + usernum + "/" + located);
		}

	}

	// 상대땅 가져올때 설치되어 있는 플래그들 변경 함수
	public void ChangeLandFlag(int landPosition, int userNum , int changePosition, int yournum) {
		System.out.println(userNum + "," + landPosition);
		for (int i = 0; i < planetData.get(landPosition).building.size(); i++) {
			
			//상대편 땅에 있는 깃발 과 빌딩 색깔 바꿈.
			if(yournum == -1 || yournum != -1) {
				root.getChildren().remove(planetData.get(landPosition).building.get(i));
				if (userNum == 0) {
					if (i == 0) {
						Image im = new Image("file:../../resources/images/flag" + (userNum + 1) + ".PNG");
						((ImageView) planetData.get(landPosition).building.get(i)).setImage(im);
					} else {
						((Rectangle) planetData.get(landPosition).building.get(i)).setFill(Color.DODGERBLUE);
					}
					planetData.get(landPosition).owner = user[userNum];
				} else {
					if (i == 0) {
						Image im = new Image("file:../../resources/images/flag" + (userNum + 1) + ".PNG");
						((ImageView) planetData.get(landPosition).building.get(i)).setImage(im);
					} else {
						((Rectangle) planetData.get(landPosition).building.get(i)).setFill(Color.RED);
					}
					planetData.get(landPosition).owner = user[userNum];
				}
				root.getChildren().add((Node) planetData.get(landPosition).building.get(i));
			}
			
			//youtnum = -1일 경우 상대편 땅을 내땅으로 바꿈.
			//yournum != -1일 경우 상대편 당과 내땅 체인지.
			if(yournum != -1) {
				root.getChildren().remove(planetData.get(changePosition).building.get(i));
				if(yournum == 0) {
					if (i == 0) {
						Image im = new Image("file:../../resources/images/flag" + (yournum + 1) + ".PNG");
						((ImageView) planetData.get(changePosition).building.get(i)).setImage(im);
					} else {
						((Rectangle) planetData.get(changePosition).building.get(i)).setFill(Color.DODGERBLUE);
					}
					planetData.get(changePosition).owner = user[yournum];
					
				}else if(yournum == 1) {
					if (i == 0) {
						Image im = new Image("file:../../resources/images/flag" + (yournum + 1) + ".PNG");
						((ImageView) planetData.get(changePosition).building.get(i)).setImage(im);
					} else {
						((Rectangle) planetData.get(changePosition).building.get(i)).setFill(Color.RED);
					}
				}
					planetData.get(changePosition).owner = user[yournum];
					root.getChildren().add((Node) planetData.get(changePosition).building.get(i));
			}

			
		}

		if (myturn == userNum) {
			// 소켓
			SocketConnect.getOutMsg().println("ChangeLandFlag/" + landPosition + "/" + userNum + "/" + changePosition+ "/" +yournum);
		
		}

	}

	public void setPosition(int curturn) {

		path.getElements().add(new MoveTo(cntX[curturn], cntY[curturn]));
		if (posi[curturn] == BOTTOM) {
			path.getElements().add(new HLineTo(cntX[curturn] - 83));
			cntX[curturn] -= 83;

			if (cntX[curturn] == -732) {
				posi[curturn] = LEFT;
			}
		} else if (posi[curturn] == LEFT) {
			path.getElements().add(new VLineTo(cntY[curturn] - 83));
			cntY[curturn] -= 83;
			if (cntY[curturn] == -395) {
				posi[curturn] = TOP;
			}

		} else if (posi[curturn] == TOP) {
			path.getElements().add(new HLineTo(cntX[curturn] + 83));
			cntX[curturn] += 83;
			if (cntX[curturn] == 15) {
				posi[curturn] = RIGHT;
			}
		} else if (posi[curturn] == RIGHT) {
			path.getElements().add(new VLineTo(cntY[curturn] + 83));
			cntY[curturn] += 83;
			if (cntY[curturn] == 20) {
				posi[curturn] = BOTTOM;
			}
		}
		position[curturn]++;
		if (position[curturn] == pieceXY.size()) {
			position[curturn] = 0;
			setMoney(200000, 0);
		}
	}

	// 비밀 카드 상황에 따른 이동 메서드.
	public void setPosition2(int move, int movedice) {
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
				SocketConnect.getOutMsg().println("MoveDice/" + myturn + "/" + movedice + "/" + movecount);
				path = new Path();
				Platform.runLater(() -> {

					for (int i = 0; i < movecount; i++) {
						setPosition(movedice);
					}
					pathTransition.setDuration(Duration.millis(150 + 50 * (movecount)));
					pathTransition.setPath(path);
					if (movedice == 0) {
						pathTransition.setNode(C1);
					} else {
						pathTransition.setNode(C2);
					}
					pathTransition.play();

				});
				try {
					Thread.sleep(150 + 50 * (movecount) + 300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	public void pass() { // 통행료 없이 지나가기 함수.
		AnchorPane anchorPane = null;		
		Stage pass = new Stage(StageStyle.UTILITY);
		pass.initModality(Modality.WINDOW_MODAL);
		pass.initOwner(primaryStage);
		pass.setTitle("알림");

		try {
			anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("pass.fxml"));

		} catch (IOException e) {
			e.printStackTrace();
		}
		Button Y = (Button) anchorPane.lookup("#Y");
		Button N = (Button) anchorPane.lookup("#N");

		Y.setOnAction((EventHandler<ActionEvent>) new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Platform.runLater(() -> {
					card[myturn]--;
					SocketConnect.getOutMsg().println("Card/" + card[0] + "/" + card[1]);
					cardcnt1.setText(card[myturn] + "");

					cardpass = 0;
					try {
						passpopup.setAutoFix(true);
						passpopup.setAutoHide(true);
						passpopup.setHideOnEscape(true);

						passpopup.getContent().add(FXMLLoader.load(getClass().getResource("passpopup.fxml")));
						passpopup.show(primaryStage);
					} catch (IOException e1) {						
						e1.printStackTrace();
					}
					cardpass = 2;
					pass.close();
					SocketConnect.getOutMsg().println("ChangeTurn/" + turn);

				});
			}
		});
		N.setOnAction((EventHandler<ActionEvent>) new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				showDialog();
				pass.close();
				SocketConnect.getOutMsg().println("ChangeTurn/" + turn);
			}
		});

		Scene scene = new Scene(anchorPane);
		pass.setScene(scene);
		pass.show();
		
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
			e.printStackTrace();
		}		
		Button Yes = (Button) anchorPane.lookup("#Yes");
		
		Button No = (Button) anchorPane.lookup("#No");

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

	private void getChoiceWarp(ChoiceBox<String> choiceBox, Stage dialog) {
		int index = choiceBox.getItems().indexOf(choiceBox.getValue());
		int move = 0;
		if (index >= position[myturn]) {
			move = index - position[myturn];
		} else {
			move = 19 + index;
		}
		int movecount = move;
		System.out.println(move);
		dialog.close();
		System.out.println(position[myturn]);
		Thread thread = new Thread() {
			@Override
			public void run() {
				path = new Path();
				Platform.runLater(() -> {

					SocketConnect.getOutMsg().println("MoveDice/" + myturn + "/" + myturn + "/" + movecount);
					for (int i = 0; i < movecount; i++) {
						setPosition(myturn);
					}
					pathTransition.setDuration(Duration.millis(150 + 50 * (movecount)));
					pathTransition.setPath(path);
					if (myturn == 0) {
						pathTransition.setNode(C1);
					} else {
						pathTransition.setNode(C2);
					}
					pathTransition.play();

				});
				try {
					Thread.sleep(150 + 50 * (movecount) + 300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Platform.runLater(() -> {

					showDialog();
				});

			}

		};
		thread.setDaemon(true);
		thread.start();

	}

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
				if (!pd.getOwner().equals(user[myturn]) && !pd.getOwner().equals("X")) {
					yourLand.getItems().add(pd.name);
				}
			}
			break;
		case 1:

			for (PlanetData pd : planetData) {
				if (!pd.getOwner().equals(user[myturn]) && !pd.getOwner().equals("X")) {
					yourLand.getItems().add(pd.name);
					cnt1++;
				} else if (pd.getOwner().equals(user[myturn])) {
					myLand.getItems().add(pd.name);
					cnt2++;
				}
			}

			break;
		case 2:
			for (PlanetData pd : planetData) {
				if (!pd.getOwner().equals(user[myturn]) && pd.getPrice() != 0) {
					yourLand.getItems().add(pd.name);
				}
			}
			break;
		case 3:
			yourLand.setVisible(false);
			yourLandLabel.setVisible(false);
			for (PlanetData pd : planetData) {
				if (pd.getOwner().equals(user[myturn])) {
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
		int position1=0;
		int position2=0;
		String[] name = { choiceYourBox.getValue(), choiceMyBox.getValue() };
		// System.out.println(myturn + "," + nextTurn + ":" + name[0] + "," + name[1]);
		if (name[0] == null || name[1] == null) {
			return;
		}

		for (int i = 0; i < planetData.size(); i++) {
			if (planetData.get(i).name.equals(name[0])) { // 선택한 상대 땅일때 동작
				 // 땅 가져올때 플래그들 변경 함수
				position1 = i;
			}
			if(planetData.get(i).name.equals(name[1])) {
				position2 = i;
			}
			
			ChangeLandFlag(position1, myturn , position2, yourturn);
			
			choiceLand.setVisible(false);
		}

	}

	// 비밀카드로 땅 선택시 동작 함수
	private void getChoice(ChoiceBox<String> choiceBox, int index) {
		String name = choiceBox.getValue();		

		if (name == null) {
			return;
		}

		for (int i = 0; i < planetData.size(); i++) {
			if (planetData.get(i).name.equals(name)) {
				// 선택한 땅일때 동작
				if (index == 3) {
					if (planetData.get(i).count == 1) {
						setBuilding(i, -33, myturn);
						setBuilding(i, -10, myturn);
					} else if (planetData.get(i).count == 2) {
						setBuilding(i, -10, myturn);
						setBuilding(i, 13, myturn);

					}

				} else {
					// 플래그 추가 혹은 변경
					if (!planetData.get(i).owner.equals("X")) { // 상대땅 가져오기 일때만 기존 플레그 제거
						ChangeLandFlag(i, myturn , -1 , -1); // 땅 가져올대 플래그들 변경 함수
					} else {
						setFlag(i, myturn); // 플래그 설치 함수

					}
					// 소유자 변경
				}

			}
		}

		choiceLand.setVisible(false);
	}

	public void BuyLand(String name, String owner, int price) {

		Stage dialog = new Stage(StageStyle.UTILITY);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(primaryStage);
		dialog.setTitle("구매 확인");
		int cost = 0;
		if (planetData.get(position[myturn]).count == 0) {
			cost = price;
		} else {
			cost = price + (price / 2) * (planetData.get(position[myturn]).count - 1);
		}

		AnchorPane anchorPane = null;

		try {
			anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("dialog2.fxml"));
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		Label BuyMessage = (Label) anchorPane.lookup("#BuyMessage");
		MyMoney = (Label) anchorPane.lookup("#MyMoney");

		Button Complete = (Button) anchorPane.lookup("#Complete");

		Complete.setOnAction(event -> dialog.close());
//         Rectangle rec[] = new Rectangle[2];

//         ImageView flag = new ImageView();
		System.out.println("cost : " + cost);
		System.out.println("price : " + price);
		if (money[myturn] < cost) {
			BuyMessage.setText("보유금액이 부족합니다.");
		} else {
			if (planetData.get(position[myturn]).owner.equals("X")) {
				planetData.get(position[myturn]).owner = user[myturn];
				PlanetOwner.setText(user[myturn]);
			}
			if (!planetData.get(position[myturn]).owner.equals(user[myturn])) { // 내땅이 아닐경우
				ChangeLandFlag(position[myturn], myturn , -1, -1); // 땅 가져올대 플래그들 변경 함수
				setMoney(-cost, cost);
				PlanetOwner.setText(user[myturn]);
				BuyMessage.setText("구매 완료하였습니다.");
			} else { // 빈땅 이거나가 내땅인경우
				if (planetData.get(position[myturn]).count == 0) { // 빈땅일때
					setFlag(position[myturn], myturn);
					BuyMessage.setText("구매 완료하였습니다.");
					if (owner.equals("X")) {
						setMoney(-price, 0);
					} else {
						setMoney(-cost, 0);
					}

				} else if (planetData.get(position[myturn]).count > 3) {
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("알림");
					alert.setHeaderText("구매 불가능");
					alert.setContentText("이미 지을 수 있는 빌딩 개수의최대입니다.");
					alert.showAndWait();
					cnt1 = 0;
					cnt2 = 0;
					return;
				} else {
					int locate[] = { -33, - 10, 13 };
					setBuilding(position[myturn], locate[planetData.get(position[myturn]).count - 1], myturn); // 건물 설치
					// 함수 호출
					BuyMessage.setText("구매 완료하였습니다.");
					setMoney(-(price / 2), 0);
				}
			}

		}
		CostPlanet.setVisible(false);
		BuyPlanet.setVisible(false);
		Scene scene = new Scene(anchorPane);
		dialog.setScene(scene);
		dialog.show();

		// ChangeLandFlag(1, 0);
		// ChangeLandFlag(2, 1);
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
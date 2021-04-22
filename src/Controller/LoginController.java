package Controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import conn.PlayerClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author oXCToo
 */
public class LoginController implements Initializable {
	@FXML
	private Label lblErrors,btnForgot,pw;

	@FXML
	private TextField txtUsername,txtPassword;

	@FXML
	private Button btnSignin,btnSignup;

	private Stage primaryStage;
	Connection con = null;
	PreparedStatement psmt = null;
	ResultSet rs = null;
	PlayerClient SocketConnect;
	String id = "success";
	int win;
	int lose;
	AnchorPane root;
	AppController appController;
	static String findPw;	// 찾은 패스워드

	public String getId() {
		return id;
	}

	public void setRoot(AnchorPane root) {
		this.root = root;
	}

	public void setAppController(AppController appController) {
		this.appController = appController;
	}

	@FXML
	public void handleButtonAction(MouseEvent event) throws SQLException {

		if (event.getSource() == btnSignin) {			
			logIn();
			// login here
		} else if (event.getSource() == btnSignup) {
			signup();
		}
	}
	
	@FXML
	public void handleLabelAction(MouseEvent event) throws SQLException {
		if (event.getSource() == btnForgot) {
			forgotPW();
		}
	}
	
	public void forgotPW() {
		AnchorPane anchorPane = null;
		Stage signup = new Stage(StageStyle.UTILITY);
		signup.initModality(Modality.WINDOW_MODAL);
		signup.initOwner(primaryStage);
		signup.setTitle("Find Password");

		try {
			anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("signup.fxml"));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TextField name_signup = (TextField) anchorPane.lookup("#name_signup");		
		TextField id_signup = (TextField) anchorPane.lookup("#id_signup");
		TextField pw_signup = (TextField) anchorPane.lookup("#pw_signup");
		Button signup_btn = (Button) anchorPane.lookup("#signup_btn");
		signup_btn.setText("Find");
		Button back_btn = (Button) anchorPane.lookup("#back_btn");
		Label lblErrors = (Label) anchorPane.lookup("#lblErrors");
		Label title = (Label) anchorPane.lookup("#title");
		Label pw = (Label) anchorPane.lookup("#pw");
		title.setText("Find PW");
		pw.setVisible(false);
		pw_signup.setVisible(false);

		signup_btn.setOnAction((EventHandler<ActionEvent>) new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Platform.runLater(() -> {

					String name = name_signup.getText();
					String id = id_signup.getText();
					String sql = "SELECT pwd FROM MEMBER Where id = '" + id + "' and name = '" + name + "'";					
					if (name.isEmpty() || id.isEmpty()) {
						lblErrors.setTextFill(Color.RED);
						lblErrors.setText("Please, fill in all the blanks");
					} else {
						
						SocketConnect.getOutMsg().println("FindPW/" + sql);
						signup.close();
					}

				});
			}
		});

		back_btn.setOnAction(event -> signup.close());
		Scene scene = new Scene(anchorPane);
		signup.setScene(scene);
		signup.show();
	}
	
	public void signup() {
		AnchorPane anchorPane = null;
		Stage signup = new Stage(StageStyle.UTILITY);
		signup.initModality(Modality.WINDOW_MODAL);
		signup.initOwner(primaryStage);
		signup.setTitle("sign up");

		try {
			anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("signup.fxml"));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TextField name_signup = (TextField) anchorPane.lookup("#name_signup");
		TextField id_signup = (TextField) anchorPane.lookup("#id_signup");
		TextField pw_signup = (TextField) anchorPane.lookup("#pw_signup");
		Button signup_btn = (Button) anchorPane.lookup("#signup_btn");
		Button back_btn = (Button) anchorPane.lookup("#back_btn");
		Label lblErrors = (Label) anchorPane.lookup("#lblErrors");

		signup_btn.setOnAction((EventHandler<ActionEvent>) new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Platform.runLater(() -> {

					String name = name_signup.getText();
					String id = id_signup.getText();
					String pw = pw_signup.getText();
					String sql = "insert into member values(null, '" + id + "','" + pw + "'," + 0 + "," + 0 + ",'"
							+ name + "')";
					if (name.isEmpty() || id.isEmpty() || pw.isEmpty()) {
						lblErrors.setTextFill(Color.RED);
						lblErrors.setText("Please, fill in all the blanks");
					} else {
						SocketConnect.getOutMsg().println("Signup/" + sql);
						signup.close();
					}

				});
			}
		});

		back_btn.setOnAction(event -> signup.close());
		Scene scene = new Scene(anchorPane);
		signup.setScene(scene);
		signup.show();
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
						System.out.println("소켓 통신 error");
					}
					if (msg != null) {						
						rmsg = msg.split("/");
						try {
							id = rmsg[0];
						} catch (Exception e2) {
							e2.getMessage();
						}
						if (msg.equals("Error")) {
							Platform.runLater(() -> {
								setLblError(Color.TOMATO, "ID/Password 가 틀렸습니다.");
								id = "Error";
								SocketConnect.setMsg(null);
							});
						} 
						
						// 패스워드 찾기
						else if(id.equals("FindPW")) {
							System.out.println("pw>>>>>"+rmsg[1]);
							findPw = rmsg[1];
							Platform.runLater(() ->{
								Stage dialog = new Stage(StageStyle.UTILITY);
								dialog.initModality(Modality.WINDOW_MODAL);
								dialog.initOwner(primaryStage);
								dialog.setTitle("패스워드 확인");
								AnchorPane anchorPane = null;

								try {
									anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("dialog2.fxml"));
								} catch (IOException e2) {
									e2.printStackTrace();
								}

								Label BuyMessage = (Label) anchorPane.lookup("#BuyMessage");
								if(findPw.equals("NotFound")) {
									BuyMessage.setText("존재하지 않는 사용자 입니다.");
								}else {
									String pw = "찾으시는 패스워드는 '"+ findPw +"' 입니다.";
									BuyMessage.setText(pw);
								}

								Button Complete = (Button) anchorPane.lookup("#Complete");
								Complete.setOnAction(event -> dialog.close());
								
								Scene scene = new Scene(anchorPane);
								dialog.setScene(scene);
								dialog.show();
							});
						} else {
							Platform.runLater(() -> {
								setLblError(Color.GREEN, "Login Successful..Redirecting..");
							});
							String login = msg;
							Platform.runLater(() -> {
								Stage stage = (Stage) btnSignin.getScene().getWindow();								
								stage.close();
								appController.setPrimaryStage(stage);
								appController.setRoot(root);
								appController.setSocket(SocketConnect);
								appController.setLogin(login);
								appController.getMsg();
								Scene scene = new Scene(root);
								stage.setTitle("BlueMarble");
								stage.setScene(scene);
								stage.show();
								
							});
							break;
						}
					}
				}
			}
		};
		thread.start();
	}

	// 로그인 체크
	private void logIn() {
		String id = txtUsername.getText();
		String pwd = txtPassword.getText();
		System.out.println(id + "," + pwd);
		if (id.isEmpty() || pwd.isEmpty()) {
			setLblError(Color.TOMATO, "Empty credentials");
			id = "Error";
		} else {
			// query
			String sql = "SELECT id,WIN,LOSE FROM MEMBER Where id = '" + id + "' and pwd = '" + pwd + "'";
			SocketConnect.getOutMsg().println("Login/" + sql);

		}

	}

	private void setLblError(Color color, String text) {
		lblErrors.setTextFill(color);
		lblErrors.setText(text);
		System.out.println(text);
	}

	public void setSocket(PlayerClient SocketConnect) {
		this.SocketConnect = SocketConnect;
		if (SocketConnect == null) {
			lblErrors.setTextFill(Color.TOMATO);
			lblErrors.setText("Server Error : Check");
		} else {
			lblErrors.setTextFill(Color.GREEN);
			lblErrors.setText("Server is up : Good to go");
		}
		getMsg();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		
	}
}
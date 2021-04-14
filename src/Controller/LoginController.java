package Controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import conn.DBConn;
import conn.PlayerClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
	private Label lblErrors;

	@FXML
	private TextField txtUsername;

	@FXML
	private TextField txtPassword;

	@FXML
	private Button btnSignin;

	@FXML
	private Button btnSignup;

	private Stage primaryStage;
	Connection con = null;
	PreparedStatement psmt = null;
	ResultSet rs = null;
	PlayerClient SocketConnect;
	String id = "success";
	int win;
	int lose;

	public String getId() {
		return id;
	}

	@FXML
	public void handleButtonAction(MouseEvent event) throws SQLException {

		if (event.getSource() == btnSignin) {
			logIn();
			// login here
			getMsg();
		} else if (event.getSource() == btnSignup) {
			signup();
		}
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
					String dice = "basic";
					String skin = "normal";
					String sql = "insert into member values(null, '" + id + "','" + pw + "'," + 0 + "," + 0 + "," + 1000
							+ ",'" + dice + "','" + skin + "','" + name + "')";
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
				String[] rmsg;
				String msg;
				while (true) {
					if (SocketConnect.getMsg() != null) {
						msg = SocketConnect.getMsg();
						rmsg = msg.split("/");
						id = rmsg[1];
						System.out.println(id);
						Platform.runLater(() -> {
							if (msg.equals("Error")) {
								setLblError(Color.TOMATO, "Enter Correct ID/Password");
								id = "Error";
								SocketConnect.setMsg(null);
							} else {
								setLblError(Color.GREEN, "Login Successful..Redirecting..");
							}

						});

						break;
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (!msg.equals("Error")) {
					Platform.runLater(() -> {
						Stage stage = (Stage) btnSignin.getScene().getWindow();
						stage.close();

						FXMLLoader loader = new FXMLLoader(getClass().getResource("../application/Root.fxml"));
						AnchorPane root;
						try {
							root = loader.load();
							AppController controller = loader.getController();
							controller.setPrimaryStage(stage);
							controller.setRoot(root);
							controller.setSocket(SocketConnect);
							Scene scene = new Scene(root);
							stage.setTitle("BlueMarble");
							stage.setScene(scene);
							stage.show();

						} catch (IOException e) {
							System.err.println(e.getMessage());
						}

					});

				}

			}

		};
		// thread.setDaemon(true);
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
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
	}
}
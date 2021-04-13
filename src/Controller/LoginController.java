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
import javafx.stage.Stage;

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

	Connection con = null;
	PreparedStatement psmt = null;
	ResultSet rs = null;
	PlayerClient SocketConnect;
	String id = "success";
	int win;
	int lose;

	@FXML
	public void handleButtonAction(MouseEvent event) throws SQLException {

		if (event.getSource() == btnSignin) {
			logIn();
			// login here
			getMsg();
		}
	}

	public void getMsg() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				String msg;
				while (true) {
					if (SocketConnect.getMsg() != null) {
						msg = SocketConnect.getMsg();

						System.out.println(msg);
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
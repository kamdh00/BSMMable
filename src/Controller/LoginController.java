package Controller;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import conn.DBConn;
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

	/// --
	Connection con = null;
	PreparedStatement psmt = null;
	ResultSet rs = null;
	
	private Stage primaryStage;

	@FXML
	public void handleButtonAction(MouseEvent event) {

		if (event.getSource() == btnSignin) {
			// login here
			if (logIn().equals("Success")) {
				try {
                    Node node = (Node) event.getSource();
                    Stage stage = (Stage) node.getScene().getWindow();
                    stage.close();
                    
                    FXMLLoader loader= new FXMLLoader(getClass().getResource("../application/Root.fxml"));                    
              	  	AnchorPane root = loader.load();
              	  	AppController controller = loader.getController();
              	  	controller.setPrimaryStage(stage);
              	  	controller.setRoot(root);
              	  	Scene scene = new Scene(root);
              	  	stage.setTitle("BlueMarble");
              	  	stage.setScene(scene);
              	  	stage.show();
				} catch (IOException ex) {
					System.err.println(ex.getMessage());
				}

			}
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// TODO
		if (con == null) {
			lblErrors.setTextFill(Color.TOMATO);
			lblErrors.setText("Server Error : Check");
		} else {
			lblErrors.setTextFill(Color.GREEN);
			lblErrors.setText("Server is up : Good to go");
		}
	}

	public LoginController() throws SQLException {
		con = DBConn.getConnection();
	}

	// 로그인 체크
	private String logIn() {
		String status = "Success";
		String id = txtUsername.getText();
		String pwd = txtPassword.getText();
		System.out.println(id + "," + pwd);
		if (id.isEmpty() || pwd.isEmpty()) {
			setLblError(Color.TOMATO, "Empty credentials");
			status = "Error";
		} else {
			// query
			String sql = "SELECT * FROM MEMBER Where id = '" + id + "' and pwd = '" + pwd + "'";
			try {
				psmt = con.prepareStatement(sql);
				rs = psmt.executeQuery();
				if (!rs.next()) {
					setLblError(Color.TOMATO, "Enter Correct ID/Password");
					status = "Error";
				} else {
					setLblError(Color.GREEN, "Login Successful..Redirecting..");
				}
			} catch (SQLException ex) {
				System.err.println(ex.getMessage());
				status = "Exception";
			}
		}

		return status;
	}

	private void setLblError(Color color, String text) {
		lblErrors.setTextFill(color);
		lblErrors.setText(text);
		System.out.println(text);
	}
}
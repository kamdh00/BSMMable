package conn;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;



public class PlayerClient implements Runnable {
    private String ip;
    private String id;
    private Socket socket;
    private BufferedReader inMsg = null;
    private PrintWriter outMsg = null;
    private String msg =null;    
    public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	private Thread thread;

    boolean status;
    
    public BufferedReader getInMsg() {
		return inMsg;
	}

	public void setInMsg(BufferedReader inMsg) {
		this.inMsg = inMsg;
	}

	public PrintWriter getOutMsg() {
		return outMsg;
	}

	public void setOutMsg(PrintWriter outMsg) {
		this.outMsg = outMsg;
	}

	public PlayerClient(String ip) {
        this.ip = ip;
        connectServer();
    }
    
    public void connectServer() {
        try {
            socket = new Socket(ip, 8888);
            System.out.println("[Client]Server 연결 성공!!");
            inMsg = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outMsg = new PrintWriter(socket.getOutputStream(), true);
            thread = new Thread(this);
            thread.start();
           
        } catch(Exception e) {
            System.out.println("[Client]connectServer() Exception 발생!!");
        }
    }
    public void run() {
        status = true;
        while(status) {
            try {           
                msg = inMsg.readLine();                
                System.out.println("pc : "+msg);                
            } catch(IOException e) {
                status = false;
            }
        }

        System.out.println("[Client]" + thread.getName() + "종료됨");
    }    
}
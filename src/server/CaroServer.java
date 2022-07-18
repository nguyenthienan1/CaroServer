package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import db.SQLConnection;
import io.Session;

public class CaroServer implements Runnable {
	public static int PORT = 8888;
	public static SQLConnection sql;

	public static void main(String[] args) {
		try {
			sql = new SQLConnection();
			sql.checkData("SELECT * from user");
		} catch (Exception e) {
			e.printStackTrace();
		}
		new Thread(new CaroServer()).start();
		ServerSocket listenSocket = null;
		try {
			listenSocket = new ServerSocket(PORT);
			System.out.println("Listen " + PORT);
			while (true) {
				Socket clientSocket = listenSocket.accept();
				Session conn = new Session(clientSocket);
				conn.start();
				System.out.println("Accept socket: " + clientSocket.getRemoteSocketAddress());
			}
		} catch (IOException ioE) {
			ioE.printStackTrace();
			System.exit(0);
		}

		if (listenSocket != null) {
			try {
				listenSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
			}
			System.out.println("Players online: " + PlayerManager.gI().size());
		}

	}
}
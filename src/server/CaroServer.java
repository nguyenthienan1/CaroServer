package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import db.SQLConnection;
import io.Session;

public class CaroServer extends Thread {
	public static int PORT = 8888;
	public static boolean server;
	public static SQLConnection sql;

	public static void main(String[] args) {
		new CaroServer().start();
		while (true) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			String str = "";
			try {
				str = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (str.equals("online")) {
				System.out.println(PlayerManager.gI().size());
			}
			if (str.equals("gc")) {
				System.gc();
			}
		}
	}

	@Override
	public void run() {
		try {
			sql = new SQLConnection();
			sql.checkData("SELECT * from user");
		} catch (Exception e) {
			e.printStackTrace();
		}
		server = true;
		ServerSocket listenSocket = null;
		try {
			listenSocket = new ServerSocket(PORT);
			System.out.println("Listen " + PORT);
			while (server) {
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
		server = false;
		System.exit(0);

	}
}
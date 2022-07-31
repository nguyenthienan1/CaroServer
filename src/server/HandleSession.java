package server;

import java.sql.ResultSet;

import caro.Player;
import io.Cmd_Client2Server;
import io.Message;
import io.Session;

public class HandleSession extends Cmd_Client2Server {
	private static HandleSession instance;

	public static HandleSession gI() {
		if (instance == null) {
			instance = new HandleSession();
		}
		return instance;
	}

	synchronized public void processSesionMessage(Session conn, Message m) throws Exception {
		Player player = null;
		switch (m.command) {
		case LOGIN:
			String username = m.reader().readUTF();
			String password = m.reader().readUTF();
			ResultSet rs = CaroServer.sql.statement.executeQuery("SELECT * FROM `user` WHERE (`username`LIKE'"
					+ username + "' AND `password`LIKE'" + password + "');");

			if (rs != null && rs.first()) {
				player = PlayerManager.gI().get(username);
				if (player != null) {
					conn.sendMessageDialog("Your account login on other device, please try again");
				} else {
					int id = rs.getInt("id");
					conn.username = username;
					player = new Player(conn);
					player.id = id;
					PlayerManager.gI().put(player);
					player.loginSuccess();
					System.out.println("Player " + player.username + " login");
				}
			} else {
				conn.sendMessageDialog("Username or password incorrect");
			}
			break;
		case REGISTER:
			String name = m.reader().readUTF();
			String pass = m.reader().readUTF();
			String repass = m.reader().readUTF();
			String check = Player.checkRegister(name, pass, repass);
			if (check != null) {
				conn.sendMessageDialog(check);
				break;
			}

			ResultSet resultSet = CaroServer.sql.statement
					.executeQuery("SELECT * FROM `user` WHERE (`username`LIKE'" + name + "');");
			if (resultSet != null && resultSet.first()) {
				conn.sendMessageDialog("Username already used");
			} else {
				String sql = "INSERT INTO `user`(`id`, `username`, `password`) VALUES (0,'" + name + "','" + pass
						+ "')";
				if (CaroServer.sql.executeSQLUpdate(sql))
					conn.sendMessageDialog("Register success");
			}
			break;
		case LOG_OUT:
		case PIECE:
		case CREATE_ROOM:
		case JOIN_ROOM:
		case UPDATE_LIST_ROOM:
		case LEAVE_ROOM:
		case CHAT_ROOM:
		case READY:
			player = PlayerManager.gI().get(conn.username);
			if (player == null) {
				conn.sendMessageDialog("An error occurred");
				System.out.println("Player null");
			} else {
				player.processMessage(m);
			}
			break;
		default:
			conn.sendMessageDialog("empty");
			System.out.println("empty case");
			break;
		}
	}
}

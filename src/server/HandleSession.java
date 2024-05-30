package server;

import java.sql.ResultSet;

import caro.Player;
import io.Cmd_Client2Server;
import io.Message;
import io.Session;

public class HandleSession extends Cmd_Client2Server {
	private Session session;

	public HandleSession(Session session) {
		this.session = session;
	}

	private ServiceSession getServiceSesion() {
		return session.getService();
	}

	public void processSessionMessage(Message m) {
		switch (m.command) {
		case LOGIN:
			login(m);
			break;
		case REGISTER:
			register(m);
			break;
		case LOG_OUT:
		case PIECE:
		case CREATE_ROOM:
		case JOIN_ROOM:
		case UPDATE_LIST_ROOM:
		case LEAVE_ROOM:
		case CHAT_ROOM:
		case READY:
			Player player = PlayerManager.gI().get(session.id);
			if (player == null) {
				getServiceSesion().sendMessageDialog("An error occurred");
				System.out.println("Player null");
			} else {
				player.processMessage(m);
			}
			break;
		default:
			getServiceSesion().sendMessageDialog("empty");
			System.out.println("empty case");
			break;
		}
	}

	private void login(Message m) {
		try {
			String username = m.reader().readUTF();
			String password = m.reader().readUTF();
			ResultSet rs = CaroServer.sql.statement.executeQuery("SELECT * FROM `user` WHERE (`username`='"
					+ username + "' AND `password` = '" + password + "');");

			if (rs != null && rs.first()) {
				int id = rs.getInt("id");
				Player player = PlayerManager.gI().get(id);
				if (player != null) {
					getServiceSesion().sendMessageDialog("Tài khoản của bạn đang đăng nhập ở nơi khác, vui lòng thử lại sau");
				} else {
					session.username = username;
					session.id = id;
					player = new Player(session);
					PlayerManager.gI().put(player);
					getServiceSesion().loginSuccess();
					System.out.println("Player " + player.username + " login");
				}
			} else {
				getServiceSesion().sendMessageDialog("Tên tài khoản hoặc mật khẩu không chính xác");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void register(Message m) {
		try {
			String username = m.reader().readUTF();
			String pass = m.reader().readUTF();
			String repass = m.reader().readUTF();
			String check = checkRegister(username, pass, repass);
			if (check != null) {
				getServiceSesion().sendMessageDialog(check);
				return;
			}

			ResultSet resultSet = CaroServer.sql.statement
					.executeQuery("SELECT * FROM `user` WHERE (`username`LIKE'" + username + "');");
			if (resultSet != null && resultSet.first()) {
				getServiceSesion().sendMessageDialog("Username already used");
			} else {
				String sql = "INSERT INTO `user`(`id`, `username`, `password`) VALUES (0,'" + username + "','" + pass
						+ "')";
				if (CaroServer.sql.executeSQLUpdate(sql))
					getServiceSesion().sendMessageDialog("Tạo tài khoản thành công");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String checkRegister(String name, String pass, String repass) {
		if (name.length() < 6 || name.length() > 13) {
			return "Tên tài khoản phải có độ dài lớn hơn 6 và nhỏ hơn 12";
		}
		if (pass.length() < 6 || pass.length() > 13) {
			return "Mật khẩu phải có độ dài lớn hơn 6 và nhỏ hơn 12";
		}
		if (!pass.equals(repass)) {
			return "Mật khẩu và mật khẩu nhập lại không khớp nhau";
		}
		String check = "abcdefghijklmnopqrstuvwxyz1234567890";
		for (int i = 0; i < name.length(); i++) {
			if (!check.contains(String.valueOf(name.charAt(i)))) {
				return "Tên tài khoản chỉ được chứa các kí tự a-z, 0-9 và không chứa kí tự đặc biệt";
			}
		}
		for (int i = 0; i < pass.length(); i++) {
			if (!check.contains(String.valueOf(pass.charAt(i)))) {
				return "Mật khẩu chỉ được chứa các kí tự a-z, 0-9 và không chứa kí tự đặc biệt";
			}
		}
		return null;
	}
}

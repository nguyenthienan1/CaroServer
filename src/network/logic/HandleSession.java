package network.logic;

import static network.io.Cmd_Client2Server.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import caro.AIPlayer;
import caro.HumanPlayer;
import caro.Player;
import caro.Room;
import database.SQLConnection;
import network.Session;
import network.io.Message;
import server.PlayerManager;
import server.RoomManager;

public class HandleSession {
	private Session session;

	public HandleSession(Session session) {
		this.session = session;
	}

	private Service getService() {
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
		case ADD_BOT:
			Player player = PlayerManager.gI().get(session.id);
			if (player == null) {
				getService().sendMessageDialog("An error occurred");
				System.out.println("Player null");
			} else {
				processPlayerMessage(m, player);
			}
			break;
		default:
			getService().sendMessageDialog("empty");
			System.out.println("empty case");
			break;
		}
	}

	private void processPlayerMessage(Message m, Player p) {
		switch (m.command) {
		case LOG_OUT:
			logOut(p);
			break;
		case CREATE_ROOM:
			createRoom(m, p);
			break;
		case JOIN_ROOM:
			joinRoom(m, p);
			break;
		case UPDATE_LIST_ROOM:
			p.getService().sendListRoom();
			break;
		case PIECE:
		case LEAVE_ROOM:
		case CHAT_ROOM:
		case READY:
		case ADD_BOT:
			if (p.room == null) {
				break;
			}
			processRoomMsg(m, p, p.room);
			break;
		}
	}

	public void processRoomMsg(Message m, Player p, Room room) {
		switch (m.command) {
		case PIECE:
			setPiece(m, p, room);
			break;
		case LEAVE_ROOM:
			leaveRoom(m, p, room);
			break;
		case CHAT_ROOM:
			chatRoom(m, p, room);
			break;
		case READY:
			ready(m, p, room);
			break;
		case ADD_BOT:
			addBotPlayer(p, room);
			break;
		}
	}

	private void login(Message m) {
		try {
			String username = m.reader().readUTF();
			String password = m.reader().readUTF();

			PreparedStatement statement = SQLConnection.gI().getConnection()
					.prepareStatement("SELECT * FROM `user` WHERE `username`=? AND `password` = ?;");
			statement.setString(1, username);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();

			if (rs != null && rs.first()) {
				int id = rs.getInt("id");
				HumanPlayer player = PlayerManager.gI().get(id);
				if (player != null) {
					getService().sendMessageDialog("Tài khoản của bạn đang đăng nhập ở nơi khác, vui lòng thử lại sau");
				} else {
					session.username = username;
					session.id = id;
					player = new HumanPlayer(session);
					PlayerManager.gI().put(player);
					getService().loginSuccess();
					getService().sendListRoom();
					System.out.println("Player " + player.username + " login");
				}
			} else {
				getService().sendMessageDialog("Tên tài khoản hoặc mật khẩu không chính xác");
			}

			rs.close();
			statement.close();
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
				getService().sendMessageDialog(check);
				return;
			}

			Statement statement = SQLConnection.gI().getConnection().createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM `user` WHERE `username`='" + username + "';");
			if (resultSet != null && resultSet.first()) {
				getService().sendMessageDialog("Tên tài khoản đã tồn tại");
			} else {
				String insertQuery = "INSERT INTO `user`(`id`, `username`, `password`) VALUES (0,?,?);";
				PreparedStatement preparedStatement = SQLConnection.gI().getConnection().prepareStatement(insertQuery);
				preparedStatement.setString(1, username);
				preparedStatement.setString(2, pass);

				int rowsAffected = preparedStatement.executeUpdate();
				if (rowsAffected > 0) {
					getService().sendMessageDialog("Tạo tài khoản thành công");
				}

				preparedStatement.close();
			}
			resultSet.close();
			statement.close();
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

	private void logOut(Player player) {
		PlayerManager.gI().remove((HumanPlayer) player);
		player.getService().logOutSuccess();
		System.out.println("Player " + player.username + " log out");
	}

	private void createRoom(Message m, Player player) {
		try {
			if (player.room != null) {
				player.getService().sendMessageDialog("Bạn đang ở trong phòng khác");
				return;
			}
			Room room = new Room(Room.baseId);
			player.room = room;
			RoomManager.gI().add(room);
			room.addPlayer(player);
			player.getService().joinRoomSuccess(room.roomNumber);

			room.sendListPlayerBC();
			PlayerManager.gI().sendListRoomBC();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void joinRoom(Message m, Player player) {
		try {
			if (player.room != null) {
				player.getService().sendMessageDialog("Bạn đang ở trong phòng khác");
				return;
			}
			int roomID = m.reader().readInt();
			Room room = RoomManager.gI().get(roomID);
			if (room == null) {
				player.getService().sendMessageDialog("Không tìm thấy phòng đã chọn, hãy cập nhật danh sách phòng");
				return;
			}
			player.getService().joinRoomSuccess(room.roomNumber);
			room.addPlayer(player);
			player.room = room;

			room.sendChatBC(player.username + " đã vào phòng");
			room.sendListPlayerBC();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void ready(Message m, Player player, Room room) {
		try {
			if (player.isSpecPlayer()) {
				return;
			}
			if (room.isStarted) {
				player.getService().sendMessageDialog("Trận đấu đang diễn ra");
				return;
			}
			if (player.isReady) {
				player.getService().sendMessageDialog("Bạn đã sẵn sàng rồi");
				return;
			}
			if (room.players.size() < 2) {
				player.getService().sendMessageDialog("Vui lòng chờ thêm người chơi khác vào phòng");
				return;
			}

			room.playerReady(player);

			Player aiPlayer = room.hasBotPlayer();
			if (aiPlayer != null && !aiPlayer.isReady) {
				room.playerReady(aiPlayer);
			}

			room.sendListPlayerBC();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setPiece(Message m, Player player, Room room) {
		try {
			if (player.isSpecPlayer()) {
				return;
			}
			if (!room.isStarted) {
//				getServiceSession().sendMessageDialog("Trận đấu chưa bắt đầu");
				return;
			}
			if (!player.isTurn) {
				player.getService().sendMessageDialog("Hãy chờ đến lượt");
				return;
			}

			int xPiece = m.reader().readInt();
			int yPiece = m.reader().readInt();

			int checkSetPiece = 0;
			if (player instanceof HumanPlayer) {
				checkSetPiece = room.setPiece(xPiece, yPiece, player);
			}

			if (checkSetPiece == 1) {
				Player aiPlayer = room.hasBotPlayer();
				if (aiPlayer != null) {
					((AIPlayer) aiPlayer).move();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void leaveRoom(Message m, Player player, Room room) {
		try {
			if (!player.isSpecPlayer() && room.isStarted) {
				room.finishMatch();
			}

			room.removePlayer(player);
			room.sendListPlayerBC();

			if (room.countHumanPlayer() == 0) {
				RoomManager.gI().remove(room);
			} else {
				room.sendChatBC(player.username + " đã rời phòng");
			}

			player.room = null;
			player.getService().leaveRoomSuccess();
			PlayerManager.gI().sendListRoomBC();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void chatRoom(Message m, Player player, Room room) {
		try {
			String content = m.reader().readUTF();
			content = content.trim();
			if (!content.equals("")) {
				room.sendChatBC(player.username + ": " + content);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addBotPlayer(Player player, Room room) {
		if (!player.isSpecPlayer() && room.isStarted) {
			return;
		}
		if (room.players.size() >= 2) {
			player.getService().sendMessageDialog("Phòng đã đầy không thể thêm");
			return;
		}

		AIPlayer ai_player = new AIPlayer();
		room.addPlayer(ai_player);
		ai_player.room = room;
		room.playerReady(ai_player);

		room.sendListPlayerBC();
	}
}

package server;

import java.sql.ResultSet;

import caro.Board;
import caro.Player;
import caro.Room;
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

	public void processSesionMessage(Session conn, Message m) throws Exception {
		Player player = PlayerManager.gI().get(conn.username);
		switch (m.command) {
		case Cmd_Client2Server.LOGIN:
			String username = m.reader().readUTF();
			String password = m.reader().readUTF();
			ResultSet rs = CaroServer.sql.statement.executeQuery("SELECT * FROM `user` WHERE (`username`LIKE'"
					+ username + "' AND `password`LIKE'" + password + "');");
			if (rs != null && rs.first()) {
				player = PlayerManager.gI().get(username);
				if (player != null) {
					conn.SendMessageDialog("Your account is login in other device, please try again");
				} else {
					int id = rs.getInt("id");
					conn.username = username;
					player = new Player(conn);
					player.id = id;
					PlayerManager.gI().put(player);
					player.LoginOK();
					System.out.println("Player " + player.username + " login");
				}
			} else {
				conn.SendMessageDialog("Username or password incorrect");
			}
			break;
		case Cmd_Client2Server.REGISTER:
			String check = "abcdefghijklmnopqrstuvwxyz1234567890";
			String name = m.reader().readUTF();
			String pass = m.reader().readUTF();
			String repass = m.reader().readUTF();
			if (name.length() < 6 || name.length() > 13) {
				conn.SendMessageDialog("Username is 6-12 in length");
				break;
			}
			if (pass.length() < 6 || pass.length() > 13) {
				conn.SendMessageDialog("Password is 6-12 in length");
				break;
			}
			if (!pass.equals(repass)) {
				conn.SendMessageDialog("Your password and confirmation password do not match");
				break;
			}
			boolean flag = true;
			for (int i = 0; i < name.length(); i++) {
				if (!check.contains(name.charAt(i) + "")) {
					conn.SendMessageDialog("Username contains only a-z and 1-9");
					flag = false;
					break;
				}
			}
			if (!flag) break;
			for (int i = 0; i < pass.length(); i++) {
				if (!check.contains(pass.charAt(i) + "")) {
					conn.SendMessageDialog("Password contains only a-z and 1-9");
					flag = false;
					break;
				}
			}
			if (!flag) break;

			ResultSet resultSet = CaroServer.sql.statement
					.executeQuery("SELECT * FROM `user` WHERE (`username`LIKE'" + name + "');");
			if (resultSet != null && resultSet.first()) {
				conn.SendMessageDialog("Username already used");
			} else {
				String sql = "INSERT INTO `user`(`id`, `username`, `password`) VALUES (0,'" + name + "','" + pass + "')";
				try {
					int int1 = CaroServer.sql.executeSQLUpdate(sql);
					if (int1 == 0) {
						System.out.print("Cant insert");
					}
				} catch (Exception e) {
					System.out.print("Cant insert");
					e.printStackTrace();
				}
				conn.SendMessageDialog("Register success");
			}
			break;
		case Cmd_Client2Server.LOG_OUT:
		case Cmd_Client2Server.PIECE:
		case Cmd_Client2Server.CREATE_ROOM:
		case Cmd_Client2Server.JOIN_ROOM:
		case Cmd_Client2Server.UPDATE_LIST_ROOM:
		case Cmd_Client2Server.LEAVE_ROOM:
		case Cmd_Client2Server.CHAT_ROOM:
		case Cmd_Client2Server.READY:
			if (player == null) {
				conn.SendMessageDialog("An error occurred");
			} else {
				processPlayerMessage(player, m);
			}
			break;
		default:
			conn.SendMessageDialog("empty");
			break;
		}
	}

	public void processPlayerMessage(Player player, Message m) throws Exception {
		switch (m.command) {
		case Cmd_Client2Server.LOG_OUT:
			PlayerManager.gI().remove(player);
			player.LogOutOk();
			System.out.println("Player " + player.username + " log out");
			break;
		case Cmd_Client2Server.PIECE:
			Room room = RoomManager.gI().GetRoomWithPlayer(player);
			if (room == null) {
				break;
			}
			if (!room.isFight) {
				player.SendMessageDialog("The match not started yet");
				break;
			}
			if (player != room.turnPlayer) {
				player.SendMessageDialog("It's not your turn yet");
				break;
			}

			int xPiece = m.reader().readInt();
			int yPiece = m.reader().readInt();
			int checkSetPiece = 0;
			if (player == room.players[0]) {
				checkSetPiece = room.board.setPiece(xPiece, yPiece, room.isX[0]);
			} else if (player == room.players[1]) {
				checkSetPiece = room.board.setPiece(xPiece, yPiece, room.isX[1]);
			}
			if (checkSetPiece != 0) {
				room.SendBoard();
				room.waitPlayer = player;
				if (player != room.players[0]) {
					room.turnPlayer = room.players[0];
				} else {
					room.turnPlayer = room.players[1];
				}
				if (checkSetPiece == 2) {
					room.XWin();
					room.finishMatch();
				} else if (checkSetPiece == 3) {
					room.YWin();
					room.finishMatch();
				}
			}

			break;
		case Cmd_Client2Server.CREATE_ROOM:
			Room room2 = RoomManager.gI().GetRoomWithPlayer(player);
			if (room2 != null) {
				player.SendMessageDialog("You are in another room");
				break;
			}
			room2 = new Room(Room.baseId);
			RoomManager.gI().add(room2);
			room2.players[0] = player;
			player.JoinRoomOk(room2.RoomNumber);
			break;
		case Cmd_Client2Server.JOIN_ROOM:
			Room room3 = RoomManager.gI().GetRoomWithPlayer(player);
			if (room3 != null) {
				player.SendMessageDialog("You are in another room");
				break;
			}
			room3 = RoomManager.gI().GetRoomWithNum(m.reader().readInt());
			if (room3 == null) {
				player.SendMessageDialog("Not found room selected, please update list room");
				break;
			}
			if (!room3.addPlayer(player)) {
				player.SendMessageDialog("Room is full");
				break;
			}
			player.JoinRoomOk(room3.RoomNumber);
			break;
		case Cmd_Client2Server.UPDATE_LIST_ROOM:
			player.SendListRoom();
			break;
		case Cmd_Client2Server.LEAVE_ROOM:
			Room room5 = RoomManager.gI().GetRoomWithPlayer(player);
			if (room5 == null) {
				break;
			}
			room5.removePlayer(player);
			for (int i = 0; i < room5.players.length; i++) {
				if (room5.players[i] != null) {
					room5.players[i].SendMessageDialog("Player '" + player.username + "' leaves room");
				}
			}
			if (room5.sizeOfPlayers() == 0) {
				RoomManager.gI().remove(room5);
			}
			player.LeaveRoomOK();
			if (room5.isFight) {
				room5.finishMatch();
			}
			break;
		case Cmd_Client2Server.CHAT_ROOM:
			Room room6 = RoomManager.gI().GetRoomWithPlayer(player);
			if (room6 == null) {
				break;
			}
			String content = m.reader().readUTF();
			content = content.trim();
			if (!content.equals("")) {
				room6.SendChat(player.username + ": " + content);
			}
			break;
		case Cmd_Client2Server.READY:
			Room room7 = RoomManager.gI().GetRoomWithPlayer(player);
			if (room7 == null) {
				break;
			}
			if (room7.isFight) {
				player.SendMessageDialog("The match has started, can't ready");
				break;
			}
			if (player == room7.turnPlayer || player == room7.waitPlayer) {
				player.SendMessageDialog("You have readied");
				break;
			}
			if (room7.sizeOfPlayers() < 2) {
				player.SendMessageDialog("Please wait more player join room");
				break;
			}
			room7.countReady++;
			if (room7.countReady == 1) {
				if (player == room7.players[0]) {
					room7.isX[0] = true;
				} else if (player == room7.players[1]) {
					room7.isX[1] = true;
				}
				room7.turnPlayer = player;
				room7.SendChat("Server: player " + player.username + " are ready");
			} else if (room7.countReady == 2) {
				room7.board = new Board();
				room7.SendChat("Server: player " + player.username + " are ready");
				room7.isFight = true;
				room7.SendChat("Server: Match start");
				room7.waitPlayer = player;
			}
			break;
		}
	}
}

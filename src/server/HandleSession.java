package server;

import java.sql.ResultSet;

import caro.Board;
import caro.Player;
import caro.Room;
import io.Cmd_Client2Server;
import io.Message;
import io.Session;

public class HandleSession {
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
			ResultSet red = CaroServer.sql.st.executeQuery("SELECT * FROM `user` WHERE (`username`LIKE'" + username
					+ "' AND `password`LIKE'" + password + "');");
			if (red != null && red.first()) {
				player = PlayerManager.gI().get(username);
				if (player != null) {
					conn.SendMessageDialog("Your account is login in other device, please try again");
				} else {
					conn.username = username;
					player = new Player(conn);
					PlayerManager.gI().put(player);
					player.LoginOK();
				}
			} else {
				conn.SendMessageDialog("Username or password incorrect");
			}
			break;
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
		}
	}

	public void processPlayerMessage(Player player, Message m) throws Exception {
		switch (m.command) {
		case Cmd_Client2Server.PIECE:
			Room room = RoomManager.gI().GetRoomWithPlayer(player);
			if (room == null) {
				break;
			}
			if (!room.isFight) {
				player.SendMessageDialog("The match not started yet");
				break;
			}
			if (player != room.currentPlayer) {
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
					room.currentPlayer = room.players[0];
				} else {
					room.currentPlayer = room.players[1];
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
					room5.players[i].SendMessageDialog("Player '" + player.username + "' leaves the room");
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
			if (player == room7.currentPlayer || player == room7.waitPlayer) {
				player.SendMessageDialog("You are ready");
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
				room7.currentPlayer = player;
				room7.SendChat("Server: player " + player.username + " is ready");
			} else if (room7.countReady == 2) {
				room7.board = new Board();
				room7.SendChat("Server: player " + player.username + " is ready");
				room7.isFight = true;
				room7.SendChat("Server: Match start");
				room7.waitPlayer = player;
			}
			break;
		}
	}
}

package caro;

import java.io.IOException;
import java.util.*;

import io.Cmd_Client2Server;
import io.Cmd_Server2Client;
import io.Message;
import io.Session;
import server.PlayerManager;
import server.RoomManager;

public class Player extends Cmd_Client2Server {
	public Session conn;
	public String username;
	public int id;

	public Player(Session session) {
		conn = session;
		username = session.username;
		id = session.id;
	}

	public void loginSuccess() {
		Message m = new Message(Cmd_Server2Client.LOGIN);
		conn.sendMessage(m);
	}

	public void logOutSuccess() {
		Message m = new Message(Cmd_Server2Client.LOG_OUT_SUCCESS);
		conn.sendMessage(m);
	}

	public void sendMessageDialog(String mes) {
		conn.sendMessageDialog(mes);
	}

	public void joinRoomSuccess(int num) throws IOException {
		Message m = new Message(Cmd_Server2Client.JOIN_ROOM_SUCCESS);
		m.writer().writeInt(num);
		conn.sendMessage(m);
	}

	public void sendListRoom() throws IOException {
		Message m = new Message(Cmd_Server2Client.SEND_LIST_ROOM);
		ArrayList<Room> rooms = RoomManager.gI().toList();
		m.writer().writeInt(rooms.size());
		for (Room room : rooms) {
			m.writer().writeInt(room.roomNumber);
			m.writer().writeInt(room.size());
			m.writer().writeBoolean(room.isFight);
		}
		conn.sendMessage(m);
	}

	public void leaveRoomSuccess() {
		Message m = new Message(Cmd_Server2Client.LEAVE_ROOM_SUCCESS);
		conn.sendMessage(m);
	}

	public void processMessage(Message m) throws Exception {
		Room room = null;
		FightPlayer fightPlayer = null;
		switch (m.command) {
		case LOG_OUT:
			PlayerManager.gI().remove(this);
			logOutSuccess();
			System.out.println("Player " + username + " log out");
			break;
		case CREATE_ROOM:
			fightPlayer = Room.cHashMapPlayerFight.get(id);
			if (fightPlayer != null) {
				sendMessageDialog("You are in another room");
				break;
			}
			room = new Room(Room.baseId);
			RoomManager.gI().add(room);
			room.addFightPlayer(this);
			joinRoomSuccess(room.roomNumber);
			break;
		case JOIN_ROOM:
			fightPlayer = Room.cHashMapPlayerFight.get(id);
			if (fightPlayer != null) {
				sendMessageDialog("You are in another room");
				break;
			}
			room = RoomManager.gI().get(m.reader().readInt());
			if (room == null) {
				sendMessageDialog("Room not found, please update list room");
				break;
			}
			if (!room.addFightPlayer(this)) {
				sendMessageDialog("Room full");
				break;
			}
			joinRoomSuccess(room.roomNumber);
			break;
		case UPDATE_LIST_ROOM:
			sendListRoom();
			break;
		case PIECE:
		case LEAVE_ROOM:
		case CHAT_ROOM:
		case READY:
			fightPlayer = Room.cHashMapPlayerFight.get(id);
			if (fightPlayer != null) {
				room = RoomManager.gI().get(fightPlayer.roomNumber);
				if (room != null) {
					room.processMessage(fightPlayer, m);
				} else {
					System.out.println("Room null");
					sendMessageDialog("An error occurred");
				}
			} else {
				System.out.println("Player fight null");
				sendMessageDialog("An error occurred");
			}
			break;
		}
	}

	public void disconnect() {
		FightPlayer fightPlayer = Room.cHashMapPlayerFight.get(id);
		if (fightPlayer != null) {
			Room room = RoomManager.gI().get(fightPlayer.roomNumber);
			if (room != null) {
				room.removeFightPlayer(fightPlayer);
				if (room.size() == 0) {
					RoomManager.gI().remove(room.roomNumber);
					room = null;
				} else {
					room.finishMatch();
				}
				fightPlayer = null;
			}
		}
		PlayerManager.gI().remove(this);
	}

	public static String checkRegister(String name, String pass, String repass) {
		if (name.length() < 6 || name.length() > 13) {
			return "Username is 6-12 in length";
		}
		if (pass.length() < 6 || pass.length() > 13) {
			return "Password is 6-12 in length";
		}
		if (!pass.equals(repass)) {
			return "Your password and confirmation password do not match";
		}
		String check = "abcdefghijklmnopqrstuvwxyz1234567890";
		for (int i = 0; i < name.length(); i++) {
			if (!check.contains(String.valueOf(name.charAt(i)))) {
				return "Username contains only a-z and 1-9";
			}
		}
		for (int i = 0; i < pass.length(); i++) {
			if (!check.contains(String.valueOf(pass.charAt(i)))) {
				return "Password contains only a-z and 1-9";
			}
		}
		return null;
	}
}

package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import caro.Board;
import caro.FightPlayer;
import caro.Room;
import io.Cmd_Server2Client;
import io.Message;
import io.Session;

public class ServiceSession extends Cmd_Server2Client {
	public Session session;

	public ServiceSession(Session s) {
		session = s;
	}

	private void sendMessage(Message m) {
		session.sendMessage(m);
	}

	public void sendMessageDialog(String mes) {
		Message m = new Message(SHOW_MESSAGE_DIALOG);
		try {
			m.writer().writeUTF(mes);
		} catch (IOException e) {
		}
		sendMessage(m);
	}

	public void loginSuccess() {
		Message m = new Message(LOGIN);
		sendMessage(m);
	}

	public void logOutSuccess() {
		Message m = new Message(LOG_OUT_SUCCESS);
		sendMessage(m);
	}

	public void joinRoomSuccess(int num) {
		try {
			Message m = new Message(JOIN_ROOM_SUCCESS);
			m.writer().writeInt(num);
			sendMessage(m);
		} catch (Exception e) {
		}
	}

	public void sendListRoom() {
		try {
			Message m = new Message(SEND_LIST_ROOM);
			ArrayList<Room> rooms = RoomManager.gI().toArrayList();
			m.writer().writeInt(rooms.size());
			for (Room room : rooms) {
				m.writer().writeInt(room.roomNumber);
				m.writer().writeInt(room.size());
				m.writer().writeBoolean(room.isFight);
			}
			sendMessage(m);
		} catch (Exception e) {
		}
	}

	public void leaveRoomSuccess() {
		Message m = new Message(LEAVE_ROOM_SUCCESS);
		sendMessage(m);
	}

	public void sendBoard(Board board) {
		try {
			Message m = new Message(SEND_BOARD);
			for (int i = 0; i < 20; i++) {
				for (int j = 0; j < 20; j++) {
					m.writer().writeShort(board.matrix[i][j]);
				}
			}
			m.writer().writeInt(board.flagPiece.x);
			m.writer().writeInt(board.flagPiece.y);
			sendMessage(m);
		} catch (Exception e) {
		}
	}

	public void sendChatRoom(String content) {
		try {
			Message m = new Message(CHAT_ROOM);
			m.writer().writeUTF(content);
			sendMessage(m);
		} catch (Exception e) {
		}
	}

	public void sendResetBoard() {
		Message m = new Message(RESET_BOARD);
		sendMessage(m);
	}

	public void senListPlayerInRoom(Vector<FightPlayer> players, int type) {
		Message message = new Message(LIST_PLAYER_ROOM);
		try {
			message.writer().writeByte(type);
			message.writer().writeInt(players.size());
			for (FightPlayer player : players) {
				message.writer().writeUTF(player.player.username);
			}
		} catch (IOException e) {
		}
		sendMessage(message);
	}
}

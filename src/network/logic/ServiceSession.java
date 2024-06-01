package network.logic;

import static network.io.Cmd_Server2Client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import caro.Board;
import caro.Player;
import caro.Room;
import network.Session;
import network.io.Message;
import server.RoomManager;

public class ServiceSession implements Service {
	public Session session;

	public ServiceSession(Session s) {
		session = s;
	}

	private void sendMessage(Message m) {
		session.sendMessage(m);
	}

	@Override
	public void sendMessageDialog(String mes) {
		Message m = new Message(SHOW_MESSAGE_DIALOG);
		try {
			m.writer().writeUTF(mes);
		} catch (IOException e) {
		}
		sendMessage(m);
	}

	@Override
	public void loginSuccess() {
		Message m = new Message(LOGIN);
		sendMessage(m);
	}

	@Override
	public void logOutSuccess() {
		Message m = new Message(LOG_OUT_SUCCESS);
		sendMessage(m);
	}

	@Override
	public void joinRoomSuccess(int num) {
		try {
			Message m = new Message(JOIN_ROOM_SUCCESS);
			m.writer().writeInt(num);
			sendMessage(m);
		} catch (Exception e) {
		}
	}

	@Override
	public void sendListRoom() {
		try {
			Message m = new Message(SEND_LIST_ROOM);
			ArrayList<Room> rooms = RoomManager.gI().toArrayList();
			m.writer().writeInt(rooms.size());
			for (Room room : rooms) {
				m.writer().writeInt(room.roomNumber);
				m.writer().writeInt(room.players.size());
				m.writer().writeBoolean(room.isStarted);
			}
			sendMessage(m);
		} catch (Exception e) {
		}
	}

	@Override
	public void leaveRoomSuccess() {
		Message m = new Message(LEAVE_ROOM_SUCCESS);
		sendMessage(m);
	}

	@Override
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

	@Override
	public void sendChatRoom(String content) {
		try {
			Message m = new Message(CHAT_ROOM);
			m.writer().writeUTF(content);
			sendMessage(m);
		} catch (Exception e) {
		}
	}

	@Override
	public void sendResetBoard() {
		Message m = new Message(RESET_BOARD);
		sendMessage(m);
	}

	@Override
	public void senListPlayerInRoom(Set<Player> players, int type) {
		Message message = new Message(LIST_PLAYER_ROOM);
		try {
			message.writer().writeByte(type);
			message.writer().writeInt(players.size());
			for (Player player : players) {
				if (type == 0) {
					message.writer()
							.writeUTF(player.username + " - " + (player.isReady ? "Đã sẵn sàng" : "Chưa sẵn sàng"));
				} else {
					message.writer().writeUTF(player.username);
				}
			}
		} catch (IOException e) {
		}
		sendMessage(message);
	}

	@Override
	public void sendInfoRoom(String info) {
		Message message = new Message(INFO_IN_ROOM);
		try {
			message.writer().writeUTF(info);
		} catch (IOException e) {
		}
		sendMessage(message);
	}
}

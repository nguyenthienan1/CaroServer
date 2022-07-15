package caro;

import java.io.IOException;

import io.Cmd_Server2Client;
import io.Message;
import io.Session;
import server.RoomManager;

public class Player {
	public Session conn;
	public String username;

	public Player(Session session) {
		conn = session;
		username = session.username;
	}

	public void LoginOK() {
		Message m = new Message(Cmd_Server2Client.LOGIN);
		conn.sendMessage(m);
	}

	public void SendMessageDialog(String mes) {
		conn.SendMessageDialog(mes);
	}

	public void JoinRoomOk(int num) throws IOException {
		Message m = new Message(Cmd_Server2Client.JOIN_ROOM_OK);
		m.writer().writeInt(num);
		conn.sendMessage(m);
		m.cleanup();
	}

	public void SendListRoom() throws IOException {
		Message m = new Message(Cmd_Server2Client.SEND_LIST_ROOM);
		m.writer().writeInt(RoomManager.gI().size());
		for (int i = 0; i < RoomManager.gI().size(); i++) {
			Room r = RoomManager.gI().get(i);
			m.writer().writeInt(r.RoomNumber);
			m.writer().writeInt(r.sizeOfPlayers());
		}
		conn.sendMessage(m);
		m.cleanup();
	}

	public void LeaveRoomOK() {
		Message m = new Message(Cmd_Server2Client.LEAVE_ROOM_OK);
		conn.sendMessage(m);
		m.cleanup();
	}
}

package caro;

import network.Session;
import network.logic.Service;
import server.PlayerManager;
import server.RoomManager;

public class HumanPlayer extends Player {

	public Session session;

	public HumanPlayer(Session session) {
		this.session = session;
		username = session.username;
		id = session.id;
	}

	public Service getService() {
		return session.getService();
	}

	public void disconnect() {
		if (room != null) {
			if (!isSpecPlayer() && room.isStarted) {
				room.finishMatch();
			}

			room.removePlayer(this);
			room.sendListPlayerBC();
			if (room.players.size() == 0 && room.spectatingPlayers.size() == 0) {
				RoomManager.gI().remove(room.roomNumber);
			} else {
				room.sendChatBC(username + " đã rời phòng do mất kết nối");
			}

			this.room = null;
		}
		PlayerManager.gI().remove(this);
	}
}

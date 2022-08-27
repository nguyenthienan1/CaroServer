package caro;

import io.Cmd_Client2Server;
import io.Message;
import io.Session;
import server.PlayerManager;
import server.RoomManager;
import server.ServiceSession;

public class Player extends Cmd_Client2Server {
	public Session session;
	public String username;
	public int id;

	public Player(Session session) {
		this.session = session;
		username = session.username;
		id = session.id;
	}

	private ServiceSession getServiceSession() {
		return session.getService();
	}

	public void processMessage(Message m) {
		switch (m.command) {
		case LOG_OUT:
			logOut();
			break;
		case CREATE_ROOM:
			createRoom(m);
			break;
		case JOIN_ROOM:
			joinRoom(m);
			break;
		case UPDATE_LIST_ROOM:
			getServiceSession().sendListRoom();
			break;
		case PIECE:
		case LEAVE_ROOM:
		case CHAT_ROOM:
		case READY:
			FightPlayer fightPlayer = Room.cHashMapPlayerFight.get(id);
			if (fightPlayer != null) {
				fightPlayer.processMessage(m);
			} else {
				System.out.println("Player fight null");
				getServiceSession().sendMessageDialog("An error occurred");
			}
			break;
		}
	}

	private void logOut() {
		PlayerManager.gI().remove(this);
		getServiceSession().logOutSuccess();
		System.out.println("Player " + username + " log out");
	}

	private void createRoom(Message m) {
		try {
			FightPlayer fightPlayer = Room.cHashMapPlayerFight.get(id);
			if (fightPlayer != null) {
				getServiceSession().sendMessageDialog("You are in another room");
				return;
			}
			Room room = new Room(Room.baseId);
			RoomManager.gI().add(room);
			room.addFightPlayer(this);
			getServiceSession().joinRoomSuccess(room.roomNumber);
		} catch (Exception e) {
		}
	}

	private void joinRoom(Message m) {
		try {
			FightPlayer fightPlayer = Room.cHashMapPlayerFight.get(id);
			if (fightPlayer != null) {
				getServiceSession().sendMessageDialog("You are in another room");
				return;
			}
			Room room = RoomManager.gI().get(m.reader().readInt());
			if (room == null) {
				getServiceSession().sendMessageDialog("Room not found, please update list room");
				return;
			}
			if (!room.addFightPlayer(this)) {
				getServiceSession().sendMessageDialog("Room full");
				return;
			}
			getServiceSession().joinRoomSuccess(room.roomNumber);
		} catch (Exception e) {
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
}

package server;

import java.util.Vector;

import caro.Player;
import caro.Room;

public class RoomManager {
	private Vector<Room> Rooms = new Vector<>();
	public static RoomManager instance;

	public static RoomManager gI() {
		if (instance == null) {
			instance = new RoomManager();
		}
		return instance;
	}

	public int size() {
		return Rooms.size();
	}

	public Room get(int at) {
		return Rooms.get(at);
	}

	public void remove(Room room) {
		Rooms.remove(room);
	}

	public void remove(int at) {
		Rooms.remove(at);
	}

	public void add(Room room) {
		Rooms.add(room);
	}

	public boolean contains(Room room) {
		return Rooms.contains(room);
	}

	public Room GetRoom(Player player) {
		for (int i = 0; i < size(); i++) {
			Room r = get(i);
			if (r.players[0] == player) {
				return r;
			} else if (r.players[1] == player) {
				return r;
			}
		}
		return null;
	}

	public Room GetRoom(int roomNum) {
		for (int i = 0; i < size(); i++) {
			Room r = get(i);
			if (r.RoomNumber == roomNum) {
				return r;
			}
		}
		return null;
	}
}

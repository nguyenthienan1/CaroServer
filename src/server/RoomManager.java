package server;

import java.util.Vector;

import caro.FightPlayer;
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
		for (Room room : Rooms) {
			for (FightPlayer fightPlayer : room.vecFightPlayers) {
				if (fightPlayer.player == player) {
					return room;
				}
			}
		}
		return null;
	}

	public Room GetRoom(int roomNum) {
		for (Room room : Rooms) {
			if (room.RoomNumber == roomNum) {
				return room;
			}
		}
		return null;
	}
}

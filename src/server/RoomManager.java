package server;

import java.util.*;
import java.util.concurrent.*;

import caro.Room;

public class RoomManager {
	private ConcurrentHashMap<Integer, Room> cHashMapRoom = new ConcurrentHashMap<Integer, Room>();
	public static RoomManager instance;

	public static RoomManager gI() {
		if (instance == null) {
			instance = new RoomManager();
		}
		return instance;
	}

	public int size() {
		return cHashMapRoom.size();
	}

	public Room get(int room_number) {
		return cHashMapRoom.get(room_number);
	}

	public void remove(int room_number) {
		cHashMapRoom.remove(room_number);
	}

	public void add(Room room) {
		cHashMapRoom.put(room.roomNumber, room);
	}

	public ArrayList<Room> toList() {
		return new ArrayList<Room>(cHashMapRoom.values());
	}
}

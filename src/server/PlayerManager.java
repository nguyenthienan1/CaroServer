package server;

import java.util.concurrent.*;

import caro.HumanPlayer;
import caro.Player;

public class PlayerManager {
	private static PlayerManager instance;
	private ConcurrentHashMap<Integer, HumanPlayer> players = new ConcurrentHashMap<Integer, HumanPlayer>();

	public static PlayerManager gI() {
		if (instance == null) {
			instance = new PlayerManager();
		}
		return instance;
	}

	public void put(HumanPlayer p) {
		players.put(p.id, p);
		// System.out.println("Add player " + p.username);
	}
	
	public void remove(int id) {
		players.remove(id);
		// System.out.println("Remove player " + p.username);
	}

	public void remove(HumanPlayer p) {
		players.remove(p.id);
		// System.out.println("Remove player " + p.username);
	}

	public HumanPlayer get(int id) {
		return players.get(id);
	}

	public int size() {
		return players.size();
	}

	public void show() {
		System.out.print("\tid\tname");
		System.out.println("\n----------------------------------");
		players.forEach((name, player) -> {
			System.out.print("\t" + player.id + "\t" + name);
			System.out.println();
		});
	}
	
	public void sendListRoomBC() {
		for (Player player : players.values()) {
			player.getService().sendListRoom();
		}
	}
}

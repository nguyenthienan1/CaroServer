package server;

import java.util.concurrent.*;

import caro.Player;

public class PlayerManager {
	private static PlayerManager instance;
	private ConcurrentHashMap<Integer, Player> cHashMapPlayer = new ConcurrentHashMap<Integer, Player>();

	public static PlayerManager gI() {
		if (instance == null) {
			instance = new PlayerManager();
		}
		return instance;
	}

	public void put(Player p) {
		cHashMapPlayer.put(p.id, p);
		// System.out.println("Add player " + p.username);
	}
	
	public void remove(int id) {
		cHashMapPlayer.remove(id);
		// System.out.println("Remove player " + p.username);
	}

	public void remove(Player p) {
		cHashMapPlayer.remove(p.id);
		// System.out.println("Remove player " + p.username);
	}

	public Player get(int id) {
		return cHashMapPlayer.get(id);
	}

	public int size() {
		return cHashMapPlayer.size();
	}

	public void show() {
		System.out.print("\tid\tname");
		System.out.println("\n----------------------------------");
		cHashMapPlayer.forEach((name, player) -> {
			System.out.print("\t" + player.id + "\t" + name);
			System.out.println();
		});
	}
}

package server;

import java.util.HashMap;

import caro.Player;

public class PlayerManager {
	private static PlayerManager instance;
	private HashMap<String, Player> playerHashMapName = new HashMap<>();

	public static PlayerManager gI() {
		if (instance == null) {
			instance = new PlayerManager();
		}
		return instance;
	}

	public void put(Player p) {
		playerHashMapName.put(p.username, p);
		// System.out.println("Add player " + p.username);
	}

	public void remove(Player p) {
		playerHashMapName.remove(p.username);
		// System.out.println("Remove player " + p.username);
	}

	public void remove(String username) {
		playerHashMapName.remove(username);
		// System.out.println("Remove player " + username);
	}

	public Player get(String username) {
		return playerHashMapName.get(username);
	}

	public int size() {
		return playerHashMapName.size();
	}
}

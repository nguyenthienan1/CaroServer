package server;

import java.util.HashMap;

import caro.Player;

public class PlayerManager {
	private static PlayerManager instance;
	private HashMap<String, Player> playerHashMap = new HashMap<>();

	public static PlayerManager gI() {
		if (instance == null) {
			instance = new PlayerManager();
		}
		return instance;
	}

	public void put(Player p) {
		playerHashMap.put(p.username, p);
		System.out.println("Add player " + p.username + ", total player: " + size());
	}

	public void remove(Player p) {
		playerHashMap.remove(p.username);
	}

	public void remove(String username) {
		playerHashMap.remove(username);
	}

	public Player get(String username) {
		return playerHashMap.get(username);
	}

	public int size() {
		return playerHashMap.size();
	}
}

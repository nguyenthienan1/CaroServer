package caro;

import static caro.Board.*;

import network.logic.Service;

public abstract class Player {
	public String username;
	public int id;
	public Room room;
	public boolean isReady;
	public boolean isTurn;
	public int symbol;

	public Player() {
		symbol = isTurn ? X : O;
	}

	public Service getService() {
		return null;
	}

	/***
	 * kiểm tra player hiện tại có phải là player đang xem
	 * 
	 * @return
	 */
	public boolean isSpecPlayer() {
		boolean isSpecPlayer = room.spectatingPlayers.contains(this);
		if (isSpecPlayer) {
			return true;
		}
		return false;
	}
}

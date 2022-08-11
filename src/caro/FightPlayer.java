package caro;

public class FightPlayer {
	public Player player;
	public int roomNumber;
	public boolean isReady;
	public boolean isX;
	public boolean isTurn;

	public FightPlayer(Player p, int roomId) {
		player = p;
		isReady = false;
		roomNumber = roomId;
	}

	public void sendMessageDialog(String mes) {
		player.sendMessageDialog(mes);
	}
}

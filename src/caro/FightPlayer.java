package caro;

public class FightPlayer {
	public Player player;
	public boolean isReady;
	public boolean isX;

	public FightPlayer(Player p) {
		player = p;
		isReady = false;
	}

	public void sendMessageDialog(String mes) {
		player.sendMessageDialog(mes);
	}
}

package caro;

import java.io.IOException;

import io.Cmd_Server2Client;
import io.Message;

public class Room {
	public static int baseId;
	public int RoomNumber;
	public Player[] players = new Player[2];
	public Player turnPlayer = null;
	public Player waitPlayer = null;
	public Board board = new Board();
	public int countReady;
	public boolean[] isX = new boolean[2];
	public boolean isFight;

	public Room(int roomNum) {
		baseId++;
		RoomNumber = roomNum;
	}

	public boolean addPlayer(Player player) {
		for (int i = 0; i < players.length; i++) {
			if (players[i] == null) {
				players[i] = player;
				return true;
			}
		}
		return false;
	}

	public void removePlayer(Player player) {
		for (int i = 0; i < players.length; i++) {
			if (players[i] == player) {
				players[i] = null;
			}
		}
	}

	public int sizeOfPlayers() {
		int count = 0;
		for (int i = 0; i < players.length; i++) {
			if (players[i] != null) {
				count++;
			}
		}
		return count;
	}

	public void finishMatch() {
		countReady = 0;
		isX[0] = false;
		isX[1] = false;
		isFight = false;
		turnPlayer = null;
		waitPlayer = null;
	}

	public void XWin() {
		if (isX[0]) {
			players[0].SendMessageDialog("You win");
			players[1].SendMessageDialog("You lose, player " + players[0].username + " won");
		} else {
			players[1].SendMessageDialog("You win");
			players[0].SendMessageDialog("You lose, player " + players[1].username + " won");
		}
	}

	public void YWin() {
		if (!isX[0]) {
			players[0].SendMessageDialog("You win");
			players[1].SendMessageDialog("You lose, player " + players[0].username + " won");
		} else {
			players[1].SendMessageDialog("You win");
			players[0].SendMessageDialog("You lose, player " + players[1].username + " won");
		}
	}

	public void SendBoard() throws IOException {
		Message m = new Message(Cmd_Server2Client.SEND_BOARD);
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 20; j++) {
				m.writer().writeShort(board.matrix[i][j]);
			}
		}
		m.writer().writeInt(board.flagPiece.x);
		m.writer().writeInt(board.flagPiece.y);
		for (int i = 0; i < players.length; i++) {
			if (players[i] != null) {
				players[i].conn.sendMessage(m);
			}
		}
		m.cleanup();
	}

	public void SendChat(String content) throws IOException {
		Message m = new Message(Cmd_Server2Client.CHAT_ROOM);
		m.writer().writeUTF(content);
		for (int i = 0; i < players.length; i++) {
			if (players[i] != null) {
				players[i].conn.sendMessage(m);
			}
		}
		m.cleanup();
	}
}

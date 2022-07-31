package caro;

import java.io.IOException;
import java.util.Vector;

import io.Cmd_Client2Server;
import io.Cmd_Server2Client;
import io.Message;
import server.RoomManager;

public class Room extends Cmd_Client2Server {
	public static int baseId;
	public int RoomNumber;
	public Vector<FightPlayer> vecFightPlayers = new Vector<FightPlayer>();
	public FightPlayer currentPlayer = null;
	public FightPlayer waitPlayer = null;
	public Board board = new Board();
	public int countReady;
	public boolean isFight;

	public Room(int roomNum) {
		baseId++;
		RoomNumber = roomNum;
	}

	public boolean addFightPlayer(Player player) {
		if (vecFightPlayers.size() >= 2) {
			return false;
		}
		FightPlayer fightPlayer = new FightPlayer(player);
		vecFightPlayers.add(fightPlayer);
		return true;
	}

	public void removeFightPlayer(FightPlayer fightPlayer) {
		vecFightPlayers.remove(fightPlayer);
	}

	public FightPlayer getFightPlayer(Player player) {
		for (FightPlayer fightPlayer : vecFightPlayers) {
			if (fightPlayer.player == player) {
				return fightPlayer;
			}
		}
		return null;
	}

	public int size() {
		return vecFightPlayers.size();
	}

	private void swapTurn(FightPlayer fightPlayer) {
		waitPlayer = fightPlayer;
		for (FightPlayer fightP : vecFightPlayers) {
			if (fightP != fightPlayer) {
				currentPlayer = fightP;
			}
		}
	}

	public void processMessage(FightPlayer fightPlayer, Message m) throws Exception {
		switch (m.command) {
		case PIECE:
			if (!isFight) {
				fightPlayer.sendMessageDialog("The match not started yet");
				break;
			}
			if (fightPlayer != currentPlayer) {
				fightPlayer.sendMessageDialog("It's not your turn yet");
				break;
			}

			int xPiece = m.reader().readInt();
			int yPiece = m.reader().readInt();

			int checkSetPiece = board.setPiece(xPiece, yPiece, fightPlayer.isX);
			if (checkSetPiece != 0) {
				sendBoard();
				swapTurn(fightPlayer);
				if (checkSetPiece == 2) {
					win(true);
				} else if (checkSetPiece == 3) {
					win(false);
				}
			}
			break;
		case LEAVE_ROOM:
			removeFightPlayer(fightPlayer);
			if (size() == 0) {
				RoomManager.gI().remove(this);
			}
			sendMessageDialog(fightPlayer.player.username + " leaved room");
			fightPlayer.player.leaveRoomSuccess();
			if (isFight) {
				finishMatch();
			}
			break;
		case CHAT_ROOM:
			String content = m.reader().readUTF();
			content = content.trim();
			if (!content.equals("")) {
				sendChat(fightPlayer.player.username + ": " + content);
			}
			break;
		case READY:
			if (isFight) {
				fightPlayer.sendMessageDialog("The match has started, can't ready");
				break;
			}
			if (fightPlayer == currentPlayer || fightPlayer == waitPlayer) {
				fightPlayer.sendMessageDialog("You have readied");
				break;
			}
			if (size() < 2) {
				fightPlayer.sendMessageDialog("Please wait more player join room");
				break;
			}
			countReady++;
			if (countReady == 1) {
				fightPlayer.isX = true;
				currentPlayer = fightPlayer;
				sendChat("Server: player " + fightPlayer.player.username + " are ready");
			} else if (countReady == 2) {
				waitPlayer = fightPlayer;
				board = new Board();
				sendChat("Server: player " + fightPlayer.player.username + " are ready");
				isFight = true;
				sendChat("Server: Match start");
			}
			break;
		}
	}

	public void finishMatch() {
		countReady = 0;
		isFight = false;
		currentPlayer = null;
		waitPlayer = null;
		board = new Board();
		for (FightPlayer fightPlayer : vecFightPlayers) {
			fightPlayer.isX = false;
		}
		resetBoard();
	}

	private void win(boolean isX) {
		Player pWin = null;
		Player pLose = null;
		for (FightPlayer fightPlayer : vecFightPlayers) {
			if (fightPlayer.isX == isX) {
				pWin = fightPlayer.player;
			} else {
				pLose = fightPlayer.player;
			}
		}
		if (pWin != null && pLose != null) {
			pWin.sendMessageDialog("You win");
			pLose.sendMessageDialog("You lose, player " + pWin.username + " won");
			finishMatch();
		}
	}

	private void sendBroadCast(Message m) {
		for (FightPlayer fightPlayer : vecFightPlayers) {
			fightPlayer.player.conn.sendMessage(m);
		}
	}

	private void sendBoard() throws IOException {
		Message m = new Message(Cmd_Server2Client.SEND_BOARD);
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 20; j++) {
				m.writer().writeShort(board.matrix[i][j]);
			}
		}
		m.writer().writeInt(board.flagPiece.x);
		m.writer().writeInt(board.flagPiece.y);
		sendBroadCast(m);
	}

	private void sendChat(String content) throws IOException {
		Message m = new Message(Cmd_Server2Client.CHAT_ROOM);
		m.writer().writeUTF(content);
		sendBroadCast(m);
	}

	private void sendMessageDialog(String content) {
		for (FightPlayer fightPlayer : vecFightPlayers) {
			fightPlayer.player.sendMessageDialog(content);
		}
	}

	public void resetBoard() {
		Message m = new Message(Cmd_Server2Client.RESET_BOARD);
		sendBroadCast(m);
	}

}

package caro;

import java.util.*;
import java.util.concurrent.*;

import io.Cmd_Client2Server;

public class Room extends Cmd_Client2Server {
	public static ConcurrentHashMap<Integer, FightPlayer> cHashMapPlayerFight = new ConcurrentHashMap<Integer, FightPlayer>();
	public static int baseId;
	public int roomNumber;
	public Vector<FightPlayer> vecFightPlayers = new Vector<FightPlayer>();
	public Board board = new Board();
	public int countReady;
	public boolean isFight;

	public Room(int room_number) {
		baseId++;
		roomNumber = room_number;
	}

	public boolean addFightPlayer(Player player) {
		if (size() >= 2) {
			return false;
		}
		FightPlayer fightPlayer = new FightPlayer(player, roomNumber);
		vecFightPlayers.add(fightPlayer);
		cHashMapPlayerFight.put(player.id, fightPlayer);
		return true;
	}

	public void removeFightPlayer(FightPlayer fightPlayer) {
		vecFightPlayers.remove(fightPlayer);
		cHashMapPlayerFight.remove(fightPlayer.player.id);
	}

	public int size() {
		return vecFightPlayers.size();
	}

	public void swapTurn() {
		for (FightPlayer fightP : vecFightPlayers) {
			fightP.isTurn = !fightP.isTurn;
		}
	}

	public void finishMatch() {
		countReady = 0;
		isFight = false;
		board = new Board();
		for (FightPlayer fightPlayer : vecFightPlayers) {
			fightPlayer.isX = false;
			fightPlayer.isTurn = false;
			fightPlayer.isReady = false;
		}
	}

	public void win(boolean isX) {
		FightPlayer pWin = null;
		FightPlayer pLose = null;
		for (FightPlayer fightPlayer : vecFightPlayers) {
			if (fightPlayer.isX == isX) {
				pWin = fightPlayer;
			} else {
				pLose = fightPlayer;
			}
		}
		if (pWin != null && pLose != null) {
			pWin.getServiceSession().sendMessageDialog("You win");
			pLose.getServiceSession().sendMessageDialog("You lose, player " + pWin.player.username + " won");
			finishMatch();
		}
	}

	//BC = BroadCast
	public void sendBoardBC() {
		for (FightPlayer fightPlayer : vecFightPlayers) {
			fightPlayer.getServiceSession().sendBoard(board);
		}
	}

	public void sendChatBC(String content) {
		for (FightPlayer fightPlayer : vecFightPlayers) {
			fightPlayer.getServiceSession().sendChatRoom(content);
		}
	}

	public void sendMsgDialogBC(String content) {
		for (FightPlayer fightPlayer : vecFightPlayers) {
			fightPlayer.getServiceSession().sendMessageDialog(content);
		}
	}

	public void sendResetBoardBC() {
		for (FightPlayer fightPlayer : vecFightPlayers) {
			fightPlayer.getServiceSession().sendResetBoard();
		}
	}

}

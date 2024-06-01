package caro;

import java.util.concurrent.*;

import network.io.Cmd_Client2Server;

import static caro.Board.*;

public class Room extends Cmd_Client2Server {
	public static int baseId;
	public int roomNumber;
	public CopyOnWriteArraySet<Player> players = new CopyOnWriteArraySet<>();
	public CopyOnWriteArraySet<Player> spectatingPlayers = new CopyOnWriteArraySet<>();
	public Board board = new Board();
	public int countReady;
	public boolean isStarted;

	public Room(int room_number) {
		baseId++;
		roomNumber = room_number;
	}

	public Player getPlayer(int id) {
		for (Player player : players) {
			if (player.id == id) {
				return player;
			}
		}
		return null;
	}

	public void addPlayer(Player player) {
		if (players.size() >= 2) {
			player.getService().sendMessageDialog("Phòng này đã đủ người chơi, bạn sẽ vào với tư cách người xem");
			spectatingPlayers.add(player);
			if (isStarted) {
				player.getService().sendBoard(board);
			}
			return;
		}
		players.add(player);
	}

	public void removePlayer(Player player) {
		players.remove(player);
		spectatingPlayers.remove(player);
	}

	public Player hasBotPlayer() {
		for (Player player : players) {
			if (player instanceof AIPlayer) {
				return player;
			}
		}
		return null;
	}

	public int countHumanPlayer() {
		int count = spectatingPlayers.size();
		for (Player player : players) {
			if (player instanceof HumanPlayer) {
				count++;
			}
		}
		return count;
	}

	public void playerReady(Player player) {
		countReady++;
		player.isReady = true;
		if (countReady == 1) {
			player.symbol = X;
			player.isTurn = true;
//			sendChatBC("Server: player " + player.username + " đã sẵn sàng");
		} else if (countReady == 2) {
			player.symbol = O;
			player.isTurn = false;
			board = new Board();
//			sendChatBC("Server: player " + player.username + " đã sẵn sàng");
			isStarted = true;
			sendChatBC("Trận đấu bắt đầu");
			sendResetBoardBC();
			sendInfoTurn();

			Player bot = hasBotPlayer();
			if (bot != null && bot.isTurn) {
				((AIPlayer) bot).move();
			}
		}
	}

	public void swapTurn() {
		for (Player fightP : players) {
			fightP.isTurn = !fightP.isTurn;
		}
	}

	/***
	 * Đặt cờ vào bàn và kiểm tra thắng thua
	 * @param xPiece
	 * @param yPiece
	 * @param player
	 * @return
	 */
	public int setPiece(int xPiece, int yPiece, Player player) {
		int checkSetPiece = board.setPiece(xPiece, yPiece, player.symbol);
		if (checkSetPiece != 0) {
			sendBoardBC();
			swapTurn();

			if (checkSetPiece == 1) {
				sendInfoTurn();
			} else if (checkSetPiece == 2) {
				win(X);
			} else if (checkSetPiece == 3) {
				win(O);
			} else if (checkSetPiece == 4) {
				draw();
			}
		}
		return checkSetPiece;
	}

	public void sendInfoTurn() {
		Player playerHasTurn = null;
		Player playerNotHasTurn = null;
		for (Player player : players) {
			if (player.isTurn) {
				playerHasTurn = player;
			} else {
				playerNotHasTurn = player;
			}
		}
		playerHasTurn.getService().sendInfoRoom("Đến lượt bạn");
		playerNotHasTurn.getService().sendInfoRoom("Đến lượt của " + playerHasTurn.username);

		for (Player player : spectatingPlayers) {
			player.getService().sendInfoRoom("Đến lượt của " + playerHasTurn.username);
		}
	}

	public void finishMatch() {
		countReady = 0;
		isStarted = false;
		board = new Board();
		for (Player fightPlayer : players) {
			fightPlayer.symbol = 0;
			fightPlayer.isTurn = false;
			fightPlayer.isReady = false;
		}
	}

	public void win(int player) {
		Player pWin = null;
		Player pLose = null;
		for (Player fightPlayer : players) {
			if (fightPlayer.symbol == player) {
				pWin = fightPlayer;
			} else {
				pLose = fightPlayer;
			}
		}
		if (pWin != null && pLose != null) {
			pWin.getService().sendMessageDialog("Bạn đã thắng, xin chúc mừng");
			pLose.getService().sendMessageDialog("Thua mất rồi, người chơi " + pWin.username + " đã chiến thắng");
			finishMatch();
		}
		sendInfoBC("Trận đấu kết thúc, " + pWin.username + " là người chiến thắng");
		sendListPlayerBC();
	}

	public void draw() {
		for (Player fightPlayer : players) {
			fightPlayer.getService().sendMessageDialog("Trận đấu hoà do bàn cờ đã đầy");
		}
		finishMatch();
		sendInfoBC("Trận đấu kết thúc");
		sendListPlayerBC();
	}

	// BC = BroadCast
	public void sendBoardBC() {
		for (Player fightPlayer : players) {
			fightPlayer.getService().sendBoard(board);
		}
		for (Player player : spectatingPlayers) {
			player.getService().sendBoard(board);
		}
	}

	public void sendChatBC(String content) {
		for (Player fightPlayer : players) {
			fightPlayer.getService().sendChatRoom(content);
		}
		for (Player player : spectatingPlayers) {
			player.getService().sendChatRoom(content);
		}
	}

	public void sendMsgDialogBC(String content) {
		for (Player fightPlayer : players) {
			fightPlayer.getService().sendMessageDialog(content);
		}
	}

	public void sendResetBoardBC() {
		for (Player fightPlayer : players) {
			fightPlayer.getService().sendResetBoard();
		}
		for (Player player : spectatingPlayers) {
			player.getService().sendResetBoard();
		}
	}

	public void sendListPlayerBC() {
		for (Player fightPlayer : players) {
			fightPlayer.getService().senListPlayerInRoom(players, 0);
			fightPlayer.getService().senListPlayerInRoom(spectatingPlayers, 1);
		}
		for (Player player : spectatingPlayers) {
			player.getService().senListPlayerInRoom(players, 0);
			player.getService().senListPlayerInRoom(spectatingPlayers, 1);
		}
	}

	public void sendInfoBC(String info) {
		for (Player fightPlayer : players) {
			fightPlayer.getService().sendInfoRoom(info);
		}
		for (Player player : spectatingPlayers) {
			player.getService().sendInfoRoom(info);
		}
	}

}

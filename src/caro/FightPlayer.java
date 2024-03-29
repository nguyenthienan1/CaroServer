package caro;

import io.Cmd_Client2Server;
import io.Message;
import server.RoomManager;
import server.ServiceSession;

public class FightPlayer extends Cmd_Client2Server {
	public Player player;
	public int roomNumber;
	public boolean isReady;
	public boolean isX;
	public boolean isTurn;

	public FightPlayer(Player p, int roomId) {
		player = p;
		roomNumber = roomId;
	}

	public ServiceSession getServiceSession() {
		return player.session.getService();
	}

	public void processMessage(Message m) {
		Room room = RoomManager.gI().get(roomNumber);
		if (room == null) {
			System.out.println("Room null");
			getServiceSession().sendMessageDialog("An error occurred");
			return;
		}
		switch (m.command) {
		case PIECE:
			setPiece(room, m);
			break;
		case LEAVE_ROOM:
			leaveRoom(room, m);
			break;
		case CHAT_ROOM:
			chatRoom(room, m);
			break;
		case READY:
			ready(room, m);
			break;
		default:
			break;
		}
	}

	private void setPiece(Room room, Message m) {
		try {
			if (!room.isFight) {
				getServiceSession().sendMessageDialog("The match not started yet");
				return;
			}
			if (!isTurn) {
				getServiceSession().sendMessageDialog("It's not your turn yet");
				return;
			}

			int xPiece = m.reader().readInt();
			int yPiece = m.reader().readInt();

			int checkSetPiece = room.board.setPiece(xPiece, yPiece, isX);
			if (checkSetPiece != 0) {
				room.sendBoardBC();
				room.swapTurn();
				if (checkSetPiece == 2) {
					room.win(true);
				} else if (checkSetPiece == 3) {
					room.win(false);
				}
			}
		} catch (Exception e) {
		}
	}

	private void leaveRoom(Room room, Message m) {
		try {
			room.removeFightPlayer(this);
			if (room.isFight) {
				room.finishMatch();
			}
			if (room.size() == 0) {
				RoomManager.gI().remove(room);
				room = null;
			} else {
				room.sendMsgDialogBC(player.username + " leaved room");
			}
			getServiceSession().leaveRoomSuccess();
		} catch (Exception e) {
		}
	}

	private void chatRoom(Room room, Message m) {
		try {
			String content = m.reader().readUTF();
			content = content.trim();
			if (!content.equals("")) {
				room.sendChatBC(player.username + ": " + content);
			}
		} catch (Exception e) {
		}
	}

	private void ready(Room room, Message m) {
		try {
			if (room.isFight) {
				getServiceSession().sendMessageDialog("The match has started, can't ready");
				return;
			}
			if (isReady) {
				getServiceSession().sendMessageDialog("You have readied");
				return;
			}
			if (room.size() < 2) {
				getServiceSession().sendMessageDialog("Please wait more player join room");
				return;
			}
			room.countReady++;
			isReady = true;
			if (room.countReady == 1) {
				isX = true;
				isTurn = true;
				room.sendChatBC("Server: player " + player.username + " are ready");
			} else if (room.countReady == 2) {
				isX = false;
				isTurn = false;
				room.board = new Board();
				room.sendChatBC("Server: player " + player.username + " are ready");
				room.isFight = true;
				room.sendChatBC("Server: Match start");
				room.sendResetBoardBC();
			}
		} catch (Exception e) {
		}
	}
}

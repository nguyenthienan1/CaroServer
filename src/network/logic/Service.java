package network.logic;

import java.util.Set;

import caro.Board;
import caro.Player;

public interface Service {
	public void sendMessageDialog(String mes);

	public void loginSuccess();

	public void logOutSuccess();

	public void joinRoomSuccess(int num);

	public void sendListRoom();

	public void leaveRoomSuccess();

	public void sendBoard(Board board);

	public void sendChatRoom(String content);

	public void sendResetBoard();

	public void senListPlayerInRoom(Set<Player> players, int type);

	public void sendInfoRoom(String info);
}

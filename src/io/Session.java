package io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import caro.Player;
import caro.Room;
import server.HandleSession;
import server.PlayerManager;
import server.RoomManager;

public class Session {
	public String username;
	public Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	public long connectTime;
	public int id;
	public boolean connected;
	private final BlockingQueue<Message> DataQueue = new ArrayBlockingQueue<>(64);

	public Session(Socket s) {
		connectTime = System.currentTimeMillis();
		try {
			if (s != null) {
				socket = s;
				dis = new DataInputStream(s.getInputStream());
				dos = new DataOutputStream(s.getOutputStream());
				connected = true;
			}
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
	}

	public void start() {
		sendMessageThread();
		receiveMessageThread();
	}

	public void sendMessage(Message m) {
		if (connected) {
			try {
				DataQueue.put(m);
			} catch (Exception ignored) {
			}
		}
	}

	private void sendMessageThread() {
		new Thread(() -> {
			Message m;
			while (connected) {
				try {
					m = DataQueue.poll(5, TimeUnit.SECONDS);
					if (m != null) {
						doSendMessage(m);
					}
				} catch (Exception e) {
					e.printStackTrace();
					disconnect();
					break;
				}
			}
			DataQueue.clear();
			dos = null;
			System.out.println("Finish send thread: " + socket.getRemoteSocketAddress());
		}).start();
	}

	private void receiveMessageThread() {
		new Thread(() -> {
			Message message;
			try {
				while (connected) {
					message = readMessage();
					HandleSession.gI().processSesionMessage(this, message);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			disconnect();
			dis = null;
			System.out.println("Finish receive thread: " + socket.getRemoteSocketAddress());
		}).start();
	}

	private void doSendMessage(Message m) throws IOException {
		dos.writeInt(m.command);
		byte[] data = m.getData();
		int size = data.length;
		dos.writeInt(size);
		if (size > 0) {
			dos.write(data);
		}
//		 System.out.println("Send message: command (" + m.command + ") size [" + size
//		 + "]");
	}

	private Message readMessage() throws IOException {
		int cmd = dis.readInt();
		int size = dis.readInt();
		byte[] data = new byte[size];
		if (size > 0) {
			dis.readFully(data);
		}
//		 System.out.println("Receive message: command (" + cmd + ") size [" + size +
//		 "]");
		return new Message(cmd, data);
	}

	public void SendMessageDialog(String mes) {
		Message m = new Message(Cmd_Server2Client.SHOW_MESSAGE_DIALOG);
		try {
			m.writer().writeUTF(mes);
		} catch (IOException e) {
		}
		sendMessage(m);
		m.cleanup();
	}

	public void disconnect() {
		if (connected) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			connected = false;
			Player p = PlayerManager.gI().get(username);
			if (p != null) {
				Room r = RoomManager.gI().GetRoomWithPlayer(p);
				if (r != null) {
					r.removePlayer(p);
					if (r.sizeOfPlayers() == 0) {
						RoomManager.gI().remove(r);
					} else {
						r.finishMatch();
					}
				}
				PlayerManager.gI().remove(p);
			}
		}
	}
}

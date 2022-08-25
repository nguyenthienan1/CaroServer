package io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

import caro.Player;
import server.HandleSession;
import server.PlayerManager;

public class Session {
	private HandleSession handleSession = new HandleSession();
	public String username;
	public Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private SendMessageThread sendMessageThread;
	private ReceiveMessageThread receiveMessageThread;
	public long connectTime;
	public int id;
	public boolean connected;
	private LinkedBlockingQueue<Message> DataQueue = new LinkedBlockingQueue<>();

	public Session(Socket s) {
		connectTime = System.currentTimeMillis();
		try {
			if (s != null) {
				socket = s;
				dis = new DataInputStream(s.getInputStream());
				dos = new DataOutputStream(s.getOutputStream());
				connected = true;
				sendMessageThread = new SendMessageThread();
				receiveMessageThread = new ReceiveMessageThread();
			}
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
	}

	public void start() {
		sendMessageThread.start();
		receiveMessageThread.start();
	}

	public void sendMessage(Message m) {
		if (connected) {
			try {
				DataQueue.put(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class SendMessageThread extends Thread {
		@Override
		public void run() {
			Message m;
			try {
				while (connected) {
					m = DataQueue.poll(5, TimeUnit.SECONDS);
					if (m != null) {
						doSendMessage(m);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			disconnect();
			// System.out.println("Finish send thread: " + socket.getRemoteSocketAddress());
		}
	}

	class ReceiveMessageThread extends Thread {
		@Override
		public void run() {
			Message message;
			try {
				while (connected) {
					message = readMessage();
					handleSession.processSessionMessage(Session.this, message);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			disconnect();
			// System.out.println("Finish receive thread: " +
			// socket.getRemoteSocketAddress());
		}
	}

	private void doSendMessage(Message m) throws Exception {
		dos.writeInt(m.command);
		byte[] data = m.getData();
		if (data != null) {
			int size = data.length;
			dos.writeInt(size);
			dos.write(data);
			// System.out.println("Send message: command (" + m.command + ") size [" + size
			// + "]");
		} else {
			dos.writeInt(0);
		}
		dos.flush();
	}

	private Message readMessage() throws Exception {
		int cmd = dis.readInt();
		int size = dis.readInt();
		byte[] data = new byte[size];
		int len = 0;
		int byteRead = 0;
		while (len != -1 && byteRead < size) {
			len = dis.read(data, byteRead, size - byteRead);
			if (len > 0) {
				byteRead += len;
			}
		}
		// System.out.println("Receive message: command (" + cmd + ") size [" + size +
		// "]");
		return new Message(cmd, data);
	}

	public void sendMessageDialog(String mes) {
		Message m = new Message(Cmd_Server2Client.SHOW_MESSAGE_DIALOG);
		try {
			m.writer().writeUTF(mes);
		} catch (IOException e) {
		}
		sendMessage(m);
	}

	public void disconnect() {
		if (connected) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			connected = false;
			Player p = PlayerManager.gI().get(id);
			if (p != null) {
				p.disconnect();
				System.out.println("Player " + p.username + " disconnected");
				p = null;
			}
		}
	}
}

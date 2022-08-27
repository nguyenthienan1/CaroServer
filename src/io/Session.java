package io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

import caro.Player;
import server.HandleSession;
import server.PlayerManager;
import server.ServiceSession;

public class Session {
	private HandleSession handle;
	private ServiceSession service;
	public String username;
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private SendMessageThread sendMessageThread;
	private ReceiveMessageThread receiveMessageThread;
	public long connectTime;
	public int id;
	public boolean connected;
	private LinkedBlockingQueue<Message> DataQueue = new LinkedBlockingQueue<>();

	public Session(Socket socket) {
		connectTime = System.currentTimeMillis();
		try {
			if (socket != null) {
				this.socket = socket;
				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());
				connected = true;
				sendMessageThread = new SendMessageThread();
				receiveMessageThread = new ReceiveMessageThread();
				handle = new HandleSession(this);
				service = new ServiceSession(this);
			}
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
	}

	public void start() {
		sendMessageThread.start();
		receiveMessageThread.start();
	}

	public ServiceSession getService() {
		return service;
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
					if (m == null) {
						continue;
					}
					doSendMessage(m);
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
					handle.processSessionMessage(message);
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

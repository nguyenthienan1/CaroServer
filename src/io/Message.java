package io;

import java.io.*;

public class Message {
	public int command;
	private ByteArrayOutputStream os = null;
	private DataOutputStream dos = null;
	private DataInputStream dis = null;

	public Message(int cmd) {
		command = cmd;
		os = new ByteArrayOutputStream();
		dos = new DataOutputStream(os);
	}

	public Message(int cmd, byte[] data) {
		command = cmd;
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		dis = new DataInputStream(is);
	}

	public DataOutputStream writer() {
		return dos;
	}

	public DataInputStream reader() {
		return dis;
	}

	public byte[] getData() {
		return os.toByteArray();
	}
}

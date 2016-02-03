package edu.uta.cse.app;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

	private static Logger LOGGER = LoggerFactory.getLogger(Server.class);
	private boolean running = true;

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean getRunning() {
		return this.running;
	}

	public void startServer(String portNumber, String baseDir)
			throws IOException {
		int port = Integer.parseInt(portNumber);
		ServerSocket server = new ServerSocket(port);
		LOGGER.info("Ready to serve");
		while (getRunning()) {
			Socket client = server.accept();
			LOGGER.info("Client connected");
			DataInputStream giveFile = new DataInputStream(
					client.getInputStream());
			String fileName = giveFile.readUTF();
			LOGGER.info("Client requesting file: " + fileName);
			OutputStream os = client.getOutputStream();
			InputStream in = new FileInputStream(baseDir + fileName);
			IOUtils.copy(in, os);
			client.close();
			LOGGER.info("Server sent file to client");
		}
		server.close();
	}

}

package edu.uta.cse.app;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {

	private static Logger LOGGER = LoggerFactory.getLogger(Client.class);

	public void startClient(String ip, String portNumber, String destFilePath,
			String downloadFile) throws UnknownHostException, IOException {
		int port = Integer.parseInt(portNumber);
		Socket socket = new Socket(ip, port);
		DataOutputStream askFile = new DataOutputStream(
				socket.getOutputStream());
		askFile.writeUTF(downloadFile);
		InputStream is = socket.getInputStream();
		OutputStream os = new FileOutputStream(destFilePath);
		IOUtils.copy(is, os);
		socket.close();
		LOGGER.info("File Downloaded !");
	}

	public boolean handshake(String ip, String port, String fileName, String part) throws IOException {
		Socket client = new Socket(ip, Integer.parseInt(port));
		OutputStream outToServer = client.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToServer);
        out.writeUTF("handshake");
        InputStream inFromServer = client.getInputStream();
        DataInputStream in =
                       new DataInputStream(inFromServer);
        String ackMessage = in.readUTF();
        client.close();
        System.out.println("Server says " + ackMessage);
        if(ackMessage.equals("acknowleged")){
        	return true;
        }else{
        	return false;
        }
        
	}
}

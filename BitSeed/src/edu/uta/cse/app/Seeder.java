package edu.uta.cse.app;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uta.cse.util.Constants;
import edu.uta.cse.util.FileUtil;


public class Seeder {

	private static  Logger LOGGER = LoggerFactory.getLogger(Seeder.class);
	public static void main(String[] args) {
		
		File file = new File("E:/temp/p2pexamples.pdf");
		FileUtil.createTorrent("E:/temp/p2pexamples.pdf", Constants.DEST_PATH);
		publishTorrent(file.getName(), Constants.SEEDER_PORT,Constants.noOfParts);
		
		// starting server
		Server server = new Server();
		try {
			server.startServer(Constants.SEEDER_PORT, Constants.DEST_PATH);
		} catch (IOException e) {
			LOGGER.error("Problem with creating seed.");
			e.printStackTrace();
		}
	}

	private static void publishTorrent(String name, String seederPort, int noOfParts) {
		try {
			URL url = new URL(Constants.POST_URL);
			
			Map<String, String> info = new HashMap<String, String>();
			info.put("fileName",name);
			info.put("port",seederPort);			
			info.put("noOfParts",Integer.toString(noOfParts));
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			String POST_PARAMS="params="+name+":"+seederPort+":"+Integer.toString(noOfParts);
			connection.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(POST_PARAMS);
			wr.flush();
			wr.close();
			int responseCode = connection.getResponseCode();
	        LOGGER.info("POST Response Code confirmation " + responseCode);
	        if(responseCode!=200){
	        	throw new RuntimeException("Failed to post the new Torrent in Tracker : HTTP error code : "
						+ connection.getResponseCode());
	        }
	        
		} catch (MalformedURLException e1) {			
			LOGGER.error("Malformed URL Exception occured :"+ e1.getMessage());
			e1.printStackTrace();
		} catch (ProtocolException e1) {
			LOGGER.error("Protocol Exception occured :"+ e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e1) {
			LOGGER.error("IO Exception occured :"+ e1.getMessage());
			e1.printStackTrace();
		}
	}
}

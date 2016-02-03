package edu.uta.cse.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class Constants {

	public static int noOfParts=0;
	public static final String CHUNK_SIZE;
	public static final String DEST_PATH;
	public static final String PEER_PORT;
	

	static {

		Configuration config = null;
		try {
			config = new PropertiesConfiguration("config.properties");
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

		CHUNK_SIZE = config.getString("chunk.size");
		DEST_PATH = config.getString("peer.dest");
		PEER_PORT = config.getString("peer.port");
	
	}
}

package edu.uta.cse.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class Constants {

	public static int noOfParts=0;
	public static final String CHUNK_SIZE;
	public static final String DEST_PATH;
	public static final String TRACKER_URL;
	public static final String SEEDER_PORT;
	public static final String POST_URL;

	static {

		Configuration config = null;
		try {
			config = new PropertiesConfiguration("config.properties");
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

		CHUNK_SIZE = config.getString("chunk.size");
		DEST_PATH = config.getString("torrent.dest");
		TRACKER_URL = config.getString("tracker.url");
		SEEDER_PORT = config.getString("seeder.port");
		POST_URL = config.getString("post.url");
	}
}

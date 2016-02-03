package edu.uta.cse.util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackerUtil {

	final static Logger LOGGER = LoggerFactory.getLogger(TrackerUtil.class);
	public static final int CHUNK_SIZE = 256000;
	public static String getTrackerResponse(String trackerUrl, String infoHash,
			String peerId, String port, String uploaded, String downloaded,
			String left, String event, String parts)
			throws UnsupportedEncodingException, MalformedURLException,
			IOException, ProtocolException {
		
		String encodeType = "UTF-8";
		String queryString = 
				"&info_hash=" + URLEncoder.encode(infoHash, encodeType) +
				"&peer_id=" + URLEncoder.encode(peerId, encodeType) +
				"&port=" + URLEncoder.encode(port, encodeType) +
				"&uploaded=" + URLEncoder.encode(uploaded, encodeType) +
				"&downloaded=" + URLEncoder.encode(downloaded, encodeType) +
				"&left=" + URLEncoder.encode(left, encodeType) +
				"&parts=" + URLEncoder.encode(parts, encodeType) +
				"&event=" + URLEncoder.encode(event, encodeType);
		
		System.out.println("baseUrl : "+ queryString);
		URL url = new URL(trackerUrl + queryString);
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "text/plain");
		if (connection.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ connection.getResponseCode());
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String response;
		response = reader.readLine();
		return response;
	}
}

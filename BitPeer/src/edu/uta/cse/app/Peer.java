package edu.uta.cse.app;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Random;
import java.util.TreeMap;

import org.pixie.bencoding.BDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uta.cse.util.Constants;
import edu.uta.cse.util.FileUtil;
import edu.uta.cse.util.TrackerUtil;

public class Peer {

	private final static Logger LOGGER = LoggerFactory.getLogger(Peer.class);
	
	@SuppressWarnings({ "unchecked", "unused" })
	public static void main(String[] args) {
		int partSize=0;
		String peerConfig = Integer.toString(ThreadLocalRandom.current().nextInt(10));

		final String portNumber = Constants.PEER_PORT + peerConfig;
		String peerId = "id_peer" + peerConfig;
		final String baseDir = Constants.DEST_PATH+"peer" + peerConfig + "/";
		FileUtil.createDestFolder(baseDir);
		File file = new File("E:/projExecution/p2pexamples.pdf.torrent");
		
		//read torrentfile
		Map<String, Object> parsedInfo = FileUtil.readMetaInfoFile(file.getAbsolutePath());
		LOGGER.info("Parsed Meta info contents!");
		parsedInfo.forEach((k, v) -> 
	    LOGGER.info(k + "=" + v));
		String fileName = (String)parsedInfo.get("filename");
		partSize = Integer.parseInt((String)parsedInfo.get("noOfParts"));
		LOGGER.info("Part size :"+partSize);
		
		String parts= "0";
		int tmp = partSize;
		while (tmp>1){
				parts=parts+"0";
				tmp --;
		}
		
		/*
		 * if (args.length == 1){ peerConfig = args[0]; } else if (args.length
		 * == 0) { // OK } else { // invalid usage
		 * System.out.println("Invalid command");
		 * System.out.println("Usage: java -jar Peer.java <Peer number>");
		 * System.exit(0); }
		 */
		

		String infoHash = "infohash";
		String uploaded = "0";
		String downloaded = "0";
		String left = (String)parsedInfo.get("noOfParts");
		LOGGER.info("Validating the left---->>>"+left);
		String event = "started";
		

		//start server
		final Server server = new Server();
		Thread serverThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					server.startServer(portNumber, baseDir);
				} catch (IOException e) {
					LOGGER.info("Error creating the server");
					e.printStackTrace();
				}
			}
		});
		
		//serverThread.setDaemon(true);
		serverThread.start();


		// get tracker url
		String trackerUrl = (String) parsedInfo.get("url");

		Map<String, Object> peersInfo = new HashMap<String, Object>();
		// connect to tracker and get response
		try {
			String response = TrackerUtil.getTrackerResponse(trackerUrl,
					infoHash, peerId, portNumber, uploaded, downloaded, left,
					event, parts);
			LOGGER.info("Response received from the Tracker : " + response);
			peersInfo = BDecoder.decode(response);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Decoding failed");
			e.printStackTrace();
		} catch (MalformedURLException e) {
			LOGGER.error("Decoding failed");
			e.printStackTrace();
		} catch (ProtocolException e) {
			LOGGER.error("Decoding failed");
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("Decoding failed");
			e.printStackTrace();
		}

		// get peers list
		List<Map<String, Object>> peersList = (List<Map<String, Object>>) peersInfo
				.get("peers");
		FileUtil.createXMLFile(peersList,baseDir);
		// connect to peers and download
		LOGGER.debug("Peers : " + peersList);
		
		// piece download strategy
		Map<String, Object> strategyMap = new TreeMap<String, Object>();
		for (Map<String, Object> peer : peersList) {
			// if not self

			if (!((String) peer.get("peer_id")).equals(peerId)) {
				String partsBits = (String) peer.get("parts");

				for (int i = 0; i < partsBits.length(); i++) {
					// if the peer has that part
					if (partsBits.charAt(i) == '1') {
						List<Map<String, Object>> peerList = (List<Map<String, Object>>) strategyMap
								.get(i + "");
						if (peerList == null) {
							peerList = new ArrayList<Map<String, Object>>();
						}

						peerList.add(peer);
						strategyMap.put(i + "", peerList);
					}
				}
			} else {
				System.out.println("Not the current peer");
			}
		}
		LOGGER.info("Piece download strategy: " + strategyMap);

		strategyMap.forEach((k, v) -> LOGGER.info(k + "=" + v.toString()));

		LOGGER.info("Rarest first order!");
		Map<String, Object> rarestFirstMap = sortByComparator(strategyMap);
		 LOGGER.info("Rarest first strategy" + rarestFirstMap);

		rarestFirstMap.forEach((k, v) -> LOGGER.info(k + "="
				+ v.toString()));

		//get parts
		for (Entry<String, Object> partInfo : strategyMap.entrySet()) {
			String part = partInfo.getKey();
			List<Map<String, String>> partPeers = (List<Map<String, String>>) partInfo
					.getValue();

			boolean downloadFailed;
			do {
				downloadFailed = false;
				int peerNumber = new Random().nextInt(partPeers.size());
				Map<String, String> partPeer = partPeers.get(peerNumber);
				String port = (String) partPeer.get("port");
				String ip = (String) partPeer.get("ip");
				String partPeerId = (String) partPeer.get("peer_id");

				LOGGER.debug("Downloading part " + part + " from " + partPeerId);
				Client client = new Client();
				try {
					client.startClient(ip, port, baseDir + fileName + ".part"
							+ part, fileName + ".part" + part);
				} catch (IOException e) {
					LOGGER.error("Peer not available or might have choked");
					downloadFailed = true;
					e.printStackTrace();
				}
			} while (downloadFailed);
		}

		// recreate the file after downloading all the parts
		int numParts = parts.length();
		LOGGER.info("Parts length: "+numParts);
		if (FileUtil.recreateFile(baseDir, fileName, numParts)) {
			LOGGER.debug(fileName + " downloaded.");

			// inform tracker
			Map<String, Object> informInfo = new HashMap<String, Object>();
			// connect to tracker and get response
			try {
				String updatingParts= "1";
				while (partSize>1){
					updatingParts=updatingParts+"1";
						partSize --;
				}
				
				uploaded = "0";
				downloaded = (String)parsedInfo.get("noOfParts");
				left = "0";
				event = "completed";
				parts = updatingParts;
				String response = TrackerUtil.getTrackerResponse(trackerUrl,
						infoHash, peerId, portNumber, uploaded, downloaded,
						left, event, parts);
				informInfo = BDecoder.decode(response);
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("Failed to decode the response!");
				e.printStackTrace();
			} catch (MalformedURLException e) {
				LOGGER.error("Failed to decode the response!");
				e.printStackTrace();
			} catch (ProtocolException e) {
				LOGGER.error("Failed to decode the response!");
				e.printStackTrace();
			} catch (IOException e) {
				LOGGER.error("Encountered IO Exception");
				e.printStackTrace();
			}
		} else {
			LOGGER.info("Download error: All parts not downloaded.");
		}
	}

	private static Map<String, Object> sortByComparator(
			Map<String, Object> unsortMap) {

		List<Entry<String, Object>> list = new LinkedList<Entry<String, Object>>(
				unsortMap.entrySet());
		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<String, Object>>() {
			@SuppressWarnings("unchecked")
			public int compare(Entry<String, Object> o1,
					Entry<String, Object> o2) {
				return ((Integer) ((List<Map<String, Object>>) o1.getValue())
						.size())
						.compareTo((Integer) ((List<Map<String, Object>>) o2
								.getValue()).size());
			}
		});

		// Maintaining insertion order with the help of LinkedList
		Map<String, Object> sortedMap = new LinkedHashMap<String, Object>();
		for (Entry<String, Object> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
}

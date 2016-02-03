package edu.uta.cse.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uta.cse.util.BencodeUtil;

/**
 * Servlet implementation class TrackerController
 */
@WebServlet("*.trackingservice")
public class TrackerController extends HttpServlet {
	private static Logger LOGGER = LoggerFactory
			.getLogger(TrackerController.class);
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public TrackerController() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String file = request.getParameter("file");
		String infoHash = request.getParameter("info_hash");
		String peerId = request.getParameter("peer_id");
		String port = request.getParameter("port");
		String uploaded = request.getParameter("uploaded");
		String downloaded = request.getParameter("downloaded");
		String left = request.getParameter("left");
		String event = request.getParameter("event");
		String parts = request.getParameter("parts");
		String ip = "localhost";

		// process received info
		LOGGER.info("Received: " + file + " " + infoHash + " " + peerId + " "
				+ port + " " + uploaded + " " + downloaded + " " + left + " "
				+ event + " " + parts + " ");

		// retrieve all the hash tables
		ServletContext application = getServletContext();
		Map<String, Object> peersTable = (Map<String, Object>) application
				.getAttribute("peersTable");
		Map<String, Object> seedersTable = (Map<String, Object>) application
				.getAttribute("seedersTable");
		Map<String, Object> leechersTable = (Map<String, Object>) application
				.getAttribute("leechersTable");

		if (event.equals("started")) {
			// create an entry in peers table
			Map<String, String> peerMap = new HashMap<String, String>();
			peerMap.put("ip", ip);
			peerMap.put("port", port);
			peerMap.put("parts", parts);
			peersTable.put(peerId, peerMap);

			// update leechers table to add a new leecher
			LOGGER.info("updating leechers table");
			List<String> leechers = (List<String>) leechersTable.get(file);
			if (leechers == null) {
				leechers = new ArrayList<String>();
			}
			if (!leechers.contains(peerId)) {
				leechers.add(peerId);
			}
			leechersTable.put(file, leechers);

		} else if (event.equals("stopped")) {
			LOGGER.info("handling stop event, this is WIP");

		} else if (event.equals("completed")) {
			// update seeders table to add new seeder
			LOGGER.info("updating seeders table");
			List<String> seeders = (List<String>) seedersTable.get(file);
			seeders.add(peerId);
			seedersTable.put(file, seeders);

			// update leechers table to remove the peer as leecher
			LOGGER.info("Leecher table " + leechersTable.size());
			if (leechersTable.size() > 0) {
				List<String> leechers = (List<String>) leechersTable.get(file);
				leechers.remove(peerId);
				leechersTable.put(file, leechers);
			}

			// update peers table for parts
			LOGGER.info("Updating peers table");
			LOGGER.debug("else if parts: " + parts);
			if (peersTable.containsKey(peerId)) {
				System.out.println("Contains key!");
				((Map<String, String>) peersTable.get(peerId)).put("parts",
						parts);
			} else {
				System.out.println("Does not contain key!!");
			}

		} else {
			// update peers table for parts
			LOGGER.info("Updating peers table");
			System.out.println("parts ka value:" + parts);
			((Map<String, String>) peersTable.get(peerId)).put("parts", parts);

		}

		// create tracker response
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("failInfo", "Reason");
		responseMap.put("warnInfo", "Message");
		responseMap.put("interval", 60 + "");
		responseMap.put("complete", seedersTable.size() + "");
		responseMap.put("incomp", leechersTable.size() + "");

		// get the list of peers with the file required
		List<String> peersWithFile = new ArrayList<String>();
		peersWithFile.addAll((List<String>) seedersTable.get(file));
		if (leechersTable.containsKey(file)) {
			peersWithFile.addAll((List<String>) leechersTable.get(file));
		}

		// get the peer info for all the peers having that form the swarm
		List<Object> peersList = new ArrayList<Object>();
		LOGGER.info("peerList");
		for (Object str : peersList) {
			LOGGER.info((String) str);
		}
		Map<String, Object> peerMap = new HashMap<String, Object>();
		LOGGER.info("size of peerMap" + peerMap.size());
		LOGGER.info("contents of peerMap");
		for (Map.Entry<String, Object> entry : peerMap.entrySet()) {
			LOGGER.info("key : " + entry.getKey() + " Value : "
					+ (String) entry.getValue());
		}

		for (String peer : peersWithFile) {

			if (peersTable.containsKey(peer)) {
				peerMap = (Map<String, Object>) peersTable.get(peer);
				peerMap.put("peer_id", peer);
				peersList.add(peerMap);
			}

		}

		responseMap.put("peers", peersList);
		LOGGER.info("Response map: " + responseMap);

		// get the bencoded response
		String bencodedResponse = BencodeUtil.bencode(responseMap);

		// send the response to the client
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");
		out.write(bencodedResponse);
		out.close();
	}

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.out.println("post request recieved!!");
		ServletContext application = getServletContext();
		Map<String, Object> peersTable = (Map<String, Object>) application
				.getAttribute("peersTable");
		Map<String, Object> seedersTable = (Map<String, Object>) application
				.getAttribute("seedersTable");
		Map<String, Object> leechersTable = (Map<String, Object>) application
				.getAttribute("leechersTable");
		String[] paramList = request.getParameter("params").split(":");

		String parts= "1";
		int tmp = Integer.parseInt(paramList[2]);
		while (tmp>1){
				parts=parts+"1";
				tmp --;
		}
		LOGGER.info("Parts:"+parts);
		// There should be atleast one seed present
		List<String> peerList = new ArrayList<String>();
		peerList.add("id_peer1");
		seedersTable.put(paramList[0], peerList);

		// Add the first seed information in the peers table
		Map<String, String> peerInfo = new HashMap<String, String>();
		peerInfo.put("ip", "localhost");
		peerInfo.put("port", paramList[1]);
		peerInfo.put("parts", parts);
		peersTable.put("id_peer1", peerInfo);

		application.setAttribute("peersTable", peersTable);
		application.setAttribute("seedersTable", seedersTable);
		application.setAttribute("leechersTable", leechersTable);
		
	}
}

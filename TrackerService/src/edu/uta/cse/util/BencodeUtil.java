package edu.uta.cse.util;

import java.util.List;
import java.util.Map;

public class BencodeUtil {

	public static String bencode(Map<String, Object> responseMap) {
		// TODO Auto-generated method stub
		StringBuilder bencodedResponse = new StringBuilder();
		
//		bencodedResponse.append("d");
		bencodedResponse.append("failInfo".length() + ":failInfo");
		bencodedResponse.append(((String)responseMap.get("failInfo")).length() + ":" + (String)responseMap.get("failInfo"));
		bencodedResponse.append("warnInfo".length() + ":warnInfo");
		bencodedResponse.append(((String)responseMap.get("warnInfo")).length() + ":" + (String)responseMap.get("warnInfo"));
		bencodedResponse.append("interval".length() + ":interval");
		bencodedResponse.append(((String)responseMap.get("interval")).length() + ":" + (String)responseMap.get("interval"));
		bencodedResponse.append("complete".length() + ":complete");
		bencodedResponse.append(((String)responseMap.get("complete")).length() + ":" + (String)responseMap.get("complete"));
		bencodedResponse.append("incomp".length() + ":incomp");
		bencodedResponse.append(((String)responseMap.get("incomp")).length() + ":" + (String)responseMap.get("incomp"));
		
		bencodedResponse.append("peers".length() + ":peers");
		bencodedResponse.append("l");
		List<Object> peersList = (List<Object>) responseMap.get("peers");
		for (Object object : peersList) {
			bencodedResponse.append("d");
			
			Map<String, Object> peerMap = (Map<String, Object>) object;
			bencodedResponse.append("peer_id".length() + ":peer_id");
			bencodedResponse.append(((String)peerMap.get("peer_id")).length() + ":" + (String)peerMap.get("peer_id"));
			bencodedResponse.append("ip".length() + ":ip");
			bencodedResponse.append(((String)peerMap.get("ip")).length() + ":" + (String)peerMap.get("ip"));
			bencodedResponse.append("port".length() + ":port");
			bencodedResponse.append(((String)peerMap.get("port")).length() + ":" + (String)peerMap.get("port"));
			bencodedResponse.append("parts".length() + ":parts");
			bencodedResponse.append(((String)peerMap.get("parts")).length() + ":" + (String)peerMap.get("parts"));
			
			bencodedResponse.append("e");
		}
		// end list
		bencodedResponse.append("e");
		// end dictionary

		
		return bencodedResponse.toString();
	}

}

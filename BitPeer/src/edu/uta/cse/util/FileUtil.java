package edu.uta.cse.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FileUtil {

	final static Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
	public static final int CHUNK_SIZE = 256000;

	public static void createDestFolder(String filePath) {
		LOGGER.info(" Peer download path :" + filePath);
		File dest = new File(filePath);

		if (!dest.exists()) {
			if (dest.mkdirs()) {
				LOGGER.info("Destination directory created!");
			} else {
				LOGGER.info("Error creating destination folder structure");
			}
		}
	}
	public static Map<String, Object> readMetaInfoFile(String path) {
		BufferedReader br = null;
		File file = new File(path);
		char[] buffer = new char[(int) file.length()];
		try {
			// open the torrent file
			br = new BufferedReader(new FileReader(new File(path)));
			br.read(buffer);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < buffer.length; i++) {
			sb.append(buffer[i]);
		}
		System.out.println("Metainfo: " + sb.toString());

		// parse the terse format
		String metaInfo = sb.toString();
		metaInfo = metaInfo.substring(8);
		String fileNameLengthString = metaInfo.substring(0,
				metaInfo.indexOf(":"));
		int fileNameLength = Integer.parseInt(fileNameLengthString);
		metaInfo = metaInfo.substring(metaInfo.indexOf(":") + 1);
		String fileName = metaInfo.substring(0, fileNameLength);
		metaInfo = metaInfo.substring(fileNameLength + 1);
		String fileLengthString = metaInfo.substring(0, metaInfo.indexOf("e"));
		int fileLength = Integer.parseInt(fileLengthString);
		metaInfo = metaInfo.substring(fileLengthString.length() + 2);
		String pieceLengthString = metaInfo.substring(0, metaInfo.indexOf("e"));
		int pieceLength = Integer.parseInt(pieceLengthString);
		metaInfo = metaInfo.substring(pieceLengthString.length() + 1);
		String piecesHashLengthString = metaInfo.substring(0,
				metaInfo.indexOf(":"));
		metaInfo = metaInfo.substring(piecesHashLengthString.length() + 1);

		String piecesHash = metaInfo
				.substring(0, metaInfo.indexOf("3:url") - 1);

		String noOfParts = metaInfo.substring(metaInfo.indexOf("2:parts"),
				metaInfo.indexOf("3:url"));
		noOfParts = noOfParts.substring(noOfParts.indexOf("i") + 1,
				noOfParts.length() - 1);

		metaInfo = metaInfo.substring(metaInfo.indexOf("3:url") + 5);
		String urlLengthString = metaInfo.substring(0, metaInfo.indexOf(":"));
		metaInfo = metaInfo.substring(urlLengthString.length() + 1);
		String url = metaInfo.substring(0, metaInfo.length() - 1);

		// store the information in the terse format into a map
		Map<String, Object> metainfoMap = new HashMap<String, Object>();
		metainfoMap.put("filename", fileName);
		metainfoMap.put("fileLength", fileLength);
		metainfoMap.put("pieceLength", pieceLength);
		metainfoMap.put("piecesHash", piecesHash);
		metainfoMap.put("url", url);
		metainfoMap.put("noOfParts", noOfParts);

		return metainfoMap;

	}

	/**
	 * Recreates the complete file from the parts in the given directory
	 */
	public static synchronized boolean  recreateFile(String recreatedFileDir,
			String recreatedFileName, int numParts) {
		//recreatedFileDir = recreatedFileDir.replace("/", "");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(recreatedFileDir + "/"
					+ recreatedFileName));
			File dir = new File(recreatedFileDir);
			File[] parts = dir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.contains(".part");
				}
			});

			if (parts.length != 5) {
				return false;
			}
			File temp = new File(recreatedFileDir + "/" + recreatedFileName
					+ ".part" + (parts.length - 1));
			byte[] buffer = new byte[((parts.length - 1) * CHUNK_SIZE)
					+ ((int) temp.length())];
			for (int part = 0; part < parts.length; part++) {
				String partName = recreatedFileDir + "/" + recreatedFileName
						+ ".part" + part;
				File partFile = new File(partName);
				System.out.println("From file: " + partFile.getName()
						+ " of size: " + partFile.length());
				FileInputStream fis = new FileInputStream(partFile);
				fis.read(buffer, part * CHUNK_SIZE, (int) partFile.length());// CHUNK_SIZE);
				fis.close();
			}
			fos.write(buffer);
			System.out.println("Created file successfully : " + buffer.length
					+ " bytes");
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	public static void createXMLFile(List<Map<String, Object>> peerList,String destPath)
	{	
		try 
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			Document doc = db.newDocument();
			Element rootElement = doc.createElement("root");
			doc.appendChild(rootElement);
			for(Map<String, Object> peer:peerList )
			{
				Element peer_info = doc.createElement("Peer_info");
				rootElement.appendChild(peer_info );
				
				Element peer_id = doc.createElement("peer_id");
				peer_id.appendChild(doc.createTextNode(peer.get("peer_id").toString())	);
				peer_info.appendChild(peer_id);	
				
				Element port = doc.createElement("port");
				port.appendChild(doc.createTextNode(peer.get("port").toString()));
				peer_info.appendChild(port);
				
				Element parts = doc.createElement("parts");
				parts.appendChild(doc.createTextNode(peer.get("parts").toString()));
				peer_info.appendChild(parts);
				
				Element ip = doc.createElement("ip");
				ip.appendChild(doc.createTextNode(peer.get("ip").toString()));
				peer_info.appendChild(ip);
				
			}
			TransformerFactory tranfact = TransformerFactory.newInstance();
			Transformer tf = tranfact.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(destPath+"/peers_info.xml"));
			
			tf.transform(source, result);
			System.out.println("XML has been created");		
		} 
		catch (ParserConfigurationException pce) 
		{
			pce.printStackTrace();
		}
		catch(TransformerException tf)
		{
			tf.printStackTrace();
		}
	}

}

package edu.uta.cse.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

	final static Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
	public static final int CHUNK_SIZE = Integer.parseInt(Constants.CHUNK_SIZE);

	public static void createMetaInfoFile(String fileName, long fileLength,
			int pieceLength, String piecesHash, int noOfParts, String destDir) {
		StringBuilder content = new StringBuilder();
		String trackerUrl =Constants.TRACKER_URL
				+ fileName;
		content.append("d");
		content.append("4:info");
		content.append("l");
		content.append(fileName.length() + ":" + fileName + "i" + fileLength
				+ "e" + "i" + pieceLength + "e");
		content.append(piecesHash.length() + ":" + piecesHash);
		content.append("e");
		content.append("2:parts");
		content.append("i" + noOfParts + "e");
		content.append("3:url");
		content.append(trackerUrl.length() + ":" + trackerUrl);
		content.append("e");

		LOGGER.info("Update file shard size " );
		Constants.noOfParts=noOfParts;
		LOGGER.info("Checking : "+ Constants.noOfParts);
		LOGGER.debug(content.toString());
		String torrentFilePath = destDir + fileName + ".torrent";
		LOGGER.info("Torrent path :" + torrentFilePath);
		BufferedWriter bw = null;
		createDestFolder(destDir);
		try {
			bw = new BufferedWriter(new FileWriter(new File(torrentFilePath)));
			bw.write(content.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				LOGGER.error("IOException occured : " + e.getMessage());
				e.printStackTrace();
			}
		}

		LOGGER.info("Torrent/metainfo file created.");
	}

	private static void createDestFolder(String torrentFilePath) {
		LOGGER.info("torrnet dest path :" + torrentFilePath);
		File dest = new File(torrentFilePath);

		if (!dest.exists()) {
			if (dest.mkdirs()) {
				LOGGER.info("Destination directory created!");
			} else {
				LOGGER.info("Error creating destination folder structure");
			}
		}
	}

	public static void createTorrent(String srcFile, String destDir) {

		if (validateFileExists(srcFile)) {
			createDestFolder(destDir);
			FileInputStream fis = null;
			byte[] buffer = new byte[CHUNK_SIZE];
			StringBuilder piecesHash = new StringBuilder();
			MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("MD5");

			} catch (NoSuchAlgorithmException e) {
				LOGGER.error("Execprion occured while getting MD5 instance: "
						+ e.getMessage());
				e.printStackTrace();
			}

			try {
				File sourceFile = new File(srcFile);
				fis = new FileInputStream(sourceFile);
				int bytesRead = fis.read(buffer, 0, buffer.length);

				int part = 0;
				for (part = 0; bytesRead == CHUNK_SIZE; part++) {
					String partFileName = destDir + sourceFile.getName()
							+ ".part" + part;
					File partFile = new File(partFileName);
					FileOutputStream fos = new FileOutputStream(partFile);
					fos.write(buffer);
					fos.close();

					byte[] mdBytes = md.digest(buffer);
					for (int i = 0; i < mdBytes.length; i++) {
						piecesHash.append(Integer
								.toHexString(0xFF & mdBytes[i]));
					}

					bytesRead = fis.read(buffer, 0, buffer.length);
				}

				if (bytesRead > -1) {
					buffer = Arrays.copyOf(buffer, bytesRead);
					String partFileName = destDir + sourceFile.getName()
							+ ".part" + part;
					File partFile = new File(partFileName);
					FileOutputStream fos = new FileOutputStream(partFile);
					fos.write(buffer);
					fos.close();
					LOGGER.info("File size: " + partFile.length());

					byte[] mdBytes = md.digest(buffer);
					for (int i = 0; i < mdBytes.length; i++) {
						piecesHash.append(Integer
								.toHexString(0xFF & mdBytes[i]));
					}

				}
				LOGGER.info("part count: " + part++);
				LOGGER.info("File parts created!");
				// creating torrent file
				createMetaInfoFile(sourceFile.getName(), sourceFile.length(),
						CHUNK_SIZE, piecesHash.toString(), part, destDir);
			} catch (IOException e) {
				LOGGER.error("IOException occured :" + e.getLocalizedMessage());
				e.printStackTrace();
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			LOGGER.error("The specified file does not exist");
		}
	}

	private static boolean validateFileExists(String srcFile) {
		File targetFile = new File(srcFile);
		boolean result = false;
		if (targetFile.isFile() && targetFile.canRead()) {
			LOGGER.info("File exists at location and can be read to create Torrent file!ss");
			result = true;
		} else {
			LOGGER.error("Targe File does not exist!!");
		}
		return result;
	}
}

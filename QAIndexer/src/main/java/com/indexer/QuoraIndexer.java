package com.indexer;

import java.net.UnknownHostException;

public class QuoraIndexer {

	private final static String index = "quora";
	private final static String type = "qa";
	private final static String dataPath = "W:/crawl/www.quora.com";

	public static void main(String[] args) {
		try {
			Indexer indexer = new Indexer(index, type, dataPath);
			indexer.processFiles();
			indexer.closeConnection();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}

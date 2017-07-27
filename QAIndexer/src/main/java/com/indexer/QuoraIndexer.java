package com.indexer;

import java.net.UnknownHostException;

/**
 * Represents a indexer that can index crawled pages of Quora to elasticsearch.
 */
public class QuoraIndexer {

	private final static String index = "quora";
	private final static String type = "qa";
	// Please change the datapath.
	private final static String dataPath = "W:/crawl/www.quora.com";

	private final static boolean needUpdate = true;
	private final static boolean needSkip = false;

	public static void main(String[] args) {
		try {
			Indexer indexer = new Indexer(index, type, dataPath);
			indexer.processFiles(needUpdate, needSkip);
			indexer.closeConnection();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}

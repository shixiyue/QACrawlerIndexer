package com.indexer;

import java.io.File;
import java.net.UnknownHostException;

/**
 * Represents a indexer that can index processed StackExchange pages to
 * elasticsearch.
 */
public class StackExchangeIndexer {

	private final static String index = "stack_exchange";
	private final static String type = "qa";
	// Please change the datapath.
	private final static String dataPath = "C:/stackoverflow/parsed/";

	private final static boolean needUpdate = false;
	private final static boolean needSkip = true;

	public static void main(String[] args) {
		File mainDirectory = new File(dataPath);
		String[] subDirectories = mainDirectory.list();
		for (String directory : subDirectories) {
			try {
				directory = dataPath + directory + "/";
				System.out.println(directory);
				Indexer indexer = new Indexer(index, type, directory);
				indexer.processFiles(needUpdate, needSkip);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}

}

package com.indexer;

import java.io.File;
import java.net.UnknownHostException;

public class StackExchangeIndexer {

	private final static String index = "stack_exchange";
	private final static String type = "qa";
	private final static String dataPath = "W:/StackExchange/";

	public static void main(String[] args) {
		File mainDirectory = new File(dataPath);
		String[] subDirectories = mainDirectory.list();
		for (String directory: subDirectories) {
			try {
				directory = dataPath + directory + "/";
				Indexer indexer = new Indexer(index, type, directory);
				indexer.processFiles();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}

}

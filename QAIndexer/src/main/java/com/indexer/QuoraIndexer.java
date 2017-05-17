package com.indexer;

import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.indexer.Config;

public class QuoraIndexer {

	private final String index = "qa";
	private final String type = "quora";
	private final String defaultHost = "localhost";
	private final String dataPath = "W:/QuoraData/www.quora.com/";
	private final int defaultPort = 9300;

	private Logger logger = LogManager.getLogger(QuoraIndexer.class);
	private TransportClient client;
	private BulkProcessor bulkProcessor;

	/**
	 * Constructor which sets index for elasticsearch
	 * 
	 * @throws UnknownHostException
	 *             The host is unknown
	 */
	public QuoraIndexer() throws UnknownHostException {
		client = new PreBuiltTransportClient(Settings.EMPTY)
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(defaultHost), defaultPort));
		logger.info("Set target host and port to " + defaultHost + ":" + defaultPort);
	}

	/**
	 * Constructor which sets index and ip for elasticsearch
	 * 
	 * @param ip
	 *            IP of the server running elasticsearch
	 * @throws UnknownHostException
	 *             The host is unknown
	 */
	public QuoraIndexer(String ip) throws UnknownHostException {
		client = new PreBuiltTransportClient(Settings.EMPTY)
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), defaultPort));
		logger.info("Set target host and port to " + ip + ":" + defaultPort);
	}

	/**
	 * Constructor which sets index, ip, and port for elasticsearch
	 * 
	 * @param ip
	 *            IP of the server running elasticsearch
	 * @param port
	 *            Port of the elasticsearch service
	 * @throws UnknownHostException
	 *             The host is unknown
	 */
	public QuoraIndexer(String ip, int port) throws UnknownHostException {
		client = new PreBuiltTransportClient(Settings.EMPTY)
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), port));
		logger.info("Set target host and port to " + ip + ":" + port);
	}

	private void initBulkProcessor() {
		bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
			@Override
			public void beforeBulk(long executionId, BulkRequest request) {
				logger.info("Number of actions: " + request.numberOfActions());
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
				logger.error("Bulk execution failed [" + executionId + "].\n" + failure.toString());
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
				logger.info("Bulk execution completed [" + executionId + "].\n" + "Took (ms): "
						+ response.getTookInMillis() + "\n" + "Failures: " + response.hasFailures() + "\n" + "Count: "
						+ response.getItems().length);
			}
		}).build();
	}

	/**
	 * Closes the client connection
	 */
	public void closeConnection() {
		client.close();
	}

	public static void main(String[] args) {
		try {
			QuoraIndexer indexer = new QuoraIndexer();
			indexer.processFiles();
			indexer.closeConnection();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private void processFiles() {
		JSONParser parser = new JSONParser();
		initBulkProcessor();
		try (Stream<Path> filePathStream = Files.walk(Paths.get(dataPath))) {
			filePathStream.forEach(filePath -> {
				if (!Files.isRegularFile(filePath)) {
					return;
				}
				processJsonFile(parser, filePath.toString());
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		bulkProcessor.close();
	}

	private void processJsonFile(JSONParser parser, String file) {
		try {
			JSONObject jsonData = (JSONObject) parser.parse(new FileReader(file));
			updateJson(jsonData);
			bulkProcessor.add(Requests.indexRequest(index).type(type).source(jsonData));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			logger.error("Failed to add file " + file);
		}
	}

	private void updateJson(JSONObject jsonData) {
		updateTopics(jsonData);
		updateQuestion(jsonData);
		updateAnswers(jsonData); // To be removed
	}

	private void updateTopics(JSONObject jsonData) {
		JSONArray categoriesArray = (JSONArray) jsonData.get(Config.CATEGORIES);
		jsonData.remove(Config.CATEGORIES);

		if (categoriesArray == null) {
			jsonData.put(Config.TOPICS, "");
			return;
		}
		StringJoiner categoriesStringBuilder = new StringJoiner(Config.stringDelimiter);
		for (Object category : categoriesArray) {
			categoriesStringBuilder.add((String) category);
		}
		jsonData.put(Config.TOPICS, categoriesStringBuilder.toString());
	}

	private void updateQuestion(JSONObject jsonData) {
		String question = (String) jsonData.get(Config.QUESTION) + Config.stringDelimiter
				+ (String) jsonData.get(Config.DESCRIPTION);
		jsonData.put(Config.QUESTION, question);
		jsonData.remove(Config.DESCRIPTION);
	}

	// To be removed
	private static void updateAnswers(JSONObject jsonData) {
		JSONArray answers = (JSONArray) jsonData.get(Config.ANSWERS);
		if (answers == null) {
			return;
		}
		for (Object answer : answers) {
			JSONObject answerObject = (JSONObject) answer;
			try {
				answerObject.put(Config.VOTE, Integer.parseInt((String) answerObject.get(Config.VOTE)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

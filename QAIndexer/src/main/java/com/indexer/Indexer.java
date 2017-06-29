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

public class Indexer {

	private final static String defaultHost = Config.READPEER_IP;
	private final static int defaultPort = Config.READPEER_PORT;
	
	private Logger logger = LogManager.getLogger(QuoraIndexer.class);
	
	private TransportClient client;
	private BulkProcessor bulkProcessor;
	
	private String index;
	private String type;
	private String dataPath;

	/**
	 * Constructor which sets index for elasticsearch
	 * 
	 * @throws UnknownHostException
	 *             The host is unknown
	 */
	public Indexer(String index, String type, String dataPath) throws UnknownHostException {
		this(defaultHost, defaultPort, index, type, dataPath);
	}

	/**
	 * Constructor which sets index and ip for elasticsearch
	 * 
	 * @param ip
	 *            IP of the server running elasticsearch
	 * @throws UnknownHostException
	 *             The host is unknown
	 */
	public Indexer(String ip, String index, String type, String dataPath) throws UnknownHostException {
		this(ip, defaultPort, index, type, dataPath);
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
	public Indexer(String ip, int port, String index, String type, String dataPath) throws UnknownHostException {
		client = new PreBuiltTransportClient(Settings.EMPTY)
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), port));
		logger.info("Set target host and port to " + ip + ":" + port);
		
		this.index = index;
		this.type = type;
		this.dataPath = dataPath;
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

	public void processFiles() {
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
	}

	private void updateTopics(JSONObject jsonData) {
		JSONArray topicsArray = (JSONArray) jsonData.get(Config.TOPICS);
		if (topicsArray == null) {
			jsonData.put(Config.TOPICS, "");
			return;
		}
		StringJoiner categoriesStringBuilder = new StringJoiner(Config.stringDelimiter);
		for (Object topic : topicsArray) {
			categoriesStringBuilder.add((String) topic);
		}
		jsonData.put(Config.TOPICS, categoriesStringBuilder.toString());
	}

	private void updateQuestion(JSONObject jsonData) {
		String question = (String) jsonData.get(Config.QUESTION) + Config.stringDelimiter
				+ (String) jsonData.get(Config.DESCRIPTION);
		jsonData.put(Config.QUESTION, question);
		jsonData.remove(Config.DESCRIPTION);
	}

}

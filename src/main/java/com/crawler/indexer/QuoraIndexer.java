package com.crawler.indexer;

import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.crawler.customutil.Config;

public class QuoraIndexer {
	
	private Logger logger = Logger.getLogger(QuoraIndexer.class);
	private TransportClient client;
	private String index = "quora";
	private final String defaultHost = "localhost";
	private final int defaultPort = 9300;
	private final static String dataPath = "D:/Intern/QuoraData/QuoraData/www.quora.com/";
	
	/**
     * Constructor which sets index for elasticsearch
     * @throws UnknownHostException The host is unknown
     */
    public QuoraIndexer() throws UnknownHostException {
        client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(defaultHost), defaultPort));
        logger.info("Set target host and port to " + defaultHost + ":" + defaultPort);
    }

    /**
     * Constructor which sets index and ip for elasticsearch
     * @param ip IP of the server running elasticsearch
     * @throws UnknownHostException The host is unknown
     */
    public QuoraIndexer(String ip) throws UnknownHostException {
        client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), defaultPort));
        logger.info("Set target host and port to " + ip + ":" + defaultPort);
    }

    /**
     * Constructor which sets index, ip, and port for elasticsearch
     * @param ip IP of the server running elasticsearch
     * @param port Port of the elasticsearch service
     * @throws UnknownHostException The host is unknown
     */
    public QuoraIndexer(String ip, int port) throws UnknownHostException {
        client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), port));
        logger.info("Set target host and port to " + ip + ":" + port);
    }
    
    /*
    public void addAnnotation(String id, String annotation) {
        // build index request
        Map<String, String> indexRequest = new HashMap<>();
        indexRequest.put(field, annotation);

        // send request
        IndexResponse response = client.prepareIndex(index, type, id)
                .setSource(indexRequest)
                .get();

        if (response.status().getStatus() == 201) {
            logger.info("Added annotation " + id);
        } else {
            logger.info("Failed to add annotation " + id);
        }
    }*/
    
    /**
     * Closes the client connection
     */
    public void closeConnection() {
        client.close();
    }
    
    public static void main(String[] args) {
    	readFiles();
    }
    
    private static void readFiles() {
    	JSONParser parser = new JSONParser();
    	
    	try (Stream<Path> filePathStream=Files.walk(Paths.get(dataPath))) {
    	    filePathStream.forEach(filePath -> {
    	        if (!Files.isRegularFile(filePath)) {
    	            return;
    	        }
    	        try {
					JSONObject jsonData = (JSONObject) parser.parse(new FileReader(filePath.toString()));
					updateJson(jsonData);
					IndexResponse response = new QuoraIndexer().client.prepareIndex("q&a", "quora")
					        .setSource(jsonData)
					        .get();
					System.out.println(response);
				} catch (IOException | ParseException e) {
					e.printStackTrace();
					System.out.println(filePath.toString());
				}
    	    });
    	} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private static void updateJson(JSONObject jsonData) {
    	updateTopics(jsonData);
    	updateQuestion(jsonData);
    	updateAnswers(jsonData);
    }
    
    private static void updateTopics(JSONObject jsonData) {
    	JSONArray categoriesArray = (JSONArray) jsonData.get(Config.CATEGORIES);
    	jsonData.remove(Config.CATEGORIES);
    	
    	if (categoriesArray == null) {
    		jsonData.put(Config.TOPICS, "");
    		return;
    	}
    	StringJoiner categoriesStringBuilder = new StringJoiner(Config.stringDelimiter);
    	for (Object category: categoriesArray) {
    		categoriesStringBuilder.add((String)category);
    	}
    	jsonData.put(Config.TOPICS, categoriesStringBuilder.toString());
    }
    
    private static void updateQuestion(JSONObject jsonData) {
    	String question = (String) jsonData.get(Config.QUESTION) + Config.stringDelimiter + (String) jsonData.get(Config.DESCRIPTION);
    	jsonData.put(Config.QUESTION, question);
    	jsonData.remove(Config.DESCRIPTION);
    }
    
    private static void updateAnswers(JSONObject jsonData) {
    	JSONArray answers = (JSONArray) jsonData.get(Config.ANSWERS);
    	if (answers == null) {
    		return;
    	}
    	for (Object answer: answers) {
    		JSONObject answerObject = (JSONObject) answer;
    		try {
    			answerObject.put(Config.VOTE, Integer.parseInt((String)answerObject.get(Config.VOTE)));
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    }

}

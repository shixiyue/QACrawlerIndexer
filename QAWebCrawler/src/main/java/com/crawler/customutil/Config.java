package com.crawler.customutil;

import us.codecraft.webmagic.Site;

public class Config {
	
	// Parameters for JSON format
	public final static String ANSWER = "answer";
	public final static String VOTE = "vote";
	public final static String URL = "url";
	public final static String QUESTION = "question";
	public final static String DESCRIPTION = "description";
	public final static String ANSWERS = "answers";
	public final static String TOPICS = "topics";
	
	// Settings
	public final static String dataPath = "/crawl";
	public final static String fileCachePath = "src/main/resources/filecachepath/";
	public final static String seleniumPath = "src/main/resources/chromedriver";
	public final static int numOfThread = 5;
	public final static int numOfExpectedData = 50000000;
	public final static double falseRate = 0.01;
	
	// Crawler configuration
	public final static Site site = Site.me().setCycleRetryTimes(5).setRetryTimes(5).setSleepTime(1000).setTimeOut(3 * 60 * 1000)
            .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3").setCharset("UTF-8");

}

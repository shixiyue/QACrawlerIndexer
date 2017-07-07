package com.crawler.customprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.management.JMException;

import com.crawler.customutil.Config;
import com.crawler.customutil.CustomJsonFilePipeline;
import com.crawler.customutil.PersistentBloomFilter;

import net.htmlparser.jericho.Source;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;

public abstract class CustomPageProcessor implements PageProcessor {

	/**
	 * Finds urls from the page related question field.
	 */
	protected abstract List<String> getRelatedUrls(Page page);

	/**
	 * Extracts useful content (url, question, description, topics and answer
	 * list) from the page.
	 */
	protected abstract void processContent(Page page);

	@Override
	public Site getSite() {
		return Config.site;
	}

	/**
	 * Processes the page.
	 */
	@Override
	public void process(Page page) {
		addRelatedQuestionsUrls(page);
		processContent(page);
	}

	/**
	 * Adds urls of related questions to the queue to be crawled.
	 */
	protected void addRelatedQuestionsUrls(Page page) {
		page.addTargetRequests(getRelatedUrls(page));
	}

	protected void putPageFields(Page page, String url, String question, String description, List<String> topics,
			ArrayList<HashMap<String, Object>> answerList) {
		if (shouldSkip(question, answerList)) {
			page.setSkip(true);
			return;
		}

		page.putField(Config.URL, url);
		page.putField(Config.QUESTION, question);
		page.putField(Config.DESCRIPTION, description);
		page.putField(Config.TOPICS, topics);
		page.putField(Config.ANSWERS, answerList);
	}

	/**
	 * Returns true when the page is not useful and should be skipped, i.e. the
	 * question is empty or the list of useful answers is empty.
	 */
	private boolean shouldSkip(String question, ArrayList<HashMap<String, Object>> answerList) {
		return question.isEmpty() || answerList.isEmpty();
	}

	/**
	 * Extracts all text from the given html text
	 */
	protected String extractAllText(String htmlText) {
		Source source = new Source(htmlText);
		return source.getTextExtractor().toString();
	}
	
	/**
	 * Parses votesText, which is in the format "XX,XXX Upvotes".
	 * 
	 * @return Integer an integer representation of the number of votes
	 */
	protected Integer formatVote(String votesText) {
		return Integer.parseInt(votesText.split(" ")[0].replaceAll(",", ""));
	}

	/**
	 * Runs the spider Docs:
	 * http://webmagic.io/docs/en/posts/ch6-custom-componenet/pipeline.html
	 * 
	 * @throws JMException
	 */
	public static void run(PageProcessor pageProcessor, String initialUrl, String bloomObjPath) throws JMException {
		run(pageProcessor, initialUrl, bloomObjPath, new SeleniumDownloader(Config.seleniumPath));
	}

	public static void run(PageProcessor pageProcessor, String initialUrl, String bloomObjPath,
			Downloader seleninumDownloader) throws JMException {
		final PersistentBloomFilter pbf = new PersistentBloomFilter(Config.numOfExpectedData, Config.falseRate,
				bloomObjPath);
		Spider spider = Spider.create(pageProcessor).addUrl(initialUrl)
				.addPipeline(new CustomJsonFilePipeline(Config.dataPath)).setDownloader(seleninumDownloader)
				.thread(Config.numOfThread)
				.setScheduler(new FileCacheQueueScheduler(Config.fileCachePath).setDuplicateRemover(pbf));

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				pbf.storeBloomFilter();
			}
		}));

		spider.run();
		SpiderMonitor.instance().register(spider);
	}

}

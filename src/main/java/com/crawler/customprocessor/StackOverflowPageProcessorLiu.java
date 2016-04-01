package com.crawler.customprocessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import net.htmlparser.jericho.Source;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;

public class StackOverflowPageProcessorLiu implements PageProcessor {
	private Site site = Site
			.me()
			.setCycleRetryTimes(5)
			.setRetryTimes(5)
			.setSleepTime(1000)
			.setTimeOut(3 * 60 * 1000)
			.setUserAgent(
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0")
			.addHeader("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
			.addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
			.setCharset("UTF-8");

	public static final String URL_LIST = "http://stackoverflow\\.com/questions\\?page\\=\\d+";
	public static final String URL_POST = "";
	static List<String> urls = new ArrayList<String>();
	static PrintWriter pw;
	static boolean flag = false;

	public void process(Page page) {
		// String
		// page.addTargetRequests();
		// Extract the contents
		String reputation = page.getHtml()
				.xpath("//div[@class='reputation']/text()").toString();
		System.out.println(page.getUrl().toString());
		if (reputation == null)
			reputation = "Page Not Found";
		System.out.println(reputation.trim());
		pw.println(page.getUrl().toString());
		pw.println(reputation.trim());
		pw.flush();
		//page.addTargetRequest(urls.get(count));

	}

	/**
	 *
	 * @param htmlText
	 * @return
	 */
	public String extractAllText(String htmlText) {
		Source source = new Source(htmlText);
		return source.getTextExtractor().toString();
	}

	public Site getSite() {
		return site;
	}

	public static void main(String args[]) throws FileNotFoundException {
		Scanner scan = new Scanner(new File("/Users/sesame/Developer/QAWebCrawler/src/main/resources/userlist.txt"));
		pw = new PrintWriter("reputation.txt");
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			line = line.substring(0, line.length() - 2);
			urls.add("http://stackoverflow.com/users/" + line);
		}
		Spider stackOverFlowSpider = Spider
				.create(new StackOverflowPageProcessorLiu());
				
				
		for(int i=0;i< urls.size();i++){
			stackOverFlowSpider.addUrl(urls.get(i));
		}	
		stackOverFlowSpider.addPipeline(
						new FilePipeline(
								"/Users/sesame/Downloads/crawlresult"))
				.setDownloader(
						new SeleniumDownloader(
								"src/main/resources/chromedriver"))
				.thread(10)
				.setScheduler(
						new FileCacheQueueScheduler(
								"/Users/sesame/Downloads/crawlresult"));

		stackOverFlowSpider.run();
		pw.flush();
		pw.close();
	}
}

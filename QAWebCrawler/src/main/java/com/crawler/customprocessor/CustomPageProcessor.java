package com.crawler.customprocessor;

import javax.management.JMException;

import com.crawler.customutil.Config;
import com.crawler.customutil.CustomJsonFilePipeline;
import com.crawler.customutil.PersistentBloomFilter;

import net.htmlparser.jericho.Source;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;

public abstract class CustomPageProcessor implements PageProcessor {

	@Override
	public Site getSite() {
		return Config.site;
	}
	
	/**
	 * Extracts all text from the given html text
	 */
	public String extractAllText(String htmlText) {
		Source source = new Source(htmlText);
		return source.getTextExtractor().toString();
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
	
	public static void run(PageProcessor pageProcessor, String initialUrl, String bloomObjPath, Downloader seleninumDownloader) throws JMException {
		final PersistentBloomFilter pbf = new PersistentBloomFilter(Config.numOfExpectedData, Config.falseRate,
				bloomObjPath);
		Spider spider = Spider.create(pageProcessor).addUrl(initialUrl)
				.addPipeline(new CustomJsonFilePipeline(Config.dataPath))
				.setDownloader(seleninumDownloader).thread(Config.numOfThread)
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

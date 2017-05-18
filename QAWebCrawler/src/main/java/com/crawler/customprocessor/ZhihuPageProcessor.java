package com.crawler.customprocessor;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.scheduler.component.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.selector.Html;

import java.io.File;
import java.util.List;

import javax.management.JMException;

import com.crawler.customutil.Config;
import com.crawler.customutil.CustomJsonFilePipeline;
import com.crawler.customutil.PersistentBloomFilter;

public class ZhihuPageProcessor implements PageProcessor {

    @Override
    public void process(Page page) {
    	
    	System.out.println(page.getUrl().toString());
    	
        List<String> relativeUrl = page.getHtml().xpath("//li[@class='item clearfix']/div/a/@href").all();
        page.addTargetRequests(relativeUrl);
        relativeUrl = page.getHtml().xpath("//div[@id='zh-question-related-questions']//a[@class='question_link']/@href").all();
        page.addTargetRequests(relativeUrl);
        String url = page.getUrl().toString();
        String question = page.getHtml().xpath("//h2[@class='zm-item-title zm-editable-content']/text()").toString();
        String description = page.getHtml().xpath("//div[@class='zm-editable-content']/div/text()").toString();
        List<String> categories = page.getHtml().xpath("//").all();
//        String answerNo = page.getHtml().xpath("//h3[@id='zh-question-answer-num']/text()").toString();
        List<String> answers = page.getHtml().xpath("//div[@class='zm-editable-content clearfix']").all();

        page.putField("url", url);
        page.putField("question", question);
        page.putField("description", description);
//        page.putField("answerNo", answerNo);

        int count = 1;
        for(String answer:answers) {
            String num = "answer" + count;
            page.putField(num, new Html(answer).xpath("//div[@class='zm-editable-content clearfix']/text()").toString());
            count++;
        }

        if (answers.size() == 0 || answers.size() == 1) {
            page.setSkip(true);
        }
        System.out.println(question);
    }

    @Override
    public Site getSite() {
        return Config.site;
    }

    /**
	 * The Spider starts from links given in
	 * "src\main\resources\filecachepath\www.quora.com.urls.txt" and find more
	 * pages to crawl through the Quora related question field. New links will
	 * be added to the txt file. Those pages will be downloaded, processed and
	 * output as json files.
	 * 
	 * Note: "www.quora.com.urls - Copy.txt" contains links of 150 popular
	 * questions that are evenly distributed in 30 most popular topics (i.e. we
	 * select 5 questions from each topic), based on the list in
	 * https://www.quora.com/What-are-the-most-followed-topics-on-Quora-2 Those
	 * links are chosen for initial links.
	 *
	 * @param args
	 * @throws JMException
	 */
	public static void main(String[] args) throws JMException {
		final String bloomObjPath = "src/main/resources/bloompath/zhihu/bloom.ser";
		String fileCachePath = "src/main/resources/";
		String seleniumPath = "src/main/resources/chromedriver";
		// A dummy placeholder URL.
		String url = "https://www.zhihu.com/question/20696837";
		int numOfExpectedData = 50000000;
		double falseRate = 0.01;
		final PersistentBloomFilter pbf;

		File file = new File(bloomObjPath);
		if (file.exists() && file.isFile()) {
			pbf = new PersistentBloomFilter(numOfExpectedData, falseRate, bloomObjPath);
			System.out.println("Bloom Filter Object exists in the path, loading it to current process...");
		} else {
			pbf = new PersistentBloomFilter(numOfExpectedData, falseRate);
			System.out.println("Bloom Filter Object does not exist, preparing a new one...");
		}

		// Docs:
		// http://webmagic.io/docs/en/posts/ch6-custom-componenet/pipeline.html
		Spider quoraSpider = Spider.create(new ZhihuPageProcessor()).addUrl(url)
				.addPipeline(new CustomJsonFilePipeline("/123")).setDownloader(new SeleniumDownloader(seleniumPath))
				.thread(1).setScheduler(new FileCacheQueueScheduler(fileCachePath).setDuplicateRemover(pbf));

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				pbf.storeBloomFilter(bloomObjPath);
			}
		}));

		quoraSpider.run();

		SpiderMonitor.instance().register(quoraSpider);
	}
	
}
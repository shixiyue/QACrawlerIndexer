package com.crawler.customprocessor;

import java.util.ArrayList;
import java.util.List;
import com.crawler.custompipeline.QuoraTopicPipeline;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;
import us.codecraft.webmagic.scheduler.component.BloomFilterDuplicateRemover;

public class QuoraTopicProcessor implements PageProcessor {
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

    public static final String URL_LIST = "https://www\\.quora\\.com/sitemap/topics\\?page_id\\=\\d+";
    public static final String URL_POST = "https://www\\.quora\\.com/topic/\\w+";

    public void process(Page page) {

        if (page.getUrl().regex(URL_LIST).match()) {
            //add next list to the request queue
            page.addTargetRequests(page.getHtml().links().regex(URL_LIST).all());
            page.addTargetRequests(page.getHtml().xpath("//div[@class='ContentWrapper']").links().regex(URL_POST).all());
            page.setSkip(true);

        } else {

            //Extract the content
            String topic = page.getHtml()
                    .xpath("//h1//span[@class='TopicNameSpan TopicName']/text()")
                    .toString();

            List<String> relatedTopics = page.getHtml()
                        .xpath("//div[@class='section_wrapper']//a[@class='RelatedTopicsListItem HoverMenu']//span/text()")
                        .all();

            if (relatedTopics.size()==0) {
                page.setSkip(true);
            }

            page.putField("topic", topic);
            page.putField("relatedtopics", relatedTopics);

        }

    }

    @Override
    public Site getSite(){return site;}

    public static void main(String args[]) {

        String bloomPath = "src/main/resources/bloompath/bloom.obj";
        String fileCachePath = "src/main/resources/filecachepath/";
        String seleniumPath = "src/main/resources/chromedriver";
        String url = "https://www.quora.com/sitemap/topics?page_id=1506";
        Spider quoraTopicSpider = Spider
                .create(new QuoraTopicProcessor())
                .addUrl(url)
                .addPipeline(new QuoraTopicPipeline())
                .setDownloader(new SeleniumDownloader(seleniumPath))
                .thread(3)
                .setScheduler(new FileCacheQueueScheduler(fileCachePath).setDuplicateRemover(new BloomFilterDuplicateRemover(50000000)));

        quoraTopicSpider.run();

    }
}
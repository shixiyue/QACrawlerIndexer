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

import java.util.List;

import javax.management.JMException;

public class ZhihuPageProcessor implements PageProcessor {

    private Site site = Site.me().setCycleRetryTimes(5).setRetryTimes(5).setSleepTime(1000).setTimeOut(3 * 60 * 1000)
            .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3").setCharset("UTF-8");

    @Override
    public void process(Page page) {
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
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) throws JMException {
        Spider zhihuSpider = Spider.create(new ZhihuPageProcessor()).addUrl("https://www.zhihu.com/question/20696837")
                //.addPipeline(new ZhihuPipeline())
                .setDownloader(new SeleniumDownloader("src/main/resources/chromedriver"))
                .thread(5)
                .setScheduler(new FileCacheQueueScheduler("/Users/Beibei/downloads/crawlresult").setDuplicateRemover(new BloomFilterDuplicateRemover(10000000)));

        SpiderMonitor.instance().register(zhihuSpider);

        zhihuSpider.start();

    }
}
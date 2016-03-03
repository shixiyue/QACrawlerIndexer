package com.crawler.customprocessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.htmlparser.jericho.Source;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;
import us.codecraft.webmagic.scheduler.component.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.selector.Html;

public class StackOverflowPageProcessor implements PageProcessor {
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

    public void process(Page page) {

        if (page.getUrl().regex(URL_LIST).match()) {
            page.addTargetRequests(page
                    .getHtml()
                    .xpath("//div[@class='question-summary']//a[@class='question-hyperlink']/@href")
                    .all());

            page.addTargetRequests(page
                    .getHtml()
                    .xpath("//div[@class='pager fl']/a/@href")
                    .all());

        } else {
            //Extract the contents
            String question = page.getHtml()
                    .xpath("//div[@id='question-header']/h1/a/text()")
                    .toString();

            String description = page.getHtml()
                    .xpath("//div[@class='question']//div[@class='post-text']")
                    .toString();

            String ansNum = page.getHtml()
                    .xpath("//div[@id='answers-header']/div/h2/text()")
                    .toString();

            List<String> answers = page.getHtml()
                    .xpath("")
                    .all();

            List<String> comments = page.getHtml()
                    .xpath("")
                    .all();

            //Page skip rule
            if (question.equals(null) || ansNum.length() == 1) {
                page.setSkip(true);
            }
            page.putField("question", question);

            //description post-processing
            String desText = "";

            if (description.equals(null)) {
                desText = extractAllText(description);
            }
            page.putField("description", desText);

            //Answers post-processing
            int count = 1;

            for (String answer : answers) {
                String num = "answer" + count;
                String answerText = extractAllText(new Html(answer).xpath(
                        "").toString());
                page.putField(num, answerText);
                count++;
            }

            //Comments post-processing
            count = 1;
            int innerCont = 1;

        }

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

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String args[]) {
        Spider stackOverFlowSpider = Spider
                .create(new StackOverflowPageProcessor())
                .addUrl("http://stackoverflow.com/questions?page=1&sort=newest")
                .addPipeline(
                        new JsonFilePipeline(
                                "/Users/sesame/downloads/stackoverflowcrawlresult"))
                .setDownloader(
                        new SeleniumDownloader(
                                "src/main/resources/chromedriver"))
                .thread(1)
                .setScheduler(
                        new FileCacheQueueScheduler(
                                "/Users/sesame/downloads/stackoverflow")
                                .setDuplicateRemover(new BloomFilterDuplicateRemover(
                                        20000000)));

        stackOverFlowSpider.run();
    }
}

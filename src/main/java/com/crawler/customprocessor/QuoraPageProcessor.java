package com.crawler.customprocessor;

import com.crawler.customutil.Indexer;
import com.crawler.customutil.PersistentBloomFilter;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;
import us.codecraft.webmagic.selector.Html;

import java.io.File;
import java.util.List;

import javax.management.JMException;

import com.crawler.custompipeline.QuoraPipeline;

import net.htmlparser.jericho.Source;

public class QuoraPageProcessor implements PageProcessor {

    private Site site = Site
            .me()
            .setCycleRetryTimes(5)
            .setRetryTimes(5)
            .setSleepTime(1000)
            .setTimeOut(5 * 60 * 1000)
            .setUserAgent(
                    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0")
            .addHeader("Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
            .setCharset("UTF-8");

//    private static Indexer indexer = null;

    /**
     *
     * @param page
     */
    @Override
    public void process(Page page) {

        List<String> relativeUrl = page
                .getHtml()
                .xpath("//li[@class='related_question']//a[@class='question_link']/@href")
                .all();
        page.addTargetRequests(relativeUrl);

        String url = page.getUrl().toString();
        String question = page.getHtml()
                .xpath("//h1//span[@class='rendered_qtext']/text()").toString();
        List<String> categories = page.getHtml().xpath("//div[@class='QuestionTopicListItem TopicListItem topic_pill']/div/a//span[@class='TopicNameSpan TopicName']/text()").all();
        String description = page
                .getHtml()
                .xpath("//div[@class='question_details']//span[@class='rendered_qtext']/text()")
                .toString();
        List<String> answers = page
                .getHtml()
                .xpath("//div[@class='pagedlist_item']//div[@class='ExpandedQText ExpandedAnswer']/span")
                .all();
        page.putField("url", url);
        page.putField("categories", categories);
        page.putField("question", question);
        page.putField("description", description);

        if (answers.size() == 0 || question.equals("")) {
            page.setSkip(true);
        }

        int count = 1;
        for (String answer : answers) {
            String num = "answer" + count;
            String answerText = extractAllText(new Html(answer).xpath(
                    "//span[@class='rendered_qtext']").toString());
            page.putField(num, answerText);
            count++;
        }

//        try {
//            indexer.createIndex(answers, question, categories, url);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    public String extractAllText(String htmlText) {
        Source source = new Source(htmlText);
        return source.getTextExtractor().toString();
    }

    @Override
    public Site getSite() {
        return site;
    }

    /**
     * This Spider starts from a random link and crawl the page through the
     * quora related question field. For next step, a key word based crawler.
     * You can use： scheduler.push(new
     * Request("www.quora.com/topic/arg0-arg1-arg2-arg3")) where args form the
     * keyword phrase
     *
     * @param args
     * @throws JMException
     */
    public static void main(String[] args) throws JMException {

        final String bloomObjPath = "src/main/resources/bloompath/bloom.ser";
        String fileCachePath = "src/main/resources/filecachepath/";
        String seleniumPath = "src/main/resources/chromedriver";
        String url = "https://www.quora.com/I-wanna-study-hard-but-I-cant-how-can-I-motivate-myself-for-that";
        int numOfExpectedData = 50000000;
        double falseRate = 0.01;
        final PersistentBloomFilter pbf;

        File file = new File(bloomObjPath);
        if(file.exists() && file.isFile()) {
            pbf = new PersistentBloomFilter(numOfExpectedData, falseRate, bloomObjPath);
            System.out.println("Bloom Filter Object exists in the path, loading it to current process...");
        } else {
            pbf = new PersistentBloomFilter(numOfExpectedData, falseRate);
            System.out.println("Bloom Filter Object does not exist, preparing a new one...");
        }

        Spider quoraSpider = Spider
                .create(new QuoraPageProcessor())
                .addUrl(url)
                .addPipeline(new QuoraPipeline())
                .setDownloader(new SeleniumDownloader(seleniumPath))
                .thread(5)
                .setScheduler(new FileCacheQueueScheduler(fileCachePath).setDuplicateRemover(pbf));

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                pbf.storeBloomFilter(bloomObjPath);
//                try {
//                    indexer.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }));

//        indexer = new Indexer("/Users/sesame/Downloads/indexpath/");

        quoraSpider.run();

        SpiderMonitor.instance().register(quoraSpider);
    }
}

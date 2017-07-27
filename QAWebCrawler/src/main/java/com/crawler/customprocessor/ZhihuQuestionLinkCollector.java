package com.crawler.customprocessor;

import us.codecraft.webmagic.Page;

import java.util.List;

import javax.management.JMException;

/**
 * Represents a link collector that will collect links for all questions under a
 * given topic.
 */
public class ZhihuQuestionLinkCollector extends CustomPageProcessor {

	public ZhihuQuestionLinkCollector() {
		shouldProcessContent = false;
		shouldAddQuestionUrls = true;
	}

	@Override
	public List<String> getRelatedUrls(Page page) {
		return page.getHtml().xpath("//a[@class='question_link']/@href").all();
	}

	@Override
	public void processContent(Page page) {
		throw new UnsupportedOperationException("Invalid operation for Question Link Collector.");
	}

	public static void main(String[] args) throws JMException {
		final String bloomObjPath = "src/main/resources/bloompath/zhihu/bloom.ser";
		String initialUrl = "https://www.zhihu.com/question"; // A dummy
																// placeholder
																// URL.

		run(new ZhihuQuestionLinkCollector(), initialUrl, bloomObjPath);
	}

}
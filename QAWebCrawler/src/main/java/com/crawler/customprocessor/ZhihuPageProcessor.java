package com.crawler.customprocessor;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.management.JMException;

import com.crawler.customutil.Config;
import com.crawler.customutil.CustomZhihuSeleniumDownloader;

public class ZhihuPageProcessor extends CustomPageProcessor {
	
	public ZhihuPageProcessor() {
		shouldProcessContent = true;
		shouldAddQuestionUrls = false;
	}

	@Override
	public List<String> getRelatedUrls(Page page) {
		return page.getHtml().xpath("//div[@class='SimilarQuestions-item']/a/@href").all();
	}

	@Override
	public void processContent(Page page) {
		String url = page.getUrl().toString();
		String question = page.getHtml().xpath("//h1[@class='QuestionHeader-title']/text()").toString();
		String description = page.getHtml()
				.xpath("//div[@class='QuestionRichText QuestionRichText--expandable']//span/text()").toString();
		List<String> topics = page.getHtml()
				.xpath("//div[@class='Tag QuestionTopic']//div[@class='Popover']/div/text()").all();
		ArrayList<HashMap<String, Object>> answerList = getAnswerList(page);

		putPageFields(page, url, question, description, topics, answerList);
	}

	/**
	 * Gets answers and their votes from the web page.
	 * 
	 * @return ArrayList<HashMap<String, Object>> answerList
	 */
	private ArrayList<HashMap<String, Object>> getAnswerList(Page page) {
		List<String> answers = page.getHtml()
				.xpath("//div[@class='ContentItem AnswerItem']//div[@class='RichContent-inner']").all();
		List<String> votes = page.getHtml()
				.xpath("//div[@class='ContentItem-actions']//button[@class='Button VoteButton VoteButton--up']/text()")
				.all();
		ArrayList<HashMap<String, Object>> answerList = new ArrayList<HashMap<String, Object>>();

		for (int i = 0; i < answers.size(); i++) {
			String votesText = votes.get(i).toString();

			int vote;
			if (votesText.equals("0")) { // i.e the answer does not have any vote
				// then we consider the answer not useful and don't store it.
				// (There's no negative vote in Quora)
				continue;
			} else {
				vote = formatVote(votesText);
			}
			List<String> paragraphs = new Html(answers.get(i)).xpath("//p//text()|//b/text()|//span/text()").all();
			String answerText = String.join("", paragraphs);

			HashMap<String, Object> answer = new HashMap<String, Object>();
			answer.put(Config.VOTE, vote);
			answer.put(Config.ANSWER, answerText);
			answerList.add(answer);
		}
		return answerList;
	}
	
	/**
	 * Parses votesText, which is in the format "XX,XXX Upvotes".
	 * 
	 * @return Integer an integer representation of the number of votes
	 */
	private Integer formatVote(String votesText) {
		if (votesText.endsWith("K")) {
			return (int) (Double.parseDouble(votesText.substring(0, votesText.length() - 1)) * 1000);
		} else {
			return Integer.parseInt(votesText);
		}
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
	 * https://zhuanlan.zhihu.com/p/21395286 Those links are chosen for seed
	 * links.
	 *
	 * @param args
	 * @throws JMException
	 */
	public static void main(String[] args) throws JMException {
		final String bloomObjPath = "src/main/resources/bloompath/zhihu/bloom.ser";
		// A dummy placeholder URL.
		String initialUrl = "https://www.zhihu.com/question/35005800";
		run(new ZhihuPageProcessor(), initialUrl, bloomObjPath, new CustomZhihuSeleniumDownloader(Config.seleniumPath));
	}

}
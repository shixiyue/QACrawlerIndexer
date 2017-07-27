package com.crawler.customprocessor;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.management.JMException;

import com.crawler.customutil.Config;
import com.crawler.customutil.CustomZhihuSeleniumDownloader;

/**
 * Represents a page processor that can process web pages of Zhihu. It uses the
 * crawler framework WebMagic.
 */
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
			if (votesText.equals("0")) { // i.e the answer does not have any
											// vote
				// then we consider the answer not useful and don't store it.
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
	 * Parses votesText, which is in the format "XXK (the K is optional)
	 * Upvotes".
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
	 * "src\main\resources\filecachepath\www.zhihu.com.urls.txt".
	 * 
	 * The method that collect more links through Zhihu related question field
	 * is not useful, as we can only get limited questions (less than 5k)
	 * through this method. Thus, we need to provide all links at the start. We
	 * can collect all questions under a specific topic through: 1. first run
	 * resources/filecachepath/generate_url.py 2. then run
	 * ZhihuQuestionLinkCollector
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
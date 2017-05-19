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
	
	/**
	 * Processes the page.
	 */
	@Override
	public void process(Page page) {
		addRelatedQuestionsUrls(page);
		extractContent(page);
	}

	/**
	 * Finds urls from the Quora related question field and adds them to the
	 * queue to be crawled.
	 */
	private void addRelatedQuestionsUrls(Page page) {
		List<String> relativeUrl = page.getHtml().xpath("//div[@class='SimilarQuestions-item']/a/@href").all();
        page.addTargetRequests(relativeUrl);
	}

	/**
	 * Extracts useful content (url, question, description, topics and answer
	 * list) from the page.
	 */
	private void extractContent(Page page) {
		String url = page.getUrl().toString();
		String question = page.getHtml().xpath("//h1[@class='QuestionHeader-title']/text()").toString();
		String description = page.getHtml().xpath("//div[@class='QuestionRichText QuestionRichText--expandable']//span/text()").toString();
		List<String> topics = page.getHtml()
				.xpath("//div[@class='Tag QuestionTopic']//div[@class='Popover']/div/text()")
				.all();
		ArrayList<HashMap<String, Object>> answerList = getAnswerList(page);

		page.putField(Config.URL, url);
		page.putField(Config.TOPICS, topics);
		page.putField(Config.QUESTION, question);
		page.putField(Config.DESCRIPTION, description);
		page.putField(Config.ANSWERS, answerList);

		if (shouldSkip(question, answerList)) {
			page.setSkip(true);
		}
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
				.xpath("//div[@class='ContentItem-actions']//button[@class='Button VoteButton VoteButton--up']/text()").all();
		System.out.println(votes);
		ArrayList<HashMap<String, Object>> answerList = new ArrayList<HashMap<String, Object>>();

		for (int i = 0; i < answers.size(); i++) {
			String votesText = votes.get(i).toString();
			System.out.println(votesText);
			int vote;
			if (votesText == null) { // i.e the answer does not have any vote
				// then we consider the answer not useful and don't store it.
				// (There's no negative vote in Quora)
				continue;
			} else {
				vote = formatVote(votesText);
			}
			String answerText = new Html(answers.get(i))
					.xpath("//span[@class='RichText CopyrightRichText-richText']/text()").toString();
			System.out.println(answerText);

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
		return Integer.parseInt(votesText.split(" ")[0].replaceAll(",", ""));
	}

	/**
	 * Returns true when the page is not useful and should be skipped, i.e. the
	 * question is empty or the list of useful answers is empty.
	 */
	private boolean shouldSkip(String question, ArrayList<HashMap<String, Object>> answerList) {
		return question.isEmpty() || answerList.isEmpty();
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
	 * https://zhuanlan.zhihu.com/p/21395286
	 * Those links are chosen for seed links.
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
package com.crawler.customprocessor;

import com.crawler.customutil.Config;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.management.JMException;

/**
 * Represents a page processor that can process web pages of Quora. It uses the
 * crawler framework WebMagic.
 */
public class QuoraPageProcessor extends CustomPageProcessor {

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
		List<String> relatedUrl = page.getHtml()
				.xpath("//li[@class='related_question']//a[@class='question_link']/@href").all();
		page.addTargetRequests(relatedUrl);
	}

	/**
	 * Extracts useful content (url, question, description, topics and answer
	 * list) from the page.
	 */
	private void extractContent(Page page) {
		String url = page.getUrl().toString();
		String question = page.getHtml().xpath("//h1//span[@class='rendered_qtext']/text()").toString();
		String description = page.getHtml()
				.xpath("//div[@class='question_details']//span[@class='rendered_qtext']/text()").toString();
		List<String> topics = page.getHtml()
				.xpath("//div[@class='QuestionTopicListItem TopicListItem topic_pill']/div/a//span[@class='TopicNameSpan TopicName']/text()")
				.all();
		ArrayList<HashMap<String, Object>> answerList = getAnswerList(page);

		page.putField(Config.URL, url);
		page.putField(Config.QUESTION, question);
		page.putField(Config.DESCRIPTION, description);
		page.putField(Config.TOPICS, topics);
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
				.xpath("//div[@class='pagedlist_item']//div[@class='ExpandedQText ExpandedAnswer']/span").all();
		List<String> votes = page.getHtml()
				.xpath("//div[@class='pagedlist_item']//div[@class='ExpandedQText ExpandedAnswer']/div").all();
		ArrayList<HashMap<String, Object>> answerList = new ArrayList<HashMap<String, Object>>();

		for (int i = 0; i < answers.size(); i++) {
			String votesText = new Html(votes.get(i))
					.xpath("//a[@class='AnswerVoterListModalLink VoterListModalLink']/text()").toString();
			int vote;
			if (votesText == null) { // i.e the answer does not have any vote
				// then we consider the answer not useful and don't store it.
				// (There's no negative vote in Quora)
				continue;
			} else {
				vote = formatVote(votesText);
			}
			String answerText = extractAllText(
					new Html(answers.get(i)).xpath("//span[@class='rendered_qtext']").toString());

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
	 * https://www.quora.com/What-are-the-most-followed-topics-on-Quora-2 Those
	 * links are chosen for seed links.
	 *
	 * @param args
	 * @throws JMException
	 */
	public static void main(String[] args) throws JMException {
		final String bloomObjPath = "src/main/resources/bloompath/quora/bloom.ser";
		// A dummy placeholder URL.
		String initialUrl = "https://www.quora.com/I-wanna-study-hard-but-I-cant-how-can-I-motivate-myself-for-that";
		run(new QuoraPageProcessor(), initialUrl, bloomObjPath);
	}
}

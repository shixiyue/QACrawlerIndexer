import json
import xml.etree.ElementTree as ET
import re
import os
from utility import *
from bs4 import BeautifulSoup

def process_posts():
    all_websites = os.listdir(data_path)
    for website in all_websites:
        data_directory = data_path + website + data_file_name
        parsed_data_directory = parsed_data_path + website + '/'

        read_posts(website, data_directory, parsed_data_directory)
        remove_not_useful_question(parsed_data_directory)

def read_posts(website, data_directory, parsed_data_directory):
    with open(data_directory, mode='r', encoding='utf-8') as posts:
        os.makedirs(os.path.dirname(parsed_data_directory), exist_ok=True)
        skip_first_two_rows(posts)
        for line in posts:
            try:
                xml_line = ET.fromstring(line)
                parse_line(xml_line, website)
            except:
                continue

# Parses the line in xml format that belongs to the given website
def parse_line(xml_line, website):
    post_id = xml_line.attrib[ID]
    description = remove_html_tags(xml_line.attrib[BODY])
    type_id = xml_line.attrib[POST_TYPE_ID]
    vote = int(xml_line.attrib[SCORE])
    if type_id == QUESTION_TYPE_ID:
        parse_question(xml_line, website, post_id, description, vote)
    if type_id == ANSWER_TYPE_ID:
        if should_skip_answer(vote):
            return
        parse_answer(xml_line, website, description, vote)

def remove_not_useful_question(parsed_data_directory):
    for entry in os.scandir(parsed_data_directory):
        with open(entry.path, mode='r', encoding='utf-8') as json_data:
            question_answers = json.load(json_data)
        if len(question_answers[ANSWERS]) == 0:
            print(entry.path)
            os.remove(entry.path)

def should_skip_answer(vote):
    return vote < 1

# Parses the content of the given question
def parse_question(xml, website, post_id, description, vote):
    url = LINK_PREFIX + website + LINK_SUFFIX + post_id
    title = xml.attrib[TITLE]
    topics = default_topics(website)
    topics.extend(format_tags(xml.attrib[TAGS]))
    question_answers = {URL: url, TOPICS: topics, QUESTION: title,
        DESCRIPTION: description, VOTE: vote, ANSWERS: []}
    file_name = parsed_data_path + website + '/' + post_id + '.json'
    with open(file_name, mode='w', encoding='utf-8') as json_data:
        json.dump(question_answers, json_data, ensure_ascii=False)

# Parses the content of the given answer
def parse_answer(xml, website, description, vote):
    post_id = xml.attrib[PARENT_ID]
    file_name = parsed_data_path + website + '/' + post_id + '.json'
    with open(file_name, mode='r', encoding='utf-8') as json_data:
        question_answers = json.load(json_data)
    answer = {ANSWER: description, VOTE: vote}
    question_answers[ANSWERS].append(answer)
    with open(file_name, mode='w', encoding='utf-8') as json_data:
        json.dump(question_answers, json_data, ensure_ascii=False)

def skip_first_two_rows(posts):
    next(posts)
    next(posts)

def remove_html_tags(string):
    soup = BeautifulSoup(string, "html.parser")
    return remove_new_line(soup.get_text())

def remove_new_line(string):
    return re.sub( '\s+', ' ', string).strip()

def format_tags(string):
    # Input format: <word1-word2><word1-word2>
    # Output format: ["word1 word2", "word1 word2"]
    if string.startswith('<'):
        string = string[len('<'):-len('>')]
        return [tag.replace('-', ' ') for tag in string.split('><')]
    else:
        return []

process_posts()

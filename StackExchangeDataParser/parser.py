import json
import xml.etree.ElementTree as ET
import re
import os
from utility import *

def read_posts():
    all_websites = filter(lambda filename: filename != ".DS_Store", os.listdir(data_path))
    for website in all_websites:
        with open(data_path + website + data_file_name, mode='r', encoding='utf-8') as posts:
            os.makedirs(os.path.dirname(parsed_data_path + website + '/'), exist_ok=True)
            skip_first_two_rows(posts)
            for line in posts:
                try:
                    xml = ET.fromstring(line)
                    parse_xml(xml, website)
                except:
                    continue

def parse_xml(xml, website):
    post_id = xml.attrib[ID]
    description = remove_new_line(remove_html_tags(xml.attrib[BODY]))
    type_id = xml.attrib[POST_TYPE_ID]
    vote = xml.attrib[SCORE]
    if type_id == QUESTION_TYPE_ID:
        parse_question(xml, website, post_id, description, vote)
    if type_id == ANSWER_TYPE_ID:
        parse_answer(xml, website, description, vote)

def parse_question(xml, website, post_id, description, vote):
    url = LINK_PREFIX + website + LINK_SUFFIX + post_id
    title = xml.attrib[TITLE]
    categories = default_categories(website)
    categories.extend(format_tags(xml.attrib[TAGS]))
    question_answers = {URL: url, CATEGORIES: categories, QUESTION: title,
        DESCRIPTION: description, VOTE: vote, ANSWERS: []}
    file_name = parsed_data_path + website + '/' + post_id + '.json'
    with open(file_name, mode='w', encoding='utf-8') as json_data:
        json.dump(question_answers, json_data, ensure_ascii=False)

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

html_re = re.compile('<[^<]+?>')
def remove_html_tags(string):
     return (re.sub(html_re, '', string)).replace('&nbsp;', '')

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

read_posts()

import json
import xml.etree.ElementTree as ET
import re

LINK_PREFIX = "http://"
LINK_SUFFIX = "questions/"
QUESTION_TYPE_ID = "1"
ANSWER_TYPE_ID = "2"

ID = 'Id'
BODY = 'Body'
POST_TYPE_ID = 'PostTypeId'


html_re = re.compile('<[^<]+?>')

def read_posts(website):
    with open(website + "Posts.xml", mode='r', encoding='utf-8') as posts:
        skip_first_two_rows(posts)
        for line in posts:
            try:
                xml = ET.fromstring(line)
                parse_xml(xml)
            except:
                continue

def parse_xml(xml):
    post_id = xml.attrib[ID]
    description = re.sub(html_re, '', xml.attrib[BODY])
    description = remove_new_line(description)
    type_id = xml.attrib['PostTypeId']
    vote = xml.attrib['Score']
    if type_id == QUESTION_TYPE_ID:
        url = LINK_PREFIX + website + LINK_SUFFIX + post_id
        title = xml.attrib['Title']
        categories = []
        tags = xml.attrib['Tags']
        if tags.startswith('<'):
            tags = tags[len('<'):-len('>')]
            categories = [tag.replace("-", " ") for tag in tags.split("><")]
        categories.append('anime')
        question_answers = {"url": url, "categories": categories, "question": title, "description": description, "vote": vote,
        "answers": []}
        with open(website + 'parsed/' + post_id + '.json', mode='w', encoding='utf-8') as json_data:
            json.dump(question_answers, json_data, ensure_ascii=False)
    if type_id == ANSWER_TYPE_ID:
        post_id = xml.attrib['ParentId']
        with open(website + 'parsed/' + post_id + '.json', mode="r", encoding='utf-8') as json_data:
            question_answers = json.load(json_data)
        answer = {"answer": description, "vote": vote}
        question_answers["answers"].append(answer)
        with open(website + 'parsed/' + post_id + '.json', mode='w', encoding='utf-8') as json_data:
            json.dump(question_answers, json_data, ensure_ascii=False)

def skip_first_two_rows(posts):
    next(posts)
    next(posts)

def remove_new_line(string):
    return string.replace('\n\n', ' ').replace('\n', ' ')

read_posts("anime.stackexchange.com/")

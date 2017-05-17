import json
import os
import re

ANSWERS = 'answers'
VOTE = 'vote'
ANSWER = 'answer'

data_path = "www.quora.com/"

invalid_escape = re.compile(r'\\[0-7]{1,6}')  # up to 6 digits for byte values up to FFFF

def filterAnswers():
    print("Read posts")
    all_data = os.listdir(data_path)

    for data_file_name in all_data:
        file_path = data_path + data_file_name

        with open(file_path, mode='r', encoding='utf-8') as json_data:
            try:
                question_answers = json.loads(repair(json_data.read()), strict=False)
            except Exception as e:
                print(data_file_name)
                print(e)
                continue

        validAnswers = []
        for answer in question_answers[ANSWERS]:
            try:
                if int(answer[VOTE]) > 0:
                    validAnswers.append(answer)
            except Exception as e:
                print(data_file_name)
                print(e)
                continue

        if len(validAnswers) > 0:
            question_answers[ANSWERS] = validAnswers
            with open(file_path, mode='w', encoding='utf-8') as json_data:
                json.dump(question_answers, json_data, ensure_ascii=False)
        else:
            os.remove(file_path)
            print(file_path)

def replace_with_byte(match):
    return chr(int(match.group(0)[1:], 8))

def repair(brokenjson):
    return invalid_escape.sub(replace_with_byte, brokenjson)

filterAnswers()

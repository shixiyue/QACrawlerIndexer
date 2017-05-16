import json
import os
import statistics
from utility import *
import numpy as np

question_vote = dict()
answer_length = dict()
answer_vote = dict()
stats = dict()

def read_all_posts():
    print("Read posts")
    all_websites = filter(lambda filename: filename != ".DS_Store", os.listdir(parsed_data_path))
    for website in all_websites:
        print("Processing " + website)

        question_vote_array = []
        answer_vote_array = []
        answer_length_array = []

        directory = parsed_data_path + website + '/'
        all_data_files = filter(lambda filename: filename != ".DS_Store", os.listdir(directory))
        for data_file_name in all_data_files:
            with open(directory + data_file_name, mode='r', encoding='utf-8') as json_data:
                question_answers = json.load(json_data)
            question_vote_array.append(int(question_answers[VOTE]))
            for answer in question_answers[ANSWERS]:
                answer_vote_array.append(int(answer[VOTE]))
                answer_length_array.append(len(answer[ANSWER].split()))
        question_vote[website] = question_vote_array
        answer_vote[website] = answer_vote_array
        answer_length[website] = answer_length_array
        print(np.percentile(answer_vote_array, 50))
        print(np.percentile(answer_vote_array, 25))
        print(np.percentile(question_vote_array, 25))
        print(np.percentile(question_vote_array, 25))

def analyze():
    print("Analyze...")
    calculate_average_and_sd("question_vote", question_vote)
    calculate_average_and_sd("answer_vote", answer_vote)
    calculate_average_and_sd("answer_length", answer_length)
    with open("stats.json", mode='w', encoding='utf-8') as json_data:
        json.dump(stats, json_data)

def calculate_average_and_sd(category, data_set):
    stats[category]["all"] = []
    for website, data in data_set:
        stats[category][website]["mean"] = statistics.mean(data)
        stats[category][website]["sd"] = statistics.pstdev(data)
        stats[category]["all"].extend(data)
    stats[category]["mean"] = statistics.mean(stats[category]["all"])
    stats[category]["sd"] = statistics.pstdev(stats[category]["all"])
    del stats[category]["all"]

read_all_posts()
analyze()

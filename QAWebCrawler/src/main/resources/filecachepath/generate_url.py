def generate_urls(prefix, count):
	with open("www.zhihu.com.urls.txt", mode='w', encoding='utf-8') as urls:
		for i in range(1, count):
			urls.write(prefix + str(i) + '\n')

generate_urls('https://www.zhihu.com/topic/19556664/questions?page=', 30052)
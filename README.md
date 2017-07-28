# QACrawlerIndexer

 * QAWebCrawler: Crawl data from www.quora.com and www.zhihu.com
   * com.crawler.customutil.Config stores those default configuration for the crawler. By default, cralwed data will be stored as Json files in /crawl and the crawler will use 5 threads. Change those corresponding variables if you would like to update configuration.
   * To crawl data from Quora: run com.crawler.customprocessor.QuoraPageProcessor.java
   * To crawl data from Zhihu: 1. first run resources/filecachepath/generate_url.py (change variable topic_base_url and num_of_page if you are going to crawl data from a another topic) 2. then run com.crawler.customprocessor.ZhihuQuestionLinkCollector.java 3. finally run com.crawler.customprocessor.ZhihuPageProcessor.java
 * StackExchangeDataParser: Prase and format data.
   * To parse data: Downloaded data from Stack Exchange Data Dump https://archive.org/details/stackexchange, unzip, place it at StackExchangeDataParser/data, run StackExchangeDataParser/parser.py, and data will be parsed and stored at StackExchangeDataParser/parsed
 * QAIndexer: Index data into Elasticsearch. The mapping and data format please refer to QAIndexer/Mapping
   * Open com.indexer.QuoraIndexer or com.indexer.StackExchangeIndexer, change the value of varibale dataPath to where you store data, and run the program. Note: Currently, data are indexed into the elasticsearch at readpeer server(172.29.34.20). If you would like to index into another IP address, please change the variable.
 

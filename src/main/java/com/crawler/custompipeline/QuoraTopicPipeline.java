package com.crawler.custompipeline;

import java.util.List;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import us.codecraft.webmagic.utils.FilePersistentBase;;

public class QuoraTopicPipeline implements Pipeline {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private MongoClient mongoClient;
    private MongoDatabase db;

    public QuoraTopicPipeline() {

        mongoClient = new MongoClient("localhost", 27017);
        db = mongoClient.getDatabase("topic");

    }

    public void process(ResultItems resultItems, Task task) {

        Document doc = new Document("topic", resultItems.get("topic"));
        doc.append("relatedtopics", resultItems.get("relatedtopics"));

        db.getCollection("quora").insertOne(doc);


    }
}

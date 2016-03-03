package com.crawler.custompipeline;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;;

public class QuoraPipeline implements Pipeline {

    private MongoClient mongoClient;
    private MongoDatabase db;

    public QuoraPipeline() {
        mongoClient = new MongoClient("localhost", 27017);
        db = mongoClient.getDatabase("qadb");
    }

    public void process(ResultItems resultItems, Task task) {

        int size = resultItems.getAll().size();

        Document doc = new Document("url", resultItems.get("url"));
        doc.append("categories", resultItems.get("categories"));
        doc.append("question", resultItems.get("question"));
        doc.append("description", resultItems.get("description"));
        Document ansDoc = new Document();

        if (size > 4) {
            for (int i = 1; i < size - 3; i++) {
                String ansIndex = "answer" + i;
                ansDoc.append(ansIndex, resultItems.get(ansIndex));
            }
        }

        doc.append("answer", ansDoc);
        db.getCollection("quora").insertOne(doc);

    }
}

package com.crawler.customutil;

/**
 * Created by sesame on 19/2/16.
 */
import com.mongodb.DB;
import com.mongodb.Mongo;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

import java.util.List;

/**
 * Created by sesame on 18/2/16.
 */
public class Indexer {

    public static String indexDirectoryPath;

    public Indexer(String indexDirectoryPath) {
        this.indexDirectoryPath = indexDirectoryPath;
    }

//    private static class Holder {
//        private static final Indexer INSTANCE = new Indexer(indexDirectoryPath);
//    }
//
//    public static Indexer getInstance() {
//        return Holder.INSTANCE;
//    }

    public void createIndex(List<String> answers, String question, List<String> categories, String url) throws IOException {
        Directory indexDirectory = FSDirectory.open(new File(indexDirectoryPath));

        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_45, analyzer);

        IndexWriter writer = new IndexWriter(indexDirectory, config);
        Document doc = new Document();
        StringBuilder answerStr = new StringBuilder();
        for(String answer : answers) {
            answerStr.append(answer).append(" ");
        }
        Field fieldAnswer = new Field(LuceneConstants.CONTENTS, answerStr.toString(), Field.Store.YES, Field.Index.ANALYZED);
        fieldAnswer.setBoost(4.0f);
        doc.add(fieldAnswer);

        Field fieldQuestion = new Field(LuceneConstants.QUESTION, question, Field.Store.YES, Field.Index.ANALYZED);
        fieldQuestion.setBoost(3.0f);
        doc.add(fieldQuestion);

        StringBuilder categoryStr = new StringBuilder();
        for(String category : categories) {
            category.replace("\"", "");
            categoryStr.append(category).append(" ");
        }
        Field fieldCategory = new Field(LuceneConstants.CATEGORY, categoryStr.toString(), Field.Store.YES, Field.Index.ANALYZED);
        fieldCategory.setBoost(3.0f);
        doc.add(fieldCategory);

        Field fieldUrl = new Field(LuceneConstants.URL, url, Field.Store.YES, Field.Index.NOT_ANALYZED);
        doc.add(fieldUrl);

        writer.addDocument(doc);

        writer.close();
    }

//    public int createIndex() throws IOException{
//
//        //Add answer field
//        StringBuilder answerStr = new StringBuilder();
//        BasicDBObject ansObj = (BasicDBObject) obj.get("answer");
//        int ansLen = ansObj.size();
//        for (int j = 1; j < ansLen - 1; j++) {
//            String answer = ansObj.get("answer" + j).toString();
//            answerStr.append(answer).append(" ");
//        }
//        Field fieldAnswer = new Field(LuceneConstants.CONTENTS, answerStr.toString(), Field.Store.YES, Field.Index.ANALYZED);
//        fieldAnswer.setBoost(4.0f);
//        doc.add(fieldAnswer);
//
//        //Add question field
//        String question = obj.get("question").toString();
//        Field fieldQuestion = new Field(LuceneConstants.QUESTION, question, Field.Store.YES, Field.Index.ANALYZED);
//        fieldQuestion.setBoost(3.0f);
//        doc.add(fieldQuestion);
//
//        //Add name field
//        String name = obj.get("_id").toString();
//        Field fieldName = new Field(LuceneConstants.FILE_NAME, name, Field.Store.YES, Field.Index.NOT_ANALYZED);
//        doc.add(fieldName);
//
//        //Add category field
//        List<String> categories = (List<String>) obj.get("categories");
//        StringBuilder categoryStr = new StringBuilder();
//        for (String category : categories) {
//            category.replace("\"", "");
//            categoryStr.append(category).append(" ");
//        }
//        Field fieldCategory = new Field(LuceneConstants.CATEGORY, categoryStr.toString(), Field.Store.YES, Field.Index.ANALYZED);
//        fieldCategory.setBoost(3.0f);
//        doc.add(fieldCategory);
//
//        String url = obj.get("url").toString();
//        Field fieldUrl = new Field(LuceneConstants.URL, url, Field.Store.YES, Field.Index.NOT_ANALYZED);
//        doc.add(fieldUrl);
//
//        writer.addDocument(doc);
//
//
//        return writer.numDocs();
//    }
//
//    public void close() throws CorruptIndexException, IOException {
//        //Close Indexer
//        writer.close();
//    }

}

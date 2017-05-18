package com.crawler.customutil;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Store results to files in JSON format.
 * Modified from https://github.com/code4craft/webmagic/tree/master/webmagic-core/src/main/java/us/codecraft/webmagic/pipeline
 *
 */
public class CustomJsonFilePipeline extends FilePersistentBase implements Pipeline {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public CustomJsonFilePipeline(String path) {
        setPath(path);
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        String path = this.path + PATH_SEPERATOR + task.getUUID() + PATH_SEPERATOR;
        try {
        	String fileName = getFileName((String)resultItems.get(Config.URL)); // Use URL to be filename to ensure uniqueness
            PrintWriter printWriter = new PrintWriter(new FileWriter(getFile(path + fileName + ".json")));
            printWriter.write(JSON.toJSONString(resultItems.getAll()));
            printWriter.close();
        } catch (IOException e) {
            logger.warn("write file error", e);
        }
    }
    
    private String getFileName(String url) {
    	String[] urlComponents = url.split("/");
    	String fileName = urlComponents[urlComponents.length - 1];
    	fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    	fileName = fileName.substring(0, Math.min(fileName.length(), 200));
    	return fileName;
    }
}

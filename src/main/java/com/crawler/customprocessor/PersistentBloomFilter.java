package com.crawler.customprocessor;

/**
 * Created by sesame on 3/3/16.
 */
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

public class PersistentBloomFilter implements DuplicateRemover {

    private int expectedInsertions;
    private double fpp;
    private AtomicInteger counter;
    private final BloomFilter<CharSequence> bloomFilter;

    public PersistentBloomFilter(int expectedInsertions) {
        this(expectedInsertions, 0.01);
    }

    public PersistentBloomFilter(int expectedInsertions, double fpp) {
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
        this.bloomFilter = rebuildBloomFilter();
    }

    public PersistentBloomFilter(int expectedInsertions, double fpp, String path) {
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
        this.bloomFilter = rebuildBloomFilter(path);
    }

    protected BloomFilter<CharSequence> rebuildBloomFilter() {
        counter = new AtomicInteger(0);
        return BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), expectedInsertions, fpp);
    }

    protected BloomFilter<CharSequence> rebuildBloomFilter(String path) {
        counter = new AtomicInteger(0);
        Object  object = readObject(path);
        return (BloomFilter) object;
    }

    public void storeBloomFilter(String path) {
        writeObject(bloomFilter, path);
    }

    @Override
    public boolean isDuplicate(Request request, Task task) {
        boolean isDuplicate = bloomFilter.mightContain(getUrl(request));
        if (!isDuplicate) {
            bloomFilter.put(getUrl(request));
            counter.incrementAndGet();
        }
        return isDuplicate;
    }

    protected String getUrl(Request request) {
        return request.getUrl();
    }

    @Override
    public void resetDuplicateCheck(Task task) {
        rebuildBloomFilter();
    }

    @Override
    public int getTotalRequestsCount(Task task) {
        return counter.get();
    }

    public void writeObject(Serializable s, String path) {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(path));
            objectOutputStream.writeObject(s);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object readObject(String path) {
        Object object = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(path));
            object = objectInputStream.readObject();
            objectInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return object;
    }
}

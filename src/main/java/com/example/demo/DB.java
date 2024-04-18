package com.example.demo;

import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

public class DB extends Thread {
    Crawler crawler = new Crawler();

    private int number_Theads;
    private Object lock; // Define the lock object as a member variable

    public DB(int number_Threads, Object lock) {
        this.number_Theads = number_Threads;
        this.lock = lock;
    }


    private void InsertWordsIntoDB(String[] words,String id,int NumberofWords) throws URISyntaxException, IOException, InterruptedException {
        Map<String, Integer> WordsWithURL = new HashMap<>();
        Request request = new Request();
        for (String word : words) {
            String StampWord = crawler.Stemmping(word);
            if (StampWord.length() != 0) {

                Integer value1 = WordsWithURL.getOrDefault(StampWord, 0); // Value is 10
                value1++;
                WordsWithURL.put(StampWord, value1);
            }
        }
        List<String> keys = new ArrayList<>(WordsWithURL.keySet());

        synchronized (lock) {
            int numThreads=100;
            for (int i = 0; i < numThreads; i++)//number of thread 50
            {
                int finalI = i;
                Thread wordThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int size = keys.size();
                        int num = size / numThreads;//1000/50 20
                        if (finalI < numThreads-1) {
                            for (int j = finalI * num; j < (finalI * num + num); j++) {

                                Integer value = WordsWithURL.get(keys.get(j));
                                String data = String.format("{\"Word\":\"%s\",\"id\":\"%s\",\"NumberofWords\":%d,\"Occure\":%d}", keys.get(j),id,NumberofWords, value);
                                System.out.println("Thread number " + j + " data= " + data);

                                try {
                                    request.post("http://localhost:3000/Word/Insert", data);//insert lock
                                } catch (URISyntaxException e) {
                                    throw new RuntimeException(e);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }


                            }
                        } else {
                            for (int j = finalI * num; j < keys.size(); j++) {

                                Integer value = WordsWithURL.get(keys.get(j));
                                String data = String.format("{\"Word\":\"%s\",\"id\":\"%s\",\"NumberofWords\":%d,\"Occure\":%d}", keys.get(j),id,NumberofWords, value);
                                System.out.println("Thread number " + j + " data= " + data);

                                try {
                                    request.post("http://localhost:3000/Word/Insert", data);//insert lock
                                } catch (URISyntaxException e) {
                                    throw new RuntimeException(e);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }


                            }
                        }
                    }

                });
                wordThread.start();
            }
        }
    }


    public void getFromFileInsetIntoDB(String line) throws InterruptedException, URISyntaxException, JSONException {
        Request request = new Request();

        try {
            String loadFile = "CrawledURLs.txt";
            String URLHttp = "http://localhost:3000/URL/insert";
            String url = line.split(" ")[0];
            Document doc = Jsoup.connect(url).get();
            String Title = doc.title();

            String[] words = doc.body().select("*").text().replaceAll("[^a-zA-Z ]", " ").toLowerCase().split("\\s+");
            int Rank = Integer.parseInt(line.split(" ")[1]);
            int NumberofWords = words.length;
            var URL = String.format("{\"URL\":\"%s\",\"Title\":\"%s\",\"Rank\":%d,\"NumberofWords\":%d}", url, Title, Rank, NumberofWords);
            System.out.println(URL);
            String res;

            res = request.post(URLHttp, URL);//lock
            String avilable = getStringfromJson(res, "message");
            System.out.println("Thread number " + this.getName() + " URL= " + URL);

            if (avilable.equals("Created Successfully")) {
                String id = (getStringfromJson(res, "id"));
                InsertWordsIntoDB(words,id,NumberofWords);


            }} catch (IOException e) {
            System.out.println("cannot connect to this URL");
        }


    }

    private String getStringfromJson(String Json, String wantdata) throws JSONException {
        JSONObject jsonObject = new JSONObject(Json);
        // Get the value of the "message" field
        String message = jsonObject.getString(wantdata);
        return message;
    }

    public void run() {
        int size = Crawler.links.size();
        int number = Integer.parseInt(this.getName());
//        int start = number * (size / number_Theads);//each thread take hoe many link
        if (number < size) {
            try {
                getFromFileInsetIntoDB(Crawler.links.get(number));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }


        }

    }

}

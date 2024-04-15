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
    Crawler crawler=new Crawler();

    private int number_Theads;
    private Object lock; // Define the lock object as a member variable

    public DB(int number_Threads,Object lock) {
        this.number_Theads = number_Threads;
        this.lock=lock;
    }



    private void InsertWordsIntoDB(String[] words, String URL) throws URISyntaxException, IOException, InterruptedException {
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
        for (String Key : WordsWithURL.keySet()) {
            Integer value = WordsWithURL.get(Key);
            String data = String.format("{\"Word\":\"%s\",\"URL\":\"%s\",\"Occure\":%d}", Key, URL, value);
            System.out.println("Thread number "+this.getName() +" data= "+data);
                      synchronized (lock) {
       request.post("http://localhost:3000/Word/Insert", data);//insert lock
//
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
            System.out.println("Thread number "+this.getName() +" URL= "+URL);

           if (avilable.equals("Created Successfully"))
                InsertWordsIntoDB(words, url);
        } catch (IOException e) {
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
        int start = number * (size / number_Theads);//each thread take hoe many link

        if (number != number_Theads - 1) {
            for (int i = start; i < start + (size / number_Theads); i++) {
                try {
                    getFromFileInsetIntoDB(Crawler.links.get(i));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        } else {
            for (int i = start; i < size; i++) {//if number not divisible by total number of thread
                try {
                    getFromFileInsetIntoDB(crawler.links.get(i));
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

}

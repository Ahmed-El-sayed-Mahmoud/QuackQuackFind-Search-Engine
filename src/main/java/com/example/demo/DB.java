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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class DB {
    Crawler crawler = new Crawler();

    private int number_Theads;
    private Object lock; // Define the lock object as a member variable
    public Set<String> stopWords;
    //public  Set<String>AllWords;
    public DB(Object lock, Set<String> stopWords) {
        //this.number_Theads = number_Threads;
        this.lock = lock;
        this.stopWords = stopWords;
    }
    public DB()
    {}
    public DB(Set<String> stopWords)
    {
        this.stopWords = stopWords;
    }
    public String Stemmping(String word) throws IOException {

        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);

        TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(word));

        // Apply Porter stemming using PorterStemFilter
        PorterStemFilter porterStemFilter = new PorterStemFilter(tokenStream);

        // Retrieve token attributes
        CharTermAttribute charTermAttribute = porterStemFilter.addAttribute(CharTermAttribute.class);

        // Process tokens and retrieve the stemmed output
        porterStemFilter.reset();
        porterStemFilter.incrementToken();
        String stemmedWord = charTermAttribute.toString();

        // Close the TokenStream
        porterStemFilter.end();
        porterStemFilter.close();

        // Return the stemmed word
        return stemmedWord;
    }

    public  String RemoveStopWords(String Word)
    {
        String[] words = Word.split("\\s+"); // Split by whitespace

        StringBuilder result = new StringBuilder();

        for (String word : words) {

            String lowercaseWord = word.toLowerCase();

            if (!stopWords.contains(lowercaseWord)) {
                result.append(word).append(" ");
            }
        }

        return result.toString().trim();
    }

    // Method to load stopwords from file


    private void InsertWordsIntoDB(List<String> words, String URL, String Title, int NumberofWords,int Rank) throws URISyntaxException, IOException, InterruptedException {
        Map<String, Integer> wordsWithURL = new ConcurrentHashMap<>();
        Request request = new Request();
        List<Thread> threads = new ArrayList<>(); // Create a list to keep track of all created threads

        for (String string : words) {
            String[] sunWords = string.split("\\s+");
            for (String word : sunWords) {
                String stampedWord = Stemmping(word);
                if (!stampedWord.isEmpty()) {
                    int val = wordsWithURL.getOrDefault(stampedWord, 0);
                    wordsWithURL.put(stampedWord, ++val);
                }
            }
        }

        List<String> keys = new CopyOnWriteArrayList<>(wordsWithURL.keySet());

        synchronized (lock) {
            int numThreads = 100;
            int size = keys.size();
            int batchSize = (size + numThreads - 1) / numThreads; // Calculate batch size to evenly distribute elements among threads

            for (int i = 0; i < numThreads; i++) {
                int finalI = i;
                Thread wordThread = new Thread(() -> {
                    for (int j = finalI * batchSize; j < Math.min((finalI + 1) * batchSize, size); j++) {
                        // Process each element within the assigned batch
                        Integer value = wordsWithURL.get(keys.get(j));
                        String data = String.format("{\"Word\":\"%s\",\"URL\":\"%s\",\"Title\":\"%s\",\"NumberofWords\":%d,\"Occure\":%d,\"Rank\":%d}", keys.get(j), URL, Title, NumberofWords, value,Rank);
                        System.out.println("Thread number " + finalI + " data= " + data);

                        try {
                            request.post("http://localhost:3000/Word/Insert", data);
                        } catch (URISyntaxException | IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                threads.add(wordThread); // Add the thread to the list
                wordThread.start(); // Start the thread
            }

            // Join all threads to wait for their completion
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public void getFromFileInsetIntoDB(String line) throws InterruptedException, URISyntaxException, JSONException {
        Request request = new Request();

        try {
            // String loadFile = "CrawledURLs.txt";
            String URLHttp = "http://localhost:3000/URL/insert";
            String url = line.split(" ")[0];
            Document doc = Jsoup.connect(url).get();
            String Title = doc.title();
            int NumberofWords = (doc.text().replaceAll("[^a-zA-Z ]", " ").toLowerCase().split("\\s+")).length;
            Elements paragraphElements = doc.select("p");
            Elements headersElements = doc.select("h1, h2, h3, h4, h5, h6");

            // Process and extract text from each paragraph
            List<String> words = new ArrayList<>();
            //words.add(Title);

//paragraph
            for (Element paragraph : paragraphElements) {
                String paragraphText = paragraph.text().replaceAll("[^a-zA-Z ]", " ").toLowerCase();
                words.add(paragraphText);
                //System.out.println(paragraphText);
            }
            for (Element header : headersElements) {
                String headerText = header.text().replaceAll("[^a-zA-Z ]", " ").toLowerCase();
                // System.out.println(headerText);
                words.add(headerText);
            }

            int Rank = Integer.parseInt(line.split(" ")[1]);

            //var URL = String.format("{\"URL\":\"%s\",\"Title\":\"%s\",\"Rank\":%d,\"NumberofWords\":%d}", url, Title, Rank, NumberofWords);
            //System.out.println(URL);


            InsertWordsIntoDB(words, url, Title, NumberofWords,Rank);


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
    public void insert() throws JSONException, URISyntaxException, InterruptedException {
        for(int i=0;i<crawler.links.size();i++)
            getFromFileInsetIntoDB(crawler.links.get(i));
    }

}
//
//    public void run() {
//        int size = Crawler.links.size();
//        int number = Integer.parseInt(this.getName());
//        try {
//            getFromFileInsetIntoDB(Crawler.links.get(number));
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//    }
////    int start = number * (size / number_Theads);//each thread take hoe many link
////         if(number!=number_Theads-1){
////        for(int i=start;i<start+(size / number_Theads);i++) {
////           {
////               System.out.println(Crawler.links.get(i)+"index "+i+ "thread "+number);
//////                try {
//////                    getFromFileInsetIntoDB(Crawler.links.get(i));
//////                } catch (InterruptedException e) {
//////                    throw new RuntimeException(e);
//////                } catch (URISyntaxException e) {
//////                    throw new RuntimeException(e);
//////                } catch (JSONException e) {
//////                    throw new RuntimeException(e);
//////                }
////            }
////        }}
////        else
////            {
////                for(int i=start;i<start+(size / number_Theads)+(size % number_Theads);i++) {
////                    {
////                        try {
////                            getFromFileInsetIntoDB(Crawler.links.get(i));
////                        } catch (InterruptedException e) {
////                            throw new RuntimeException(e);
////                        } catch (URISyntaxException e) {
////                            throw new RuntimeException(e);
////                        } catch (JSONException e) {
////                            throw new RuntimeException(e);
////                        }
////                    }
////            }
////
////        }
//
//}
//


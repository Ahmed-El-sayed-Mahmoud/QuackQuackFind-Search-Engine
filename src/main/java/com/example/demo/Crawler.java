package com.example.demo;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class Crawler {
    private HashMap<String, Integer> crawledURLS = new HashMap<String, Integer>();
    private HashMap<String, HashSet<String>> robotsDisallow = new HashMap<>();
    private HashSet<String> pendingURLS = new LinkedHashSet<String>();
    private Queue<String> queue = new LinkedList<String>();

  //  public Set<String> StopWords;
    static public ArrayList<String> links;

    public void StartFromSeed(int no_threads, int maxPending, int layers) {
        LoadSeedFromFile();
        StartThreading(no_threads, maxPending, layers);
        printCrawled();
        SaveCrawledToFile();
        SavePendingToFile();
    }

    public void StartFromSave(int no_threads, int maxPending, int layers) {
        //LoadCrawledFromFile();
        LoadPendingFromFile();
        StartThreading(no_threads, maxPending, layers);
        SavePendingToFile();
        SaveCrawledToFile();
    }


    private void StartThreading(int no_threads, int maxPending, int layers) {
        queue.addAll(pendingURLS);
        no_threads++;
        Thread[] thread = new Thread[no_threads];
        AtomicBoolean signalQueueEmpty = new AtomicBoolean(true);
        for (int i = 0; i < no_threads; i++) {
            thread[i] = new Thread(new CrawlerThreads(crawledURLS, pendingURLS, queue, robotsDisallow, maxPending, layers, signalQueueEmpty));
            thread[i].setName("thread " + i);
        }
        for (int i = 0; i < no_threads; i++) {
            thread[i].start();
        }
        for (Thread t : thread) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void SaveCrawledToFile() {
        String savePath = "CrawledURLs.txt";
        try {
            FileWriter writer = new FileWriter(savePath);
            for (HashMap.Entry<String, Integer> entry : crawledURLS.entrySet()) {
                String url = entry.getKey();
                int count = entry.getValue();
                writer.write(url + " " + count + "\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Error in saving crawled links " + e.getMessage());
        }
    }

    private void SavePendingToFile() {
        String savePath = "PendingURLs.txt";
        try {
            FileWriter writer = new FileWriter(savePath);
            for (String s : pendingURLS) {
                writer.write(s);
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Error in saving pending links " + e.getMessage());
        }
    }
/*

    private void LoadCrawledFromFile() {
        try {
            String loadFile = "CrawledURLs.txt";
            FileReader fileReader = new FileReader(loadFile);
            BufferedReader reader = new BufferedReader(fileReader);
            String line;
            while ((line = reader.readLine()) != null) {
                crawledURLS.add(line);
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        }
    }
*/


    private void LoadPendingFromFile() {
        try {
            String loadFile = "PendingURLs.txt";
            FileReader fileReader = new FileReader(loadFile);
            BufferedReader reader = new BufferedReader(fileReader);
            String line;
            while ((line = reader.readLine()) != null) {
                pendingURLS.add(line);
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        }
    }

    private void LoadSeedFromFile() {
        try {
            String loadFile = "SeedURLs.txt";
            FileReader fileReader = new FileReader(loadFile);
            BufferedReader reader = new BufferedReader(fileReader);
            String line;
            while ((line = reader.readLine()) != null) {
                pendingURLS.add(line);
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        }
    }

    private void printCrawled() {
        for (HashMap.Entry<String, Integer> entry : crawledURLS.entrySet()) {
            String url = entry.getKey();
            int count = entry.getValue();
            System.out.println(url + " " + count + "\n");
        }
    }

    public Set<String>  stopedWord() {
        BufferedReader reader = null;
        ArrayList<String> stopword = new ArrayList<String>();

        try {
            reader = new BufferedReader(new FileReader("stopWord"));
            String line;
            while ((line = reader.readLine()) != null) {
                stopword.add(line);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Set<String> set = new HashSet<>(stopword);
        return set;
    }

//    public String Stemmping(String word) throws IOException {
//        // Create an EnglishStemmer instance
//        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30, StopWords);//analize text
//        TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(word));
//
//        // Apply Porter stemming using PorterStemFilter
//        PorterStemFilter porterStemFilter = new PorterStemFilter(tokenStream);
//
//        // Retrieve token attributes
//        CharTermAttribute charTermAttribute = porterStemFilter.addAttribute(CharTermAttribute.class);//return text after stemming it
//
//        // Process tokens and print the stemmed output
//        porterStemFilter.reset();
//        porterStemFilter.incrementToken();
//        System.out.println("Stemmed word: " + charTermAttribute.toString());
//
//        String WordAfterStemmping = charTermAttribute.toString();
//        // Close the TokenStream
//        porterStemFilter.end();
//        porterStemFilter.close();
//        return WordAfterStemmping;
//    }

//    private void InsertWordsIntoDB(String[] words, String URL) throws URISyntaxException, IOException, InterruptedException {
//        Map<String, Integer> WordsWithURL = new HashMap<>();
//        Request request = new Request();
//        for (String word : words) {
//            String StampWord = Stemmping(word);
//            if (StampWord.length() != 0) {
//
//                Integer value1 = WordsWithURL.getOrDefault(StampWord, 0); // Value is 10
//                value1++;
//                WordsWithURL.put(StampWord, value1);
//            }
//        }
//        for (String Key : WordsWithURL.keySet()) {
//            Integer value = WordsWithURL.get(Key);
//            String data = String.format("{\"Word\":\"%s\",\"URL\":\"%s\",\"Occure\":%d}", Key, URL, value);
//            request.post("http://localhost:3000/Word/Insert", data);//insert lock
//        }
//    }

//    public void getFromFileInsetIntoDB() throws InterruptedException, URISyntaxException, JSONException {
//        Request request = new Request();
//
//        try {
//            String loadFile = "CrawledURLs.txt";
//            String URLHttp = "http://localhost:3000/URL/insert";
//
//            FileReader fileReader = new FileReader(loadFile);
//            BufferedReader reader = new BufferedReader(fileReader);
//            String line;
//            int i = 0;
//
//            while ((line = reader.readLine()) != null) {
//                try {
//                    String url = line.split(" ")[0];
//                    Document doc = Jsoup.connect(url).get();
//                    String Title = doc.title();
//
//                    String[] words = doc.body().select("*").text().replaceAll("[^a-zA-Z ]", " ").toLowerCase().split("\\s+");
//                    int Rank = Integer.parseInt(line.split(" ")[1]);
//                    int NumberofWords = words.length;
//                    var URL = String.format("{\"URL\":\"%s\",\"Title\":\"%s\",\"Rank\":%d,\"NumberofWords\":%d}", url, Title, Rank, NumberofWords);
//                    System.out.println(URL);
//                    String res = request.post(URLHttp, URL);//lock
//                    String avilable = getStringfromJson(res, "message");
//
//                   if (avilable.equals("Created Successfully"))
//                       InsertWordsIntoDB(words, url);
//                } catch (IOException e) {
//                    System.out.println("cannot connect to this URL");
//                }
//            }
//            reader.close();
//        } catch (IOException e) {
//            System.err.println("Error reading from file: " + e.getMessage());
//        }
//    }

    private String getStringfromJson(String Json, String wantdata) throws JSONException {
        JSONObject jsonObject = new JSONObject(Json);
        // Get the value of the "message" field
        String message = jsonObject.getString(wantdata);
        return message;
    }

    public void getFromFile(int links_number) {
        links = new ArrayList<>();

        try {
            String loadFile = "CrawledURLs.txt";
            String URLHttp = "http://localhost:3000/URL/insert";

            FileReader fileReader = new FileReader(loadFile);
            BufferedReader reader = new BufferedReader(fileReader);
            String line;
            int i = 0;
            int k = 0;
            while ((line = reader.readLine()) != null) {

                if (i >= (links_number * 500) && i < (links_number * 500 + 500)) {
                    links.add(line);
                    System.out.println(i);
                }
                if (i >= (links_number * 500 + 500))
                    break;

                i++;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

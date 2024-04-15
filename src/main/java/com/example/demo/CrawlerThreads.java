package com.example.demo;

import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CrawlerThreads implements Runnable {
    private HashMap<String, Integer> crawledURLS;
    private HashSet<String> pendingURLS;
    private Queue<String> queue;
    private HashMap<String, HashSet<String>> robotsDisallow;
    private AtomicBoolean signalQueueEmpty;
    private int layers;

    private int maxPending;
    ///////////////////////////////////////test

    public CrawlerThreads(HashMap<String, Integer> crawledURLS, HashSet<String> pendingURLS, Queue<String> queue, HashMap<String, HashSet<String>> robotsDisallow, int maxPending, int layers, AtomicBoolean signalQueueEmpty) {
        this.crawledURLS = crawledURLS;
        this.pendingURLS = pendingURLS;
        this.queue = queue;
        this.signalQueueEmpty = signalQueueEmpty;
        this.robotsDisallow = robotsDisallow;
        this.maxPending = maxPending;
        this.layers = layers;
    }

    public void run() {
        if (Thread.currentThread().getName().equals("thread 0")) {
            SyncAtomicInitQueue();
        } else {
            crawl();
        }
    }

    private void crawl() {
        while (layers > 0 && SyncPendingsize() < maxPending) {
            System.out.println(Thread.currentThread().getName() + " number of Crawled URLS " + SyncCrawledSize());
            String cur_URL = SyncQueuePoll();
            crawl_step(cur_URL);
            SyncRemovePending(cur_URL);

        }
        terminateQueueInit();
    }

    private void terminateQueueInit() {
        synchronized (signalQueueEmpty) {
            signalQueueEmpty.set(false);
            signalQueueEmpty.notify();
        }
    }

    private void SyncAtomicInitQueue() {
        while (signalQueueEmpty.get()) {
            synchronized (queue) {
                synchronized (pendingURLS) {
                    queue.addAll(pendingURLS);
                    layers--;
                    queue.notifyAll();
                }
            }
            synchronized (signalQueueEmpty) {
                try {
                    signalQueueEmpty.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String SyncQueuePoll() {
        synchronized (queue) {
            if (queue.isEmpty()) {
                try {
                    synchronized (signalQueueEmpty) {
                        signalQueueEmpty.notify();
                    }
                    queue.wait();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            return queue.poll();
        }
    }

    private boolean SyncQueueEmpty() {
        synchronized (queue) {
            return queue.isEmpty();
        }
    }

    private int SyncPendingsize() {
        synchronized (pendingURLS) {
            return pendingURLS.size();
        }
    }

    private int SyncCrawledSize() {
        synchronized (crawledURLS) {
            return crawledURLS.size();
        }
    }


    private void SyncAddCrawled(String url) {
        synchronized (crawledURLS) {
            if (crawledURLS.containsKey(url)) {
                crawledURLS.put(url, crawledURLS.get(url) + 1);
            } else {
                crawledURLS.put(url, 1);
            }
        }
    }

    private boolean SyncContainsCrawled(String url) {
        synchronized (crawledURLS) {
            return crawledURLS.containsKey(url);
        }
    }


    private void SyncAddPending(String url) {
        synchronized (pendingURLS) {
            pendingURLS.add(url);
        }
    }

    private void SyncRemovePending(String url) {
        synchronized (pendingURLS) {
            pendingURLS.remove(url);
        }
    }


    private void SyncRobotDisallowPut(String key, HashSet<String> val) {
        synchronized (robotsDisallow) {
            robotsDisallow.put(key, val);
        }
    }

    private HashSet<String> SyncRobotDisallowGet(String key) {
        synchronized (robotsDisallow) {
            return robotsDisallow.get(key);
        }
    }

    private boolean SyncRobotDisallowContains(String key) {
        synchronized (robotsDisallow) {
            return robotsDisallow.containsKey(key);
        }
    }

    private synchronized void crawl_step(String url) {
        try {
            Elements elements = new Elements();
            int i = 10;
            while (elements.isEmpty() && i > 0) {
                i--;
                Document doc = Jsoup.connect(url).get();
                elements = doc.select("a[href]");
                Thread.sleep(1000);
                //  System.out.println(doc);
            }
            String userAgent = "Crawler";
            URL baseUrl = new URL(url);
            String baseHost = baseUrl.getProtocol() + "://" + baseUrl.getHost();
            String compactedUrl = baseHost + baseUrl.getPath();

            for (Element e : elements) {
                String href = e.attr("abs:href");
                URL newUrl= new URL (href);
                String compactedhref =newUrl.getProtocol() + "://" + newUrl.getHost() + newUrl.getPath();
                String processedHref = processLinks(compactedhref, compactedUrl);
                boolean allowed = robotsTxtHandler(baseHost, userAgent, compactedhref);

                if (processedHref != null && UrlValidator.getInstance().isValid(processedHref) && allowed) {
                    SyncAddPending(processedHref);
                    SyncAddCrawled(processedHref);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String processLinks(String link, String base) {
        try {
            URL u = new URL(base);
            if (link.startsWith("./")) {
                link = link.substring(2, link.length());  // remove the ./
                link = u.getProtocol() + "://" + u.getAuthority() + stripPath(u.getPath()) + link;
            } else if (link.startsWith("#")) {
                link = base + link;
            } else if (link.startsWith("javascript")) {
                link = null;
            } else if (link.startsWith("../") || (!link.startsWith("https://") && !link.startsWith("http://"))) {
                link = u.getProtocol() + "://" + u.getAuthority() + stripPath(u.getPath()) + link;
            }
            return link;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private String stripPath(String path) {
        int pos = path.lastIndexOf("/");
        if (pos > -1) {
            return path.substring(0, pos + 1);
        } else {
            return path;
        }
    }

    private String getHTML(String url, String userAgent) {
        URL u;
        try {
            u = new URL(url);
            URLConnection connection = u.openConnection();
            connection.setRequestProperty("User-Agen", userAgent);
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line, HTML = "";
            while ((line = reader.readLine()) != null) {
                HTML += line + "\n";
            }
            HTML = HTML.trim();
            return HTML;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

    }

    private boolean robotsTxtHandler(String baseurl, String userAgent, String curURL) {
        try {
            URL robotUrl = new URL(baseurl + "/robots.txt");
            if (!SyncRobotDisallowContains(robotUrl.toString())) {
                processRobots(robotUrl, userAgent);
            }
            HashSet<String> set = SyncRobotDisallowGet(robotUrl.toString());
            if (set != null) {
                boolean found = set.stream().anyMatch(str -> str.startsWith(curURL));
                return !found;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }

    }

    private synchronized void processRobots(URL robotUrl, String userAgent) {
        if (SyncRobotDisallowContains(robotUrl.toString())) {
            return;
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) robotUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("user-agent", "crawler1");
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return;
            }
            HashSet<String> set = new HashSet<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            boolean isUserAgentMatch = false;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    isUserAgentMatch = false;
                } else if (line.toLowerCase().startsWith("user-agent:")) {
                    String[] parts = line.split(":", 2);
                    String agent = parts[1].trim();
                    if ("*".equals(agent) || userAgent.equalsIgnoreCase(agent)) {
                        isUserAgentMatch = true;
                    } else {
                        isUserAgentMatch = false;
                    }
                } else if (isUserAgentMatch && line.toLowerCase().startsWith("disallow:")) {
                    String[] parts = line.split(":", 2);
                    String disallowedUrl = parts[1].trim();
                    set.add(disallowedUrl);
                }
            }
            reader.close();
            SyncRobotDisallowPut(robotUrl.toString(), set);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(Thread.currentThread().getName());
        }
    }


    public void insertUrlIntoDB(String Url, Document doc) throws InterruptedException, URISyntaxException {
        Request request = new Request();
        try {
            String URLHttp = "http://localhost:3000/URL/insert";

            String Title = doc.title();//title
            String[] Words = doc.body().select("*").text().split("\\s+");
            int NumberofWords = Words.length;
            int Rank = 0;
            var URL = String.format("{\"URL\":\"%s\",\"Title\":\"%s\",\"Rank\":%d,\"NumberofWords\":%d}", Url, Title, Rank, NumberofWords);
            synchronized (this) {
                String res = request.post(URLHttp, URL);
                System.out.println(res);

            }
//                System.out.println(i++);
//            }
            //here must call update IDF after inserted all file
//            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        }

    }

}

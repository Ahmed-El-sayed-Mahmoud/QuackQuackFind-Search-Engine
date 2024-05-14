package com.example.demo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.text.WordUtils;


public class ResultDocThread implements Runnable {
    //    final Map<String,ResultDoc> UniqueResultDocs;
//    DB db;
//    String [] Query;
//    final Map<String, List<ResultDoc>> UrlsFromDB;
//    List<JSONObject>jsonObjects;
//    String Word;
//    String NormalQuery;
//    boolean PhraseSearch;
    List<ResultDoc> Docs;
    boolean PhraseSearch;
    String NormalQuery;
    int Start;
    int Last;

    //    public ResultDocThread(Map<String,ResultDoc> UniqueResultDocs, List<JSONObject> jsonObjects, Map<String,List<ResultDoc>> UrlsFromDB,
//                           String [] Query,String NormalQuery,boolean PhraseSearch)
//    {
//        this.UniqueResultDocs = UniqueResultDocs;
//        this.jsonObjects = jsonObjects;
//        db = new DB(Crawler.stopedWord());
//        this.Query = Query;
//        this.UrlsFromDB = UrlsFromDB;
//        this.Word = Word;
//        this.NormalQuery = NormalQuery;
//        this.PhraseSearch = PhraseSearch;
//    }
    public ResultDocThread(List<ResultDoc> Docs,int start,int last, boolean PhraseSearch, String NormalQuery) {
        this.Docs = Docs;
        this.PhraseSearch = PhraseSearch;
        this.NormalQuery = NormalQuery;
        this.Start = start;
        this.Last = last;
    }

    @Override
    public void run() {
        try {
            CalcRes();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void CalcRes() throws JSONException, IOException {
        // for(JSONObject obj : jsonObjects)
        //    {
//            ResultDoc doc=new ResultDoc();
//            doc.Url=obj.getString("Url");
//            doc.Title=obj.getString("Title").toLowerCase();
//            doc.rank=obj.getInt("Rank");
//            doc.Tf=obj.getJSONObject("TF").getDouble("$numberDecimal");
//            String [] title= doc.Title.toLowerCase().split("\\s+|\\s*-\\s*|\\s*\\.\\s*");
//            for(int j=0;j<title.length;j++)
//                title[j]=db.Stemmping(title[j]);
//            /////////////
//            //doc.Title= String.join(" ", title);///////////////////////// / / / / / / /
//            /////////////////////////////
//            int i=0;
//            String StemmedTitle=String.join(" ",title);
//            for(String s: Query)
//            {
//                if(StemmedTitle.contains(s))
//                    doc.WordsTitleIncluded++;
//            }
//            doc.QueryToTile= (double)doc.WordsTitleIncluded/title.length;
        for (int i=Start;i<Last;i++) {
            ResultDoc doc=Docs.get(i);
            URL object = new URL(doc.Url);
            HttpURLConnection conn = (HttpURLConnection) object.openConnection();
            conn.setRequestMethod("HEAD");
            int responseCode = conn.getResponseCode();
            if (responseCode == 200 && conn.getConnectTimeout() == 0) {

                Document document = Jsoup.connect(doc.Url).userAgent("Opera").get();
                Elements paragraphs = document.select("p");
                Elements headers = document.select("h5,h4, h3,h2,h1");


                for (Element paragraph : paragraphs) {
                    if (doc.Describtion == null || doc.Describtion.isEmpty())
                        doc.Describtion = highlightQueryWords(paragraph, NormalQuery, PhraseSearch);
                    else
                        break;
                }

                for (Element header : headers) {
                    if (doc.Describtion == null || doc.Describtion.isEmpty())
                        doc.Describtion = highlightQueryWords(header, NormalQuery, PhraseSearch);
                    else break;
                }
                if ((doc.Describtion == null || doc.Describtion.length() == 0))
                    doc.Show = false;


            } else {
                doc.Show = false;
                if(conn.getConnectTimeout() != 0)
                    System.out.println("attttaaaaack");
            }
        }
//
//            synchronized (this.UniqueResultDocs)
//            {
//                if(UniqueResultDocs.containsKey(doc.Url))
//                    doc.WordsIncluded++;
//
//                UniqueResultDocs.put(doc.Url,doc);
//            }
//            synchronized (this.UrlsFromDB)
//            {
//                if(UrlsFromDB.containsKey(Word))
//                    UrlsFromDB.get(Word).add(doc);
//                else {
//                    ArrayList<ResultDoc> list= new ArrayList<ResultDoc>();
//                    list.add(doc);
//                    UrlsFromDB.put(Word,list);
//                }
    }


    public String highlightQueryWords(Element element, String NormalQuery, boolean PhraseSearch) {
        String text = element.text().toLowerCase();
        if (PhraseSearch && text.contains(NormalQuery)) {
            String highlightedText = text.replaceAll("(?i)(" + NormalQuery + ")", "<strong>$1</strong>");
            return highlightedText;
        } else if (!PhraseSearch) {
            String[] QueryWords = NormalQuery.split(" ");
            for (int i = 0; i < QueryWords.length; i++) {
                if (text.contains(QueryWords[i])) {
                    String highlightedText = text.replaceAll("(?i)(" + QueryWords[i] + ")", "<strong>$1</strong>");
                    return highlightedText;
                }
                else if(QueryWords[i].endsWith("s")&&text.contains(QueryWords[i].substring(0,QueryWords[i].length()-1)))
                {
                    String highlightedText = text.replaceAll("(?i)(" + QueryWords[i].substring(0,QueryWords[i].length()-1) + ")", "<strong>$1</strong>");
                    return highlightedText;
                }
            }
        }
        return null;
    }
}
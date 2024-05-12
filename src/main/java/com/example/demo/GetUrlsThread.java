package com.example.demo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetUrlsThread  implements  Runnable
{
    String word;
    List<JSONArray> jsonArrays;
    public GetUrlsThread( String word ,  List<JSONArray> jsonArrays)
    {
        this.word = word;
        this.jsonArrays = jsonArrays;
    }
    @Override
    public void run() {
        try {
            JSONArray ResultJsonArr = GetUrls(word);
            if(ResultJsonArr == null)
                return;
            synchronized (this.jsonArrays)
            {
                this.jsonArrays.add(ResultJsonArr);
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public  JSONArray GetUrls(String Word) throws JSONException {
        //Map<String,ResultDoc> UniqueResDocs=new HashMap<>();
        String res;
        try {
            res=Request.GetWord(Word);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        JSONObject jsonObject=new JSONObject(res);
        String ms=jsonObject.getString("message");
        if(ms.equals("This Word is not  existing"))
            return null;

        JSONArray UrlsArr= jsonObject.getJSONArray("URLS");

//        for(int i=0;i<UrlsArr.length();i++)
//        {
//
//            Object object =UrlsArr.getJSONObject(i);
//            JSONObject jObject=(JSONObject) object;
//            ResultDoc doc=new ResultDoc();
//            doc.Url=jObject.getString("Url");
//            if(UniqueResDocs.containsKey(doc.Url))
//            {
//                doc.WordsIncluded++;
//            }
//            doc.Title=jObject.getString("Title").toLowerCase();
//            //doc.=Word;
//            doc.rank=jObject.getInt("Rank");
//            doc.Tf=jObject.getJSONObject("TF").getDouble("$numberDecimal");
//            UniqueResDocs.put(doc.Url,doc);
//        }
        return UrlsArr;
    }
}

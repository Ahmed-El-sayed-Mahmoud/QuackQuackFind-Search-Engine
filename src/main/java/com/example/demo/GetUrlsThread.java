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
    String [] Words;
    public GetUrlsThread( String word ,  List<JSONArray> jsonArrays,String [] Words)
    {
        this.word = word;
        this.jsonArrays = jsonArrays;
        this.Words = Words;

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
                for(int i=0;i<Words.length;i++)
                    if(Words[i].equals(word))
                        Words[i]=Words[i]+"done";
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
        return UrlsArr;
    }
}

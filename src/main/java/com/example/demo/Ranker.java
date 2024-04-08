package com.example.demo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Ranker
{
    public static List<Doc> GetUrls(String Word) throws JSONException {
        List<Doc>ResultList=new ArrayList<>();
        Request request=new Request();
        String res;
        try {
            res=request.GetWord(Word);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        JSONObject jsonObject=new JSONObject(res);
        if(jsonObject.getString("message")=="This Word is not  existing")
            return null;
        JSONArray UrlsArr= jsonObject.getJSONArray("URLS");
        for(int i=0;i<UrlsArr.length();i++)
        {
            Object object =UrlsArr.getJSONObject(i);
            JSONObject jObject=(JSONObject) object;
            Doc doc=new Doc();
            doc.URL=jObject.getString("URL");
            doc.Title=jObject.getString("Title");
            doc.Word=Word;
            doc.Rank=jObject.getDouble("Rank");
            doc.TF=jObject.getDouble("TF");
            ResultList.add(doc);
        }
        return ResultList;


    }
}

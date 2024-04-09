package com.example.demo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class Ranker
{
    public static List<Doc> GetUrls(String Word) throws JSONException {
        List<Doc>ResultList=new ArrayList<>();
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
    public Map<String,ResultDoc> GetUniqueResultDocs(Map<String,List<Doc>> Dic)
    {
        Map<String,ResultDoc> UniqueResDocs=new HashMap<>();
        for (Map.Entry<String, List<Doc>> entry : Dic.entrySet()) {
            String key = entry.getKey();
            List<Doc> val = entry.getValue();
            for(Doc c: val)
            {
                ResultDoc resultDoc=new ResultDoc();
                resultDoc.Url=c.URL;
                resultDoc.Tf =c.TF;
                UniqueResDocs.put(resultDoc.Url,resultDoc);
            }

        }
        return UniqueResDocs;
    }
    public void SetTfIdf(Map<String,ResultDoc> UniqueResDocs,Map<String,List<Doc>> UrlsFromDB) throws InterruptedException {
        int NumUniqueUrls= UniqueResDocs.size();
        int NumQueryTerms=UrlsFromDB.size();
        Thread[] threads = new Thread[NumQueryTerms];
        int index=0;
        for (Map.Entry<String, List<Doc>> entry : UrlsFromDB.entrySet()) {
            String key = entry.getKey();
            threads[index]=new Thread(new RankerThread(UniqueResDocs,entry.getValue(),NumUniqueUrls));
            threads[index++].start();
        }
        for(int i=0;i<NumQueryTerms;i++)
            threads[i].join();

    }

    public List<ResultDoc>GetRankedDocsTfIdf(Map<String,ResultDoc> UniqueResDocs)
    {
        List<ResultDoc> list =new ArrayList<>( UniqueResDocs.values());
        Comparator<ResultDoc> TFIDFComparator = Comparator.comparing(ResultDoc::GetTfIdf).reversed();
        list.sort(TFIDFComparator);
        return  list;
    }
    public List<ResultDoc>GetResult(String Query) throws JSONException, InterruptedException {
        Map<String,List<Doc>> list=new HashMap<>();
        String[] wordsArray = Query.split("\\s+");
        for(String x : wordsArray)
        {
            List<Doc> WordUrls=GetUrls(x);
            if(WordUrls==null)
                continue;
            list.put(x,WordUrls);
        }
        Map<String,ResultDoc> UniqueResults= GetUniqueResultDocs(list);
        SetTfIdf(UniqueResults,list);
        List<ResultDoc> result=GetRankedDocsTfIdf(UniqueResults);
        return result;

    }

}

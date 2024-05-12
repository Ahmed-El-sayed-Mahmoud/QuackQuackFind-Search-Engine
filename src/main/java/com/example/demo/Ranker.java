package com.example.demo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.management.Query;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class Ranker
{
    int THREADS_NUMS=30;
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
            doc.URL=jObject.getString("Url");
            doc.Title=jObject.getString("Title").toLowerCase();
            doc.Word=Word;
            doc.Rank=jObject.getInt("Rank");
            doc.TF=jObject.getJSONObject("TF").getDouble("$numberDecimal");
            ResultList.add(doc);
        }
        return ResultList;
    }
    public Map<String,ResultDoc> GetUniqueResultDocs(Map<String,List<Doc>> Dic,String[]wordsArray)
    {
        Map<String,ResultDoc> UniqueResDocs=new HashMap<>();
        for (Map.Entry<String, List<Doc>> entry : Dic.entrySet()) {
           // String key = entry.getKey();
            List<Doc> val = entry.getValue();
            for(Doc c: val)
            {
                if(UniqueResDocs.containsKey(c.URL))
                {
                    UniqueResDocs.get(c.URL).WordsIncluded++;
                    continue;
                }
                ResultDoc resultDoc=new ResultDoc();
                resultDoc.Url=c.URL.toLowerCase();
                resultDoc.Tf =c.TF;
                resultDoc.rank=c.Rank;
                resultDoc.Title=c.Title.toLowerCase();
                int i=0;
                for(String s:wordsArray)
                {
                    if(resultDoc.Title.contains(wordsArray[i++]))
                        resultDoc.WordsTitleIncluded++;
                }
                resultDoc.QueryToTile= (double)resultDoc.WordsTitleIncluded/c.Title.split("\\s+").length;
                UniqueResDocs.put(resultDoc.Url,resultDoc);
            }

        }
        return UniqueResDocs;
    }
    public void SetTfIdf(Map<String,ResultDoc> UniqueResDocs,Map<String,List<ResultDoc>> UrlsFromDB) throws InterruptedException {
        int NumUniqueUrls= UniqueResDocs.size();
        int NumQueryTerms=UrlsFromDB.size();
        Thread[] threads = new Thread[NumQueryTerms];
        int index=0;
        for (Map.Entry<String, List<ResultDoc>> entry : UrlsFromDB.entrySet()) {
           // String key = entry.getKey();
            threads[index]=new Thread(new RankerThread(UniqueResDocs,entry.getValue(),NumUniqueUrls));
            threads[index++].start();
        }
        for(int i=0;i<NumQueryTerms;i++)
            threads[i].join();

    }

    public List<ResultDoc>GetRankedDocsTfIdf(Map<String,ResultDoc> UniqueResDocs)
    {
        List<ResultDoc> list =new ArrayList<>( UniqueResDocs.values());
        Comparator<ResultDoc> TFIDFComparator = Comparator.comparing(ResultDoc::WordsIncluded)
                .thenComparing(ResultDoc::WordsTitleIncluded)
                .thenComparing(ResultDoc::QueryToTile)
                .thenComparing(ResultDoc::WordsTitleTFIDF)
                .thenComparing(ResultDoc::CalcScores).thenComparing(ResultDoc::SecondScores).reversed();


       List<ResultDoc> SortedDocs= list.parallelStream()
                .sorted(TFIDFComparator).limit(200)
                .toList();
        return  SortedDocs;
    }
    public  List<ResultDoc>GetResult(String[] wordsArray,String NormalQuery,boolean PhraseSearch) throws JSONException, InterruptedException, IOException {
//        Map<String,List<Doc>> list=new HashMap<>();
//        //String[] wordsArray = Query.split("\\s+");
//        for(String x : wordsArray)
//        {
//            List<Doc> WordUrls=GetUrls(x);
//            if(WordUrls==null)
//                continue;
//            list.put(x,WordUrls);
//        }
//        Map<String,ResultDoc> UniqueResults= GetUniqueResultDocs(list,wordsArray);
//        SetTfIdf(UniqueResults,list);
//        List<ResultDoc> result=GetRankedDocsTfIdf(UniqueResults,wordsArray);
//        return result;
            DB db=new DB(Crawler.stopedWord());
            Map<String,ResultDoc> UniqueResults=new HashMap<>();
            Map<String,List<ResultDoc>> UrlsFromDB=new HashMap<>();

            List<JSONArray>JSONArrayList=new ArrayList<>();
            Thread[] threads = new Thread[wordsArray.length];
            for(int i=0;i<threads.length;i++)
            {
                threads[i]=new Thread(new GetUrlsThread(wordsArray[i],JSONArrayList));
                threads[i].start();
            }
            for(int i=0;i<threads.length;i++)
            {
                threads[i].join();
            }

            List<ResultDoc> ResultDocs=new ArrayList<>();
            String CurrentWord;
            for(int i=0;i<JSONArrayList.size();i++)
            {
                CurrentWord=wordsArray[i];
                for(int j=0;j<JSONArrayList.get(i).length();j++)
                {
                    JSONObject obj=JSONArrayList.get(i).getJSONObject(j);
                    ResultDoc doc=new ResultDoc();
                    doc.Url=obj.getString("Url");
                    doc.Title=obj.getString("Title").toLowerCase();
                    doc.rank=obj.getInt("Rank");
                    doc.Tf=obj.getJSONObject("TF").getDouble("$numberDecimal");
                    String [] title= doc.Title.toLowerCase().split("\\s+|\\s*-\\s*|\\s*\\.\\s*");
                    for(int n=0;n<title.length;n++)
                        title[n]=db.Stemmping(title[n]);
                    /////////////
                    //doc.Title= String.join(" ", title);///////////////////////// / / / / / / /
                    /////////////////////////////
                    String StemmedTitle=String.join(" ",title);
                    for(String s: wordsArray)
                    {
                        if(StemmedTitle.contains(s))
                            doc.WordsTitleIncluded++;
                    }
                    doc.QueryToTile= (double)doc.WordsTitleIncluded/title.length;
                    ResultDocs.add(doc);
                    if(!UniqueResults.containsKey(doc.Url))
                        UniqueResults.put(doc.Url,doc);
                    if(!UrlsFromDB.containsKey(CurrentWord))
                    {
                        List<ResultDoc> temp=new ArrayList<>();
                        temp.add(doc);
                        UrlsFromDB.put(CurrentWord,temp);
                    }
                    else
                        UrlsFromDB.get(CurrentWord).add(doc);
                }
            }

            SetTfIdf(UniqueResults,UrlsFromDB);
            List<ResultDoc> FinalResultDocs=GetRankedDocsTfIdf(UniqueResults);



            int StartIndex=0;
            int LastIndex;
            if(FinalResultDocs.size()<THREADS_NUMS)
                THREADS_NUMS=FinalResultDocs.size();
            Thread[] ResultThreads=new Thread[THREADS_NUMS];
            for (int i=0;i<ResultThreads.length;i++)
            {
                StartIndex=i*(FinalResultDocs.size()/THREADS_NUMS);
                if(i==THREADS_NUMS-1)
                    LastIndex=FinalResultDocs.size()-i*(FinalResultDocs.size()/THREADS_NUMS);
                else
                    LastIndex=StartIndex+(FinalResultDocs.size()/THREADS_NUMS);
                if(StartIndex<LastIndex&&LastIndex<FinalResultDocs.size())
                {
                    ResultThreads[i]=new Thread(new ResultDocThread(FinalResultDocs.subList(StartIndex,LastIndex),
                            PhraseSearch,NormalQuery));

                    ResultThreads[i].start();
                }

            }
            for(int i=0;i<ResultThreads.length;i++)
                if(ResultThreads[i]!=null)
                    ResultThreads[i].join();





        return FinalResultDocs;
    }

}

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
    int THREADS_NUMS=10;
    DB db=new DB(Crawler.stopedWord());
    public static List<Doc> GetUrls(String Word) throws JSONException {
        if(Word.length()==0)
            return null;
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
//            doc.URL=jObject.getString("Url");
//            doc.Title=jObject.getString("Title").toLowerCase();
//            doc.Word=Word;
//            doc.Rank=jObject.getInt("Rank");
//            doc.TF=jObject.getJSONObject("TF").getDouble("$numberDecimal");
            ResultList.add(doc);
        }
        return ResultList;
    }
//    public Map<String,ResultDoc> GetUniqueResultDocs(Map<String,List<ResultDoc>> Dic,String[]wordsArray)
//    {
//        Map<String,ResultDoc> UniqueResDocs=new HashMap<>();
//        for (Map.Entry<String, List<ResultDoc>> entry : Dic.entrySet()) {
//           // String key = entry.getKey();
//            List<ResultDoc> val = entry.getValue();
//            for(ResultDoc c: val)
//            {
//                if(UniqueResDocs.containsKey(c.URL))
//                {
//                    UniqueResDocs.get(c.URL).WordsIncluded++;
//                    continue;
//                }
//                ResultDoc resultDoc=new ResultDoc();
//                resultDoc.Url=c.URL.toLowerCase();
//                resultDoc.Tf =c.TF;
//                resultDoc.rank=c.Rank;
//                resultDoc.Title=c.Title.toLowerCase();
//                int i=0;
//                for(String s:wordsArray)
//                {
//                    if(resultDoc.Title.contains(wordsArray[i++]))
//                        resultDoc.WordsTitleIncluded++;
//                }
//                resultDoc.QueryToTile= (double)resultDoc.WordsTitleIncluded/c.Title.split("\\s+").length;
//                UniqueResDocs.put(resultDoc.Url,resultDoc);
//            }
//
//        }
//        return UniqueResDocs;
//    }
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

    public List<ResultDoc>GetRankedDocsTfIdf(Map<String,ResultDoc> UniqueResDocs,boolean PhraseSearch,int WordsNum,boolean logical) throws InterruptedException
    {
        List<ResultDoc> list =new ArrayList<>( UniqueResDocs.values());
        if(PhraseSearch)
            list=list.stream().filter(t->t.WordsIncluded==WordsNum).toList();
        Comparator<ResultDoc> TFIDFComparator = Comparator.comparing(ResultDoc::WordsIncluded)
                .thenComparing(ResultDoc::WordsTitleIncluded)
                .thenComparing(ResultDoc::QueryToTile)
                .thenComparing(ResultDoc::WordsTitleTFIDF)
                .thenComparing(ResultDoc::CalcScores).thenComparing(ResultDoc::SecondScores).reversed();

        List<ResultDoc> SortedDocs;
        if(!logical)
       SortedDocs= list.parallelStream()
                .sorted(TFIDFComparator).limit(100)
                .toList();
        else SortedDocs= list.parallelStream()
                .sorted(TFIDFComparator)
                .toList();
        return  SortedDocs;
    }
    public  List<ResultDoc>GetResult(String[] wordsArray,String NormalQuery,boolean PhraseSearch,boolean logical) throws JSONException, InterruptedException, IOException {
            Map<String,ResultDoc> UniqueResults=new HashMap<>();
            Map<String,List<ResultDoc>> UrlsFromDB=new HashMap<>();

            List<JSONArray>JSONArrayList=new ArrayList<>();
            Thread[] threads = new Thread[wordsArray.length];
            for(int i=0;i<threads.length;i++)
            {
                if(wordsArray[i]!="")
                {
                    threads[i] = new Thread(new GetUrlsThread(wordsArray[i], JSONArrayList,wordsArray));
                    threads[i].start();
                }
            }
            for(int i=0;i<threads.length;i++)
            {
                if(threads[i]!=null)
                    threads[i].join();
            }
            if(JSONArrayList.isEmpty())
                return new ArrayList<ResultDoc>();
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
                    else
                        UniqueResults.get(doc.Url).WordsIncluded++;
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
            List<ResultDoc> FinalResultDocs=GetRankedDocsTfIdf(UniqueResults,PhraseSearch,wordsArray.length,logical);


//            int StartIndex=0;
//            int LastIndex;
            if(FinalResultDocs.size()<THREADS_NUMS)
                THREADS_NUMS=FinalResultDocs.size();
            Thread[] ResultThreads=new Thread[THREADS_NUMS];
            int index=0;
            for (int i=0;i<ResultThreads.length;i++)
            {

//                if(i==THREADS_NUMS-1)
//                {
//                    StartIndex=10-(i+1)*(10/THREADS_NUMS);
//                    LastIndex=10;
//                }
//
//                else
//                {
//                    StartIndex=i*(10/THREADS_NUMS);
//                    LastIndex=StartIndex+(10/THREADS_NUMS);
//                }

//                if(StartIndex<=LastIndex&&LastIndex<=10)
//                {
                List<ResultDoc>FinalResultsWithDes=new ArrayList<ResultDoc>();
                    if(PhraseSearch) {
                        if (index + 2 <=FinalResultDocs.size())
                        {
                            ResultThreads[i] = new Thread(new ResultDocThread(FinalResultDocs,index, index + 2,
                                    PhraseSearch, NormalQuery));
                            index+=2;
                        }

                        else if(i+1<=FinalResultDocs.size())
                        {

                            ResultThreads[i] = new Thread(new ResultDocThread(FinalResultDocs,index,index+1,
                                    PhraseSearch, NormalQuery));
                            index+=1;
                        }

                    }
                    else
                        ResultThreads[i]=new Thread(new ResultDocThread(FinalResultDocs,i,i+1,
                            PhraseSearch,NormalQuery));
                    
                    ResultThreads[i].start();
                //}
            }
            for(int i=0;i<ResultThreads.length;i++)
                if(ResultThreads[i]!=null)
                    ResultThreads[i].join();



        return FinalResultDocs;
    }
    public List<ResultDoc> GetDescription(List<ResultDoc> docs,String NormalQuery,boolean PhraseSearch) throws InterruptedException {
        int StartIndex=0;
        int LastIndex;
        if(docs.size()<THREADS_NUMS)
            THREADS_NUMS=docs.size();
        Thread[] ResultThreads=new Thread[THREADS_NUMS];
        for (int i=0;i<ResultThreads.length;i++)
        {
            if(THREADS_NUMS==1)
            {
                StartIndex=0;
                LastIndex=docs.size();
            }
            else if(i==THREADS_NUMS-1)
            {
                StartIndex=docs.size()-(i)*(docs.size()/THREADS_NUMS);
                LastIndex=docs.size()-1;
            }

            else
            {
                StartIndex=i*(docs.size()/THREADS_NUMS);
                LastIndex=StartIndex+(docs.size()/THREADS_NUMS);
            }

            if(StartIndex<=LastIndex&&LastIndex<=docs.size())
            {
                ResultThreads[i]=new Thread(new ResultDocThread(docs,StartIndex,LastIndex+1,
                        PhraseSearch,NormalQuery));

                ResultThreads[i].start();
            }
        }
        for(int i=0;i<ResultThreads.length;i++)
            if(ResultThreads[i]!=null)
                ResultThreads[i].join();

        return docs;

    }
    public List<ResultDoc> LogicalSearch(String []Queries,int operation) throws IOException, InterruptedException {
        String[][]StemmedQuiries=new String[Queries.length][];
        for(int i=0;i<Queries.length;i++)
        {
            Queries[i]=Queries[i].substring(1,Queries[i].length()-1).toLowerCase();
            StemmedQuiries[i]=Queries[i].split("\\s+");
            for(int j=0;j<StemmedQuiries[i].length;j++)
            {
                StemmedQuiries[i][j]= db.Stemmping(StemmedQuiries[i][j]);
            }
            List<String> list = new ArrayList<>(Arrays.asList(StemmedQuiries[i]));
            list.removeIf(t -> t.equals(""));
            StemmedQuiries[i] = list.toArray(new String[0]);
        }
       List<List<ResultDoc>> QuiriesResults=new ArrayList<List<ResultDoc>>();
        Thread[] threads=new Thread[Queries.length];
        for(int i=0;i<Queries.length;i++)
        {
            List<ResultDoc>Result=new ArrayList<ResultDoc>();
            QuiriesResults.add(Result);
            threads[i]=new Thread(new LogicalSearchThread(Queries[i],QuiriesResults.get(i),StemmedQuiries[i]));
            threads[i].start();
        }
        for(int i=0;i<Queries.length;i++)
            threads[i].join();

        List<ResultDoc> mainList = QuiriesResults.get(0);
        switch (operation) {
            case 0:
                for(int i=1;i<QuiriesResults.size();i++)
                    mainList.retainAll(QuiriesResults.get(i));

                break;
            case 1:
                for(int i=1;i<QuiriesResults.size();i++)
                    mainList.addAll(QuiriesResults.get(i));

                break;
            case 2:
                for(int i=1;i<QuiriesResults.size();i++)
                    mainList.removeAll(QuiriesResults.get(i));

                break;
        }
        HashSet<ResultDoc> setWithoutDuplicates = new HashSet<>(mainList);
        List<ResultDoc> listWithoutDuplicates = new ArrayList<>(setWithoutDuplicates);
       SortLogical(mainList);
//        List<ResultDoc> linal=new ArrayList<ResultDoc>();
//        for(int i=0;i<QuiriesResults.get(1).size();i++)
//        {
//
//            if(QuiriesResults.get(0).contains(QuiriesResults.get(1).get(i)));
//                linal.add(QuiriesResults.get(1).get(i));
//        }
//        return linal;
      return  mainList;
    }
    public List<ResultDoc> SortLogical(List<ResultDoc> Docs)
    {
        Comparator<ResultDoc> Comparator = java.util.Comparator.comparing(ResultDoc::logical)
                .reversed();
        return Docs.parallelStream()
                .sorted(Comparator)
                .toList();
    }

}

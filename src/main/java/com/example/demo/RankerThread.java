package com.example.demo;

import java.util.List;
import java.util.Map;

public class RankerThread implements Runnable
{
    Map<String, ResultDoc> uniqueResDocs;
    List<ResultDoc> DocsFromDb;
    int numUniqueUrls;
    public RankerThread(Map<String, ResultDoc> uniqueResDocs, List<ResultDoc> value, int numUniqueUrls)
    {
        this.numUniqueUrls=numUniqueUrls;
        this.uniqueResDocs=uniqueResDocs;
        this.DocsFromDb=value;
    }

    public void GetTfIdf()
    {

        int size=DocsFromDb.size();
        for(ResultDoc doc : DocsFromDb)
        {
            synchronized (this.uniqueResDocs.get(doc.Url))
            {

                if(numUniqueUrls<=size)
                {
                    uniqueResDocs.get(doc.Url).TfIdf += doc.Tf ;

                }
                else
                    uniqueResDocs.get(doc.Url).TfIdf += doc.Tf * Math.log10((double)numUniqueUrls/size);


            }
        }
    }

    @Override
    public void run() {
        GetTfIdf();
    }
}

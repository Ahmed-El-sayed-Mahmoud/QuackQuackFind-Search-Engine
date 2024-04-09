package com.example.demo;

import java.util.List;
import java.util.Map;

public class RankerThread implements Runnable
{
    Map<String, ResultDoc> uniqueResDocs;
    List<Doc> DocsFromDb;
    int numUniqueUrls;
    public RankerThread(Map<String, ResultDoc> uniqueResDocs, List<Doc> value, int numUniqueUrls)
    {
        this.numUniqueUrls=numUniqueUrls;
        this.uniqueResDocs=uniqueResDocs;
        this.DocsFromDb=value;
    }

    public void GetTfIdf()
    {
        int size=DocsFromDb.size();
        for(Doc doc : DocsFromDb)
        {
            synchronized (this.uniqueResDocs.get(doc.URL))
            {
                uniqueResDocs.get(doc.URL).TfIdf += doc.TF * Math.log10( ((double)numUniqueUrls / size));
            }
        }
    }

    @Override
    public void run() {
        GetTfIdf();
    }
}

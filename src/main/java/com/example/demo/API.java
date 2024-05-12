package com.example.demo;

import org.json.JSONException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class API {
    DB db;
    Ranker ranker=new Ranker();
    public API()
    {
        db = new DB(Crawler.stopedWord());
    }
    @GetMapping("search/{query}")
    public List<ResultDoc> greet(@PathVariable String query) throws JSONException, IOException, InterruptedException {
        boolean PhraseSearch=false;
        if(query.charAt(0)=='"')
        {
            PhraseSearch=true;
            query=query.substring(1,query.length()-1);
        }
        String NormalQuery=db.RemoveStopWords(query);
        String[] StemmedQueryWords=query.split("\\s+");
        for(int i=0;i<StemmedQueryWords.length;i++)
        {
           StemmedQueryWords[i]=db.Stemmping(StemmedQueryWords[i]);
        }
        List<String> list = new ArrayList<>(Arrays.asList(StemmedQueryWords));
        list.remove("");
        StemmedQueryWords = list.toArray(new String[0]);
        //String str=db.Stemmping(query);
        for(String s : StemmedQueryWords)
            System.out.println(s);

        return  ranker.GetResult(StemmedQueryWords,NormalQuery,PhraseSearch);
    }
}

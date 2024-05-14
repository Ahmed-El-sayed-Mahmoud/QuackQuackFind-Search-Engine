package com.example.demo;

import org.json.JSONException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@CrossOrigin(origins = "*")
@RestController

public class API {
    public API(){
        int x=0;
    }
    DB db=new DB(Crawler.stopedWord());
    Ranker ranker=new Ranker();

    @GetMapping("search/{query}")
    public List<ResultDoc> greet(@PathVariable String query) throws JSONException, IOException, InterruptedException {
        boolean PhraseSearch=false;
        boolean LogicalSearch=false;

        if(query.contains("AND")||query.contains("OR")||query.contains("NOT"))
        {

            String[] Queries =query.split("\\s+(?i)(and|or|not)\\s+");
            int Op=0;
            if(query.contains("AND"))
                Op=0;
            else if(query.contains("OR"))
                Op=1;
            else
                Op=2;
            return ranker.LogicalSearch(Queries,Op);
        }
        query=query.toLowerCase();
        if(query.charAt(0)=='"')
        {
            PhraseSearch=true;
            query=query.substring(1,query.length()-1);
        }
        String NormalQuery=query;
        if(!PhraseSearch)
            NormalQuery=db.RemoveStopWords(query);
        String[] StemmedQueryWords=query.split("\\s+");
        for(int i=0;i<StemmedQueryWords.length;i++)
        {
           StemmedQueryWords[i]=db.Stemmping(StemmedQueryWords[i]);
        }
        List<String> list = new ArrayList<>(Arrays.asList(StemmedQueryWords));
        list.removeIf(t -> t.equals(""));
        String ll=db.RemoveStopWords(String.join(" ",list));
        list=new ArrayList<>(Arrays.asList(ll.split("\\s+")));
        StemmedQueryWords = list.toArray(new String[0]);
        //String str=db.Stemmping(query);
        for(String s : StemmedQueryWords)
            System.out.println(s);

        return  ranker.GetResult(StemmedQueryWords,NormalQuery,PhraseSearch);
    }

    @PostMapping("api")
    public List<ResultDoc> processDocs(@RequestBody DocReq docReq ) throws JSONException, IOException, InterruptedException {
        // Process the array of Doc objects here3
        ResultDoc[] docs= docReq.docs;
        String NormalQuery=docReq.NormalQuery.toLowerCase();
        boolean PhraseSearch=false;
        if(NormalQuery.charAt(0)=='"')
        {
            PhraseSearch=true;
            NormalQuery=NormalQuery.substring(1,NormalQuery.length()-1);
        }
        return ranker.GetDescription(Arrays.asList(docs),NormalQuery,PhraseSearch);
    }
}

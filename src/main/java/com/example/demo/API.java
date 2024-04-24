package com.example.demo;

import org.json.JSONException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class API {
    DB db =new DB();
    Ranker ranker=new Ranker();
    @GetMapping("search/{query}")
    public List<ResultDoc> greet(@PathVariable String query) throws JSONException, IOException, InterruptedException {
        String[] QueryWords=query.split("\\s+");
        for(int i=0;i<QueryWords.length;i++)
        {
            QueryWords[i]=db.Stemmping(QueryWords[i]);
        }
        //String str=db.Stemmping(query);
        for(String s : QueryWords)
            System.out.println(s);
       return  ranker.GetResult(QueryWords);
    }
}

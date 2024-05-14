package com.example.demo;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogicalSearchThread implements Runnable
{
    String NormalQuery;
    List<ResultDoc> Result;
    Ranker ranker;
    String[]StemmedWords;
    public LogicalSearchThread(String word, List<ResultDoc> result, String []StemmedWords)
    {
        NormalQuery=word;
        Result=result;
        this.StemmedWords=StemmedWords;
        //ranker=new Ranker();
        this.ranker=new Ranker();
    }
    @Override
    public void run() {
        try {
            Result.addAll( ranker.GetResult(StemmedWords,NormalQuery,true,true));

        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.example.demo;

import org.json.JSONException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class SearchEngineApplication {

    public static void main(String[] args) throws JSONException, URISyntaxException, InterruptedException, FileNotFoundException {
        Object lock = new Object();
        //	SpringApplication.run(SearchEngineApplication.class, args);
        Crawler bot = new Crawler();
        /*
        bot.stopedWord();
        bot.getFromFile();//get urls from file
        int number_Threads = 1000;
        for (int i = 0; i < number_Threads; i++) {
            Thread thread = new DB(number_Threads, lock);
            thread.setName(Integer.toString(i));
            thread.start();
        }
*/
        bot.StartFromSeed(100, 1000, 3);
        //bot.getFromFileInsetIntoDB();
        //List<Doc> list =Ranker.GetUrls("list");
//		Map<String,List<Doc>> list=new HashMap<>();
//		List<Doc>list1=new ArrayList<>();
//		list1.add(new Doc("url1","sdf",1,1,"list"));
//		list1.add(new Doc("url2","sdf",1,1,"list"));
//		list1.add(new Doc("url3","sdf",1,1,"list"));
//		list.put("list",list1);
//		List<Doc>list2=new ArrayList<>();
//		list2.add(new Doc("url4","sdf",1,1,"list"));
//		list2.add(new Doc("url2","sdf",1,1,"list"));
//		list2.add(new Doc("url1","sdf",1,1,"list"));
//		list.put("hash",list2);
        //	Ranker ranker=new Ranker();
//		Map<String,ResultDoc> map= ranker.GetUniqueResultDocs(list);
//		ranker.SetTfIdf(map,list);
//		List<ResultDoc> re=ranker.GetRankedDocsTfIdf(map);
        //	List<ResultDoc> re=ranker.GetResult("data structure integer string");

    }

}

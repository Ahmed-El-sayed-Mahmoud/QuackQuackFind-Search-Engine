package com.example.demo;

import org.json.JSONException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

@SpringBootApplication
public class SearchEngineApplication {

    public static void main(String[] args) throws JSONException, URISyntaxException, InterruptedException, FileNotFoundException {
        Object lock = new Object();
        //	SpringApplication.run(SearchEngineApplication.class, args);
        Crawler bot = new Crawler();
<<<<<<< Updated upstream
=======

       // bot.StartFromSeed(100, 1000, 3);
        System.out.println("Please enter a number represent the starting of indexing links for example 0 mean store first 500 links 1 for the other 500 links and so on up to 13 for index the last 500 links");
        Scanner scanner=new Scanner(System.in);
        int links_number=scanner.nextInt();
        System.out.println(links_number);
>>>>>>> Stashed changes
        bot.stopedWord();
        bot.getFromFile(links_number);//get urls from file
        int number_Threads = 500;
        for (int i = 0; i < number_Threads; i++) {
            Thread thread = new DB(number_Threads, lock);
            thread.setName(Integer.toString(i));
            thread.start();
        }
<<<<<<< Updated upstream

        //bot.StartFromSeed(100, 10000, 3);
=======
>>>>>>> Stashed changes
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

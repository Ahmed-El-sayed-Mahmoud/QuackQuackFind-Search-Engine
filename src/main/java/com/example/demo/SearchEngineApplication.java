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

    public static void main(String[] args) throws JSONException, URISyntaxException, InterruptedException, IOException {
//        Object lock = new Object();
	SpringApplication.run(SearchEngineApplication.class, args);
//        Crawler bot = new Crawler();
//
//
//        // bot.StartFromSeed(100, 1000, 3);0
//        System.out.println("Please enter a number represent the starting of indexing links for example 0 mean store first 500 links 1 for the other 500 links and so on up to 13 for index the last 500 links");
//        Scanner scanner = new Scanner(System.in);
//        int links_number = scanner.nextInt();
//        System.out.println(links_number);
//
//        Set<String> set = bot.stopedWord();
//        bot.getFromFile(links_number);//get urls from file
//        DB db = new DB(lock, set);
//        db.insert();
//        int number_Threads = 200;
//        for (int i = 0; i < number_Threads; i++) {
//            DB thread = new DB(number_Threads,lock,set);
//
//        }


        //bot.StartFromSeed(100, 10000, 3);

        	//Ranker ranker=new Ranker();

        	//List<ResultDoc> re=ranker.GetResult("algorithm");
    }

}

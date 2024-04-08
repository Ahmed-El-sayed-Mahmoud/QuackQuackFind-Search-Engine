package com.example.demo;
import org.json.JSONException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URISyntaxException;
import java.util.List;

@SpringBootApplication
public class SearchEngineApplication {

	public static void main(String[] args) throws JSONException, URISyntaxException, InterruptedException {


		SpringApplication.run(SearchEngineApplication.class, args);
		Crawler bot = new Crawler();
		bot.stopedWord();
//
		bot.StartFromSeed(100, 100000, 3);
		bot.getFromFileInsetIntoDB();

	}

}

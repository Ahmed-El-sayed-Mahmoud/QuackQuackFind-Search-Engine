package com.example.demo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Request {
    public String post(String postURL,String Data) throws URISyntaxException, IOException, InterruptedException {
        var uri = new URI(postURL);

        var httpClient = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.ofString(Data))
                .header("Content-type", "application/json")
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Handle the response body
        String responseBody = response.body();

        return responseBody;

    }
    public String GetWord (String Word)throws URISyntaxException, IOException, InterruptedException
    {
        var uri = new URI("http://localhost:3000/Word/Get/"+Word);

        var httpClient = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(uri)
                .GET()
                .header("Content-type", "application/json")
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Handle the response body
        String responseBody = response.body();

        return responseBody;
    }

}

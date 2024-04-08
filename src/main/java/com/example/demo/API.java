package com.example.demo;

import org.json.JSONException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class API {
    @GetMapping("greet")
    public List<Doc> greet() throws JSONException {
        List<Doc> list= Ranker.GetUrls("list");

        return list;
    }
}

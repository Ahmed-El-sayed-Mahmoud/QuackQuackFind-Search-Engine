package com.example.demo;

public class Doc {
    public String URL;
    public String Title;
    public int Rank = 0;
    public double TF = 0;
    public String Word;

    public Doc(String URL, String title, int rank, double TF, String word) {
        this.URL = URL;
        this.Title = title;
        this.Rank = rank;
        this.TF = TF;
        this.Word = word;
    }

    public Doc() {

    }
}
package com.example.demo;

import static java.util.Objects.hash;

public class ResultDoc {
    public double TfIdf = 0;
    public String Describtion;
    public String Title;
    public String Url;
    public double Tf;
    public int rank;
    public String Word;
    public int WordsIncluded = 1;
    public double Score;
    public int WordsTitleIncluded = 0;
    public double QueryToTile;
    public String NormalTitle;
    public boolean Show=true;

    @Override
    public int hashCode() {
        return hash(Url);
    }

    public double GetTfIdf() {
        return TfIdf;
    }
    public int logical()
    {
        return Describtion==null?0:1;
    }
    public double CalcScores() {
        Score = 4 * WordsIncluded + 10 * WordsTitleIncluded;
        return Score;
    }
    public double WordsTitleIncluded()
    {
        return WordsTitleIncluded;
    }
    public double WordsTitleTFIDF()
    {
        return WordsTitleIncluded+TfIdf*500;
    }
    public double WordsIncluded()
    {
        return WordsIncluded;
    }
    public double QueryToTile()
    {
        return  QueryToTile+TfIdf*500;
    }
    public double SecondScores() {
        Score = 1000 * TfIdf + .6 * rank;
        return Score;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (Url.equals(((ResultDoc) obj).Url)) return true;

        else
            return false;
    }

}

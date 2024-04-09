package com.example.demo;

import static java.util.Objects.hash;

public class ResultDoc {
    public double TfIdf=0;
    public String Describtion;
    public String Title;
    public String Url;
    public double Tf;
    @Override
    public int hashCode() {
        return hash(Url);
    }
public double GetTfIdf()
{
    return  TfIdf;
}
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if(Url==((ResultDoc) obj).Url)
            return  true;
        else
            return false;
    }

}

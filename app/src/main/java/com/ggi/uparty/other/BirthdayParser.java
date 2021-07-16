package com.ggi.uparty.other;

public class BirthdayParser {
    public int birthYear;
    public int birthMonth;
    public int birthDay;
    public BirthdayParser(String raw){
        String month = raw.substring(0,raw.indexOf("/"));
        int firstSlash = raw.indexOf("/");
        int secondSlash = raw.indexOf("/",firstSlash + 1);
        String day = raw.substring(firstSlash + 1, secondSlash);
        String year = raw.substring(secondSlash + 1, raw.length());

        birthYear = Integer.parseInt(year);
        birthMonth = Integer.parseInt(month);
        birthDay = Integer.parseInt(day);
    }
}

package com.example;


public class Event {
    private int id;
    private String title;
    private int year, month, day, hour, minute;

    public Event(int id, String title, int year, int month, int day, int hour, int minute) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    public Event(String title, int year, int month, int day, int hour, int minute) {
        this(-1, title, year, month, day, hour, minute);
    }

    // getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getDay() { return day; }
    public int getHour() { return hour; }
    public int getMinute() { return minute; }
}

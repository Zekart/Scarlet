package com.example.testscarler.model;

import java.io.Serializable;

public class Ticker implements Serializable {
    private String time;
    private String price;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}

package com.example.testscarler.model;

import java.io.Serializable;
import java.util.List;

public class Subscriber implements Serializable {
    private String type = "subscribe";
    private List<String> product_ids;
    private List<String> channels;

    public Subscriber() {
    }

    public String getSubscriber() {
        return type;
    }

    public void setSubscriber(String subscriber) {
        this.type = subscriber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getProduct_ids() {
        return product_ids;
    }

    public void setProduct_ids(List<String> product_ids) {
        this.product_ids = product_ids;
    }

    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }
}

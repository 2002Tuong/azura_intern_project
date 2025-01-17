package com.emopass.antitheftalarm.model;

import java.io.Serializable;

public class SelectModel implements Serializable {

    private int id;
    private String title;

    public SelectModel(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

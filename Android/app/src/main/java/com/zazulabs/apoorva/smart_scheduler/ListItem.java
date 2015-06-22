package com.zazulabs.apoorva.smart_scheduler;

/**
 * Created by Saajan on 4/10/15.
 */
public class ListItem {

    private int id;
    private String title;
    private String icon;
    private String status;

    public ListItem(int id, String title, String status) {
        this.id = id;
        this.title = title;
        this.status = status;
    }

    public int getId(){
        return id;
    }
    public String getTitle(){
        return title;
    }
    public String getIcon(){
        return icon;
    }
    public String getStatus(){
        return status;
    }
}

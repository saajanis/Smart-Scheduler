package com.zazulabs.apoorva.smart_scheduler;

/**
 * Created by apoorva on 4/4/15.
 */
public class Model {
    private int icon;
    private String title;

    // private boolean isGroupHeader = false;

    public Model(String title) {
        this(-1,title);
        //isGroupHeader = true;
    }
    public Model(int icon, String title) {
        super();
        this.icon = icon;
        this.title = title;
    }
    public String getTitle(){
        return title;
    }
}

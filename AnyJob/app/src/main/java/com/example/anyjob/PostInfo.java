package com.example.anyjob;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class PostInfo implements Serializable {
    private String title;
    private ArrayList<String> description;
    private String publisher;
    private Date createdAt;
    private String id;

    public PostInfo(String title, ArrayList<String> description, String publisher, Date createdAt, String id){
        this.title = title;
        this.description = description;
        this.publisher = publisher;
        this.createdAt = createdAt;
        this.id = id;
    }

    public PostInfo(String title, ArrayList<String> description, String publisher, Date createdAt){
        this.title = title;
        this.description = description;
        this.publisher = publisher;
        this.createdAt = createdAt;
    }

    public String getTitle(){
        return this.title;
    }
    public void setTitle(){
        this.title = title;
    }

    public ArrayList<String> getDescription(){
        return this.description;
    }
    public void setDescription(){
        this.description = description;
    }

    public String getpublisher(){
        return this.publisher;
    }
    public void setpublisher(){
        this.publisher = publisher;
    }

    public Date getcreatedAt(){
        return this.createdAt;
    }
    public void setCreatedAt(){
        this.createdAt = createdAt;
    }

    public String getId(){
        return this.id;
    }
    public void setId(){
        this.id = id;
    }
}

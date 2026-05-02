package com.example.finalprojectapp;

public class Room {
    public boolean getID;
    private int id;
    private String name;

    public Room(int id, String name){
        this.id = id;
        this.name = name;
    }
    
    public String getName(){
        return name;
    }

    public int getID(){
        return id;
    }
}

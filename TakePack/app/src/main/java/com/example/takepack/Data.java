package com.example.takepack;

import java.util.ArrayList;

public class Data {
    public String name;
    public ArrayList<String> Items;

    public Data() {}

    public Data(String name,double lat,double lon,ArrayList<String> Items) {
        this.name = name;
        this.Items = Items;
    }
}

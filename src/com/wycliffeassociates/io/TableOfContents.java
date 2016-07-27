package com.wycliffeassociates.io;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by sarabiaj on 7/27/2016.
 */
public class TableOfContents {

    ArrayList<Map<String, ArrayList<Map<String, ArrayList<Map<String, ArrayList<Map<String, ArrayList<Map<String, String>>>>>>>>>> wat;

    public TableOfContents(String json){
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Map<String, ArrayList<Map<String, ArrayList<Map<String, ArrayList<Map<String, ArrayList<Map<String, String>>>>>>>>>>>(){}.getType();
        JsonReader jsr = new JsonReader(new StringReader(json));
        wat = gson.fromJson(jsr, type);
        System.out.println(wat.size());
    }

    public int length(){
        return wat.size();
    }
}

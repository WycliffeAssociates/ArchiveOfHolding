package com.wycliffeassociates.io;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sarabiaj on 7/27/2016.
 */
public class TableOfContents {

    //ArrayList<Map<String, ArrayList<Map<String, ArrayList<Map<String, ArrayList<Map<String, ArrayList<Map<String, String>>>>>>>>>> wat;
    Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>>> wat;


    public TableOfContents(String json){
        Gson gson = new Gson();
        //Type type = new TypeToken<ArrayList<Map<String, ArrayList<Map<String, ArrayList<Map<String, ArrayList<Map<String, ArrayList<Map<String, String>>>>>>>>>>>(){}.getType();
        Type type = new TypeToken<Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>>>>(){}.getType();
        JsonReader jsr = new JsonReader(new StringReader(json));
        wat = gson.fromJson(jsr, type);
        System.out.println(wat.size());
    }

    public void extract(File inputFile, File outputDirectory, long tableOfContentsSize){
        Set<String> languages = wat.keySet();
        for(String language : languages){
            Map<String, ?> sourceMap = wat.get(language);
            Set<String> sources = sourceMap.keySet();
            for(String source : sources){
                Map<String, ?> bookMap = (Map<String,?>)(sourceMap.get(source));
                Set<String> books = bookMap.keySet();
                for(String book : books){
                    Map<String, ?> chapterMap = (Map<String, ?>)(bookMap.get(book));
                    Set<String> chapters = chapterMap.keySet();
                    for(String chapter : chapters){
                        Map<String, ?> verseMap = (Map<String, ?>)(chapterMap.get(chapter));
                        Set<String> verses = verseMap.keySet();
                        for(String verse : verses){
                            File base = new File(outputDirectory, language + "/" + source + "/" + book + "/" + chapter);
                            File file = new File(base, verse);
                            Map<String, String> fileMap = (Map<String, String>)(verseMap.get(verse));
                            base.mkdirs();
                            long start = Long.parseLong(fileMap.get("start"));
                            long end = Long.parseLong(fileMap.get("length"));
                            try {
                                writeFile(file, inputFile, start+tableOfContentsSize-4, end);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private void writeFile(File file, File inputFile, long start, long end) throws IOException{
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        FileInputStream fis = new FileInputStream(inputFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        try{
            bis.skip(start);
            long sizeRemaining = end;
            byte[] buffer = new byte[5096];
            int len;
            while (sizeRemaining > 0) {
                if(buffer.length > sizeRemaining){
                    buffer = new byte[(int)sizeRemaining];
                }
                len = bis.read(buffer);
                if(len == -1){
                    break;
                }
                bos.write(buffer, 0, len);
                sizeRemaining -= len;
            }
        } finally {
            bos.close();
            fos.close();
            bis.close();
            fis.close();
        }
    }

    private void extractFiles(File outputDirectory, List<Map<String, ?>> files){
        for(Map<String, ?> map : files){
            Set<String> keys = map.keySet();
            for(String key : keys){
                Object o = map.get(key);
                if(o instanceof String){
          //          keys.
                } else {

                }
            }
        }
    }


    public int length(){
        return wat.size();
    }
}

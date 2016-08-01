package com.wycliffeassociates.io;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * Created by sarabiaj on 7/27/2016.
 */
public class LanguageLevel implements ArchiveOfHolding.TableOfContents {

    Map<String,Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>>>> mMap;

    public LanguageLevel(){}

    @Override
    public void parseJSON(String json){
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String,Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>>>>>(){}.getType();
        JsonReader jsr = new JsonReader(new StringReader(json));
        mMap = gson.fromJson(jsr, type);
    }

    @Override
    public void extract(File inputFile, File outputDirectory, long tableOfContentsSize){
        Map<String, ?> root = mMap.get((String)(mMap.keySet().toArray()[0]));
        Set<String> languages = root.keySet();
        for(String language : languages){
            Map<String, ?> sourceMap = (Map<String, ?>)root.get(language);
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
                                writeFile(file, inputFile, start+tableOfContentsSize+12, end);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public ArchiveOfHoldingEntry getEntry(InputStream is, String entryName, String...paths){
        if(mMap == null || mMap.keySet().size() <= 0){
            return  null;
        }
        Map<String, ?> root = mMap.get((String)(mMap.keySet().toArray()[0]));
        Map<String,?> iterate = root;
        Set<String> hasWhatINeed;
        for(String dir : paths){
            hasWhatINeed = iterate.keySet();
            if(hasWhatINeed != null && hasWhatINeed.contains(dir)) {
                iterate = (Map<String, ?>) iterate.get(dir);
            } else {
                return null;
            }
        }
        hasWhatINeed = iterate.keySet();
        String key = null;
        for(String s : hasWhatINeed){
            if(s.contains(entryName)){
                key = s;
            }
        }
        if(key != null) {
            Map<String, String> entry = (Map<String, String>) iterate.get(key);
            long start = Long.parseLong(entry.get("start"));
            long length = Long.parseLong(entry.get("length"));
            ArchiveOfHoldingEntry aohEntry = new ArchiveOfHoldingEntry(is, start, length, key);
            return aohEntry;
        } else {
            return null;
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
}

package org.wycliffeassociates.io;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportantSection {
    String chapter;
    String firstVerse;
    String lastVerse;

    //Extracts the identifiable section of a filename for source audio
    public ImportantSection(String name) {
        String CHAPTER = "(?:c[0]{0,2}([\\d]{1,3})_)?";
        String VERSE = "v[0]{0,2}([\\d]{1,3})(?:-[0]{0,2}([\\d]{1,3}))?";
        Pattern chapterAndVerseSection = Pattern.compile(CHAPTER + VERSE + "$");
        Matcher matcher = chapterAndVerseSection.matcher(name);
        if (matcher.find()) {
            this.chapter = matcher.group(1);
            this.firstVerse = matcher.group(2);
            this.lastVerse = matcher.group(3);
        }
    }

    public Pattern getPattern() {
        String CHAPTER = "";
        String VERSE = "";

        if(this.chapter != null) {
            CHAPTER += "c[0]{0,2}" + this.chapter + "_";
        }
        if(this.firstVerse != null) {
            // ?<!obs_ part is to avoid wrong matching with obs verses
            // that have version (v4) in their names
            VERSE += "(?<!obs_)v[0]{0,2}" + this.firstVerse;
        }
        if(this.lastVerse != null) {
            VERSE += "-[0]{0,2}" + this.lastVerse;
        }

        return Pattern.compile(CHAPTER + VERSE);
    }
}

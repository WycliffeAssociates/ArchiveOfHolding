package com.wycliffeassociates;

import com.wycliffeassociates.io.ArchiveOfHolding;
import com.wycliffeassociates.io.ArchiveOfHoldingEntry;
import com.wycliffeassociates.io.LanguageLevel;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Main {

    static String mCommand;
    static String mInputPath;
    static String mOutputPath;
    static String mName;
    static boolean mUseTr;

    public static void main(String[] args) {
        if(!handleArguments(args)){
            printExample();
            return;
        }

        if(mCommand.compareTo("-c") == 0) {
            ArchiveOfHolding aoh = new ArchiveOfHolding();
            if(mOutputPath == null) {
                aoh.createArchiveOfHolding(new File(mInputPath), mUseTr);
            } else {
                if(mName == null) {
                    aoh.createArchiveOfHolding(new File(mInputPath), new File(mOutputPath), mUseTr);
                } else {
                    aoh.createArchiveOfHolding(new File(mInputPath), new File(mOutputPath), mName, mUseTr);
                }
            }
        } else if (mCommand.compareTo("-x") == 0){
            File file = new File(mInputPath);
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                LanguageLevel ll = new LanguageLevel();
                ArchiveOfHolding aoh = new ArchiveOfHolding(bis, ll);
                bis.close();
                File input = new File(mInputPath);
                aoh.extractArchive(input, input.getParentFile());
                System.out.println(aoh.getHeader());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fis.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        } else {
            printExample();
        }
    }

    public static void printExample() {
        System.out.println("Invalid command.\n" +
                "Please use the following format:\n" +
                "aoh -c path/to/input\n" +
                "aoh -c path/to/output path/to/save/to\n" +
                "aoh -x path/to/archive\n" +
                "aoh -x path/to/archive path/to/extract/to"
        );
    }

    public static boolean handleArguments(String[] args){
        if (args.length < 2 || args.length > 3) {
            printExample();
            return false;
        }
        List<String> argList = new LinkedList<>(Arrays.asList(args));
        if(argList.contains("-x")){
            mCommand = "-x";
            argList.remove("-x");
        } else if(argList.contains("-c")) {
            mCommand = "-c";
            argList.remove("-c");
        } else {
            return false;
        }
        if(argList.contains("-tr")){
            mUseTr = true;
            argList.remove("-tr");
        }
        if(argList.size() > 0) {
            mInputPath = argList.remove(0);
        } else {
            return false;
        }
        if(argList.size() > 0){
            mOutputPath = argList.remove(0);
        }
        if(argList.size() > 0){
            mName = argList.remove(0);
        }
        return true;
    }
}

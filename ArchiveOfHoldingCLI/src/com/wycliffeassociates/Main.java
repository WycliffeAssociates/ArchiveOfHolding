package com.wycliffeassociates;

import com.wycliffeassociates.io.ArchiveOfHolding;
import com.wycliffeassociates.io.ArchiveOfHoldingEntry;
import com.wycliffeassociates.io.LanguageLevel;

import java.io.*;

public class Main {

    static String mCommand;
    static String mInputPath;
    static String mOutputPath;

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            printExample();
            return;
        }
        mCommand = args[0];
        mInputPath = args[1];
        if (args.length == 3) {
           mOutputPath = args[2];
        }

        if(mCommand.compareTo("-c") == 0) {
            ArchiveOfHolding aoh = new ArchiveOfHolding();
            aoh.createArchiveOfHolding(new File(mInputPath));
        } else if (mCommand.compareTo("-x") == 0){
            File file = new File(mInputPath);
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                LanguageLevel ll = new LanguageLevel();
                ArchiveOfHolding aoh = new ArchiveOfHolding(bis, ll);
                bis.close();
                //fis.close();
                File input = new File(mInputPath);
                aoh.extractArchive(input, input.getParentFile());
                System.out.println(aoh.getHeader());
                fis.close();

                fis = new FileInputStream(file);
                ll = new LanguageLevel();
                aoh = new ArchiveOfHolding(fis, ll);

                ArchiveOfHoldingEntry entry = aoh.getEntry("55_2ti_c04_v04", "cmn", "ulb", "2ti", "04");
                InputStream wav = entry.getInputStream();
                File test = new File("test.wav");
                FileOutputStream fos = new FileOutputStream(test);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                try{
                    byte[] buffer = new byte[5096];
                    int len;
                    while ((len = wav.read(buffer)) != -1) {
                        bos.write(buffer, 0, len);
                    }
                } finally {
                    bos.close();
                    fos.close();
                    fis.close();
                    wav.close();
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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
}

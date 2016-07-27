package com.wycliffeassociates;

import com.wycliffeassociates.io.ArchiveOfHolding;

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
                ArchiveOfHolding aoh = new ArchiveOfHolding(bis);
                System.out.println(aoh.getHeader());
                bis.close();
                fis.close();
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

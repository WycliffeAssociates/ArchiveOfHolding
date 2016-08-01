package com.wycliffeassociates.io;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by sarabiaj on 7/27/2016.
 */
public class ArchiveOfHolding {

    private static final int MAGIC_NUMBER = 0x616f6321;
    private long position = 0;
    private Header mHeader;
    TableOfContents mTable;
    InputStream mInputStream;

    public interface TableOfContents {
        void parseJSON(String json);
        void extract(File inputFile, File outputDirectory, long tableOfContentsSize);
        ArchiveOfHoldingEntry getEntry(InputStream is, String entryName, String... paths);
    }

    private class Header{

        String mTableJSON;
        long mTableOfContentsSize;
        TableOfContents mTable;

        Header(String tableOfContents){
            mTableJSON = tableOfContents;
        }

        Header(InputStream input, TableOfContents toc) throws IOException{
            readJsonFromStream(input);
            mTable = toc;
            mTable.parseJSON(mTableJSON);
        }

        long getTableOfContentsSize(){
            return mTableOfContentsSize;
        }

        void readJsonFromStream(InputStream input) throws IOException{
            byte[] bytes = new byte[4];
            input.read(bytes);
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            bb.order(ByteOrder.BIG_ENDIAN);
            int magic = bb.getInt();
            if(magic != MAGIC_NUMBER){
                return;
            }
            bytes = new byte[8];
            input.read(bytes);
            bb = ByteBuffer.wrap(bytes);
            mTableOfContentsSize = bb.getLong();
            int sizeRemaining = (int)mTableOfContentsSize;
            BoundedInputStream boundedIS = new BoundedInputStream(input, mTableOfContentsSize);
            mTableJSON = "";
            byte[] buffer = new byte[sizeRemaining];
            int len;
            while (sizeRemaining > 0) {
                if (buffer.length < sizeRemaining) {
                    buffer = new byte[(int) sizeRemaining];
                }
                len = boundedIS.read(buffer);
                if (len == -1) {
                    break;
                }
                sizeRemaining -= len;
                bb = ByteBuffer.wrap(buffer, 0, len);
                mTableJSON += new String(buffer, 0, len, Charset.forName("UTF-8"));
            }
        }

        void writeHeader(File output) throws IOException {
            FileOutputStream fos = new FileOutputStream(output, true);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            DataOutputStream dos = new DataOutputStream(bos);
            try {
                byte[] b = mTableJSON.getBytes(Charset.forName("UTF-8"));
                dos.writeInt(com.wycliffeassociates.io.ArchiveOfHolding.MAGIC_NUMBER);
                dos.writeLong(b.length);
                dos.flush();
                bos.write(b);
            } finally {
                dos.close();
                bos.close();
                fos.close();
            }
        }
        String getTableOfContentsJSON(){
            return mTableJSON;
        }
    }

    public ArchiveOfHolding(){}

    public ArchiveOfHolding(InputStream input, TableOfContents toc){
        try {
            mHeader = new Header(input, toc);
            mTable = toc;
            mInputStream = input;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArchiveOfHoldingEntry getEntry(String entryName, String...paths){
        return mTable.getEntry(mInputStream, entryName, paths);
    }

    public void extractArchive(File inputFile, File outputLocation){
        mTable.extract(inputFile, outputLocation, mHeader.getTableOfContentsSize());
    }

    public String getHeader(){
        return mHeader.getTableOfContentsJSON();
    }

    public String createTableOfContents(File directory){
        String json = "{";
        if (!directory.isDirectory()) {
            json += addFile(directory);
        } else {
            json += addDirectory(directory);
        }
        json += "}";
        return json;
    }

    protected String addFile(File file){
        String json = "";
        long length = file.length();
        String name = file.getName();
        long start = position;
        json += "\"" + name + "\":{";
        json += "\"start\":\"" + String.valueOf(start) + "\",";
        json += "\"length\":\"" + String.valueOf(length) + "\"}";
        position += length;
        return json;
    }

    protected String addDirectory(File directory){
        String json = "\"" + directory.getName() + "\"";
        json += ":{";
        File[] files = directory.listFiles();
        //sort to guarantee the same order in ToC as writing
        Arrays.sort(files);
        for(int i = 0; i < files.length; i++) {
            if(files[i].isDirectory()){
                json += addDirectory(files[i]);
            } else {
                json += addFile(files[i]);
            }
            if(i+1 < files.length){
                json += ",";
            }
        }
        json += "}";
        return json;
    }

    protected void writeFiles(File input, File output) throws IOException {
        if(input.isDirectory()){
            File[] files = input.listFiles();
            //sort to guarantee the same order as ToC
            Arrays.sort(files);
            for(File f : files){
                writeFiles(f, output);
            }
        } else {
            FileInputStream fis = new FileInputStream(input);
            BufferedInputStream bis = new BufferedInputStream(fis);
            FileOutputStream fos = new FileOutputStream(output, true);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            try {
                byte[] buffer = new byte[5096];
                int len;
                while ((len = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }
            } finally {
                bis.close();
                fis.close();
                bos.close();
                fos.close();
            }
        }
    }

    public void createArchiveOfHolding(File input){
        createArchiveOfHolding(input, input.getParentFile());
    }

    public void createArchiveOfHolding(File input, File outputDirectory){
        File output = new File(outputDirectory, "archive.aoh");
        if(output.exists()){
            output.delete();
        }
        String toc = createTableOfContents(input);
        System.out.println(toc);
        Header header = new Header(toc);
        try {
            header.writeHeader(output);
            writeFiles(input, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

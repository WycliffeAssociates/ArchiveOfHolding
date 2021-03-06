package org.wycliffeassociates.io;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by sarabiaj on 7/27/2016.
 */
public class ArchiveOfHolding {

    public interface OnProgressListener{
        void onProgressUpdate(int progress);
    }

    //ASCII aoh!
    private static final int MAGIC_NUMBER = 0x616f6821;
    private long position = 0;
    private Header mHeader;
    TableOfContents mTable;
    InputStream mInputStream;
    private OnProgressListener mProgressListener;
    private long mTotalInputSize;
    private long mTotalProgress;


    public interface TableOfContents {
        void parseJSON(String json);
        void extract(File inputFile, File outputDirectory, long tableOfContentsSize);
        ArchiveOfHoldingEntry getEntry(InputStream is, ChapterVerseSection chapterVerseSection, String... paths);
        String getVersionSlug(String sourceLanguage);
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

        void writeHeader(OutputStream output) throws IOException {
            DataOutputStream dos = new DataOutputStream(output);
            byte[] b = mTableJSON.getBytes(Charset.forName("UTF-8"));
            dos.writeInt(ArchiveOfHolding.MAGIC_NUMBER);
            dos.writeLong(b.length);
            dos.flush();
            output.write(b);
        }

        String getTableOfContentsJSON(){
            return mTableJSON;
        }
    }

    public ArchiveOfHolding(){}

    public ArchiveOfHolding(OnProgressListener progressListener){
        mProgressListener = progressListener;
    }

    public ArchiveOfHolding(InputStream input, TableOfContents toc){
        try {
            mHeader = new Header(input, toc);
            mTable = toc;
            mInputStream = input;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArchiveOfHoldingEntry getEntry(ChapterVerseSection chapterVerseSection, String...paths){
        return mTable.getEntry(mInputStream, chapterVerseSection, paths);
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

    protected void writeFiles(File input, OutputStream output) throws IOException {
        if(input.isDirectory()){
            File[] files = input.listFiles();
            //sort to guarantee the same order as ToC
            Arrays.sort(files);
            for(File f : files){
                writeFiles(f, output);
            }
        } else {
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(input);
                bis = new BufferedInputStream(fis);

                byte[] buffer = new byte[20384];
                int len;
                while ((len = bis.read(buffer)) != -1) {
                    output.write(buffer, 0, len);
                    mTotalProgress += len;
                    updateProgress();
                }
            } finally {
                bis.close();
                fis.close();
            }
        }
    }

    private void updateProgress(){
        if(mProgressListener != null){
            //consider the header to be 10% so the remainder is the file progress
            int percentage = (int)((double)(mTotalProgress) / (double)(mTotalInputSize) * 90.0);
            mProgressListener.onProgressUpdate(percentage + 10);
        }
    }

    public void createArchiveOfHolding(File input) throws IOException{
        createArchiveOfHolding(input, input.getParentFile(), input.getName(), false);
    }

    public void createArchiveOfHolding(File input, boolean useTr) throws IOException{
        createArchiveOfHolding(input, input.getParentFile(), input.getName(), useTr);
    }

    public void createArchiveOfHolding(File input, File outputDirectory, String name) throws IOException{
        createArchiveOfHolding(input, input.getParentFile(), name, false);
    }

    public void createArchiveOfHolding(File input, File outputDirectory) throws IOException{
        createArchiveOfHolding(input, outputDirectory, input.getName(), false);
    }

    public void createArchiveOfHolding(File input, File outputDirectory, boolean useTr) throws IOException{
        createArchiveOfHolding(input, outputDirectory, input.getName(), useTr);
    }

    public void createArchiveOfHolding(File input, File outputDirectory, String name, boolean useTr) throws IOException {
        if(useTr){
            name += ".tr";
        } else {
            name += ".aoh";
        }
        File output = new File(outputDirectory, name);
        if(output.exists()){
            output.delete();
        }
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(output);
            bos = new BufferedOutputStream(fos);
            createArchiveOfHolding(input, bos);
        } finally {
            bos.flush();
            bos.close();
            fos.close();
        }
    }

    public void createArchiveOfHolding(File input, OutputStream output) throws IOException{
        mTotalInputSize = getTotalProgressSize(input);
        String toc = createTableOfContents(input);
        System.out.println(toc);
        Header header = new Header(toc);
        header.writeHeader(output);
        writeFiles(input, output);
    }

    private long getTotalProgressSize(File input) {
        long total = 0;
        if (input.isDirectory()) {
            for (File f : input.listFiles()) {
                total += getTotalProgressSize(f);
            }
        }
        return total + input.length();
    }

    public void setOnProgressListener(OnProgressListener progressListener){
        mProgressListener = progressListener;
    }
}

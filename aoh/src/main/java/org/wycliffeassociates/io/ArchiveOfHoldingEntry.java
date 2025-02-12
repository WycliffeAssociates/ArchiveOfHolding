package org.wycliffeassociates.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sarabiaj on 7/29/2016.
 */
public class ArchiveOfHoldingEntry {

    protected InputStream mInputStream;
    protected long mStart;
    protected long mLength;
    protected String mName;

    public ArchiveOfHoldingEntry(InputStream is, long start, long length, String name){
        mInputStream = is;
        mStart = start;
        mLength = length;
        mName = name;
    }

    public String getName(){
        return mName;
    }

    public long getStart(){
        return mStart;
    }

    public long getLength(){
        return mLength;
    }

    public InputStream getInputStream() throws IOException {
        return new ArchiveOfHoldingInputStream(mInputStream, this);
    }

    public InputStream getInputStream(long skip) throws IOException {
        return new ArchiveOfHoldingInputStream(mInputStream, this, skip);
    }

    private class ArchiveOfHoldingInputStream extends BoundedInputStream {
        ArchiveOfHoldingInputStream(InputStream is, ArchiveOfHoldingEntry aohEntry) throws IOException {
            super(is, aohEntry.getLength());
            is.skip(aohEntry.getStart());
        }
        ArchiveOfHoldingInputStream(InputStream is, ArchiveOfHoldingEntry aohEntry, long skip) throws IOException {
            super(is, aohEntry.getLength());
            is.skip(skip);
        }
    }
}

package com.wycliffeassociates.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sarabiaj on 7/29/2016.
 */
public class ArchiveOfHoldingEntry {

    protected InputStream mInputStream;
    protected long mStart;
    protected long mLength;

    public ArchiveOfHoldingEntry(InputStream is, long start, long length){
        mInputStream = is;
        mStart = start;
        mLength = length;
    }

    protected long getStart(){
        return mStart;
    }

    public long getLength(){
        return mLength;
    }

    public InputStream getInputStream() throws IOException {
        return new ArchiveOfHoldingInputStream(mInputStream, this);
    }

    private class ArchiveOfHoldingInputStream extends InputStream {

        ArchiveOfHoldingEntry mAohEntry;
        InputStream mInputStream;
        long bytesRead = 0;

        ArchiveOfHoldingInputStream(InputStream is, ArchiveOfHoldingEntry aohEntry) throws IOException {
            mInputStream = is;
            mAohEntry = aohEntry;
            is.skip(aohEntry.getStart());
        }

        @Override
        public int read() throws IOException {
            if (mAohEntry.getLength() == bytesRead) {
                return -1;
            } else {
                bytesRead++;
                return mInputStream.read();
            }
        }
    }
}

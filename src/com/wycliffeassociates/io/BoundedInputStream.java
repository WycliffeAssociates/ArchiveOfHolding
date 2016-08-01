package com.wycliffeassociates.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sarabiaj on 8/1/2016.
 */
public class BoundedInputStream extends InputStream {

    long mLength;
    InputStream mInputStream;
    long mBytesRead = 0;

    BoundedInputStream(InputStream is, long length) throws IOException {
        mInputStream = is;
        mLength = length;
    }

    @Override
    public int read() throws IOException {
        if (mLength == mBytesRead) {
            return -1;
        } else {
            mBytesRead++;
            return mInputStream.read();
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if(mBytesRead >= mLength){
            return -1;
        }
        if(length <= 0){
            return 0;
        }
        if((mBytesRead+(length-offset)) > mLength){
            length = (int)(mLength - mBytesRead);
        }
        int returnVal = mInputStream.read(buffer, offset, length);
        if(returnVal > 0){
            mBytesRead += returnVal;
        }
        return returnVal;
    }
}

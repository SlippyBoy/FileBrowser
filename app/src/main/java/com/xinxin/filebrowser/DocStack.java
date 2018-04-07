package com.xinxin.filebrowser;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

public class DocStack extends LinkedList<DocItem> implements Durable, Parcelable {

    public static final Creator<DocStack> CREATOR = new Creator<DocStack>() {
        @Override
        public DocStack createFromParcel(Parcel in) {
            DocStack docStack = new DocStack();
            DurableUtils.readFromParcel(in, docStack);
            return docStack;
        }

        @Override
        public DocStack[] newArray(int size) {
            return new DocStack[size];
        }
    };
    public RootDir rootDir;

    public String getTitle() {
        if (rootDir != null) {
            return rootDir.title;
        }
        return null;
    }

    public String getSubTitle() {
        if (size() == 1) {
            return null;
        } else if (size() > 1) {
            return peek().path;
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        DurableUtils.writeToParcel(dest, this);
    }

    @Override
    public void reset() {
        clear();
        rootDir = null;
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        if (in.readBoolean()) {
            rootDir = new RootDir();
            rootDir.read(in);
        }
        final int size = in.readInt();
        for (int i = 0; i < size; i++) {
            DocItem docItem = new DocItem();
            docItem.read(in);
            add(docItem);
        }
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        if (rootDir != null) {
            out.writeBoolean(true);
            rootDir.write(out);
        } else {
            out.writeBoolean(false);
        }
        final int size = size();
        out.writeInt(size);
        for (int i = 0; i < size; i++) {
            final DocItem docItem = get(i);
            docItem.write(out);
        }
    }
}

package com.xinxin.filebrowser;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class DocItem implements Durable, Parcelable {

    public static final int TYPE_DIR = 1;
    public static final int TYPE_FILE = 2;
    public static final Creator<DocItem> CREATOR = new Creator<DocItem>() {
        @Override
        public DocItem createFromParcel(Parcel in) {
            DocItem docItem = new DocItem();
            DurableUtils.readFromParcel(in, docItem);
            return docItem;
        }

        @Override
        public DocItem[] newArray(int size) {
            return new DocItem[size];
        }
    };
    public @DocType
    int docType;

    public String path;
    public String name;
    public int childCount;
    public long size;
    public long lastModify;

    public DocItem() {
        reset();
    }

    public boolean isContainer() {
        return docType == TYPE_DIR;
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
        docType = -1;
        path = null;
        name = null;
        childCount = 0;
        size = 0;
        lastModify = 0;
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        docType = in.readInt();
        path = DurableUtils.readNullableString(in);
        name = DurableUtils.readNullableString(in);
        childCount = in.readInt();
        size = in.readLong();
        lastModify = in.readLong();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(docType);
        DurableUtils.writeNullableString(out, path);
        DurableUtils.writeNullableString(out, name);
        out.writeInt(childCount);
        out.writeLong(size);
        out.writeLong(lastModify);
    }

    @Override
    public String toString() {
        return "DocItem{" +
                "docType=" + docType +
                ", path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", childCount=" + childCount +
                ", size=" + size +
                ", lastModify=" + lastModify +
                '}';
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface DocType {
    }
}

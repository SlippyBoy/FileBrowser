package com.xinxin.filebrowser;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RootDir implements Durable, Parcelable {

    public static final Creator<RootDir> CREATOR = new Creator<RootDir>() {
        @Override
        public RootDir createFromParcel(Parcel in) {
            RootDir rootDir = new RootDir();
            DurableUtils.readFromParcel(in, rootDir);
            return rootDir;
        }

        @Override
        public RootDir[] newArray(int size) {
            return new RootDir[size];
        }
    };
    public String path;
    public String title;

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
        path = null;
        title = null;
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        path = DurableUtils.readNullableString(in);
        title = DurableUtils.readNullableString(in);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        DurableUtils.writeNullableString(out, path);
        DurableUtils.writeNullableString(out, title);
    }
}

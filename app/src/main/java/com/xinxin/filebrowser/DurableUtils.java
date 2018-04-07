package com.xinxin.filebrowser;

import android.os.BadParcelableException;
import android.os.Parcel;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.xinxin.filebrowser.Shared.TAG;

public class DurableUtils {

    public static <D extends Durable> byte[] writeToArray(D d) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        d.write(new DataOutputStream(out));
        return out.toByteArray();
    }

    public static <D extends Durable> D readFromArray(byte[] data, D d) throws IOException {
        if (data == null) throw new IOException("Missing data");
        final ByteArrayInputStream in = new ByteArrayInputStream(data);
        d.reset();
        try {
            d.read(new DataInputStream(in));
        } catch (IOException e) {
            d.reset();
            throw e;
        }
        return d;
    }

    public static <D extends Durable> byte[] writeToArrayOrNull(D d) {
        try {
            return writeToArray(d);
        } catch (IOException e) {
            Log.w(TAG, "Failed to write", e);
            return null;
        }
    }

    public static <D extends Durable> D readFromArrayOrNull(byte[] data, D d) {
        try {
            return readFromArray(data, d);
        } catch (IOException e) {
            Log.w(TAG, "Failed to read", e);
            return null;
        }
    }

    public static <D extends Durable> void writeToParcel(Parcel parcel, D d) {
        try {
            parcel.writeByteArray(writeToArray(d));
        } catch (IOException e) {
            throw new BadParcelableException(e);
        }
    }

    public static <D extends Durable> D readFromParcel(Parcel parcel, D d) {
        try {
            return readFromArray(parcel.createByteArray(), d);
        } catch (IOException e) {
            throw new BadParcelableException(e);
        }
    }

    public static void writeNullableString(DataOutputStream out, String value) throws IOException {
        if (value != null) {
            out.write(1);
            out.writeUTF(value);
        } else {
            out.write(0);
        }
    }

    public static String readNullableString(DataInputStream in) throws IOException {
        if (in.read() != 0) {
            return in.readUTF();
        } else {
            return null;
        }
    }
}

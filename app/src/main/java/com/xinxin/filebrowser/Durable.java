package com.xinxin.filebrowser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Durable {
    void reset();

    void read(DataInputStream in) throws IOException;

    void write(DataOutputStream out) throws IOException;
}

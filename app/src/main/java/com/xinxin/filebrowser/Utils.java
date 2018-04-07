package com.xinxin.filebrowser;

public class Utils {

    private static final int _1K = 1024;
    private static final int _1M = 1024 * 1024;
    private static final int _1G = 1024 * 1024 * 1024;
    private Utils() {
    }

    public static String formatSize(long size) {
        if (size < _1K) {
            return size + "B";
        } else if (size < _1M) {
            return String.format("%.2sK", size * 1.0f / _1K);
        } else if (size < _1G) {
            return String.format("%.2sM", size * 1.0f / _1M);
        } else {
            return String.format("%.2sG", size * 1.0f / _1G);
        }
    }
}

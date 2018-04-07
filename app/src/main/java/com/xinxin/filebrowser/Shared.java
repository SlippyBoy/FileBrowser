package com.xinxin.filebrowser;

import android.content.Intent;

public final class Shared {

    public static final String TAG = "FileBrowser";

    public static final boolean DEBUG = true;

    public static final String ACTION_PICK = "ACTION_PICK";

    public static final String EXTRA_STATE = "state";

    public static final String EXTRA_FILE_MAX_SIZE = "file_max_size";

    public static final String EXTRA_FILE_MAX_COUNT = "file_max_count";

    public static final String FILE_SCHEMA = "file";

    public static final String EXTRA_ROOT = "root";

    public static final String EXTRA_DOC = "doc";

    public static boolean isSelectMode(Intent intent) {
        return ACTION_PICK.equals(intent.getAction());
    }
}

package com.xinxin.filebrowser;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

public class DirectoryLoader extends AsyncTaskLoader<DirectoryResult> {

    private static final String TAG = "DirectoryLoader";

    private static Comparator<File> sFileComparator = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            if (o1.isDirectory() && o2.isDirectory()) {
                return o1.getName().compareTo(o2.getName());
            }
            if (o1.isDirectory() && !o2.isDirectory()) {
                return -1;
            }
            if (!o1.isDirectory() && o2.isDirectory()) {
                return 1;
            }

            if (o1.lastModified() > o2.lastModified()) {
                return -1;
            }

            if (o1.lastModified() < o2.lastModified()) {
                return 1;
            }

            return o1.getName().compareTo(o2.getName());
        }
    };

    private static FileFilter sFileFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden();
        }
    };

    private String mDir;
    private DirectoryResult mResult;

    public DirectoryLoader(@NonNull Context context, String dir) {
        super(context);
        mDir = dir;
    }

    @Nullable
    @Override
    public DirectoryResult loadInBackground() {
        long start = System.currentTimeMillis();

        final DirectoryResult result = new DirectoryResult();

        final File dir = new File(mDir);
        try {
            File[] files = dir.listFiles(sFileFilter);
            if (Shared.DEBUG)
                Log.d(TAG, "Loader list use: " + (System.currentTimeMillis() - start) + " ms");
            if (files != null) {
                Arrays.sort(files, sFileComparator);
                if (Shared.DEBUG)
                    Log.d(TAG, "Loader sort use: " + (System.currentTimeMillis() - start) + " ms");
                DocItem[] items = new DocItem[files.length];
                File file;
                File[] c;
                for (int i = 0; i < items.length; i++) {
                    items[i] = new DocItem();
                    file = files[i];
                    items[i].path = file.getAbsolutePath();
                    items[i].name = file.getName();
                    if (file.isDirectory()) {
                        items[i].docType = DocItem.TYPE_DIR;
                        items[i].childCount = ((c = file.listFiles(sFileFilter)) == null ? 0 : c.length);
                    } else {
                        items[i].size = file.length();
                        items[i].lastModify = file.lastModified();
                    }
                }
                if (Shared.DEBUG)
                    Log.d(TAG, "Loader new use: " + (System.currentTimeMillis() - start) + " ms");
                result.docItems = items;
            } else {
                result.docItems = new DocItem[0];
            }
        } catch (Exception e) {
            result.exception = e;
        }

        if (Shared.DEBUG) Log.d(TAG, "Loader use: " + (System.currentTimeMillis() - start) + " ms");
        return result;
    }

    @Override
    public void deliverResult(@Nullable DirectoryResult data) {
        if (isReset()) {
            return;
        }

        mResult = data;

        if (isStarted()) {
            super.deliverResult(mResult);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mResult != null) {
            deliverResult(mResult);
        }

        if (takeContentChanged() || mResult == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        mResult = null;
    }
}

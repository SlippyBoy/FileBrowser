package com.xinxin.filebrowser;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.xinxin.filebrowser.Shared.DEBUG;

public class State implements Parcelable {
    private static final String TAG = "State";

    public boolean selectMode;
    public long fileMaxSize;
    public int fileMaxCount;
    public DocStack stack = new DocStack();
    public HashMap<String, SparseArray<Parcelable>> dirState = new HashMap<>();
    private boolean mStackTouched;
    private HashSet<String> mSelection;
    private long mTotalSize;

    public void initExtras(Intent intent) {
        selectMode = Shared.isSelectMode(intent);
        if (selectMode) {
            fileMaxSize = intent.getLongExtra(Shared.EXTRA_FILE_MAX_SIZE, 0);
            fileMaxCount = intent.getIntExtra(Shared.EXTRA_FILE_MAX_COUNT, 0);
            mSelection = new HashSet<>();
        }
    }

    public void onRootChanged(RootDir root) {
        if (DEBUG) Log.d(TAG, "Root changed to: " + root);
        stack.rootDir = root;
        stack.clear();
        mStackTouched = true;
    }

    public void pushDoc(DocItem info) {
        if (DEBUG) Log.d(TAG, "Adding doc to stack: " + info);

        stack.push(info);
        mStackTouched = true;
    }

    public void popDoc() {
        if (DEBUG) Log.d(TAG, "Popping doc off stack.");
        stack.pop();
        mStackTouched = true;
    }

    public void setStack(DocStack stack) {
        if (DEBUG) Log.d(TAG, "Setting the stack to: " + stack);
        this.stack = stack;
        mStackTouched = true;
    }

    public boolean hasLocationChanged() {
        return mStackTouched;
    }

    public boolean isSelected(DocItem docItem) {
        return mSelection.contains(docItem.path);
    }

    public void removeSelect(DocItem docItem) {
        mTotalSize -= docItem.size;
        mSelection.remove(docItem.path);
    }

    public boolean isValidDoc(DocItem docItem) {
        return docItem.size < fileMaxSize;
    }

    public boolean isReadCountLimit() {
        return mSelection.size() >= fileMaxCount;
    }

    public void addSelect(DocItem docItem) {
        mTotalSize += docItem.size;
        mSelection.add(docItem.path);
    }

    public long getTotalSize() {
        return mTotalSize;
    }

    public int getSelectionCount() {
        return mSelection.size();
    }

    public String[] getSelections() {
        ArrayList<String> selection = new ArrayList<String>(mSelection);
        return selection.toArray(new String[0]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(selectMode ? 1 : 0);
        dest.writeLong(fileMaxSize);
        dest.writeInt(fileMaxCount);
        DurableUtils.writeToParcel(dest, stack);
        dest.writeInt(mStackTouched ? 1 : 0);
        dest.writeMap(dirState);
        if (selectMode) {
            dest.writeStringList(new ArrayList<>(mSelection));
            dest.writeLong(mTotalSize);
        }
    }

    public static final ClassLoaderCreator<State> CREATOR = new ClassLoaderCreator<State>() {

        @Override
        public State createFromParcel(Parcel in, ClassLoader loader) {
            final State state = new State();
            state.selectMode = in.readInt() == 1 ? true : false;
            state.fileMaxSize = in.readLong();
            state.fileMaxCount = in.readInt();
            state.stack = DurableUtils.readFromParcel(in, state.stack);
            state.mStackTouched = in.readInt() == 1 ? true : false;
            state.dirState = in.readHashMap(loader);
            if (state.selectMode) {
                ArrayList<String> selected = new ArrayList<>();
                in.readStringList(selected);
                state.mSelection = new HashSet<String>(selected);
                state.mTotalSize = in.readLong();
            }
            return state;
        }

        @Override
        public State createFromParcel(Parcel in) {
            return createFromParcel(in, null);
        }

        @Override
        public State[] newArray(int size) {
            return new State[size];
        }
    };
}

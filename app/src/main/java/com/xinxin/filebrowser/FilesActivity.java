package com.xinxin.filebrowser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.xinxin.filebrowser.ui.LabelView;

import static com.xinxin.filebrowser.Shared.DEBUG;

public class FilesActivity extends AppCompatActivity {

    private static final String TAG = "FilesActivity";

    private State mState;

    private TextView mRootNameView;
    private LabelView mTotalSizeView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        final Intent intent = getIntent();
        final Uri uri = intent.getData();

        mState = getState(savedInstanceState);

        if (!mState.stack.isEmpty()) {
            if (DEBUG) Log.d(TAG, "Launching with saved state.");
            refreshCurrentRootAndDirectory(AnimationView.ANIM_NONE);
        } else if (uri != null && isSupported(uri)) {
            if (DEBUG) Log.d(TAG, "Launching with root URI.");
            loadRoot(uri);
        } else {
            if (DEBUG) Log.d(TAG, "All other means skipped. Launching into default directory.");
            loadRoot(getDefaultRoot());
        }

        setupView();

        setResult(Activity.RESULT_CANCELED);
    }

    private void setupView() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (mState.selectMode) {
            findViewById(R.id.bottom_bar).setVisibility(View.VISIBLE);
            mRootNameView = findViewById(R.id.root_name);
            mRootNameView.setText(mState.stack.getTitle());
            mTotalSizeView = findViewById(R.id.total_size);

            updateBottomBar();
        }
    }

    private State getState(@Nullable Bundle icicle) {
        if (icicle != null) {
            State state = icicle.getParcelable(Shared.EXTRA_STATE);
            if (DEBUG) Log.d(TAG, "Recovered existing state object: " + state);
            return state;
        }

        final Intent intent = getIntent();

        State state = new State();
        state.initExtras(intent);

        if (DEBUG) Log.d(TAG, "Created new state object: " + state);
        return state;
    }

    private void refreshCurrentRootAndDirectory(int anim) {
        refreshDirectory(anim);

        updateTitle();
    }

    private void refreshDirectory(int anim) {
        final FragmentManager fm = getSupportFragmentManager();
        final RootDir root = getCurrentRoot();
        final DocItem cwd = getCurrentDir();

        FilesFragment.showDirectory(fm, root, cwd, anim);
    }

    private RootDir getCurrentRoot() {
        if (mState.stack.rootDir != null) {
            return mState.stack.rootDir;
        }

        return null;
    }

    private DocItem getCurrentDir() {
        return mState.stack.peek();
    }

    private void updateTitle() {
        setTitle(mState.stack.getTitle());
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mState.stack.getTitle());
            actionBar.setSubtitle(mState.stack.getSubTitle());
        }
    }

    private boolean isSupported(Uri uri) {
        return Shared.FILE_SCHEMA.equals(uri.getScheme());
    }

    private void loadRoot(Uri uri) {
        RootDir rootDir = new RootDir();
        rootDir.path = uri.getPath();
        rootDir.title = isSdcard(uri) ? getString(R.string.mobile_storeage) : uri.getLastPathSegment();

        mState.onRootChanged(rootDir);

        DocItem doc = new DocItem();
        doc.path = rootDir.path;
        doc.docType = DocItem.TYPE_DIR;
        doc.name = rootDir.title;

        openDir(doc);
    }

    private Uri getDefaultRoot() {
        return Uri.fromFile(Environment.getExternalStorageDirectory());
    }

    private boolean isSdcard(Uri uri) {
        return getDefaultRoot().equals(uri);
    }

    private void openDir(DocItem doc) {
        assert (doc.isContainer());

        mState.pushDoc(doc);

        refreshCurrentRootAndDirectory(AnimationView.ANIM_ENTER);
    }

    public State getDisplayState() {
        return mState;
    }

    public void onDocPicked(DocItem docItem) {
        if (docItem.isContainer()) {
            openDir(docItem);
        }
    }

    public void onDocSelect(DocItem docItem) {
        if (mState.isSelected(docItem)) {
            mState.removeSelect(docItem);
            notifyItemStateChanged(docItem);
        } else if (!mState.isValidDoc(docItem)) {
            Toast.makeText(this, R.string.size_over_limit, Toast.LENGTH_SHORT).show();
        } else if (mState.isReadCountLimit()) {
            Toast.makeText(this, R.string.count_over_limit, Toast.LENGTH_SHORT).show();
        } else {
            mState.addSelect(docItem);
            notifyItemStateChanged(docItem);
        }
    }

    public boolean isSelected(DocItem docItem) {
        return mState.isSelected(docItem);
    }

    private void notifyItemStateChanged(DocItem docItem) {
        final FragmentManager fm = getSupportFragmentManager();
        FilesFragment fragment = FilesFragment.get(fm);
        if (fragment != null) {
            fragment.notifyItemStateChanged(docItem);
        }
        updateBottomBar();
        invalidateOptionsMenu();
    }

    private void updateBottomBar() {
        if (mState.getSelectionCount() > 0) {
            mTotalSizeView.setVisibility(View.VISIBLE);
            mTotalSizeView.setText(Utils.formatSize(mState.getTotalSize()));
        } else {
            mTotalSizeView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mState.selectMode) {
            getMenuInflater().inflate(R.menu.menu_files, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mState.selectMode) {
            MenuItem okMenu = menu.findItem(R.id.ok);
            if (mState.getSelectionCount() > 0) {
                okMenu.setEnabled(true);
                okMenu.setTitle(getString(R.string.ok_with_nums, mState.getSelectionCount(), mState.fileMaxCount));
            } else {
                okMenu.setEnabled(false);
                okMenu.setTitle(R.string.ok);
            }
            return true;
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.ok) {
            if (mState.selectMode) {
                Intent data = new Intent();
                data.putExtra("data", mState.getSelections());
                setResult(RESULT_OK, data);
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Shared.EXTRA_STATE, mState);
    }

    @Override
    public void onBackPressed() {
        if (mState.stack.size() > 1) {
            mState.popDoc();

            refreshCurrentRootAndDirectory(AnimationView.ANIM_LEAVE);
            return;
        }
        super.onBackPressed();
    }
}

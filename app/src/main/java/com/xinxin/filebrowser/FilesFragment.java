package com.xinxin.filebrowser;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FilesFragment extends Fragment implements LoaderManager.LoaderCallbacks<DirectoryResult>, FilesAdapter.Host {

    private static final String TAG = "FilesFragment";

    private RecyclerView mRecView;
    private View mEmptyView;
    private TextView mEmptyMessageView;
    private FilesAdapter mAdapter;

    private DocItem mDoc;
    private boolean mIsSelectMode;

    public static void create(FragmentManager fm, RootDir root, DocItem doc, int anim) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(Shared.EXTRA_ROOT, root);
        bundle.putParcelable(Shared.EXTRA_DOC, doc);

        final FragmentTransaction ft = fm.beginTransaction();
        AnimationView.setupAnimations(ft, anim, bundle);

        final FilesFragment fragment = new FilesFragment();
        fragment.setArguments(bundle);

        ft.replace(R.id.container, fragment);
        ft.commitAllowingStateLoss();
    }

    public static void showDirectory(FragmentManager fm, RootDir root, DocItem doc, int anim) {
        create(fm, root, doc, anim);
    }

    public static FilesFragment get(FragmentManager fm) {
        Fragment fragment = fm.findFragmentById(R.id.container);
        return fragment instanceof FilesFragment
                ? (FilesFragment) fragment
                : null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_files, container, false);

        mRecView = root.findViewById(R.id.list);
        mEmptyView = root.findViewById(android.R.id.empty);
        mEmptyMessageView = root.findViewById(R.id.empty_message);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final State state = getDisplayState();

        mIsSelectMode = state.selectMode;

        Bundle args = savedInstanceState == null ? getArguments() : savedInstanceState;
        mDoc = args.getParcelable(Shared.EXTRA_DOC);

        mAdapter = new FilesAdapter(this);
        mRecView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mRecView.setAdapter(mAdapter);
        mRecView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLoaderManager().restartLoader(0, null, this);
            } else {
                showNoPermission();
            }
        }
    }

    public void notifyItemStateChanged(DocItem docItem) {
        mAdapter.onItemSelectionChanged(docItem);
    }

    State getDisplayState() {
        return ((FilesActivity) getActivity()).getDisplayState();
    }

    private void showNoPermission() {
        mEmptyView.setVisibility(View.VISIBLE);
        mRecView.setVisibility(View.GONE);
        mEmptyMessageView.setText(R.string.no_read_storeage_permission);
        mEmptyView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                startActivity(intent);
            }
        });
    }

    private void showQueryError() {
        showEmptyDirectory();
    }

    private void showEmptyDirectory() {
        mEmptyView.setVisibility(View.VISIBLE);
        mRecView.setVisibility(View.GONE);
        mEmptyMessageView.setText(R.string.no_files);
        mEmptyView.setOnClickListener(null);
    }

    private void showDirectory() {
        mEmptyView.setVisibility(View.GONE);
        mRecView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();

        final SparseArray<Parcelable> container = new SparseArray<Parcelable>();
        getView().saveHierarchyState(container);
        final State state = getDisplayState();
        state.dirState.put(mDoc.path, container);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Shared.EXTRA_DOC, mDoc);
    }

    @NonNull
    @Override
    public Loader<DirectoryResult> onCreateLoader(int id, @Nullable Bundle args) {
        return new DirectoryLoader(getContext(), mDoc.path);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<DirectoryResult> loader, DirectoryResult data) {
        if (!isAdded()) return;

        if (data.exception != null) {
            showQueryError();
            return;
        }

        if (data.docItems.length == 0) {
            showEmptyDirectory();
            return;
        }

        showDirectory();
        mAdapter.setDocItems(data.docItems);
        mAdapter.notifyDataSetChanged();

        State state = getDisplayState();
        final SparseArray<Parcelable> container = state.dirState.remove(mDoc.path);
        if (container != null) {
            getView().restoreHierarchyState(container);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<DirectoryResult> loader) {
        showEmptyDirectory();
    }

    @Override
    public boolean isSelectMode() {
        return mIsSelectMode;
    }

    @Override
    public boolean isSelected(DocItem doc) {
        return ((FilesActivity) getActivity()).isSelected(doc);
    }

    @Override
    public void onSelect(int position) {
        DocItem docItem = mAdapter.getDocItem(position);

        if (docItem == null) {
            return;
        }

        ((FilesActivity) getActivity()).onDocSelect(docItem);
    }

    @Override
    public void onActivate(int position) {
        DocItem docItem = mAdapter.getDocItem(position);

        if (docItem == null) {
            return;
        }

        ((FilesActivity) getActivity()).onDocPicked(docItem);
    }
}

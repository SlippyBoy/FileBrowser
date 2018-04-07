package com.xinxin.filebrowser;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.xinxin.filebrowser.ui.LabelView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.DocHolder> {

    static final String SELECTION_CHANGED_MARKER = "Selection-Changed";
    private static final int TYPE_DIR = 0;
    private static final int TYPE_FILE = 1;
    private static String sDateFormatStr;
    private Host mHost;
    private DocItem[] mDocItems;
    public FilesAdapter(Host host) {
        mHost = host;
        if (sDateFormatStr == null) {
            sDateFormatStr = mHost.getContext().getString(R.string.format_date);
        }
    }

    static DocHolder createDocHolder(LayoutInflater inflater, ViewGroup parent, int viewType, Host host) {
        if (viewType == TYPE_DIR) {
            return DirHolder.create(inflater, parent, host);
        }

        if (viewType == TYPE_FILE) {
            return FileHolder.create(inflater, parent, host);
        }

        throw new IllegalArgumentException("unknown viewType: " + viewType);
    }

    public void setDocItems(DocItem[] docItems) {
        mDocItems = docItems;
    }

    public void onItemSelectionChanged(DocItem docItem) {
        if (docItem != null && mDocItems != null) {
            for (int i = 0; i < mDocItems.length; i++) {
                if (docItem.path.equals(mDocItems[i].path)) {
                    notifyItemChanged(i, SELECTION_CHANGED_MARKER);
                    break;
                }
            }
        }
    }

    public DocItem getDocItem(int position) {
        return mDocItems == null ? null : mDocItems[position];
    }

    @NonNull
    @Override
    public DocHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return createDocHolder(LayoutInflater.from(parent.getContext()), parent, viewType, mHost);
    }

    @Override
    public void onBindViewHolder(@NonNull DocHolder holder, int position) {
        holder.bind(mDocItems[position]);
    }

    @Override
    public int getItemViewType(int position) {
        return mDocItems[position].isContainer() ? TYPE_DIR : TYPE_FILE;
    }

    @Override
    public int getItemCount() {
        return mDocItems == null ? 0 : mDocItems.length;
    }

    interface Host {
        Context getContext();

        boolean isSelectMode();

        boolean isSelected(DocItem doc);

        void onSelect(int position);

        void onActivate(int position);
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface ViewType {
    }

    static abstract class DocHolder extends RecyclerView.ViewHolder {

        Host host;

        public DocHolder(View itemView, Host host) {
            super(itemView);
            this.host = host;
        }

        abstract void bind(DocItem docItem);
    }

    static class DirHolder extends DocHolder implements View.OnClickListener {

        TextView nameView;
        LabelView labelView;
        public DirHolder(View itemView, Host host) {
            super(itemView, host);
            itemView.setOnClickListener(this);
            nameView = itemView.findViewById(R.id.name);
            labelView = itemView.findViewById(R.id.child_count);
        }

        static DirHolder create(LayoutInflater inflater, ViewGroup parent, Host host) {
            return new DirHolder(inflater.inflate(R.layout.list_dir_item, parent, false), host);
        }

        void bind(DocItem docItem) {
            nameView.setText(docItem.name);
            labelView.setText(String.valueOf(docItem.childCount));
        }

        @Override
        public void onClick(View v) {
            if (host != null) {
                host.onActivate(getAdapterPosition());
            }
        }
    }

    static class FileHolder extends DocHolder implements View.OnClickListener {

        TextView nameView;
        TextView sizeView;
        TextView dateView;
        View checkWrapper;
        CheckBox checkView;
        public FileHolder(View itemView, Host host) {
            super(itemView, host);
            nameView = itemView.findViewById(R.id.name);
            sizeView = itemView.findViewById(R.id.size);
            dateView = itemView.findViewById(R.id.date);
            if (host.isSelectMode()) {
                checkWrapper = itemView.findViewById(R.id.check_wrapper);
                checkWrapper.setOnClickListener(this);
                checkView = itemView.findViewById(R.id.check);
                checkWrapper.setVisibility(View.VISIBLE);
            } else {
                itemView.findViewById(R.id.check_wrapper).setVisibility(View.GONE);
            }
        }

        static FileHolder create(LayoutInflater inflater, ViewGroup parent, Host host) {
            return new FileHolder(inflater.inflate(R.layout.list_file_item, parent, false), host);
        }

        void bind(DocItem docItem) {
            nameView.setText(docItem.name);
            sizeView.setText(Utils.formatSize(docItem.size));
            dateView.setText(DateFormat.format(sDateFormatStr, docItem.lastModify));
            if (host.isSelectMode()) {
                checkView.setChecked(host.isSelected(docItem));
            }
        }

        @Override
        public void onClick(View v) {
            if (host != null) {
                host.onSelect(getAdapterPosition());
            }
        }
    }
}

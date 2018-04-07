package com.xinxin.filebrowser.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xinxin.filebrowser.R;

public class LabelView extends LinearLayout {

    private TextView mLabelTextView;
    private TextView mLabelContentView;

    public LabelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setOrientation(HORIZONTAL);

        View view = LayoutInflater.from(context).inflate(R.layout.label_layout, this, true);
        mLabelTextView = view.findViewById(R.id.label_text);
        mLabelContentView = view.findViewById(R.id.label_content);

        String labelText = null;
        int labelTextSize = 15;
        ColorStateList labelTextColor = null;
        int gap = 0;
        String text = null;
        int textSize = 15;
        ColorStateList textColor = null;

        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LabelView);
        int n = ta.getIndexCount();
        try {
            for (int i = 0; i < n; i++) {
                int attr = ta.getIndex(i);

                switch (attr) {
                    case R.styleable.LabelView_label:
                        labelText = ta.getString(attr);
                        break;
                    case R.styleable.LabelView_labelSize:
                        labelTextSize = ta.getDimensionPixelSize(attr, labelTextSize);
                        break;
                    case R.styleable.LabelView_labelTextColor:
                        labelTextColor = ta.getColorStateList(attr);
                        break;
                    case R.styleable.LabelView_labelGap:
                        gap = ta.getDimensionPixelSize(attr, gap);
                        break;
                    case R.styleable.LabelView_android_text:
                        text = ta.getString(attr);
                        break;
                    case R.styleable.LabelView_android_textSize:
                        textSize = ta.getDimensionPixelSize(attr, textSize);
                        break;
                    case R.styleable.LabelView_android_textColor:
                        textColor = ta.getColorStateList(attr);
                        break;
                }
            }
        } finally {
            ta.recycle();
        }

        setLabel(labelText);
        setLabelColor(labelTextColor);
        setLabelTextSize(labelTextSize);

        setText(text);
        setTextColor(textColor);
        setTextSize(textSize);

        setLabelGap(gap);
    }

    public void setLabel(String text) {
        mLabelTextView.setText(text);
    }

    public void setLabel(@StringRes int resId) {
        mLabelTextView.setText(resId);
    }

    public void setText(String text) {
        mLabelContentView.setText(text);
    }

    public void setText(@StringRes int resId) {
        mLabelContentView.setText(resId);
    }

    public void setLabelTextSize(@DimenRes int resId) {
        mLabelTextView.setTextSize(resId);
    }

    public void setLabelTextSize(float size) {
        mLabelTextView.setTextSize(size);
    }

    public void setTextSize(float size) {
        mLabelContentView.setTextSize(size);
    }

    public void setLabelColor(ColorStateList stateList) {
        if (stateList == null) {
            return;
        }
        mLabelTextView.setTextColor(stateList);
    }

    public void setLabelColor(@ColorInt int color) {
        mLabelTextView.setTextColor(ColorStateList.valueOf(color));
    }

    public void setTextColor(ColorStateList stateList) {
        if (stateList == null) {
            return;
        }
        mLabelContentView.setTextColor(stateList);
    }

    public void setTextColor(@ColorInt int color) {
        mLabelContentView.setTextColor(ColorStateList.valueOf(color));
    }

    public void setLabelGap(int size) {
        LayoutParams lp = (LayoutParams) mLabelContentView.getLayoutParams();
        lp.leftMargin = size;
    }
}

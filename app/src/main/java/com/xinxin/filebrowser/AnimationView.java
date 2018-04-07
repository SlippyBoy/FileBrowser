package com.xinxin.filebrowser;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.FragmentTransaction;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AnimationView {

    public static final int ANIM_NONE = 1;
    public static final int ANIM_LEAVE = 2;
    public static final int ANIM_ENTER = 3;

    static void setupAnimations(
            FragmentTransaction ft, @AnimationType int anim, Bundle args) {
        switch (anim) {
            case ANIM_NONE:
                break;
            case ANIM_ENTER:
                break;
            case ANIM_LEAVE:
                break;
        }
    }

    @IntDef(flag = true, value = {
            ANIM_NONE,
            ANIM_LEAVE,
            ANIM_ENTER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType {
    }
}

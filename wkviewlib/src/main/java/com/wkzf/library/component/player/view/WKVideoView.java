package com.wkzf.library.component.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by Ted on 2015/8/6.
 * WKVideoView
 */
public class WKVideoView extends VideoView {

    private int videoWidth;
    private int videoHeight;

    public WKVideoView(Context context) {
        super(context);
    }

    public WKVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WKVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressWarnings("unused")
    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    @SuppressWarnings("unused")
    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    @SuppressWarnings("unused")
    public int getVideoWidth() {
        return videoWidth;
    }

    @SuppressWarnings("unused")
    public int getVideoHeight() {
        return videoHeight;
    }
}

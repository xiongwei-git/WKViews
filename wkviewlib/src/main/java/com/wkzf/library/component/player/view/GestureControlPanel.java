package com.wkzf.library.component.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

/**
 * Created by Ted on 2015/11/18.
 */
public class GestureControlPanel extends RelativeLayout {
    private static final int FLING_VELOCITY_SLOP = 300;
    private GestureDetector mGestureDetector;
    private OnGestureControlListener mOnGestureControlListener;
    private boolean isGestureOk = false;

    private final GestureDetector.OnGestureListener mOnGestureListener =
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    //通过velocityX的值来避免左右滑动事件
                    if(Math.abs(velocityX) > Math.abs(velocityY))
                        return false;
                    if (velocityY > FLING_VELOCITY_SLOP) {
                        if (null != mOnGestureControlListener)
                            mOnGestureControlListener.onFlingDown();
                        return true;
                    } else if (velocityY < -FLING_VELOCITY_SLOP) {
                        if (null != mOnGestureControlListener)
                            mOnGestureControlListener.onFlingUp();
                        return true;
                    }
                    return false;
                }
            };

    public GestureControlPanel(Context context) {
        super(context);
    }

    public GestureControlPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GestureControlPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        mGestureDetector = new GestureDetector(getContext(), mOnGestureListener);
    }

    private float lastY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGestureOk) mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                int deltaY = (int) ((event.getY() - lastY));
                if (Math.abs(deltaY) <= ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                    if (null != mOnGestureControlListener)
                        mOnGestureControlListener.onClick(this);
                }
                return true;
            default:
                return false;
        }
    }

    public void setIsGestureOk(boolean isGestureOk) {
        this.isGestureOk = isGestureOk;
    }

    public void setOnGestureControlListener(OnGestureControlListener onGestureControlListener) {
        mOnGestureControlListener = onGestureControlListener;
    }

    public interface OnGestureControlListener {
        void onFlingUp();

        void onFlingDown();

        void onClick(View view);
    }
}

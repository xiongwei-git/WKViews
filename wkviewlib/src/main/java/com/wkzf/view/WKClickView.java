package com.wkzf.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Ted on 2015/11/2.
 * 自定义的点击效果视图，一般用来至于具有点击效果的布局上方
 */
public class WKClickView extends FrameLayout implements View.OnClickListener {
    private int type = 0;
    private int normalClickBgId = -1;
    private int rippleClickBgId = -1;

    private OnClickListener onClickListener;

    @Override
    public void onClick(View v) {
        if (null != onClickListener) {
            onClickListener.onClick(this);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        onClickListener = l;
    }

    public WKClickView(Context context) {
        super(context);
        initViews();
    }

    public WKClickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
        initFromAttributes(context, attrs);
    }

    public WKClickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFromAttributes(context, attrs);
        initViews();
    }

    private void initFromAttributes(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WKClickView);
            type = array.getInt(R.styleable.WKClickView_layout_style, 0);
            normalClickBgId = array.getResourceId(R.styleable.WKClickView_normal_bg, -1);
            rippleClickBgId = array.getResourceId(R.styleable.WKClickView_ripple_bg, -1);
            array.recycle();
        } else {
            type = 0;
            normalClickBgId = -1;
            rippleClickBgId = -1;
        }
    }


    private void initViews() {

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addClickView();
    }

    private void addClickView() {
        View clickView = new View(getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        clickView.setBackgroundResource(getClickViewResId());
        clickView.setLayoutParams(layoutParams);
        clickView.setOnClickListener(this);
        addView(clickView);
    }

    private int getClickViewResId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (rippleClickBgId > 0) return rippleClickBgId;
        } else {
            if (normalClickBgId > 0) return normalClickBgId;
        }
        if (type == 0) return R.drawable.wk_click_view_rectangle_bg;
        else if (type == 1) return R.drawable.wk_click_view_oval_bg;
        else if (type == 2) return R.drawable.wk_click_view_round_bg;
        else if (type == 3) return R.drawable.wk_click_view_semicircle_bg;
        else return R.drawable.wk_click_view_rectangle_bg;
    }


}

package com.wkzf.library.component.player.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wkzf.library.R;
import com.wkzf.library.component.player.model.Video;
import com.wkzf.library.component.player.model.VideoUrl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Ted on 2015/8/4.
 * PlayerController
 */
public class PlayerController extends FrameLayout implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private ImageView mPlayImg;//播放按钮
    private SeekBar mProgressSeekBar;//播放进度条
    private TextView mTimeTxt;//播放时间
    private ImageView mExpandShrinkImg;//最大化最小化播放按钮
    private EasySwitcher mVideoSrcSwitcher;//视频源切换器
    private EasySwitcher mVideoFormatSwitcher;//视频清晰度切换器
    private View mMenuView;
    private View mMenuViewPlaceHolder;

    private MediaControlImpl mMediaControl;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
        if (isFromUser)
            mMediaControl.onProgressTurn(ProgressState.DOING, progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mMediaControl.onProgressTurn(ProgressState.START, 0);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mMediaControl.onProgressTurn(ProgressState.STOP, 0);
    }

    private EasySwitcher.EasySwitcherCallbackImpl mSrcSwitcherCallback = new EasySwitcher.EasySwitcherCallbackImpl() {
        @Override
        public void onSelectItem(int position, String name) {
            mMediaControl.onSelectSrc(position);
        }

        @Override
        public void onShowList() {
            mMediaControl.alwaysShowController();
            mVideoFormatSwitcher.closeSwitchList();
        }
    };

    private EasySwitcher.EasySwitcherCallbackImpl mFormatSwitcherCallback = new EasySwitcher.EasySwitcherCallbackImpl() {
        @Override
        public void onSelectItem(int position, String name) {
            mMediaControl.onSelectFormat(position);
        }

        @Override
        public void onShowList() {
            mMediaControl.alwaysShowController();
            mVideoSrcSwitcher.closeSwitchList();
        }
    };

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.play_or_pause_btn) {
            mMediaControl.onPlayTurn();
        } else if (view.getId() == R.id.player_screen_control_btn) {
            mMediaControl.onPageTurn();
        }
    }

    public void initVideoList(ArrayList<Video> videoList) {
        ArrayList<String> name = new ArrayList<>();
        for (Video video : videoList) {
            name.add(video.getVideoName());
        }
        mVideoSrcSwitcher.initData(name);
    }

    public void initPlayVideo(Video video) {
        ArrayList<String> format = new ArrayList<>();
        for (VideoUrl url : video.getVideoUrl()) {
            format.add(url.getFormatName());
        }
        mVideoFormatSwitcher.initData(format);
    }

    public void closeAllSwitchList() {
        mVideoFormatSwitcher.closeSwitchList();
        mVideoSrcSwitcher.closeSwitchList();
    }

    /**
     * 初始化精简模式
     */
    public void initTrimmedMode() {
        mMenuView.setVisibility(GONE);
        mMenuViewPlaceHolder.setVisibility(GONE);
        mExpandShrinkImg.setVisibility(INVISIBLE);
    }

    /***
     * 强制横屏模式
     */
    public void forceLandscapeMode() {
        mExpandShrinkImg.setVisibility(INVISIBLE);
    }


    public void setProgressBar(int progress, int secondProgress) {
        if (progress < 0) progress = 0;
        if (progress > 100) progress = 100;
        if (secondProgress < 0) secondProgress = 0;
        if (secondProgress > 100) secondProgress = 100;
        mProgressSeekBar.setProgress(progress);
        mProgressSeekBar.setSecondaryProgress(secondProgress);
    }

    public void setPlayState(PlayState playState) {
        mPlayImg.setImageResource(playState.equals(PlayState.PLAY) ? R.drawable.video_player_controller_pause : R.drawable.video_player_controller_play);
    }

    public void setPageType(PageType pageType) {

        mExpandShrinkImg.setBackgroundResource(pageType.equals(PageType.EXPAND) ?
                R.drawable.video_player_screen_shrink : R.drawable.video_player_screen_expand);
    }

    public void setPlayProgressTxt(int nowSecond, int allSecond) {
        mTimeTxt.setText(getPlayTime(nowSecond, allSecond));
    }

    public void playFinish(int allTime) {
        mProgressSeekBar.setProgress(0);
        setPlayProgressTxt(0, allTime);
        setPlayState(PlayState.PAUSE);
    }

    public void setMediaControl(MediaControlImpl mediaControl) {
        mMediaControl = mediaControl;
    }

    public PlayerController(Context context) {
        super(context);
        initView(context);
    }

    public PlayerController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public PlayerController(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.video_player_controller_layout, this);
        mPlayImg = (ImageView) findViewById(R.id.play_pause_icon);
        mProgressSeekBar = (SeekBar) findViewById(R.id.media_controller_progress);
        mVideoSrcSwitcher = (EasySwitcher) findViewById(R.id.video_src_switcher);
        mVideoFormatSwitcher = (EasySwitcher) findViewById(R.id.video_format_switcher);
        mTimeTxt = (TextView) findViewById(R.id.time);
        mExpandShrinkImg = (ImageView) findViewById(R.id.player_screen_control_icon);
        mMenuView = findViewById(R.id.view_menu);
        mMenuViewPlaceHolder = findViewById(R.id.view_menu_placeholder);
        findViewById(R.id.play_or_pause_btn).setOnClickListener(this);
        findViewById(R.id.player_screen_control_btn).setOnClickListener(this);
        initData();
    }

    private void initData() {
        mProgressSeekBar.setOnSeekBarChangeListener(this);
        setPageType(PageType.SHRINK);
        setPlayState(PlayState.PAUSE);
        mVideoFormatSwitcher.setEasySwitcherCallback(mFormatSwitcherCallback);
        mVideoSrcSwitcher.setEasySwitcherCallback(mSrcSwitcherCallback);
    }

    @SuppressLint("SimpleDateFormat")
    private String formatPlayTime(long time) {
        DateFormat formatter = new SimpleDateFormat("mm:ss");
        return formatter.format(new Date(time));
    }

    private String getPlayTime(int playSecond, int allSecond) {
        String playSecondStr = "00:00";
        String allSecondStr = "00:00";
        if (playSecond > 0) {
            playSecondStr = formatPlayTime(playSecond);
        }
        if (allSecond > 0) {
            allSecondStr = formatPlayTime(allSecond);
        }
        return playSecondStr + "/" + allSecondStr;
    }

    /**
     * 播放样式 展开、缩放
     */
    public enum PageType {
        EXPAND, SHRINK
    }

    /**
     * 播放状态 播放 暂停
     */
    public enum PlayState {
        PLAY, PAUSE
    }

    public enum ProgressState {
        START, DOING, STOP
    }


    public interface MediaControlImpl {
        void onPlayTurn();

        void onPageTurn();

        void onProgressTurn(ProgressState state, int progress);

        void onSelectSrc(int position);

        void onSelectFormat(int position);

        void alwaysShowController();
    }

}

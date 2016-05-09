/*
 *
 * Copyright 2015 TedXiong xiong-wei@hotmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wkzf.library.component.player.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wkzf.library.R;
import com.wkzf.library.component.player.dlna.engine.DLNAContainer;
import com.wkzf.library.component.player.dlna.engine.MultiPointController;
import com.wkzf.library.component.player.dlna.inter.IController;
import com.wkzf.library.component.player.model.Video;
import com.wkzf.library.component.player.model.VideoUrl;

import org.cybergarage.upnp.Device;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ted on 2015/8/6.
 * SuperVideoPlayer
 */
public class SuperVideoPlayer extends RelativeLayout {

    private final int MSG_HIDE_CONTROLLER = 10;
    private final int MSG_UPDATE_PLAY_TIME = 11;
    private final int MSG_PLAY_ON_TV_RESULT = 12;
    private final int MSG_EXIT_FORM_TV_RESULT = 13;

    private Context mContext;
    private WKVideoView mWKVideoView;
    private PlayerController mPlayerController;
    private Timer mUpdateTimer;
    private VideoPlayCallbackImpl mVideoPlayCallback;

    private View mProgressBarView;
    private View mCloseBtnView;
    private View mTvBtnView;
    private View mDLNARootLayout;
    private View mUserControlGuideView;
    private GestureControlPanel mGestureControlPanel;

    private ArrayList<Video> mAllVideo;
    private Video mNowPlayVideo;

    private List<Device> mDevices;
    private IController mController;
    private Device mSelectDevice;
    //是否自动隐藏控制栏
    private boolean mAutoHideController = true;
    //是否显示新手引导
    private boolean mIsShowUserGuide = false;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE_PLAY_TIME) {
                updatePlayTime();
                updatePlayProgress();
            } else if (msg.what == MSG_HIDE_CONTROLLER) {
                showOrHideController();
            } else if (msg.what == MSG_PLAY_ON_TV_RESULT) {
                shareToTvResult(msg);
            } else if (msg.what == MSG_EXIT_FORM_TV_RESULT) {
                exitFromTvResult(msg);
            }
            return false;
        }
    });

    /**
     * 可推送设备列表改变的监听回调
     */
    @SuppressWarnings("unused")
    private DLNAContainer.DeviceChangeListener mDeviceChangeListener = new DLNAContainer.DeviceChangeListener() {
        @Override
        public void onDeviceChange(Device device) {

        }
    };

    private View.OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.video_close_view) {
                mVideoPlayCallback.onCloseVideo();
            } else if (view.getId() == R.id.video_share_tv_view) {
                shareToTv();
            } else if (view.getId() == R.id.txt_dlna_exit) {
                goOnPlayAtLocal();
            }else if(view.getId() == R.id.video_player_user_control_guide_view){
                mUserControlGuideView.setVisibility(GONE);
            }
        }
    };

    private GestureControlPanel.OnGestureControlListener mOnGestureControlListener = new GestureControlPanel.OnGestureControlListener() {
        @Override
        public void onFlingUp() {
            mVideoPlayCallback.onCloseVideo();
        }

        @Override
        public void onFlingDown() {
            mVideoPlayCallback.onSwitchPageType();
        }

        @Override
        public void onClick(View view) {
            showOrHideController();
        }
    };

    private PlayerController.MediaControlImpl mMediaControl = new PlayerController.MediaControlImpl() {
        @Override
        public void alwaysShowController() {
            SuperVideoPlayer.this.alwaysShowController();
        }

        @Override
        public void onSelectSrc(int position) {
            Video selectVideo = mAllVideo.get(position);
            if (selectVideo.equal(mNowPlayVideo)) return;
            mNowPlayVideo = selectVideo;
            mNowPlayVideo.setPlayUrl(0);
            mPlayerController.initPlayVideo(mNowPlayVideo);
            loadAndPlay(mNowPlayVideo.getPlayUrl(), 0);
        }

        @Override
        public void onSelectFormat(int position) {
            VideoUrl videoUrl = mNowPlayVideo.getVideoUrl().get(position);
            if (mNowPlayVideo.getPlayUrl().equal(videoUrl)) return;
            mNowPlayVideo.setPlayUrl(position);
            playVideoAtLastPos();
        }

        @Override
        public void onPlayTurn() {
            if (mWKVideoView.isPlaying()) {
                pausePlay(true);
            } else {
                goOnPlay();
            }
        }

        @Override
        public void onPageTurn() {
            mVideoPlayCallback.onSwitchPageType();
        }

        @Override
        public void onProgressTurn(PlayerController.ProgressState state, int progress) {
            if (state.equals(PlayerController.ProgressState.START)) {
                mHandler.removeMessages(MSG_HIDE_CONTROLLER);
            } else if (state.equals(PlayerController.ProgressState.STOP)) {
                resetHideTimer();
            } else {
                int time = progress * mWKVideoView.getDuration() / 100;
                mWKVideoView.seekTo(time);
                updatePlayTime();
            }
        }
    };

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        mProgressBarView.setVisibility(View.GONE);
                        setCloseButton(true);
                        initDLNAInfo();
                        showUserControlGuide();
                        return true;
                    }
                    return false;
                }
            });

        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            stopUpdateTimer();
            stopHideTimer(true);
            mPlayerController.playFinish(mWKVideoView.getDuration());
            mVideoPlayCallback.onPlayFinish();
            Toast.makeText(mContext, "视频播放完成", Toast.LENGTH_SHORT).show();
        }
    };

    public void setVideoPlayCallback(VideoPlayCallbackImpl videoPlayCallback) {
        mVideoPlayCallback = videoPlayCallback;
    }

    /**
     * 如果在地图页播放视频，请先调用该接口
     */
    @SuppressWarnings("unused")
    public void setSupportPlayOnSurfaceView() {
        mWKVideoView.setZOrderMediaOverlay(true);
    }

    @SuppressWarnings("unused")
    public WKVideoView getVideoView() {
        return mWKVideoView;
    }

    public void setPageType(PlayerController.PageType pageType) {
        mPlayerController.setPageType(pageType);
    }

    /***
     * 设置是否显示新手引导
     * @param isShowUserGuide 是否显示新手引导
     */
    public void setIsShowUserGuide(boolean isShowUserGuide) {
        mIsShowUserGuide = isShowUserGuide;
    }

    /***
     * 设置是否支持手势滑动全屏和关闭
     * @param isSupportGesture 是否支持手势滑动全屏和关闭
     */
    @SuppressWarnings("unused")
    public void setIsSupportGesture(boolean isSupportGesture){
        mGestureControlPanel.setIsGestureOk(isSupportGesture);
    }

    /***
     * 强制横屏模式
     */
    @SuppressWarnings("unused")
    public void forceLandscapeMode() {
        mPlayerController.forceLandscapeMode();
    }

    /***
     * 播放本地视频 只支持横屏播放
     *
     * @param fileUrl fileUrl
     */
    @SuppressWarnings("unused")
    public void loadLocalVideo(String fileUrl) {
        VideoUrl videoUrl = new VideoUrl();
        videoUrl.setIsOnlineVideo(false);
        videoUrl.setFormatUrl(fileUrl);
        videoUrl.setFormatName("本地视频");
        Video video = new Video();
        ArrayList<VideoUrl> videoUrls = new ArrayList<>();
        videoUrls.add(videoUrl);
        video.setVideoUrl(videoUrls);
        video.setPlayUrl(0);

        mNowPlayVideo = video;

        /***
         * 初始化控制条的精简模式
         */
        mPlayerController.initTrimmedMode();
        loadAndPlay(mNowPlayVideo.getPlayUrl(), 0);
    }

    /**
     * 播放多个视频,默认播放第一个视频，第一个格式
     *
     * @param allVideo 所有视频
     */
    @SuppressWarnings("unused")
    public void loadMultipleVideo(ArrayList<Video> allVideo) {
        loadMultipleVideo(allVideo, 0, 0);
    }

    /**
     * 播放多个视频
     *
     * @param allVideo     所有的视频
     * @param selectVideo  指定的视频
     * @param selectFormat 指定的格式
     */
    public void loadMultipleVideo(ArrayList<Video> allVideo, int selectVideo, int selectFormat) {
        loadMultipleVideo(allVideo, selectVideo, selectFormat, 0);
    }

    /***
     * @param allVideo     所有的视频
     * @param selectVideo  指定的视频
     * @param selectFormat 指定的格式
     * @param seekTime     开始进度
     */
    public void loadMultipleVideo(ArrayList<Video> allVideo, int selectVideo, int selectFormat, int seekTime) {
        if (null == allVideo || allVideo.size() == 0) {
            Toast.makeText(mContext, "视频列表为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mAllVideo.clear();
        mAllVideo.addAll(allVideo);
        mNowPlayVideo = mAllVideo.get(selectVideo);
        mNowPlayVideo.setPlayUrl(selectFormat);
        mPlayerController.initVideoList(mAllVideo);
        mPlayerController.initPlayVideo(mNowPlayVideo);
        loadAndPlay(mNowPlayVideo.getPlayUrl(), seekTime);
    }

    /**
     * 暂停播放
     *
     * @param isShowController 是否显示控制条
     */
    public void pausePlay(boolean isShowController) {
        mWKVideoView.pause();
        mPlayerController.setPlayState(PlayerController.PlayState.PAUSE);
        stopHideTimer(isShowController);
    }

    /***
     * 继续播放
     */
    public void goOnPlay() {
        mWKVideoView.start();
        mPlayerController.setPlayState(PlayerController.PlayState.PLAY);
        resetHideTimer();
        resetUpdateTimer();
    }

    /**
     * 关闭视频
     */
    public void close() {
        mPlayerController.setPlayState(PlayerController.PlayState.PAUSE);
        stopHideTimer(false);
        stopUpdateTimer();
        mWKVideoView.pause();
        mWKVideoView.stopPlayback();
        mWKVideoView.setVisibility(GONE);
    }

    /***
     * 获取支持的DLNA设备
     *
     * @return DLNA设备列表
     */
    @SuppressWarnings("unused")
    public List<Device> getDevices() {
        return mDevices;
    }

    public boolean isAutoHideController() {
        return mAutoHideController;
    }

    public void setAutoHideController(boolean autoHideController) {
        mAutoHideController = autoHideController;
    }

    public SuperVideoPlayer(Context context) {
        super(context);
        initView(context);
    }

    public SuperVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public SuperVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        View.inflate(context, R.layout.vodeo_player_view, this);
        mWKVideoView = (WKVideoView) findViewById(R.id.video_view);
        mPlayerController = (PlayerController) findViewById(R.id.controller);
        mProgressBarView = findViewById(R.id.progressbar);
        mCloseBtnView = findViewById(R.id.video_close_view);
        mTvBtnView = findViewById(R.id.video_share_tv_view);
        mDLNARootLayout = findViewById(R.id.rel_dlna_root_layout);
        mUserControlGuideView = findViewById(R.id.video_player_user_control_guide_view);
        mGestureControlPanel = (GestureControlPanel)findViewById(R.id.video_player_control_panel);
        mPlayerController.setMediaControl(mMediaControl);

        setDLNAButton(false);
        setCloseButton(false);
        mDLNARootLayout.setVisibility(GONE);
        showProgressView(false);

        mGestureControlPanel.setOnGestureControlListener(mOnGestureControlListener);
        mDLNARootLayout.setOnClickListener(mOnClickListener);
        mDLNARootLayout.findViewById(R.id.txt_dlna_exit).setOnClickListener(mOnClickListener);
        mCloseBtnView.setOnClickListener(mOnClickListener);
        mTvBtnView.setOnClickListener(mOnClickListener);
        mProgressBarView.setOnClickListener(mOnClickListener);
        mUserControlGuideView.setOnClickListener(mOnClickListener);
        mAllVideo = new ArrayList<>();
    }

    /***
     * 显示新手引导
     */
    private void showUserControlGuide() {
        if (mIsShowUserGuide){
            mUserControlGuideView.setVisibility(VISIBLE);
            mIsShowUserGuide = false;
        }
    }

    /**
     * 检测DLNA信息，如果有支持的设备，显示按钮
     */
    private void initDLNAInfo() {
        mDevices = DLNAContainer.getInstance().getDevices();
        setController(new MultiPointController());
        setDLNAButton(mDevices.size() > 0);
    }

    /**
     * 显示DLNA可以推送的按钮
     */
    private void setDLNAButton(boolean isShow) {
        mTvBtnView.setVisibility(isShow ? VISIBLE : INVISIBLE);
    }

    /**
     * 显示关闭视频的按钮
     *
     * @param isShow isShow
     */
    private void setCloseButton(boolean isShow) {
        mCloseBtnView.setVisibility(isShow ? VISIBLE : INVISIBLE);
    }

    /**
     * 更换清晰度地址时，续播
     */
    private void playVideoAtLastPos() {
        int playTime = mWKVideoView.getCurrentPosition();
        mWKVideoView.stopPlayback();
        loadAndPlay(mNowPlayVideo.getPlayUrl(), playTime);
    }

    /**
     * 加载并开始播放视频
     *
     * @param videoUrl videoUrl
     */
    private void loadAndPlay(VideoUrl videoUrl, int seekTime) {
        showProgressView(seekTime > 0);
        setCloseButton(true);
        if (TextUtils.isEmpty(videoUrl.getFormatUrl())) {
            throw new IllegalArgumentException("videoUrl should not be null");
        }
        mWKVideoView.setOnPreparedListener(mOnPreparedListener);
        if (videoUrl.isOnlineVideo()) {
            mWKVideoView.setVideoPath(videoUrl.getFormatUrl());
        } else {
            Uri uri = Uri.parse(videoUrl.getFormatUrl());
            mWKVideoView.setVideoURI(uri);
        }
        mWKVideoView.setVisibility(VISIBLE);
        startPlayVideo(seekTime);
    }

    /**
     * 播放视频
     * should called after setVideoPath()
     */
    private void startPlayVideo(int seekTime) {
        if (null == mUpdateTimer) resetUpdateTimer();
        //resetHideTimer();
        mWKVideoView.setOnCompletionListener(mOnCompletionListener);
        mWKVideoView.start();
        if (seekTime > 0) {
            mWKVideoView.seekTo(seekTime);
        }
        mPlayerController.setPlayState(PlayerController.PlayState.PLAY);
    }

    /**
     * 更新播放的进度时间
     */
    private void updatePlayTime() {
        int allTime = mWKVideoView.getDuration();
        int playTime = mWKVideoView.getCurrentPosition();
        mPlayerController.setPlayProgressTxt(playTime, allTime);
    }

    /**
     * 更新播放进度条
     */
    private void updatePlayProgress() {
        int allTime = mWKVideoView.getDuration();
        int playTime = mWKVideoView.getCurrentPosition();
        int loadProgress = mWKVideoView.getBufferPercentage();
        int progress = playTime * 100 / allTime;
        mPlayerController.setProgressBar(progress, loadProgress);
    }

    /**
     * 显示loading圈
     *
     * @param isTransparentBg isTransparentBg
     */
    private void showProgressView(Boolean isTransparentBg) {
        mProgressBarView.setVisibility(VISIBLE);
        if (!isTransparentBg) {
            mProgressBarView.setBackgroundResource(android.R.color.black);
        } else {
            mProgressBarView.setBackgroundResource(android.R.color.transparent);
        }
    }

    /***
     *
     */
    private void showOrHideController() {
        mPlayerController.closeAllSwitchList();
        if (mPlayerController.getVisibility() == View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(mContext,
                    R.anim.anim_exit_from_bottom);
            animation.setAnimationListener(new AnimationImp() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    mPlayerController.setVisibility(View.GONE);
                }
            });
            mPlayerController.startAnimation(animation);
        } else {
            mPlayerController.setVisibility(View.VISIBLE);
            mPlayerController.clearAnimation();
            Animation animation = AnimationUtils.loadAnimation(mContext,
                    R.anim.anim_enter_from_bottom);
            mPlayerController.startAnimation(animation);
            resetHideTimer();
        }
    }

    private void alwaysShowController() {
        mHandler.removeMessages(MSG_HIDE_CONTROLLER);
        mPlayerController.setVisibility(View.VISIBLE);
    }

    private void resetHideTimer() {
        if (!isAutoHideController()) return;
        mHandler.removeMessages(MSG_HIDE_CONTROLLER);
        int TIME_SHOW_CONTROLLER = 4000;
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLLER, TIME_SHOW_CONTROLLER);
    }

    private void stopHideTimer(boolean isShowController) {
        mHandler.removeMessages(MSG_HIDE_CONTROLLER);
        mPlayerController.clearAnimation();
        mPlayerController.setVisibility(isShowController ? View.VISIBLE : View.GONE);
    }

    private void resetUpdateTimer() {
        mUpdateTimer = new Timer();
        int TIME_UPDATE_PLAY_TIME = 1000;
        mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(MSG_UPDATE_PLAY_TIME);
            }
        }, 0, TIME_UPDATE_PLAY_TIME);
    }

    private void stopUpdateTimer() {
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
    }

    private void setController(IController controller) {
        mController = controller;
    }

    private void shareToTv() {
        Toast.makeText(mContext, "开始连接电视中", Toast.LENGTH_SHORT).show();
        showProgressView(true);
        DLNAContainer.getInstance().setSelectedDevice(mDevices.get(0));
        mSelectDevice = DLNAContainer.getInstance().getSelectedDevice();
        setController(new MultiPointController());
        if (mController == null || DLNAContainer.getInstance().getSelectedDevice() == null) {
            Toast.makeText(mContext, "数据异常", Toast.LENGTH_SHORT).show();
            return;
        }
        playVideoOnTv(mNowPlayVideo.getPlayUrl().getFormatUrl());
    }


    /**
     * 处理电视播放的结果，是否成功
     *
     * @param message message
     */
    private void shareToTvResult(Message message) {
        boolean isSuccess = message.arg1 == 1;
        if (isSuccess) {
            showDLNAController();
            setDLNAButton(false);
            setCloseButton(false);
            pausePlay(false);
            mProgressBarView.setVisibility(View.GONE);
        } else {
            mDLNARootLayout.setVisibility(GONE);
            Toast.makeText(mContext, "推送到电视播放失败了", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 从电视播放退出的结果
     *
     * @param message message
     */
    private void exitFromTvResult(Message message) {
        boolean isSuccess = message.arg1 == 1;
        if (!isSuccess) {
            Toast.makeText(mContext, "电视播放退出失败，请手动退出", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示推送视频播放控制页面
     */
    private void showDLNAController() {
        String name = DLNAContainer.getInstance().getSelectedDevice().getFriendlyName();
        String title = mContext.getResources().getString(R.string.share_tv_layout_title, TextUtils.isEmpty(name) ? "您的电视" : name);
        mDLNARootLayout.setVisibility(VISIBLE);
        ((TextView) mDLNARootLayout.findViewById(R.id.txt_dlna_title)).setText(title);
    }

    /**
     * Start to play the video.
     *
     * @param path The video path.
     */
    private synchronized void playVideoOnTv(final String path) {
        new Thread() {
            public void run() {
                final boolean isSuccess = mController.play(mSelectDevice, path);
                Message message = new Message();
                message.what = MSG_PLAY_ON_TV_RESULT;
                message.arg1 = isSuccess ? 1 : 0;
                mHandler.sendMessage(message);
            }
        }.start();
    }

    /**
     * 继续在本地播放
     */
    private synchronized void goOnPlayAtLocal() {
        mDLNARootLayout.setVisibility(GONE);
        initDLNAInfo();
        playVideoAtLastPos();
        new Thread() {
            @Override
            public void run() {
                final boolean isSuccess = mController.stop(mSelectDevice);
                Message message = new Message();
                message.what = MSG_EXIT_FORM_TV_RESULT;
                message.arg1 = isSuccess ? 1 : 0;
                mHandler.sendMessage(message);
            }
        }.start();
    }

    private class AnimationImp implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    }

    public interface VideoPlayCallbackImpl {
        void onCloseVideo();

        void onSwitchPageType();

        void onPlayFinish();
    }
}
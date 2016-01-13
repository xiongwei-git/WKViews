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

package com.wkzf.library.component.player.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Ted on 2015/9/2.
 * 视频对象
 */
public class Video implements Parcelable {
    private String mVideoName;//视频名称 如房源视频、小区视频
    private ArrayList<VideoUrl> mVideoUrl;//视频的地址列表

    /***************
     * 请看注释
     ***************************/
    private VideoUrl mPlayUrl;//当前正在播放的地址。 外界不用传

    public String getVideoName() {
        return mVideoName;
    }

    public void setVideoName(String videoName) {
        mVideoName = videoName;
    }

    public ArrayList<VideoUrl> getVideoUrl() {
        return mVideoUrl;
    }

    public void setVideoUrl(ArrayList<VideoUrl> videoUrl) {
        mVideoUrl = videoUrl;
    }

    public VideoUrl getPlayUrl() {
        return mPlayUrl;
    }

    public void setPlayUrl(VideoUrl playUrl) {
        mPlayUrl = playUrl;
    }

    public void setPlayUrl(int position) {
        if (position < 0 || position >= mVideoUrl.size()) return;
        setPlayUrl(mVideoUrl.get(position));
    }

    public boolean equal(Video video) {
        if (null != video) {
            return mVideoName.equals(video.getVideoName());
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Video() {
    }

    public Video(Parcel source) {
        this.mVideoName = source.readString();
        this.mVideoUrl = source.readArrayList(VideoUrl.class.getClassLoader());
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mVideoName);
        dest.writeList(mVideoUrl);
    }


    public static final Creator<Video> CREATOR = new Creator<Video>() {

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }

        @Override
        public Video createFromParcel(Parcel source) {
            return new Video(source);
        }
    };
}

package com.bhlt1998.minidouyin.videoview;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

public class MyVideoView extends VideoView {
    private int mVideoWidth = 600;
    private int mVideoHeight = 800;

    private int videoRealH = 1;
    private int videoRealW = 1;
    public MyVideoView(Context context){
        super(context);
    }
    public MyVideoView(Context context, AttributeSet attrs){
        super(context, attrs);
    }
    public MyVideoView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }
//    @Override
//    public void setVideoPath(String path) {
//        super.setVideoPath(path);
//        MediaMetadataRetriever retr = new MediaMetadataRetriever();
//        retr.setDataSource(path);
//        String height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); // 视频高度
//        String width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); // 视频宽度
//        try {
//            videoRealH=Integer.parseInt(height);
//            videoRealW=Integer.parseInt(width);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
//        float maxRatio = (float)height / (float)width;
//        float originRatio = (float)videoRealH / (float)videoRealW;
//        if(originRatio >= maxRatio){//高充满
//            mVideoHeight = height;
//            mVideoWidth = (int)(height / originRatio);
//        }else{
//            mVideoWidth = width;
//            mVideoHeight = (int)(width * originRatio );
//        }
//        Log.d("TAG","default width"+width);
//        Log.d("TAG", "default height"+height);
//        setMeasuredDimension(mVideoWidth, mVideoHeight);
        setMeasuredDimension(width,height);
    }
}

package com.bhlt1998.minidouyin;

import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.airbnb.lottie.LottieAnimationView;
import com.bhlt1998.minidouyin.videoview.MyClickListener;
import com.bhlt1998.minidouyin.videoview.MyVideoView;

public class VideoPlayerActivity extends AppCompatActivity {

    private String video_url;
    private String Nickname;
    private ImageButton imBtn;
    private MyVideoView videoView;
    private LottieAnimationView heart;
    private LottieAnimationView loading;
    private ImageView playSign;
    private boolean startPlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.black));
        startPlay = false;
        videoView = findViewById(R.id.videoView);
        heart = findViewById(R.id.heart);
        loading = findViewById(R.id.lottie_play_loading);
        imBtn = findViewById(R.id.back_button);
        playSign = findViewById(R.id.iv_playSign);
        imBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        heart.addAnimatorUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(animation.getAnimatedFraction()==1f){
                    if(heart.getVisibility()==View.VISIBLE){
                        heart.setVisibility(View.GONE);
                    }
                }
            }
        });
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            Bundle bundle = extras.getBundle("url_bundle");
            if(bundle != null){
                Nickname = bundle.getString("user_name");
                video_url = bundle.getString("video_url");
            }
        }
       // videoView.setMediaController(new MediaController(this));
        videoView.setVideoPath(video_url);
        Log.d("TAG",""+video_url);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
            @Override
            public void onPrepared(MediaPlayer mp) {
                loading.setVisibility(View.GONE);
                loading.pauseAnimation();
                mp.start();
                startPlay = true;
                mp.setLooping(true);
            }
        });

        videoView.setOnTouchListener(new MyClickListener(new MyClickListener.MyClickCallBack() {
            @Override
            public void oneClick() {
                if(startPlay) {
                    if (videoView.isPlaying()) {
                        videoView.pause();
                        playSign.setVisibility(View.VISIBLE);
                    } else {
                        videoView.start();
                        playSign.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void doubleClick() {
                if(startPlay) {
                    heart.playAnimation();
                    heart.setVisibility(View.VISIBLE);
                }
//                Toast toast = Toast.makeText(getApplicationContext(),"like",Toast.LENGTH_SHORT);
//                toast.show();
            }


        }));
    }
}

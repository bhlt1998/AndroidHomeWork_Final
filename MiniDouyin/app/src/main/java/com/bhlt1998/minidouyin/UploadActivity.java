package com.bhlt1998.minidouyin;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bhlt1998.minidouyin.bean.PostVideoResponse;
import com.bhlt1998.minidouyin.network.IMiniDouyinService;
import com.bhlt1998.minidouyin.utils.ResourceUtils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UploadActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    public Uri mSelectedImage;
    private Uri mSelectedVideo;
    public ImageButton mBtn;
    private TextView tv_choose;
    private ImageView checkImg;
    private ImageView uncheckImg;
    private ImageView checkVid;
    private ImageView uncheckVid;
    private ImageButton mBtn_exit;
    private LottieAnimationView uploading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.black));
        checkImg = findViewById(R.id.check_img);
        tv_choose = findViewById(R.id.tv_choose);
        uncheckImg = findViewById(R.id.uncheck_img);
        checkVid = findViewById(R.id.check_vid);
        uncheckVid = findViewById(R.id.uncheck_vid);
        mBtn_exit = findViewById(R.id.exit_upload);
        uploading = findViewById(R.id.loading_upload);
        initBtns();
    }
    private void initBtns() {
        mBtn = findViewById(R.id.imageButton_addPhoto);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String s = tv_choose.getText().toString();
                if (getString(R.string.select_an_image).equals(s)) {
                    chooseImage();
                } else if (getString(R.string.select_a_video).equals(s)) {
                    chooseVideo();
                } else if (getString(R.string.post_it).equals(s)) {
                    if (mSelectedVideo != null && mSelectedImage != null) {
                        postVideo();
                    } else {
                        throw new IllegalArgumentException("error data uri, mSelectedVideo = " + mSelectedVideo + ", mSelectedImage = " + mSelectedImage);
                    }
                } else if ((getString(R.string.success_try_refresh).equals(s))) {
                    tv_choose.setText(R.string.select_an_image);
                }
            }
        });
        mBtn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    public void chooseImage() {
        // Start Activity to select an image
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "选择一张图片"),PICK_IMAGE);
    }

    public void chooseVideo() {
        // Start Activity to select a video
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "选择一个视频"),PICK_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       // Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");

        if (resultCode == RESULT_OK && null != data) {

            if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                Log.d("TAG", "selectedImage = " + mSelectedImage);
                tv_choose.setText(R.string.select_a_video);
                ImgStart();
            } else if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
               Log.d("TAG", "mSelectedVideo = " + mSelectedVideo);
                tv_choose.setText(R.string.post_it);
                VidStart();
            }
        }
    }
    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(UploadActivity.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }
    private void postVideo() {
        tv_choose.setText("上传中...");
        mBtn.setEnabled(false);
        mBtn_exit.setEnabled(false);
        lottieStart();


        // -C2 (6) Send Request to post a video with its cover image
        // if success, make a text Toast and show
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://test.androidcamp.bytedance.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        MultipartBody.Part aImage = getMultipartFromUri("cover_image",mSelectedImage);
        MultipartBody.Part aVideo = getMultipartFromUri("video", mSelectedVideo);
        Call<PostVideoResponse> call = retrofit.create(IMiniDouyinService.class).postVideo(aImage,aVideo);
        call.enqueue(new Callback<PostVideoResponse>() {
            @Override
            public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                if(response.isSuccessful()){
                    Log.d("TAG","upload get response success");
                    PostVideoResponse postVideoResponse = response.body();
                    if(postVideoResponse != null && postVideoResponse.getSuccess()){
                        Log.d("TAG","upload success");
                        Toast toast = Toast.makeText(getApplicationContext(), "上传成功！", Toast.LENGTH_SHORT);
                        toast.show();
                        tv_choose.setText(R.string.select_an_image);
                        mBtn.setEnabled(true);


                    }else{
                        Log.d("TAG","upload failed");
                        Toast toast = Toast.makeText(getApplicationContext(),"上传失败",Toast.LENGTH_SHORT);
                        toast.show();
                        tv_choose.setText(R.string.select_an_image);
                        mBtn.setEnabled(true);
                    }
                }else{
                    Log.d("TAG","upload get response failed");
                    Toast toast = Toast.makeText(getApplicationContext(),"上传失败",Toast.LENGTH_SHORT);
                    toast.show();
                    tv_choose.setText(R.string.select_an_image);
                    mBtn.setEnabled(true);
                }
                lottieEnd();
                ImgEnd();
                VidEnd();
                mBtn_exit.setEnabled(true);
            }

            @Override
            public void onFailure(Call<PostVideoResponse> call, Throwable t) {
                Toast toast = Toast.makeText(getApplicationContext(),"上传失败",Toast.LENGTH_SHORT);
                toast.show();
                lottieEnd();
                tv_choose.setText(R.string.select_an_image);
                mBtn.setEnabled(true);
                mBtn_exit.setEnabled(true);
                ImgEnd();
                VidEnd();
            }
        });
    }
    private void ImgEnd(){
        checkImg.setVisibility(View.GONE);
        uncheckImg.setVisibility(View.VISIBLE);
    }
    private void ImgStart(){
        uncheckImg.setVisibility(View.GONE);
        checkImg.setVisibility(View.VISIBLE);
    }
    private void VidEnd(){
        checkVid.setVisibility(View.GONE);
        uncheckVid.setVisibility(View.VISIBLE);
    }
    private void VidStart(){
        uncheckVid.setVisibility(View.GONE);
        checkVid.setVisibility(View.VISIBLE);
    }
    private void lottieEnd(){
        uploading.setVisibility(View.INVISIBLE);
        uploading.pauseAnimation();
    }
    private void lottieStart(){
        uploading.playAnimation();
        uploading.setVisibility(View.VISIBLE);
    }
}

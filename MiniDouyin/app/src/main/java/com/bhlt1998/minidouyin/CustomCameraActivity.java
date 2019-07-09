package com.bhlt1998.minidouyin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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
import com.bhlt1998.minidouyin.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.bhlt1998.minidouyin.utils.Utils.MEDIA_TYPE_VIDEO;
import static com.bhlt1998.minidouyin.utils.Utils.getOutputMediaFile;


public class CustomCameraActivity extends AppCompatActivity {

    private SurfaceView mSurfaceView;
    private Camera mCamera;

    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isRecording = false;

    private int rotationDegree = 0;

    private File outputFile;

    private ImageButton mBtn_yes;
    private ImageButton mBtn_no;
    private ImageButton mBtn_record;
    private ImageButton mBtn_exit;
    private ImageButton mBtn_change;
    private TextView tv_ask;
    private LottieAnimationView lottie_loading;
    private TextView tv_recording;
    private ImageView iv_recording;
    private Handler handler;
    private Runnable autoStop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_custom_camera);

        mSurfaceView = findViewById(R.id.img);
        mBtn_yes = findViewById(R.id.imageButton_yes);
        mBtn_no = findViewById(R.id.imageButton_no);
        mBtn_exit = findViewById(R.id.imageButton_exitCamera);
        mBtn_change = findViewById(R.id.btn_facing);
        mBtn_record =findViewById(R.id.btn_record);
        tv_recording = findViewById(R.id.tv_recording);
        iv_recording = findViewById(R.id.iv_recording);
        lottie_loading = findViewById(R.id.lottie_loading);
        tv_ask = findViewById(R.id.tv_ask);

        tv_recording.setVisibility(View.INVISIBLE);
        iv_recording.setVisibility(View.INVISIBLE);

        chooseEnd();

        releaseCameraAndPreview();
        mCamera = getCamera(CAMERA_TYPE);
        rotationDegree = getCameraDisplayOrientation(CAMERA_TYPE);
        mCamera.setDisplayOrientation(rotationDegree);

        handler = new Handler();
        autoStop = () -> {
            mBtn_record.performClick();
        };
        //todo 给SurfaceHolder添加Callback
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    mCamera.setPreviewDisplay(holder);
                    mCamera.startPreview();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        });
        mBtn_exit.setOnClickListener(v ->{
            finish();
        });
        mBtn_yes.setOnClickListener(v -> {
            chooseEnd();

            if(outputFile!=null){
                loadingStart();
                Uri ThumbUri = getVideoThumb(outputFile.getAbsolutePath());

                postVideo(ThumbUri,Uri.fromFile(outputFile));

            }else{
                Toast toast = Toast.makeText(getApplicationContext(),"上传失败",Toast.LENGTH_SHORT);
                toast.show();
                recordStart();
            }
        });

        mBtn_record.setOnClickListener(v -> {
            //todo 录制，第一次点击是start，第二次点击是stop
            if (isRecording) {
                //todo 停止录制
                releaseMediaRecorder();
                handler.removeCallbacks(autoStop);
                isRecording = false;
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(outputFile)));
                Toast toast = Toast.makeText(getApplicationContext(),"结束录制", Toast.LENGTH_SHORT);
                toast.show();
                //
                tv_recording.setVisibility(View.INVISIBLE);
                iv_recording.setVisibility(View.INVISIBLE);
                mBtn_exit.setEnabled(true);

                recordEnd();
                chooseStart();
            } else {
                //todo 录制
                prepareVideoRecorder();
                isRecording = true;
                handler.postDelayed(autoStop,10000);//延迟十秒
                mBtn_exit.setEnabled(false);
                tv_recording.setVisibility(View.VISIBLE);
                iv_recording.setVisibility(View.VISIBLE);
//                Toast toast = Toast.makeText(getApplicationContext(),"开始录制", Toast.LENGTH_SHORT);
//                toast.show();

            }
        });
        mBtn_no.setOnClickListener(v ->{
            chooseEnd();
            recordStart();
        });
        mBtn_change.setOnClickListener(v -> {
            //todo 切换前后摄像头
            if (CAMERA_TYPE == Camera.CameraInfo.CAMERA_FACING_BACK) {
                rotationDegree = getCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_FRONT);
                try{
                    mCamera = getCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    startPreview(mSurfaceView.getHolder());
                }catch(Exception e){
                    e.printStackTrace();
                }
            } else {
                rotationDegree = getCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_BACK);

                try{
                    mCamera = getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                    startPreview(mSurfaceView.getHolder());
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

        });

    }
    public Uri getVideoThumb(String path){
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(path);
        Bitmap thumb =  media.getFrameAtTime();
        File thumbfile = getOutputMediaFile(Utils.MEDIA_TYPE_IMAGE);
        FileOutputStream fOut = null;
        try{
            fOut = new FileOutputStream(thumbfile);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return Uri.fromFile(thumbfile);
    }
    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);

        //todo 摄像头添加属性，例是否自动对焦，设置旋转方向等
        cam.setDisplayOrientation(rotationDegree);
        Camera.Parameters params = cam.getParameters();
        List<String> focusMods = params.getSupportedFocusModes();
        if(focusMods.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        cam.setParameters(params);
        return cam;
    }

    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private int getCameraDisplayOrientation(int cameraId) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }


    private void releaseCameraAndPreview() {
        //todo 释放camera资源
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    Camera.Size size;

    private void startPreview(SurfaceHolder holder) {
        //todo 开始预览
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        Camera.Size optimalPreviewSize = getOptimalPreviewSize(sizes, mSurfaceView.getWidth(),
                mSurfaceView.getHeight());
        if (optimalPreviewSize != null) {
            params.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
        }
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            mCamera.cancelAutoFocus();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private MediaRecorder mMediaRecorder;

    private boolean prepareVideoRecorder() {
        //todo 准备MediaRecorder
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        outputFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
        mMediaRecorder.setOutputFile(outputFile.toString());
        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        mMediaRecorder.setOrientationHint(rotationDegree);
        try{
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        }catch(Exception e){
            releaseMediaRecorder();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        //todo 释放MediaRecorder
        if(mMediaRecorder!= null){
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(CustomCameraActivity.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }
    private void postVideo(Uri mSelectedImage,Uri mSelectedVideo) {
//        mBtn.setText("上传中...");
//        mBtn.setEnabled(false);

        // -C2 (6) Send Request to post a video with its cover image
        // if success, make a text Toast and show
        mBtn_exit.setEnabled(false);
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
//                        mBtn.setText(R.string.select_an_image);
//                        mBtn.setEnabled(true);
                    }else{
                        Log.d("TAG","upload failed");
                        Toast toast = Toast.makeText(getApplicationContext(),"上传失败",Toast.LENGTH_SHORT);
                        toast.show();
//                        mBtn.setText(R.string.select_an_image);
//                        mBtn.setEnabled(true);
                    }
                }else{
                    Log.d("TAG","upload get response failed");
                    Toast toast = Toast.makeText(getApplicationContext(),"上传失败",Toast.LENGTH_SHORT);
                    toast.show();
//                    mBtn.setText(R.string.select_an_image);
//                    mBtn.setEnabled(true);
                }
                loadingEnd();
                recordStart();
                mBtn_exit.setEnabled(true);
            }

            @Override
            public void onFailure(Call<PostVideoResponse> call, Throwable t) {
                loadingEnd();
                recordStart();
                mBtn_exit.setEnabled(true);
                Toast toast = Toast.makeText(getApplicationContext(),"上传失败",Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
    private void loadingEnd(){
        lottie_loading.setVisibility(View.INVISIBLE);
        lottie_loading.pauseAnimation();
    }
    private void loadingStart(){
        lottie_loading.playAnimation();
        lottie_loading.setVisibility(View.VISIBLE);
    }
    private void chooseEnd(){
        tv_ask.setVisibility(View.INVISIBLE);
        mBtn_no.setVisibility(View.INVISIBLE);
        mBtn_yes.setVisibility(View.INVISIBLE);
        mBtn_no.setEnabled(false);
        mBtn_yes.setEnabled(false);
    }
    private void chooseStart(){
        tv_ask.setVisibility(View.VISIBLE);
        mBtn_yes.setEnabled(true);
        mBtn_no.setEnabled(true);
        mBtn_yes.setVisibility(View.VISIBLE);
        mBtn_no.setVisibility(View.VISIBLE);
    }
    private void recordStart(){
        mBtn_record.setEnabled(true);
        mBtn_change.setEnabled(true);
        mBtn_record.setVisibility(View.VISIBLE);
        mBtn_change.setVisibility(View.VISIBLE);
    }
    private void recordEnd(){
        mBtn_record.setVisibility(View.INVISIBLE);
        mBtn_change.setVisibility(View.INVISIBLE);
        mBtn_record.setEnabled(false);
        mBtn_change.setEnabled(false);
    }
}

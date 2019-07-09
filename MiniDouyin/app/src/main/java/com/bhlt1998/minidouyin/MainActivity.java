package com.bhlt1998.minidouyin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bhlt1998.minidouyin.adapter.MyAdapter;
import com.bhlt1998.minidouyin.bean.Feed;
import com.bhlt1998.minidouyin.bean.FeedResponse;
import com.bhlt1998.minidouyin.decoration.MyItemDecoration;
import com.bhlt1998.minidouyin.network.IMiniDouyinService;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements MyAdapter.ListItemOnClickListener {
    private RecyclerView mRv;
    private SwipeRefreshLayout mSrl;
    private ImageButton mBtn;
    private ImageButton mBtn2;
    private ImageButton mBtn3;
    private List<Feed> mFeeds = new ArrayList<>();
    private static final String TAG = "TAG";
    private MyAdapter myAdapter;
    private static final int REQUEST_CUSTOM_CAMERA = 101;
    private static final int REQUEST_READ_STORAGE = 102;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.black));

        initSwipeRefreshLayout();
        initRecyclerView();
        initBtns();
        myAdapter = new MyAdapter(this);

        mRv.setAdapter(myAdapter);

        fetchFeed();

    }

    @Override
    public void onListItemClick(Feed feed) {

//        Toast toast = Toast.makeText(getApplicationContext(),"onClick",Toast.LENGTH_SHORT);
//        toast.show();
        Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("user_name", feed.getUsername());
        bundle.putString("video_url", feed.getVideo_url());
        intent.putExtra("url_bundle", bundle);
        startActivity(intent);
    }

    //初始化 SwipeRefreshLayout
    private void initSwipeRefreshLayout(){
        mSrl = (SwipeRefreshLayout) findViewById(R.id.swiprefreshlayout);
        mSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //刷新
                        fetchFeed();
                        mSrl.setRefreshing(false);
                    }
                },2000);
            }
        });
    }
    //初始化 RecyclerView
    private void initRecyclerView(){
        mRv = findViewById(R.id.rv);
        //mRv.setLayoutManager(new LinearLayoutManager(this));

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager( 1,StaggeredGridLayoutManager.VERTICAL);
        //add
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        mRv.setItemAnimator(null);

        mRv.setLayoutManager(layoutManager);
        //mRv.addItemDecoration();
        MyItemDecoration mid = new MyItemDecoration(35);
        mRv.addItemDecoration(mid);

        if(Build.VERSION.SDK_INT >= 23) {
            mRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) mRv.getLayoutManager();
                    int[] pos = null;
                    pos = manager.findFirstVisibleItemPositions(pos);
                    if(newState == RecyclerView.SCROLL_STATE_IDLE){
                        if(pos != null && pos.length>0){
                            if(pos[0]>1){
                                mBtn2.setVisibility(View.VISIBLE);
                            }else{
                                mBtn2.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });
        }
    }

    //初始化 Button
    private void initBtns(){
        mBtn = findViewById(R.id.imageButton1);
        mBtn.setOnClickListener(v -> {
            //检查权限
            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO},REQUEST_CUSTOM_CAMERA);
            }else{
                startActivity(new Intent(MainActivity.this, CustomCameraActivity.class));
            }
        });
        mBtn2 = findViewById(R.id.imageButton2);
        mBtn2.setVisibility(View.GONE);
        mBtn3 = findViewById(R.id.imageButton_upload);
        mBtn3.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_READ_STORAGE);
            }else{
                //进入上传页面
                startActivity(new Intent(this, UploadActivity.class));
            }
        });
    }

    public void goTop(View view){
        mRv.post(new Runnable() {
            @Override
            public void run() {
                mRv.scrollToPosition(0);
            }
        });
        mBtn2.setVisibility(View.GONE);
    }
    //拉取视频信息
    public void fetchFeed(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://test.androidcamp.bytedance.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        IMiniDouyinService request = retrofit.create(IMiniDouyinService.class);
        Call<FeedResponse> call = request.getfeed();
        call.enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                if(response.isSuccessful()){
                    Log.d(TAG, "response is successful");
                    FeedResponse listfeed = response.body();
                    if(listfeed != null && listfeed.getSuccess()){
                        Log.d(TAG,"get list feed success");
                        List<Feed> feeds = listfeed.getFeeds();
                        mFeeds = feeds;
                        myAdapter.refresh(feeds);
                    }else{
                        Log.d(TAG, "get list feed fail");
                        Toast toast = Toast.makeText(getApplicationContext(),"刷新失败 请重试",Toast.LENGTH_SHORT);
                        toast.show();

                    }
                }else{
                    Log.d(TAG, "response fail");
                    Toast toast = Toast.makeText(getApplicationContext(),"刷新失败 请重试",Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {

            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CUSTOM_CAMERA: {
                // 判断权限是否已经授予
                for(int i = 0; i < grantResults.length;i++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                }
                startActivity(new Intent(MainActivity.this, CustomCameraActivity.class));
                break;
            }
            case REQUEST_READ_STORAGE:{
                for(int i = 0; i < grantResults.length;i++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                }
                //启动上传页面
                startActivity(new Intent(this, UploadActivity.class));
                break;
            }
        }
    }
}

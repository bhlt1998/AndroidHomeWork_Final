package com.bhlt1998.minidouyin.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bhlt1998.minidouyin.R;
import com.bhlt1998.minidouyin.bean.Feed;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private ListItemOnClickListener listItemOnClickListener;
    private ArrayList<Feed> feeds;

    public MyAdapter(ListItemOnClickListener listItemOnClickListener){
        this.listItemOnClickListener = listItemOnClickListener;
        feeds = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        Context context = parent.getContext();
        int layoutItem = R.layout.single_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutItem, parent, shouldAttachToParentImmediately);
        MyViewHolder viewHolder = new MyViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.bind(i);
    }

    @Override
    public int getItemCount() {
        return feeds.size();
    }

    public interface ListItemOnClickListener{
        void onListItemClick(Feed feed);
    }

    public void refresh(List<Feed> newfeeds){
        if(newfeeds != null){
            feeds.clear();
            feeds.addAll(newfeeds);
            notifyDataSetChanged();
        }
    }
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private Feed feed;
        private TextView tvNickname;
        private ImageView iv;
        private TextView tvTime;

        public MyViewHolder(View itemView){
            super(itemView);
            tvNickname = itemView.findViewById(R.id.tv_nickname);
            tvTime = itemView.findViewById(R.id.tv_time);
            iv = itemView.findViewById(R.id.iv_cover);
            itemView.setOnClickListener(MyViewHolder.this);
        }

        public void bind(int position){
            feed = feeds.get(position);
            tvNickname.setText(feed.getUsername());
            tvTime.setText(feed.getUpdatedAt());
            Log.d("TAGbind", "name:"+feed.getUsername());
            int screenWidth = itemView.getContext().getResources().getDisplayMetrics().widthPixels;
            int width = (int)(screenWidth*0.9f);
            Glide.with(itemView.getContext())
                    .setDefaultRequestOptions(new RequestOptions().centerCrop())
                    .load(feed.get_image_url())
                    .into(iv);
        }

        @Override
        public void onClick(View v) {
            listItemOnClickListener.onListItemClick(feed);
        }
    }

}

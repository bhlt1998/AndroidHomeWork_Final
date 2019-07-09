package com.bhlt1998.minidouyin.bean;

public class Feed {
    private String image_url;
    private String user_name;
    private String video_url;
    private String updatedAt;

    public String get_image_url(){
        return image_url;
    }
    public String getUsername(){
        return user_name;
    }
    public String getVideo_url(){
        return video_url;
    }
    public String getUpdatedAt(){
        String out = updatedAt.replace('T',' ');
        String out2 = out.substring(0, out.length()-5);
        return out2;
    }
}
